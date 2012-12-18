package net.es.oscars.coord.actions;

import org.apache.log4j.Logger;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;

import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.soap.gen.UpdateFailureStatusReqContent;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusRespContent;

/**
 * RMUpdateFailureStatus - sends  message to the ResourceManager to update the status of a reservation that has failed
 * @author lomax
 *
 */
public class RMUpdateFailureStatus extends CoordAction <UpdateFailureStatusReqContent, UpdateStatusRespContent> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public RMUpdateFailureStatus (String name,
                                 CoordRequest request,
                                 String gri,
                                 String status,
                                 ErrorReport errRep) {
        super (name, request, null);
        UpdateFailureStatusReqContent updateStatusContent = new  UpdateFailureStatusReqContent();
        updateStatusContent.setTransactionId(request.getTransactionId());
        updateStatusContent.setGlobalReservationId(gri);
        updateStatusContent.setStatus(status);
        updateStatusContent.setErrorReport(ErrorReport.report2fault(errRep));
        this.setRequestData(updateStatusContent);
    }
   
    /**
     * Uses a RMWorker to send an updateStatus message to the Resource Manager that updates just the
     * reservation status
     */
    public void execute() {
       Logger log = Logger.getLogger(this.getClass());
       OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
       log.info(netLogger.start("RM UpdateFailureStatus", this.getRequestData().getErrorReport().getErrorCode()));
        try {
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();

            Object[] req = {this.getRequestData()};
            Object[] res = rmClient.invoke("updateFailureStatus",req);
     
            if ((res == null) || (res[0] == null)) {
                throw new OSCARSServiceException (" UpdateFailureStatusAction:No response from ResourceManager",
                        "system");
            }
            
            UpdateStatusRespContent updateStatusResponse = (UpdateStatusRespContent) res[0];
            if (updateStatusResponse == null) {
                throw new OSCARSServiceException ("UpdateStatusAction:Store Response is null","system");
            }
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
