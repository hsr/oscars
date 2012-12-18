package net.es.oscars.coord.actions;

import java.lang.Exception;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.lang.ref.WeakReference;

import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.JobDetail;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.jobs.CoordJob;
import net.es.oscars.coord.jobs.CoordActionCleanerJob;
import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;

/**
 * CoordAction an abstract class that can be queued for later execution
 * Implemented by: CoordRequest, ProxyAction, PCERuntimeAction, PSSCreatePathAction, PSSTeardownPathAction,
 *      AuthZCheckAccessAction, LockAction, UnlockAction, NotifyAction, ForwarderAction,
 *      CommittedEventAction, CreateResvCompletedAction, ModifyResvAction
 *      RMGenerateGRIAction,RMStoreAction,RMUpdateStatusAction, RMUpdateFailureStatus
 *      RMGetStatusAction
 * @author lomax
 *
 * @param <P>  Class containing the input data for the action
 * @param <R>  Class for returning the results of the action
 */
@SuppressWarnings("unchecked")
public abstract class CoordAction <P,R> extends LinkedList <CoordAction> {
    
    public  static String           LOCALID_PREFIX = "LocalID-";
    private static final long       serialVersionUID = 1L;
    private static long             currentLocalId = 0L;
    
    public static enum State {
        UNPROCESSED,
        PROCESSING,
        PROCESSED,
        FAILED,
        CANCELLED
    }
    
    private   CoordRequest coordRequest   = null;
    protected State        state          = State.UNPROCESSED;
    private   P            requestData    = null; // data input to coordinator request
    private   R            resultData     = null; // data to be returned in coordinator reply
    private   String       name           = "Unknown"; // a unique name based on operation and gri
    private   String       id             = null; // a unique id generated for this action
                                                  // used in scheduling a coordJob execution
    protected Exception    exception      = null; // saved exception if one occurred during execution
    private   Boolean      cleanedup      = false; // true when a cleanup has happened or is happening.
    private   Boolean      failed         = false; // true when the action has been already failed.
    private   Boolean      cancelled      = false; // true when the action has been already cancelled.

    // map of action.id and actions used in scheduling a coordJob execution.
    private static HashMap<String, WeakReference<CoordAction>> actions = new HashMap<String, WeakReference<CoordAction>>();
    private static final Logger LOG = Logger.getLogger(CoordAction.class.getName());
 
    // Start the background thread that will prune empty entries in the actions map.
    private static long CLEANER_REPEAT = (15 * 60 * 1000); // 15 minutes
    
    static {
        SimpleTrigger jobTrigger = new SimpleTrigger("CoordAction.Cleaner",
                                                     null,
                                                     SimpleTrigger.REPEAT_INDEFINITELY,
                                                     CoordAction.CLEANER_REPEAT);
        
        JobDetail     jobDetail  = new JobDetail("CoordAction.Cleaner", null, CoordActionCleanerJob.class);
        jobDetail.setVolatility(false);
        
        try {
            Coordinator.getInstance().getScheduler().scheduleJob(jobDetail, jobTrigger);
        } catch (SchedulerException e) {
            LOG.error("Could not schedule CoordActionCleaner. CoordActions.actions may not be garbage collected");
            e.printStackTrace();
        } catch (OSCARSServiceException e) {
            LOG.error("Could not schedule CoordActionCleaner. CoordActions.actions may not be garbage collected");
            e.printStackTrace();
        }     
    }
    
    /**
     * Creates a CoordAction
     * 
     * @param name a unique identifier based on the operation name and gri
     * @param request is the CoordRequest this action is part of.
     * @param data input data of the action
     */
    public CoordAction (String name, CoordRequest request, P data) {
        this.coordRequest = request;
        this.requestData  = data;
        this.name         = name;
        this.state        = State.UNPROCESSED;
        // Generate a new ID (not GRI) for this action)
        CoordAction.setId(this);
        

        // Register this action
        synchronized (CoordAction.actions) {
            WeakReference<CoordAction> oldRef = CoordAction.actions.put (this.getId(), new WeakReference<CoordAction>(this));
            if (oldRef != null) {
                CoordAction oldAction = oldRef.get();
                if (oldAction != null) {
                    // An action already existed with the same name. This must not happen
                    throw new RuntimeException ("Fatal: CoordAction " + name + " already exist.");
                }
            }
        }
    }
    

