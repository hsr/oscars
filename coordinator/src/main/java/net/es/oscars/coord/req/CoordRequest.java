package net.es.oscars.coord.req;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.coord.actions.RMGetStatusAction;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.resourceManager.soap.gen.GetStatusRespContent;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.quartz.SimpleTrigger;
import org.quartz.JobDetail;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.coord.jobs.RequestTimerJob;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;

/**
 * CoordRequest base class for coordinator requests
 * stores some common parameters from the input message, implements get and set methods
 * Keeps a  pendingRequest hashMap for all uncompleted requests, and creates a RequestTimerJob to check that
 * the request has been completed within the WATCHDOG_TIMER interval, currently 15 minutes.
 * Extended by: CreateReservationRequest, PathRequest, PSSReplyRequest, CancelRequest, ModifyReservationRequest,
 *              QueryReservationRequest, ListReservationRequest, InterDomainEventRequest, GetErrRepRequest
 * 
 * @author lomax
 *
 * @param <P> Class of the Coordinator's request message content
 * @param <R> Class of the Coordinator's reply message content
 */
public class CoordRequest<P,R> extends CoordAction<P,R> implements Comparable<CoordRequest<P,R>>, Serializable {
    
    public static final String  LOCALID_PREFIX = "LocalID-";
    // Standard attribute names
    public static final String  DESCRIPTION_ATTRIBUTE = "Description";
    public static final String  LOGIN_ATTRIBUTE = "Login";
    public static final String  SUBJECT_ATTRIBUTES = "SubjectAttributes";
    public static final String  STATE_ATTRIBUTE ="ReservationState";
    protected static final String moduleName = ModuleName.COORD;

    private static long             currentLocalId = 0L;   // Get incremented for each new request
    private static final long       serialVersionUID = 2L;
    private String                  gri;
    private String                  gTransId;
    private long                    localId = 0L; // set to currentLocalId, used to index into pendingRequests
    private Long                    receivedTime;
    private AuthConditions          authConditions;
    private MessagePropertiesType   msgProps;
    private ResDetails              resDetails = null;  // local version of resDetails saved for interDomain reservations
    private boolean                 isCommitPhase = false; // set to true when pce operations enter commit phase
    private boolean                 isCompletePhase = false; // set to true when pce operations enter complete phase
    private HashMap<String, Object> attributes = new HashMap<String, Object>(); // place to store extra information
    private static final Logger LOG = Logger.getLogger(CoordRequest.class.getName());

    // list of uncompleted requests indexed by localId
    private static HashMap<String, CoordRequest> pendingRequests = new HashMap<String, CoordRequest>();
    // mapping of a string name for a request to its localId. Used for PathRequests and CancelRequests where
    // the request needs to be found by its name.
    private static HashMap<String, String> aliases = new HashMap<String, String>();

    private static long WATCHDOG_TIMER = (15 * 60 * 1000); // 15 minutes

    /**
     *
     * @param name Typically set to operation+gri by CoordImpl. Used for logging and debuggint
     * @param gTransId The global transaction of the request that CoordImpl received
     */
    public CoordRequest(String name, String gTransId) {
        super (name, null, null);
        this.setInstance(gTransId,null,null);
    }
    /**
     *
     * @param name Typically set to operation+gri by CoordImpl. Used for logging and debuggint
     * @param gTransId The global transaction of the request that CoordImpl received
     * @param gri GlobalReservationId for the reservation that is being acted on.
     */
    public CoordRequest(String name, String gTransId, String gri) {
        super (name, null, null);
        this.setInstance(gTransId,gri,null);
    }
    /**
     *
     * @param name Typically set to operation+gri by CoordImpl. Used for logging and debuggint
     * @param gTransId The global transaction of the request that CoordImpl received
     * @param gri GlobalReservationId for the reservation that is being acted on.
     * @param authConds Any authorization constraints that need to enforced during execution of the request
     */
    public CoordRequest(String name, String gTransId, String gri, AuthConditions authConds) {
        super (name, null, null);
        this.setInstance(gTransId, gri, authConds);
    }
    public Object getAttribute (String attribute) {
        synchronized (this.attributes) {
            Object object = this.attributes.get(attribute);
            return object;
        }
    }

    public Object setAttribute (String attribute, Object newObject) {
        synchronized (this.attributes) {
            Object oldObject = this.attributes.put(attribute, newObject);
            return oldObject;
        }
    }
    public String getTransactionId() {
        return this.gTransId == null ? "NoTransactionID" : this.gTransId;
    }

