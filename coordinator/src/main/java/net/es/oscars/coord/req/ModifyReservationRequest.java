package net.es.oscars.coord.req;

import java.util.HashSet;

import net.es.oscars.coord.actions.RMUpdateFailureStatus;
import net.es.oscars.coord.workers.NotifyWorker;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.ModifyResReply;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.runtimepce.PCEData;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.utils.soap.OSCARSServiceException;

/**
 * A coordRequest to handle ModifyReservation requests. Input parameters are placed in
 * setRequestData of type ModifyResContent. SetResultData of type ModifyResReply is
 * not actually used.
 *
 */
public class ModifyReservationRequest extends CoordRequest <ModifyResContent,ModifyResReply >{
    
    private static final long       serialVersionUID  = 1L;
    private static final Logger     LOG = Logger.getLogger(ModifyReservationRequest.class.getName());
    private OSCARSNetLogger         netLogger = null;
    private boolean                 hasFailed = false; // true when the failed method has been called.

    public ModifyReservationRequest(String name, 
                                    String gri,
                                    String loginName, 
                                    AuthConditions authConds, 
                                    ModifyResContent modifyResvReq) {
        super (name, modifyResvReq.getMessageProperties().getGlobalTransactionId(), gri, authConds);
        this.setRequestData(loginName, modifyResvReq);
        this.setCoordRequest(this);
        this.registerAlias(name);
        this.setMessageProperties(modifyResvReq.getMessageProperties() );
        this.setLog();
    }

