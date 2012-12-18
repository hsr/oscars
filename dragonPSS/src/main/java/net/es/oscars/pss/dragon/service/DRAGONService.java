package net.es.oscars.pss.dragon.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.api.CircuitService;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;
import net.es.oscars.pss.notify.CoordNotifier;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.pss.dragon.vlsr.VLSRConnector;


public class DRAGONService implements CircuitService {
    private Logger log = Logger.getLogger(DRAGONService.class);
    public static final String SVC_ID = "dragon";

    public void setConfig(CircuitServiceConfig config) {
    }

    /**
     * Not suppoted. return "FAIL"
     */
    public List<PSSAction> modify(List<PSSAction> actions) {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            action.setStatus(ActionStatus.FAIL);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        return results;
    }
    
    public List<PSSAction> setup(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        CoordNotifier coordNotify = new CoordNotifier();
        for (PSSAction action : actions) {
            ResDetails res = action.getRequest().getSetupReq().getReservation();
            VLSRConnector conn = (VLSRConnector)ClassFactory.getInstance().getConnectorDirectory().getConnector("dragon-vlsr");
            if (conn == null) {
                action.setStatus(ActionStatus.FAIL);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw new PSSException("unable to load 'dragon-vlsr' connector");
            }
            try {
                conn.setupPath(res);
                action.setStatus(ActionStatus.SUCCESS);
            } catch (PSSException e) {
                action.setStatus(ActionStatus.FAIL);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw e;
            }
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
            coordNotify.process(action);
        }
        return results;
    }
    
    public List<PSSAction> teardown(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        CoordNotifier coordNotify = new CoordNotifier();
        for (PSSAction action : actions) {
            ResDetails res = action.getRequest().getTeardownReq().getReservation();
            VLSRConnector conn = (VLSRConnector)ClassFactory.getInstance().getConnectorDirectory().getConnector("dragon-vlsr");
            if (conn == null) {
                action.setStatus(ActionStatus.FAIL);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw new PSSException("unable to load 'dragon-vlsr' connector");
            }
            try {
                conn.teardownPath(res);
                action.setStatus(ActionStatus.SUCCESS);
            } catch (PSSException e) {
                action.setStatus(ActionStatus.FAIL);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw e;
            }
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
            coordNotify.process(action);
        }
        return results;
    }
    
    // TODO: implement status checking
    public List<PSSAction> status(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        CoordNotifier coordNotify = new CoordNotifier();
        for (PSSAction action : actions) {
            ResDetails res = action.getRequest().getStatusReq().getReservation();
            VLSRConnector conn = (VLSRConnector)ClassFactory.getInstance().getConnectorDirectory().getConnector("dragon-vlsr");
            if (conn == null) {
                action.setStatus(ActionStatus.FAIL);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw new PSSException("unable to load 'dragon-vlsr' connector");
            }
            try {
                String status = conn.pathStatus(res, true);
                if (status.equalsIgnoreCase("success"))
                    action.setStatus(ActionStatus.SUCCESS);
                // TODO: more status here (RESERVED, ...)
                // TODO: also check destination vlsr status
                else
                    action.setStatus(ActionStatus.FAIL); // UNKNOWN ?
            } catch (PSSException e) {
                action.setStatus(ActionStatus.FAIL);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw e;
            }
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
            coordNotify.process(action);
        }
        return results;
    }

}
