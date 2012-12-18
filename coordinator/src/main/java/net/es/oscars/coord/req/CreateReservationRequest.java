package net.es.oscars.coord.req;


import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.coord.actions.*;
import net.es.oscars.coord.workers.NotifyWorker;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.runtimepce.PCEData;
import net.es.oscars.coord.runtimepce.ProxyAction;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import java.util.HashSet;

/**
 * A coordRequest to handle CreateReservation requests. Input parameters are placed in
 * setRequestData of type ResCreateContent. SetResultData of type CreateReservationResults is
 * not actually used. Instead the  new GRI is placed in the CoordRequest and read from there by
 * the caller. 
 * 
 * @author lomax
 */
// TODO need to add AuthConditions to deal with site admin privilege.
public class CreateReservationRequest extends CoordRequest <ResCreateContent,CreateReservationResults >{
    
    private static final long       serialVersionUID  = 1L;
    private PCEData pceData =  null;
    private boolean hasFailed = false;
    private static final Logger LOG = Logger.getLogger(CreateReservationRequest.class.getName());
    private OSCARSNetLogger netLogger = null;

    public CreateReservationRequest(String name, String loginName, ResCreateContent createResvReq) {
        super (name,createResvReq.getMessageProperties().getGlobalTransactionId());
        this.setRequestData(loginName, createResvReq);
        this.setCoordRequest(this);
        this.setLog();
    }

    public CreateReservationRequest(String gri, String name, String loginName, ResCreateContent createResvReq) {
        super (name, createResvReq.getMessageProperties().getGlobalTransactionId(), gri);
        this.setRequestData(loginName,createResvReq);
        this.registerAlias("createReservation-" + gri);
        this.setCoordRequest(this);
        this.setLog();
    }
    