    public void setRequestData (String loginName, ModifyResContent modifyResvReq) {
        // Set input parameter using base class method
        super.setRequestData (modifyResvReq);
        // Add the reservation description to the attribute list of the request.
        this.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE, modifyResvReq.getDescription());
        // Add login attribute
        this.setAttribute(CoordRequest.LOGIN_ATTRIBUTE, loginName);
    }

    /**
     * Called by CoordImpl to start the execution of a ModifyReservation request. 
     * Synchronous parts of the processing are done and a PCERuntime action is
     * created to start the path calculation. I
     * @params were set by the constructor or by setRequestData
     * @throws OSCARSServiceException - nothing is expected, but could get runtimeError
     */
    public void execute()  {

        ModifyResContent  request = this.getRequestData();
        ModifyResReply reply = new ModifyResReply();
        String method = "ModifyReservationRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method + " " + this.getGRI()));

        try {
            /* Call resource Manager to do its part of modifyReservation
             * the authConditions for modify are passed through the resourceManager which will
             * check for permitted domains and permitted login conditions
             * it checks that the reservation is in a state that allows modification
             * and cancels any pending setup or teardown path requests
             * returns the current reservation status if all conditions are met.
             */
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();
            Object [] req = new Object[]{this.getAuthConditions(),request};
            Object[] res = rmClient.invoke("modifyReservation",req);

            if ((res == null) || (res[0] == null)) {
                this.notifyError("No response from ResourceManager", this.getGRI());
                throw new OSCARSServiceException (ErrorCodes.COULD_NOT_CONNECT,
                                                 "No response from ResourceManager",
                                                  ErrorReport.SYSTEM);
            }
            ModifyResReply response = (ModifyResReply) res [0];
            this.setResultData(response);  // contains current reservation status
            this.setAttribute(CoordRequest.STATE_ATTRIBUTE,response.getStatus());

            LOG.debug(netLogger.getMsg(method,"received modify for reservation in state " +
                                       response.getStatus()));

            PCERuntimeAction pceRuntimeAction = new PCERuntimeAction (this.getName() + "-Modify-PCERuntimeAction",
                                                                      this,
                                                                      null,
                                                                      this.getTransactionId(),
                                                                      PCERequestTypes.PCE_MODIFY);

            PCEData pceData = new PCEData(request.getUserRequestConstraint(),
                                          request.getReservedConstraint(),
                                          request.getOptionalConstraint(),
                                          null);

            pceRuntimeAction.setRequestData(pceData);

            this.add(pceRuntimeAction);
            this.executed();

        } catch (OSCARSServiceException ex ) {
            LOG.debug(netLogger.error(method, ErrSev.MINOR, "caught OSCARSServiceException "+ ex.getMessage()));
            this.fail(ex);
        } catch (Exception ex ) {
            String message = ex.getMessage();
            if (message == null ) {
                message = ex.toString();
            }
            LOG.debug(netLogger.error(method, ErrSev.MINOR, "caught Exception "+ message));
            ex.printStackTrace();
            this.fail (new OSCARSServiceException(ErrorCodes.RESV_MODIFY_FAILED,
                                                 method + "caught Exception: " +ex.getMessage(),
                                                 ErrorReport.SYSTEM));
        }
        LOG.debug(netLogger.end(method));
        return;
    }
    private void setLog() {
        this.netLogger = OSCARSNetLogger.getTlogger();
        this.netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        this.netLogger.setGRI(this.getGRI());
    }

    /**
     * Overrides method in coordRequest to be sure that a multi-domain modifyReservation
     * is not waiting for notification from another domain.
     * @return
     */
    public boolean isRequestComplete() {
        HashSet<String> incompleteStatus = new HashSet<String>();
        incompleteStatus.add(StateEngineValues.INMODIFY);
        incompleteStatus.add(StateEngineValues.MODCOMMITTED);
        // incompleteStatus.add(StateEngineValues.INCOMMIT);
        if (isFullyCompleted() && !incompleteStatus.contains(getResvStatus())) {
            return true;
        }
        return false;
    }

    /**
     * Process an internal error (the local IDC failed to process a query: an internal error
     * is when something went wrong within the IDC itself.
     * CoordRequest implementation are expected to implement it.
     * @param errorMsg
     * @param resDetails
     */
    public void notifyError (String errorMsg, ResDetails resDetails) {
        String source = PathTools.getLocalDomainId();
        NotifyWorker.getInstance().sendError(this.getCoordRequest(),
                                             NotifyRequestTypes.RESV_MODIFY_FAILED,
                                             errorMsg,
                                             source,
                                             resDetails);
    }

    /**
     *  Does not fail the reservation but sets the status to the original status (RESERVED or ACTIVE)
     *  saves an error report, sends RESERVATION_MODIFICATION.FAILED IDE events and notify message
     *  Depending on when the error occurred, the parameters to be modified may be the original values
     *  (if roll back was possible) or the new ones (if the modification had been committed in this domain)
     * @param exception  that was handed to coordAction.fail()
     */
    public void failed (Exception exception) {
        String method = "ModifyReservationRequest.execute";
        LOG.error(netLogger.error(method, ErrSev.FATAL, " ModifyReservationRequest failed with " + exception.getMessage()));
        if (hasFailed) {
            return;
        }
        hasFailed = true;

        ErrorReport errorRep = this.getCoordRequest().getErrorReport(method,
                                                                     ErrorCodes.RESV_MODIFY_FAILED,
                                                                     exception);


         String newState= (String)this.getCoordRequest().getAttribute(CoordRequest.STATE_ATTRIBUTE);
         RMUpdateFailureStatus action = new RMUpdateFailureStatus (this.getName() + "-RMStoreAction",
                                                                   this,
                                                                   this.getGRI(),
                                                                   newState,
                                                                   errorRep);
        action.execute();

        if (action.getState() == CoordAction.State.FAILED) {
            LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                      action.getException().getMessage()));
        }
        // Send  IDE fail events
        this.getCoordRequest().sendErrorEvent(NotifyRequestTypes.RESV_MODIFY_FAILED,
                                              this.inCommitPhase(),
                                              errorRep,
                                              getResDetails().getReservedConstraint().getPathInfo().getPath(),
                                              getResDetails());

        // send notification of modifyReservation failure
        this.notifyError (errorRep.getErrorCode() + ":" + errorRep.getErrorMsg(),
                          this.getGRI());

        this.unRegisterAlias(this.getName());
        PCERuntimeAction.releaseMutex(this.getGRI());
        super.failed(exception);
    }

}
