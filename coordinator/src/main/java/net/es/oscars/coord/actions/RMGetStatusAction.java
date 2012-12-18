package net.es.oscars.coord.actions;

import org.apache.log4j.Logger;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;

import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.soap.gen.GetStatusReqContent;
import net.es.oscars.resourceManager.soap.gen.GetStatusRespContent;

/**
 * RMGetStatus - sends  message to the ResourceManager to get just the status of a reservation
 * @author lomax
 *
 */
public class RMGetStatusAction extends CoordAction <GetStatusReqContent, GetStatusRespContent> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public RMGetStatusAction (String name,
                              CoordRequest request,
                              String gri) {
        super (name, request, null);
        GetStatusReqContent getStatusContent = new GetStatusReqContent();
        getStatusContent.setTransactionId(request.getTransactionId());
        getStatusContent.setGlobalReservationId(gri);
        this.setRequestData(getStatusContent);
    }
   
    /**
     * Uses a RMWorker to send an getStatus message to the Resource Manager that gets just the
     * reservation status
     */
    public void execute() {
       Logger log = Logger.getLogger(this.getClass());
       OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
       log.info(netLogger.start("RMGetStatus"));
        try {
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();

            Object[] req = {this.getRequestData()};
            Object[] res = rmClient.invoke("getStatus",req);
     
            if ((res == null) || (res[0] == null)) {
                throw new OSCARSServiceException ("GetStatusAction:No response from ResourceManager",
                        "system");
            }
            
            GetStatusRespContent getStatusResponse = (GetStatusRespContent) res[0];
            if (getStatusResponse == null) {
                throw new OSCARSServiceException ("GetStatusAction:Store Response is null","system");
            }
            this.setResultData(getStatusResponse);
            this.executed();            
        } catch (OSCARSServiceException ex) {
            this.fail (ex);
        }
    } 
    
    public void executed() {
        this.setState(State.PROCESSED);
    }
}
