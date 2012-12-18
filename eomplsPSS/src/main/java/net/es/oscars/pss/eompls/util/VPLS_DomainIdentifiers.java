package net.es.oscars.pss.eompls.util;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.eompls.dao.SRLUtils;
import net.es.oscars.utils.topology.PathTools;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

public class VPLS_DomainIdentifiers {
    private static Logger log = Logger.getLogger(VPLS_DomainIdentifiers.class);
    protected Integer vplsId;


    public Integer getVplsId() {
        return vplsId;
    }

    public void setVplsId(Integer vplsId) {
        this.vplsId = vplsId;
    }




    public static VPLS_DomainIdentifiers reserve(String gri) throws PSSException {

        VPLS_DomainIdentifiers srids = new VPLS_DomainIdentifiers();

        String vplsScope = PathTools.getLocalDomainId() + ":vpls";
        String rangeExpr = "6000-6999";
        String idString = "";
        List<Integer> ids;

        ids = SRLUtils.getExistingIdentifiers(vplsScope, gri);
        if (ids == null || ids.size() == 0) {
            log.debug("did not find VPLS ids");
            Integer vplsId = SRLUtils.getIdentifier(vplsScope, gri, null, rangeExpr);
            srids.setVplsId(vplsId);

        } else if (ids.size() > 1) {
            idString = StringUtils.join(ids, ", ");
            log.error("multiple vpls ids found: [" + idString + "] , getting first one");
            srids.setVplsId(ids.get(0));
        } else {
            log.debug("found VPLS id: " + ids.get(0));
            srids.setVplsId(ids.get(0));
        }

        // add one record
        String thisGriScope = gri+":vpls";
        Integer tmpId = SRLUtils.getIdentifier(thisGriScope, gri, 1, "1-100");
        log.debug("saved a trick SRL: "+thisGriScope+" "+tmpId);

        return srids;

    }


    public static VPLS_DomainIdentifiers release(String gri) throws PSSException {
        VPLS_DomainIdentifiers srids = new VPLS_DomainIdentifiers();

        String vplsScope = PathTools.getLocalDomainId() + ":vpls";
        String idString;
        List<Integer> ids;


        // find if there's any SRLs
        String thisGriScope = gri+":vpls";
        ids = SRLUtils.getExistingIdentifiers(thisGriScope, gri);
        if (ids == null || ids.size() == 0) {
            throw new PSSException("no existing identifiers for gri scope: "+thisGriScope);
        } else {
            // release one
            log.debug("releasing a trick SRL: "+thisGriScope+" "+ids.get(0));

            SRLUtils.releaseIdentifier(thisGriScope, ids.get(0));
        }

        // check if this was the last one, if so release the global VPLS id
        if (ids.size() == 1) {
            ids = SRLUtils.releaseIdentifiers(vplsScope, gri);
            for (Integer id : ids) {
                srids.setVplsId(id);
            }
            idString = StringUtils.join(ids, ", ");
            log.debug("released vpls id(s) :"+idString+" for gri: "+gri);
        } else {
            ids = SRLUtils.getExistingIdentifiers(vplsScope, gri);
            srids.setVplsId(ids.get(0));
            log.debug("found vpls id: " + ids.get(0) + " but not releasing yet");
        }

        return srids;
    }

}