    public synchronized void setTransactionId (String gTransId) {
        this.gTransId = gTransId;
    }
    public String getGRI() {
        return this.gri;
    }

    public synchronized void setGRI (String gri) {
        if (gri != null) {
            this.gri = gri;
        } else {
            this.gri = CoordRequest.LOCALID_PREFIX + (new Long(CoordRequest.currentLocalId)).toString();
        }
    }
    public String getLocalId() {
        return Long.toString(this.localId);
    }

    public void setResDetails (ResDetails resDetails){
        this.resDetails = resDetails;
    }

    public ResDetails getResDetails() {
        return this.resDetails;
    }

    /**
      * Called when the pceRuntime for this request starts the commit phase
      * @param isCommit  should be true
      */
     public void setCommitPhase(boolean isCommit){
         isCommitPhase = isCommit;
     }
     
     /**
      * Called when the pceRuntime for this request starts the complete phase
      * @param isCommit  should be true
      */
     public void setCompletePhase(boolean isComplete){
         isCompletePhase = isComplete;
     }
     
     public boolean inCommitPhase() {
         return isCommitPhase;
     }
     
     public boolean inCompletePhase() {
         return isCompletePhase;
     }


    @SuppressWarnings("unchecked")
    private void setInstance (String gTransId, String gri, AuthConditions authConds) {
        synchronized (this) {
            this.localId = ++CoordRequest.currentLocalId;
        }
        this.setTransactionId(gTransId);
        this.setGRI(gri);
        this.setCoordRequest(this);
        this.setReceivedTime(System.currentTimeMillis()/1000L);
        this.setAuthConditions(authConds);
        this.setTimer();
    }

    public AuthConditions getAuthConditions() {
        return this.authConditions;
    }

    public void setAuthConditions(AuthConditions authConds){
        this.authConditions = authConds;
    }

    public MessagePropertiesType getMessageProperties() {
        return this.msgProps;
    }
    public void setMessageProperties(MessagePropertiesType msgProps){
        this.msgProps = msgProps;
    }