    /**
     * Returns the CoordAction object associated to a name
     * @param id is the id of the CoordAction that was generated when the action was created
     * @return the CoordAction object
     */
    public static CoordAction getCoordAction (String id) {
        synchronized (CoordAction.actions) {
            WeakReference<CoordAction> ref = CoordAction.actions.get(id);
            if (ref != null) {
                CoordAction action = ref.get();
                if (action != null) {
                    // An action object of the specified name already exists. Return it.
                    return action;
                }
            }
            // No action of the specified id has been yet created. return null;
            return null;
        }        
    }

    /**
     * Adds a CoordAction as a child of this CoordAction. Each of the children CoordAction might be
     * executed in parallel.  A CoordAction is a linkedList of actions. An action may be removed from
     * this list by CoordAction.gc when it is no longer referenced anywhere else
     *
     * @param action is the child CoordAction
     * @return true (as per the general contract of Collection.add).
     */
    public boolean add (CoordAction action) {
        boolean ret = super.add (action);
        return ret;
    }

    /**
     * Returns the CoordRequest this action is part of.
     * @return the CoordRequest object or null if this CoordAction is not part of any CoordRequest
     */
    public CoordRequest getCoordRequest() {
        return this.coordRequest;
    }
    
    public void setCoordRequest (CoordRequest request) {
        this.coordRequest = request;
    }
    /**
     * Returns the local identifier of the CoordAction.
     * Which is used to identify an action between scheduling and execution
     * @return the action's identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     *  CoordAction Ids are sequential numbers starting at 1 prefixed with the LOCALID_PREFEX
     * @param action
     */
    private static synchronized void setId(CoordAction action) {
        ++CoordAction.currentLocalId;
        action.id = CoordAction.LOCALID_PREFIX + (new Long(CoordAction.currentLocalId)).toString();
    }

    /**
     * Returns the name of the CoordAction
     * @return the name of the CoordAction
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the data that is used as input of the CoordAction.
     * @return the input data
     */
    public P getRequestData() {
        return this.requestData;
    }

    /**
     * Sets the input data of the CoordAction
     * @param data is the input data
     */
    public void setRequestData (P data) {
        this.requestData = data;
    }

    /**
     * Returns the output data of the CoordAction
     * @return the output data
     */
    public R getResultData() {
        return this.resultData;
    }

    /**
     * Sets the output data of this CoordAction
     * @param data is the output data
     */
    public void setResultData (R data) {
        this.resultData = data;
    }

    /**
     * Returns the list of failed CoordActions where this CoordAction is the root of the graph.
     * @return a list of CoordActions that have failed.
     */
    public List<CoordAction> getFailedCoordActions () {
        ArrayList<CoordAction> failedActions = new ArrayList<CoordAction>();
        this.getFailedCoordActions (failedActions);
        return failedActions;
    }

    private void getFailedCoordActions (ArrayList failedActions) {
        synchronized (this) {
            if (this.state == State.FAILED) {
                failedActions.add(this);
            }
        }
        for (CoordAction action : this) {
            action.getFailedCoordActions(failedActions);
        }
    }

    /**
     *
     * @return the exception associated with a failed action
     */
    public Exception getException( ) {
        return exception;
    }

    /**
     * Returns the state of this CoordAction
     * @return the state of the CoordAction
     */
    public synchronized State getState() {
        return this.state;
    }

    /**
     * Sets the state of the CoordAction. This method is idempotent.
     * If the state is changed and the action in not already in process, process the action 
     * If the new state is FAILED or PROCESSED, notifies any threads that might be waiting
     * 
     * @param state is the new state of the CoordAction.
     */
    public void setState (State state) {
        String name = this.getName();
        String event = "setState";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        //LOG.debug("setState called for " + name + " " + state);
        synchronized (this) {
            if ((this.state == State.CANCELLED) || 
                (this.state == State.FAILED)) {              
                // CANCELLED and FAILED are final state
                return;
            }
        }
        State previousState = this.state;
        this.state = state;
        boolean toBeProcessed = false;    
        synchronized (this) {
            toBeProcessed = (previousState != state) && (state != State.PROCESSING);

            if (state == State.FAILED) {
                // Fail the request passing it the exception
                if (this.getCoordRequest() != null && this.getException() !=  null ) {
                    LOG.debug(netLogger.error(event,ErrSev.MINOR,
                                              "setting coordRequest " + this.getCoordRequest().getName()  +
                                              " to fail with exception " +
                                              this.getException().getMessage()));
                    this.getCoordRequest().fail (this.getException());
                }
                // This action is failed. Unblock any thread that might be waiting.
                this.notifyAll();
                return;
            } else if ((state == State.PROCESSED) && (this.isFullyCompleted())) {
                // This action is fully completed. Notify all
                this.notifyAll();
                return;
            }
        }
        if (toBeProcessed) {
            // The state of this action has changed. Signal the CoordRequest (root action) that it must re-process
            if (this.getCoordRequest() != null) {
                // Note that CoordRequest.process is non-blocking, so it is fine to invoke it while holding the object lock
                this.getCoordRequest().process();
            } else {
                // This is the top CoordAction, likely to be  a CoordRequest. Process it.
                this.process();
            }
        }
    }
    
