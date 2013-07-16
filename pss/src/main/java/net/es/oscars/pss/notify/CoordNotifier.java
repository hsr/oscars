package net.es.oscars.pss.notify;

import java.net.URL;

import org.apache.log4j.Logger;

import net.es.oscars.coord.soap.gen.PSSReplyContent;
import net.es.oscars.pss.api.Notifier;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.utils.sharedConstants.PSSConstants;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;

/**
 * Called when a pss action fails or is finishRunning
 * Calls Coordinator with a PSSReply message
 * @author Evangelos Chaniotakis, Mary Thompson
 *
 */
public class CoordNotifier implements Notifier {
    private Logger log = Logger.getLogger(CoordNotifier.class);
    // private GenericConfig config = null;
    /**
     * Checks the status of the PSSAction and sends
     * a PSSReply message to the Coordinator with the results of the action
     * 
     * @param PSSAction the action that has finishRunning or failed
     */
    public PSSAction process(PSSAction action) throws PSSException {
        log.info("CoordNotifier:start");
        CoordClient cl;

        try {
            String coord = null;
            if (action.getActionType() == ActionType.SETUP ) {
                coord = action.getRequest().getSetupReq().getCallbackEndpoint();
            }else if (action.getActionType() == ActionType.MODIFY) {
                coord = action.getRequest().getModifyReq().getCallbackEndpoint();
            }else {
                coord = action.getRequest().getTeardownReq().getCallbackEndpoint();
            }
            
            if (coord == null || coord.trim() == "") {
                log.info("empty callback to coordinator, not processing");
                return action;
            }
            String wsdl = coord + "?wsdl";
            log.debug("coord is " + coord + " wsdl is " + wsdl);
            URL coordURL = new URL(coord);
            URL wsdlURL = new URL(coord + "?wsdl");
            log.debug("calling Coordinator at " + coordURL.toString());
            cl = CoordClient.getClient(coordURL, wsdlURL);
            PSSReplyContent reply = new PSSReplyContent();
            if (action.getStatus() == ActionStatus.SUCCESS) {
                reply.setStatus(PSSConstants.SUCCESS);
            } else {
                reply.setStatus(PSSConstants.FAIL);
                if (action.getFaultReport() != null) {
                    // Fill in any missing bits
                    if (action.getFaultReport().getModuleName() == null) {
                        action.getFaultReport().setModuleName(ServiceNames.SVC_PSS);
                    }
                    if (action.getFaultReport().getTimestamp() == 0L) {
                        action.getFaultReport().setTimestamp(System.currentTimeMillis()/1000L);
                    }
                    if (action.getFaultReport().getDomainId() == null) {
                        action.getFaultReport().setDomainId(PathTools.getLocalDomainId());
                    }
                    reply.setErrorReport(action.getFaultReport());
                }
            }
            
            
            
            if (action.getActionType().equals(ActionType.SETUP)) {
                reply.setGlobalReservationId(
                            action.getRequest().getSetupReq().getReservation().getGlobalReservationId());
                reply.setReplyType(PSSConstants.PSS_SETUP);
                reply.setTransactionId(action.getRequest().getSetupReq().getTransactionId());  //GlobalTransactionId
            } else if (action.getActionType().equals(ActionType.MODIFY)) {
                reply.setGlobalReservationId(
                        action.getRequest().getModifyReq().getReservation().getGlobalReservationId());
                reply.setReplyType(PSSConstants.PSS_MODIFY);
                reply.setTransactionId(action.getRequest().getModifyReq().getTransactionId()); //GlobalTransactionId
            } else if (action.getActionType().equals(ActionType.TEARDOWN)) {
                reply.setGlobalReservationId(
                        action.getRequest().getTeardownReq().getReservation().getGlobalReservationId());
                reply.setReplyType(PSSConstants.PSS_TEARDOWN);
                reply.setTransactionId(action.getRequest().getTeardownReq().getTransactionId()); //GlobalTransactionId
            }else {
                log.info("CoordNotifier unknown action: "+action.getActionType()+" status:"+action.getStatus()+", not sending to coordinator");
                return action;
            }

            Object[] req = new Object[]{reply};
            cl.invoke("PSSReply", req);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PSSException(e.getMessage());
        }
        return action;
    }
    

    public void setConfig(GenericConfig config) throws PSSException {
        /*
        if (config == null) {
            throw new PSSException("null config");
        } else if (config.getParams() == null) {
            throw new PSSException("no config parameters set");
        }
        
        this.config = config;
        String domainSuffix = config.getParams().get("coordUrl");
        if (domainSuffix == null) {
            throw new PSSException("required coordUrl parameter not set");
        }
        */
    }

}
