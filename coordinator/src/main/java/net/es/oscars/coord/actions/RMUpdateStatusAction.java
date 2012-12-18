package net.es.oscars.coord.actions;

import org.apache.log4j.Logger;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;

import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusReqContent;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusRespContent;

/**
 * RMUpdateStatus - sends  message to the ResourceManager to update just the status of a reservation
 * @author lomax
 *
 */
public class RMUpdateStatusAction extends CoordAction <UpdateStatusReqContent, UpdateStatusRespContent> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public RMUpdateStatusAction (String name,
                                 CoordRequest request,
                                 String gri,
                                 String status) {
        super (name, request, null);
        UpdateStatusReqContent updateStatusContent = new UpdateStatusReqContent();
        updateStatusContent.setTransactionId(request.getTransactionId());
        updateStatusContent.setGlobalReservationId(gri);
        updateStatusContent.setStatus(status);
        this.setRequestData(updateStatusContent);
    }
   
    /**
     * Uses a RMWorker to send an updateStatus message to the Resource Manager that updates just the
     * reservation status
     */
    public void execute() {
       Logger log = Logger.getLogger(this.getClass());
       OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
       log.info(netLogger.start("RMUpdateStatus", this.getRequestData().getStatus()));
        try {
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();

            Object[] req = {this.getRequestData()};
            Object[] res = rmClient.invoke("updateStatus",req);
     
            if ((res == null) || (res[0] == null)) {
                throw new OSCARSServiceException ("UpdateStatusAction:No response from ResourceManager",
                        "system");
            }
            
            UpdateStatusRespContent updateStatusResponse = (UpdateStatusRespContent) res[0];

            this.setResultData(updateStatusResponse);
            this.executed();            
        } catch (OSCARSServiceException ex) {
            fail (ex);
        }
    } 
    
    public void executed() {
        this.setState(State.PROCESSED);
    }
}