    /**
     * Check if this action and any of its children are completed (Processed, Failed or Cancelled).
     * @return true if the CoordAction and its children CoordAction in the graph is completed. Returns false otherwise.
     */
    public boolean isFullyCompleted () {

        State myState = this.getState();
        if ((myState != State.PROCESSED) && (myState != State.CANCELLED) && (myState != State.FAILED)) {
            // This action is not processed nor cancelled. No need to check children, return false already.
            return false;
        }
        if ((myState == State.FAILED) || (myState == State.CANCELLED)) {
            // If this action is either FAILED or CANCELLED, there is no need to determine the state of the
            // children CoordAction in the graph
            return true;
        }
        for (CoordAction action : this) {
            if (action.isFullyCompleted() == false) {
                // If any of the children is not fully completed, return false
                return false;
            }
        }
        // If we reach this point of the method, this means that every action is fully completed. Return true.
        return true;
    }

    /**
     * First step in the process of failing an associated set of coordActions.
     * @param exception  should include an ErrorReport of what caused the failure.
     */
    public final void fail (Exception exception)  {
        if ( ! this.failed) {
            this.failed = true;
            this.failThis(exception);
        }
    }

    /**
     * Second step in the process of failing a associated set of coordActions.
     * this.SetState(FAILED) fails the CoordRequest it is part of. Thus when any action of a CoordRequest
     * fails, the CoordRequest will be failed first.
     * The input exception will be copied to the CoordRequest.
     * @param exception is an optional Exception that can be associated to the CoordAction explaining the failure.
     */
    private void failThis (Exception exception) {
        // Mark this action as failed if not already done
        State curState = this.getState();
        if (! curState.equals(State.FAILED) && ! curState.equals(State.CANCELLED)) {
            this.exception = exception;
            this.setState(State.FAILED);
            this.failed (exception);
        }
    }

    /**
      * failed must be implemented by a derived CoordAction, e.g. CoordRequest, that needs to do
      * any error handling such as notification in a failure case.  At the end of the derived failed method
      *  it should call super.failed so that all its associated actions will be canceled.
      */
     public void failed (Exception exception) {
         this.cancel();
         this.exception = exception;  // the coordRequest may have added an ErrorReport
     }

    /**
     * Cancel the action. When an action is canceled, all children actions in the graph are also canceled.
     */
      public final void cancel () {
          if (! this.cancelled) {
              this.cancelled= true;
              if (this.getState() != State.FAILED) {
                  this.setState(State.CANCELLED);
              }
              for (CoordAction action : this) {
                  action.cancel();
              }
              this.cancelled();
          }
      }

     /**
       * cancelled may be overidden by a derived CoordAction that needs to do something when it is canceled.
       * When the derived method is completed it should call super.cancelled().
       */
      public void cancelled() {
          this.cleanup();
      }

    /**
     * Cleanup this action
     */
    public final void cleanup() {
        if ( ! this.cleanedup) {
            this.cleanedup = true;
            this.cleanupThis();
        }
    }

    private void cleanupThis () {
        if ( ! this.cleanedup) {
            this.cleanedup = true;
            this.doCleanup();
        }
    }

    /**
     * doCleanup must be implemented by CoordActions that needs to do something in order to recover from an error that
     * may have occured in another CoordAction executing the same CoordRequest.
     */
    public void doCleanup() {
        // could remove this action from its list. Currently this is done by gc
        return;
    }


    /**
     * Start the execution of the action.
     * Classes that extends this CoordAction are expected to overwrite it
     */
    public void execute() {
        for (CoordAction action : this) {
            action.execute();
        }
    }

    /**
     * Sets the CoordAction as executed.
     */
    public void executed() {
        this.setState (State.PROCESSED);
    }