    public void setRequestData (String loginName, ResCreateContent createResvReq) {
        // Set input parameter using base class method
        super.setRequestData (createResvReq);
        // Add the reservation description to the attribute list of the request.
        this.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE, createResvReq.getDescription());
        // Add login attribute
        this.setAttribute(CoordRequest.LOGIN_ATTRIBUTE, loginName);
        this.setLog();
    }

    /**
     * Called by CoordImpl to start the execution of a Create Reservation request. 
     * Synchronous parts of the processing are done and a PCERuntime action is
     * created to start the path calculation. If it returns without setting an error
     * the new GRI has been placed in this CoordRequest object  and the reservation
     * has been stored in the RM with status ACCEPTED
     * @params were set by the constructor or by setRequestData
     * @return sets the new GRI into this request object
     * @throws OSCARSServiceException - nothing is expected, but could get runtimeError
     */
    public void execute()  {
        String method = "CreateReservationRequest.execute";
        Boolean needToUpdateStatus = false;
        
        LOG.debug(netLogger.start(method));

        ResCreateContent  createResvReq = this.getRequestData();
        try {
            // Check if the request already has a GRI
            if (createResvReq.getGlobalReservationId() != null) {
                this.setGRI(createResvReq.getGlobalReservationId());
            } else {
                // No GRI yet. Ask the Resource Manager to generate one.
                RMGenerateGRIAction rmGenerateGRIAction = new RMGenerateGRIAction (this.getName() + "-GenerateGRI", this);
                // synchronous call
                rmGenerateGRIAction.execute();
                if (rmGenerateGRIAction.getState() == CoordAction.State.FAILED ){
                    throw rmGenerateGRIAction.getException();
                }
                this.setGRI(rmGenerateGRIAction.getResultData());
                this.registerAlias("createReservation-" + this.getGRI());
            }
            netLogger.setGRI(this.getGRI());

            // Store the tentative path into the resource manager
            ResDetails resDetails = new ResDetails();
            resDetails.setCreateTime(this.getReceivedTime());
            resDetails.setGlobalReservationId(this.getGRI());
            // Set description
            String description = createResvReq.getDescription();
            if (description != null) {
                resDetails.setDescription(description);
            } else {
                resDetails.setDescription("No description provided");
            }
            // Set login
            String login = (String) this.getAttribute(CoordRequest.LOGIN_ATTRIBUTE);
            resDetails.setLogin(login);
            // Set user constraints
            resDetails.setUserRequestConstraint(createResvReq.getUserRequestConstraint());
            resDetails.setReservedConstraint(createResvReq.getReservedConstraint());
            resDetails.getOptionalConstraint().addAll(createResvReq.getOptionalConstraint());

            // Set Status
            resDetails.setStatus(StateEngineValues.ACCEPTED);
            needToUpdateStatus = true;
            // Create, set and invoke the RMStoreAction
            RMStoreAction rmStoreAction = new RMStoreAction(this.getName() + "-RMStoreAction", this, resDetails);
            rmStoreAction.execute();
            if (rmStoreAction.getState().equals(CoordAction.State.FAILED)) {
                throw rmStoreAction.getException();
            }
            PCERuntimeAction pceRuntimeAction =
                    new PCERuntimeAction (this.getName() + "-Create-PCERuntimeAction",
                                          this,
                                          null,
                                          this.getTransactionId(),
                                          PCERequestTypes.PCE_CREATE);

             pceData = new PCEData(createResvReq.getUserRequestConstraint(),
                                          createResvReq.getReservedConstraint(),
                                          createResvReq.getOptionalConstraint(),
                                          null);

            pceRuntimeAction.setRequestData(pceData);

            this.add(pceRuntimeAction);
            this.executed();
        } catch (Exception ex ) {
            String message = ex.getMessage();
            if (message == null ) {
                message = ex.toString();
            }
            LOG.warn(netLogger.error(method, ErrSev.MINOR, "caught Exception "+ message));
            this.fail (new OSCARSServiceException(method + "caught Exception: " + message));
        }
        LOG.debug(netLogger.end(method));
    }

    /**
     * Overrides method in coordRequest to be sure that a multi-domain create reservation
     * is not waiting for notification from another domain.
     * @return
     */
    public boolean isRequestComplete() {
        HashSet<String> incompleteStatus = new HashSet<String>();
        incompleteStatus.add(StateEngineValues.ACCEPTED);
        incompleteStatus.add(StateEngineValues.INPATHCALCULATION);
        incompleteStatus.add(StateEngineValues.PATHCALCULATED);
        incompleteStatus.add(StateEngineValues.INCOMMIT);
        incompleteStatus.add(StateEngineValues.COMMITTED);
        if (isFullyCompleted() && !incompleteStatus.contains(getResvStatus())) {
            return true;
        }
        return false;
    }

    /**
       * Process an internal error (the local IDC failed to process a query: an internal error
       * is when something went wrong within the IDC itself.
       * CoordRequest implementations are expected to implement it.
       * @param errorMsg
       * @param resDetails
       */
     public void notifyError (String errorMsg, ResDetails resDetails) {
          String source = PathTools.getLocalDomainId();
          NotifyWorker.getInstance().sendError(this.getCoordRequest(),
                                               NotifyRequestTypes.RESV_CREATE_FAILED,
                                               errorMsg,
                                               source,
                                               resDetails);
     }


    /**
     *  failed called from coordAction.fail when the base coordAction has failed. The base coordAction
     *  will fail whenever one of its pceruntime proxy actions fails.
     *
     *  The resourceManage should be informed of the failure and store an ErrorReport
     *  A  notify message of RESERVATION_CREATE_FAILED should be sent, and if this in an
     *  interdomain reservation the peerIDCs will sent an InterdomainEvent of  RESERVATION_CREATE_FAILED
     * @param exception reason for failure
     */
    public void failed (Exception exception) {
        String method = "CreateReservationRequest.failed";
        LOG.error(netLogger.error(method, ErrSev.FATAL, " CreateReservation failed with " + exception.getMessage()));

        if (hasFailed) {
            return;
        }
        hasFailed = true;

        if (this.getGRI() == null ) {
            // died trying to get a GRI
            return;
        }
        // update status
        ErrorReport errorRep = this.getErrorReport(method, ErrorCodes.RESV_CREATE_FAILED, exception);
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

        if (pceData != null) {
            CtrlPlanePathContent reservedPath = ProxyAction.getPathFromPceData(pceData);
            if (reservedPath != null) {
            this.sendErrorEvent(NotifyRequestTypes.RESV_CREATE_FAILED,
                                this.inCommitPhase(),
                                errorRep,
                                reservedPath,
                                null);
            }
        }
        // send notification of createReservation failure
        this.notifyError (errorRep.getErrorCode() + ":" + errorRep.getErrorMsg(),
                          this.getGRI());
        this.unRegisterAlias("createReservation-" + this.getGRI());
        PCERuntimeAction.releaseMutex(this.getGRI());
        super.failed(exception); // this calls coordAction.failed
    }

    private void setLog() {
        this.netLogger = OSCARSNetLogger.getTlogger();
        this.netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        this.netLogger.setGRI(this.getGRI());
    }

}
