package net.es.oscars.coord.req;

import net.es.oscars.coord.actions.RMUpdateFailureStatus;
import net.es.oscars.coord.workers.NotifyWorker;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.PSSConstants;

import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.soap.gen.PSSReplyContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;

/** class to handle a reply message from PSS setupPath, teardownPath, modifyPath or status
 *  Currently only setup and teardown are implemented
 * @author mrt
 *
 */
public class PSSReplyRequest extends CoordRequest <PSSReplyContent,Object >{

    private static final long       serialVersionUID  = 1L;
    private static String           requestType = null;         // is reply from setupPath or teardownPath
    private static String           failedEvent = null;
    private static String           errorCode = null;
    private static final Logger     LOG = Logger.getLogger(PSSReplyRequest.class.getName());

    public PSSReplyRequest(String name, String gri, PSSReplyContent pssReply) {
        super (name, pssReply.getTransactionId(), gri);
        this.requestType= pssReply.getReplyType();
        if (this.requestType.equals(PSSConstants.PSS_SETUP)) {
            this.failedEvent = NotifyRequestTypes.PATH_SETUP_FAILED;
            this.errorCode = ErrorCodes.PATH_SETUP_FAILED;
        } else {
            this.failedEvent = NotifyRequestTypes.PATH_TEARDOWN_FAILED;
            this.errorCode = ErrorCodes.PATH_TEARDOWN_FAILED;
        }
        this.setCoordRequest(this);
    }

    public void setRequestData (PSSReplyContent params) {
        // Set input parameter using base class method
        super.setRequestData (params);
    }

    /**
     * calls ResourceManger to update the state 
     */
    public void execute()  {

        String method = "PSSReplyRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method, " for " +this.requestType));

        try {
            PSSReplyContent pssReply = this.getRequestData();
            if (pssReply == null) {
                throw new OSCARSServiceException (this.errorCode, method + " Null PSSReplyContent", ErrorReport.SYSTEM);
            }
            String gri = pssReply.getGlobalReservationId();
            CoordRequest request = null;
            CancelRequest cancelRequest = null;
            if (pssReply.getReplyType().equals(PSSConstants.PSS_SETUP)) {
                request = CoordRequest.getCoordRequestByAlias(PathRequest.PSS_CREATE_PATH + "-"
                        + gri);
            } else if (pssReply.getReplyType().equals(PSSConstants.PSS_TEARDOWN)) {
                // Look for CancelRequest first since it has a unique key
                //LOG.debug("PSSReplyRequest.execute looking for " + "cancelReservation-" + gri + "-" + this.getTransactionId());
                cancelRequest = (CancelRequest) CoordRequest.getCoordRequestByAlias("cancelReservation-" + gri);
 
                if (cancelRequest == null) { 
                    request = CoordRequest.getCoordRequestByAlias(PathRequest.PSS_TEARDOWN_PATH  +"-" + gri);
                }
            }
            if (request == null && cancelRequest == null) {
                throw new OSCARSServiceException (this.errorCode,
                                                  method + " no CreatePathRequest,TearDownPathRequest or CancelReservation associated with this PSSReply",
                                                  ErrorReport.SYSTEM);
            }
            // Notify the CreatePathRequest, TearDownPathRequest or CancelReservation request
            if (request != null) {
               ((PathRequest)request).setPSSReplyResult(pssReply);
            } else {
                cancelRequest.setPSSReplyResult(pssReply);
            }

            this.executed();
            
        } catch (Exception ex){
            this.fail (ex);
            LOG.warn(netLogger.error(method, ErrSev.MINOR," failed with exception " + ex.getMessage()));
        }
        LOG.debug(netLogger.end(method));
    }

    /**
      * Send notification that this request has failed
      * CoordRequest implementations are expected to implement it.
      * @param errorMsg
      * @param resDetails
      */
    public void notifyError (String errorMsg, ResDetails resDetails) {
        String source = PathTools.getLocalDomainId();
        NotifyWorker.getInstance().sendError(this.getCoordRequest(),
                                             this.failedEvent,
                                             errorMsg,
                                             source,
                                             resDetails);
    }

    /**
     * Called when the underlying coordAction fails.
     * This method should only be called if the replyRequest content is null or
     * if no associated pathRequest method is found. if the PSSReply contains an error
     * message, that will be handled by the PathRequest failed method.
     * @param exception
     */
    public void failed (Exception exception) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId());
        netLogger.setGRI(this.getGRI());
        String method = "PSSReplyRequest.execute";
        LOG.error(netLogger.error(method, ErrSev.FATAL, " PSSReplyRequest failed with " + exception.getMessage()));

                // Fill in any missing parts of the errorReport.
        ErrorReport errorRep = this.getCoordRequest().getErrorReport(method, this.failedEvent, exception);

        // update status to FAILED
        RMUpdateFailureStatus action = new RMUpdateFailureStatus (this.getName() + "-RMStoreAction",
                                                                  this,
                                                                  this.getGRI(),
                                                                  StateEngineValues.FAILED,
                                                                  errorRep);
        action.execute();

        if (action.getState() == CoordAction.State.FAILED) {
            LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                      action.getException().getMessage()));
        }

        this.notifyError ("rmUpdateStatus failed with exception " +
                              action.getException().getMessage(), this.getGRI());
        super.failed(exception);
    }
}