    /**
     * Starts the processing or re-processing of the graph of CoordAction for which this CoordAction is the root.
     * This method is asynchronous. Not only this method cannot fail, it might return before the CoordAction is
     * actually executed.
     * 
     * This method is idempotent.
     */
    public synchronized void process () {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "process";
        if (this.getCoordRequest() != null) {
            netLogger.init(ModuleName.PCERUNTIME,this.getCoordRequest().getTransactionId());
            netLogger.setGRI(this.getCoordRequest().getGRI());
        } else {
            netLogger.init(ModuleName.PCERUNTIME,"null");
        }
        // If the action is failed, do not process anything.
        if (this.state == State.FAILED) {
            this.notifyAll();
            return;
        }
        
        if (this.state == State.CANCELLED) {          
            for (CoordAction action : this) { 
                action.cancel();
            }
            this.notifyAll();
            return;
        }
        
        if (this.state == State.PROCESSING) {
            // Make sure that the CoordAction is processed only once
            return;
        }
              
        // Check if there is anything to process
        if (this.isFullyCompleted()) {
            // Nothing has to be processed. Signal any thread that might be waiting
            //LOG.debug("nothing to process " + this.getName() + " " + this.state);
            this.notifyAll();
            return;
        }

        if (this.state == State.UNPROCESSED) {
            // a job must be created for this action
            String nameTag = CoordJob.createId();
            SimpleTrigger jobTrigger = new SimpleTrigger(this.getName() + "-" + nameTag, null);
            JobDetail     jobDetail  = new JobDetail(this.getName() + "-" + nameTag, null, CoordJob.class);
            jobDetail.setVolatility(false);
            CoordJob.setRequestId(jobDetail, this);
            
            try {
                Coordinator.getInstance().getScheduler().scheduleJob(jobDetail, jobTrigger);
            } catch (Exception ex) {
                LOG.error(netLogger.error(event, ErrSev.MAJOR, ex.getClass().getName() + ": " + ex.getMessage() +
                        " scheduling " + this.name));
                ErrorReport errRep = new ErrorReport(ErrorCodes.SCHED_ERROR,
                                                     ex.getMessage(),
                                                     ErrorReport.SYSTEM,
                                                     this.getCoordRequest() != null ? this.getCoordRequest().getGRI() : "NoGRI",
                                                     this.getCoordRequest() != null ? this.getCoordRequest().getTransactionId() : "NoTransactionID",
                                                     System.currentTimeMillis()/1000L,
                                                     ModuleName.COORD,
                                                     PathTools.getLocalDomainId());
                OSCARSServiceException oEx = new OSCARSServiceException(errRep);
                this.fail (oEx);
            }
        }

        if ((this.state == State.PROCESSED) || (this.getCoordRequest() == null)) {
            // This action is processed, therefore the processing must be propagated to the children.
            // Note that if the action is the root action of the graph, it needs to be processed even when
            // it is not processed completely (the root action somewhat breaks the execution model that children
            // action can be processed only when the parent is fully processed. The root action is different than
            // all other actions because it does not have a CoordRequest associated.
            for (CoordAction action : this) { 
                action.process();
            }        
        }

    }
    
    /**
     * Starts the processing or re-processing of the graph of CoordAction for which this CoordAction is the root.
     * This method is synchronous: it returns when the CoordAction and its children CoordActions in the graph are
     * completed, canceled, failed or when the provided timeout is reached.
     * 
     * @param timeout is the number of millisecond this method should wait until returning.
     * @throws InterruptedException when the timeout is reached before the CoordAction is completed.
     */
    public synchronized void processAndWait (long timeout) throws InterruptedException {
        this.process();
        this.wait(timeout);
    }

    /**
     * Iterates through the actionSet hashmap and removes any action that is not referenced from
     * any place else. Called from PCERuntime.gc and the CoordActionCleanerJob
     */
    public static void gc () {
        ArrayList<String> toBeRemoved = new ArrayList<String>();

        Set <Map.Entry<String,WeakReference<CoordAction>>> actionsSet = CoordAction.actions.entrySet();
        for (Map.Entry<String,WeakReference<CoordAction>> entry : actionsSet) {
            WeakReference<CoordAction> ref = (WeakReference<CoordAction>) entry.getValue();
            if ((ref == null) || (ref.get() == null)) {
                // free entry
                toBeRemoved.add(entry.getKey());
            }
        } 

        for (String id : toBeRemoved) {
            CoordAction.actions.remove(id);
        }
        Runtime.getRuntime().gc();
    }
} 
