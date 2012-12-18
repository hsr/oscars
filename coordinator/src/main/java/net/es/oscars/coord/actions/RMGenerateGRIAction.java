package net.es.oscars.coord.actions;

import org.apache.log4j.Logger;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.actions.CoordAction.State;

import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.soap.gen.AssignGriReqContent;
import net.es.oscars.resourceManager.soap.gen.AssignGriRespContent;
import net.es.oscars.utils.soap.OSCARSServiceException;

/**
 * RMGenerateGRI sends a message to the ResourceManager to generate a unique GlobalReservationId
 * @author lomax
 *
 */
public class RMGenerateGRIAction extends CoordAction <Object, String> {

    private static final long       serialVersionUID = 1L;
    private static final Logger LOG =  Logger.getLogger(RMGenerateGRIAction.class.getName());
    private static final String moduleName = ModuleName.COORD;

    @SuppressWarnings("unchecked")
    public RMGenerateGRIAction (String name, CoordRequest request) {
        super (name, request, null);
    }
    
    /**
     * execute - Sends an "assignGri" request to the Resource Manager that generates a new GRI
     */
    public void execute()  {
        String transactionId = this.getCoordRequest().getTransactionId();
        OSCARSNetLogger netLogger = new OSCARSNetLogger(moduleName, transactionId);
        OSCARSNetLogger.setTlogger(netLogger);
        try {
            RMWorker rmWorker = RMWorker.getInstance();
            AssignGriReqContent reqContent = new AssignGriReqContent();
            reqContent.setTransactionId(transactionId);
            Object[] req = new Object[]{reqContent};
            Object[] res = rmWorker.getRMClient().invoke("assignGri",req);
            if ((res == null) || (res[0] == null)) {
                throw new OSCARSServiceException ("RMGenerateGriAction:No response from ResourceManager",
                         "system");
            }
            AssignGriRespContent response = (AssignGriRespContent) res [0];
            String gri = response.getGlobalReservationId();
            if (gri == null) {
                throw new OSCARSServiceException ("RMGenerateGriAction:Returned GRI is null", "system");
            }
            this.setResultData(gri);
            
            if (this.getCoordRequest() != null) {
                // Reset the CoordRequest with the new GRI    
                this.getCoordRequest().setGRI(gri);
            }
            // assignGRI is synchronous. Call executed.
            this.executed();            
        } catch (Exception ex) {
            // TODO logging may not be necessary. it should be done in coordImpl handleError
            String msg = ex.getMessage();
            if (msg == null ) {
                msg = ex.toString();
            }
            LOG.warn(netLogger.error("RMGenerateGRI",ErrSev.MAJOR, " caught exception " + msg));
            this.fail (ex);
        }
    } 
    
    public void executed() {
        this.setState(State.PROCESSED);
    }
}