    public void logError() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        List<CoordAction> failedActions = this.getFailedCoordActions ();
        for (CoordAction action: failedActions) {
            Exception ex = null;
            if (action.getException() != null) {
                ex = action.getException();
                LOG.warn(netLogger.error(action.getName(), ErrSev.MINOR, " failed with exception " + ex.getMessage()));
            }
        }
    }

    /**
     *   Called from various coordRequests to ensure that the OSCARSServiceException they are
     *   passing on has a useful ErrorReport.
     *
     *   Checks to see if the exception contains an errorReport or faultReport. If it doesn't
     *   it will create  an errorReport. If it finds an errorReport will be sure that the gri, transId,
     *   domainId and module name are set.
     * @param method
     * @param errorCode
     * @param ex
     * @return
     */
    public ErrorReport getErrorReport( String method, String errorCode, Exception ex) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG.debug(netLogger.start(method+".getErrorReport", ex.getClass().getName()));
        ErrorReport errorRep = null;

        if (ex.getClass().getName().endsWith("OSCARSFaultMessage")) {
            errorRep = ErrorReport.fault2report(((OSCARSFaultMessage)ex).getFaultInfo().getErrorReport());
        } else if (ex.getClass().getName().endsWith("OSCARSServiceException")) {
            errorRep = ((OSCARSServiceException)ex).getErrorReport();
        }
        if (errorRep != null) {
            if (errorRep.getErrorCode() == null){
                errorRep.setErrorCode(errorCode);
            }
            if (errorRep.getErrorMsg() == null ) {
                errorRep.setErrorMsg("unknown failure");
            }
            if (errorRep.getErrorType() == null) {
                errorRep.setErrorType(ErrorReport.SYSTEM);
            }
            if (errorRep.getGRI() == null) {
                errorRep.setGRI(this.getGRI());
            }
            if (errorRep.getTransId() == null){
                errorRep.setTransId(this.getTransactionId());
            }
            if (errorRep.getModuleName() == null) {
                errorRep.setModuleName(CoordRequest.moduleName);
            }
            if (errorRep.getTimestamp() == null ||
                    errorRep.getTimestamp() == 0L) {
                errorRep.setTimestamp(System.currentTimeMillis() / 1000L);
            }
            if (errorRep.getDomainId() == null ){
                errorRep.setDomainId(PathTools.getLocalDomainId());
            }
        }
        if (errorRep == null) {
            LOG.debug(netLogger.getMsg(method+".getErrorReport","got exception with null errorReport"));
             errorRep = new ErrorReport(errorCode, ex.getMessage(),
                                                  ErrorReport.UNKNOWN, this.getGRI(),this.getTransactionId(),
                                                  System.currentTimeMillis()/1000L, CoordRequest.moduleName,
                                                  PathTools.getLocalDomainId());
        }
        return errorRep;
    }
    public String toString() {
        return this.gri + "-" + this.getName();
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        CoordRequest t = (CoordRequest) o;
        if (o == null) {
            return false;
        }
        return (t.getGRI().equals(this.gri));
    }

    public int compareTo(CoordRequest<P,R> o) {
        return this.receivedTime.compareTo(o.getReceivedTime());
    }

    public synchronized Long getReceivedTime() {
        return this.receivedTime;
    }

    public synchronized void setReceivedTime(Long receivedTime) {
        this.receivedTime = receivedTime;
    }

    /**
     * notifyError called when the execution of the CoordRequest or its CoordAction failed.
     * Note that failure, in this context, refers to exceptions or other type of failures
     * that are "internal", such as an exception, unreachable IDC.
     * Should be implemented by derived classes.
     * @param errorMsg
     * @param resDetails
     */
    public void notifyError (String errorMsg, ResDetails resDetails) {
        throw new RuntimeException("No implementation of the method " + this.getClass().getName() + ".error()");
    }

    /**
     * notifyError called when the execution of the CoordRequest or its CoordAction failed.
     * Note that failure, in this context, refers to exceptions or other type of failures
     * This is a helper method wrapping CoordRequest.notifyError(String errorMsg, ResDetails resDetails)
     * @param errorMsg
     * @param errorGri
     */
    public final void notifyError (String errorMsg, String errorGri) {
        ResDetails resDetails = new ResDetails();
        resDetails.setGlobalReservationId(errorGri);
        resDetails.setLogin(this.getMessageProperties().getOriginator().getSubjectAttribute().get(0).getName());
        this.notifyError(errorMsg, resDetails);
    }

    /**
     * setTimer creates a RequestTimer job for each new request that is scheduled to go off after the
     * WATCHDOG_TIMER interval and check to see if the job is completed.
     */
    private void setTimer () {

        String name = this.getName();
        Date timer = new Date (System.currentTimeMillis() + CoordRequest.WATCHDOG_TIMER);

        SimpleTrigger jobTrigger = new SimpleTrigger(this.getName() + "-watchdog-" + this.getTransactionId(),
                                                     null,
                                                     timer);

        JobDetail     jobDetail  = new JobDetail(this.getName() + "-watchdog-" + this.getTransactionId(),
                                                 null,
                                                 RequestTimerJob.class);
        jobDetail.setVolatility(false);
        RequestTimerJob.setRequestId(jobDetail, Long.toString(this.localId));

        try {
            Coordinator.getInstance().getScheduler().scheduleJob(jobDetail, jobTrigger);
        } catch (Exception e) {
            OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
            LOG.error (netLogger.error("setTimer", ErrSev.MINOR,
                       "Cannot create watchdog job for request: " + this.getName()));
        }
        this.addToPending(Long.toString(this.localId));
    }

    private void addToPending (String lid) {

        synchronized (CoordRequest.pendingRequests) {
           CoordRequest oldRef = CoordRequest.pendingRequests.get(lid);
           if (oldRef != null) {
               throw new RuntimeException("CoordRequest.addToPending " + this.getName() +
                                          " attempting to enter existing lid " + lid );
           }
           pendingRequests.put(lid,this);
        }
     }



    static public CoordRequest getCoordRequestById (String lid) {
        synchronized (CoordRequest.pendingRequests) {
            CoordRequest request = CoordRequest.pendingRequests.get(lid);
            return request;
        }
    }

    static public void forget (String lid) {
        CoordRequest request = null;
        synchronized (CoordRequest.pendingRequests) {
            request = CoordRequest.pendingRequests.remove(lid);
        }
    }

    /**
     *  registers an alias. Overwrites any existing alias by that name.
     * @param alias  alias name to be registered
     */
    public void registerAlias (String alias) {
        synchronized (CoordRequest.aliases) {
            CoordRequest.aliases.put(alias,  Long.toString(this.localId));
        }
    }

     /**
      * unRegisters an alias. Deletes alias.
      * Called when PathRequest is completed or failed
      * @param alias  alias name to be registered
      */
    public void unRegisterAlias (String alias) {
        String lid = null;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        synchronized (CoordRequest.aliases) {
           lid =CoordRequest.aliases.remove(alias);
        }
        LOG.debug(netLogger.end("unregisterAlias", "lid is " + lid));
    }

    /**
     *  registers an alias if no such alias already exists.
     * @param alias name to be registered
     * @return if an coordRequest with this alias exists returns it,
     *          if not, returns null
     */
    public CoordRequest registerExclusiveAlias (String alias) {
        synchronized (CoordRequest.aliases) {
            String lid = aliases.get(alias);
            if (lid == null ){
                CoordRequest.aliases.put(alias,  Long.toString(this.localId));
                return null;
            }
        return CoordRequest.getCoordRequestById(lid);
        }
    }

    public static synchronized CoordRequest getCoordRequestByAlias (String alias) {
        synchronized (CoordRequest.aliases) {
            String lid = CoordRequest.aliases.get(alias);
            if (lid == null) {
                return null;
            }
            return CoordRequest.getCoordRequestById(lid);
        }
    }

    /**
     * Should be overridden by requests that are not complete unless all the
     * domains involved  are also complete.
     * @return  true if all the PCE actions are finished.
     */
    public boolean isRequestComplete() {
        return super.isFullyCompleted();
    }
       /**
     * Method to check status of reservation.
     *
     * @return the reservation status
     */
    public String getResvStatus() {

        RMGetStatusAction statusAction = new RMGetStatusAction("GetStatus-" + this.getGRI(),
                                                               this.getCoordRequest(),
                                                               this.getGRI());
        statusAction.execute();
        if (statusAction.getState() == CoordAction.State.FAILED){
            //can't determine state
            return null;
        }
        GetStatusRespContent qReply = statusAction.getResultData();
        return qReply.getStatus();
    }

    /**
     * Called when a coordRequest has failed and other domains may need to be informed
     * Send IDE even if not necessary. Expects targetDomain to ignore any events that it
     *    has already handled or knows nothing about.
     * @param eventType  Type of error result
     * @param isCommitPhase true if error occurred during the commit phase
     * @param errorReport details about the error
     * @param path  path for the reservation
     * @param resDetails details for the reservation , may be null
     */
    public void sendErrorEvent(String eventType,
                               boolean isCommitPhase,
                               ErrorReport errorReport,
                               CtrlPlanePathContent path,
                               ResDetails resDetails ) {

        if (path == null || PathTools.isPathLocalOnly(path)) {
            return;
        }
        OSCARSNetLogger netLogger =  OSCARSNetLogger.getTlogger();
        String event = "sendErrorEvent";
        if (resDetails == null ) {
            resDetails = new ResDetails();
            resDetails.setGlobalReservationId(this.getGRI());
        }
        LOG.debug(netLogger.start(event, eventType));

        try {
            String localDomain = PathTools.getLocalDomainId();
            String previousDomain = PathTools.getPreviousDomain(path,localDomain);
            String nextDomain =  PathTools.getNextDomain(path, localDomain);
            InternalAPIWorker apiWorker = InternalAPIWorker.getInstance ();

            if (previousDomain != null ) {
                apiWorker.sendErrorEvent(this.getCoordRequest(),
                                         eventType,
                                         errorReport,
                                         previousDomain);
                /*
                apiWorker.sendErrorEvent(this.getCoordRequest(),
                                         resDetails,
                                         eventType,
                                         errorReport.getErrorMsg(),
                                         errorReport.getDomainId(),
                                         previousDomain);
                                         */
            }

            if (nextDomain != null ) {
                    if (isCommitPhase || eventType.equals(NotifyRequestTypes.RESV_CANCEL_FAILED)) {
                        apiWorker.sendErrorEvent(this.getCoordRequest(),
                                         eventType,
                                         errorReport,
                                         nextDomain);
                   /* apiWorker.sendErrorEvent(this.getCoordRequest(),
                                             resDetails,
                                             eventType,
                                             errorReport.getErrorMsg(),
                                             errorReport.getDomainId(),
                                             nextDomain);
                                             */
                }
            }
        } catch (OSCARSServiceException oEx) {
            LOG.error(netLogger.end(event, "failed to send IDE. Exception was " +
                                       oEx.getMessage()));
        }

    }
}
