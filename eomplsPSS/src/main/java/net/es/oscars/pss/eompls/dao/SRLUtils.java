package net.es.oscars.pss.eompls.dao;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.pss.eompls.beans.ScopedResourceLock;
import net.es.oscars.pss.eompls.common.EoMPLSPSSCore;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class SRLUtils {
    private static Logger log = Logger.getLogger(SRLUtils.class);
    public static List<Integer> releaseIdentifiers(String scope, String gri) {
        log.debug("releasing ids for scope: "+scope+" gri: "+gri);
        String dbname = EoMPLSPSSCore.getInstance().getDbname();

        EoMPLSPSSCore.getInstance().getSession().beginTransaction();

        ScopedResourceLockDAO srlDAO = new ScopedResourceLockDAO(dbname);
        List<ScopedResourceLock> srls = srlDAO.getByScopeAndGri(scope, gri);
        List<Integer> ids = new ArrayList<Integer>();
        for (ScopedResourceLock srl : srls) {
            ids.add(srl.getResource());
            log.debug("releasing "+srl.toString());
            srlDAO.remove(srl);
        }
        srlDAO.flush();

        return ids;

    }

    public static void releaseIdentifier(String scope, Integer resource) {
        log.debug("releasing "+resource+" for scope: "+scope);
        String dbname = EoMPLSPSSCore.getInstance().getDbname();
        EoMPLSPSSCore.getInstance().getSession().beginTransaction();

        ScopedResourceLockDAO srlDAO = new ScopedResourceLockDAO(dbname);
        List<ScopedResourceLock> srls = srlDAO.getByScopeAndResource(scope, resource);
        List<Integer> ids = new ArrayList<Integer>();
        for (ScopedResourceLock srl : srls) {
            ids.add(srl.getResource());
            log.debug("releasing "+srl.toString());
            srlDAO.remove(srl);
        }
        srlDAO.flush();
    }


    public static Integer getIdentifier(String scope, String gri, Integer preferred, String rangeExp) {
        String dbname = EoMPLSPSSCore.getInstance().getDbname();
        EoMPLSPSSCore.getInstance().getSession().beginTransaction();

        ScopedResourceLockDAO srlDAO = new ScopedResourceLockDAO(dbname);
        ScopedResourceLock srl = srlDAO.createSRL(scope, rangeExp, preferred, gri);
        if (srl != null) {
            srlDAO.update(srl);
        }
        srlDAO.flush();
        return srl.getResource();
    }

    public static List<Integer> getExistingIdentifiers(String scope, String gri) {
        String dbname = EoMPLSPSSCore.getInstance().getDbname();
        EoMPLSPSSCore.getInstance().getSession().beginTransaction();

        ScopedResourceLockDAO srlDAO = new ScopedResourceLockDAO(dbname);
        List<ScopedResourceLock> srls = srlDAO.getByScopeAndGri(scope, gri);
        if (srls == null || srls.size() == 0) return null;

        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (ScopedResourceLock srl : srls) {
            ids.add(srl.getResource());
        }
        srlDAO.flush();
        return ids;

    }

}
