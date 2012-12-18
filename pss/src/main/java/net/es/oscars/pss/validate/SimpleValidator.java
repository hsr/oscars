package net.es.oscars.pss.validate;

import java.util.HashMap;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.pss.api.Validator;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.PSSRequest;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.pss.util.URNParserResult.URNType;
import net.es.oscars.utils.topology.PathTools;

public class SimpleValidator implements Validator {
    private String localDomainId;

    public String getLocalDomainId() {
        return localDomainId;
    }

    public void setLocalDomainId(String localDomainId) {
        this.localDomainId = localDomainId;
    }

    public void validate(PSSRequest req) throws PSSException {
        if (localDomainId == null) {
            throw new PSSException("Local domain ID not set");
        }
        String gri = null;
        PathInfo pathInfo = null;
        if (req.getSetupReq() != null) {
            if (req.getSetupReq().getReservation() == null) {
                throw new PSSException("null reservation");
            } else {
                gri = req.getSetupReq().getReservation().getGlobalReservationId();
                if (req.getSetupReq().getReservation().getReservedConstraint() == null) {
                    throw new PSSException("null reservedConstraint");
                } else {
                    pathInfo = req.getSetupReq().getReservation().getReservedConstraint().getPathInfo();
                }
            }
        } else if (req.getTeardownReq() != null) {
            if (req.getTeardownReq().getReservation() == null) {
                throw new PSSException("null reservation");
            } else {
                gri = req.getTeardownReq().getReservation().getGlobalReservationId();
                if (req.getTeardownReq().getReservation().getReservedConstraint() == null) {
                    throw new PSSException("null reservedConstraint");
                } else {
                    pathInfo = req.getTeardownReq().getReservation().getReservedConstraint().getPathInfo();
                }
            }
        } else {
            return;
        }

        if (gri == null) {
            throw new PSSException("GRI must be set");
        }


        if (pathInfo == null ) {
            throw new PSSException("PathInfos must be defined and not empty");
        }
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path == null || path.getHop() == null || path.getHop().isEmpty() || path.getHop().size() < 2) {
            throw new PSSException("path must be defined, not empty, and with at least 2 hops");
        }

        for (CtrlPlaneHopContent hop : path.getHop()) {
            if (hop.getLink() == null) {
                throw new PSSException("all hops in path must contain a link");
            }
            if (hop.getLink().getId() == null) {
                throw new PSSException("all hops in path must contain a link with an id");
            }
            URNParserResult parseRes = URNParser.parseTopoIdent(hop.getLink().getId());
            if (!parseRes.getType().equals(URNType.LINK)) {
                System.out.println("link id: "+hop.getLink().getId()+" does not parse as a link identifier");
                throw new PSSException("link id: "+hop.getLink().getId()+" does not parse as a link identifier");
            }
            }
        return;
    }

    public void setConfig(GenericConfig config) throws PSSException {
        HashMap<String, Object> localDomainSettings = PathTools.getLocalDomainSettings();
        if(localDomainSettings != null){
            localDomainId = (String) localDomainSettings.get("id");
        }
        if (localDomainId == null) {
            throw new PSSException("required localDomainId parameter not set");
        }   
        this.setLocalDomainId(localDomainId);
    }

}
