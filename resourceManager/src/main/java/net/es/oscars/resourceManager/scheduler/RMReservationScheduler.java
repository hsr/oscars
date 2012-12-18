package net.es.oscars.resourceManager.scheduler;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;


import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.common.RMUtils;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.notify.NotifySender;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;


/**
 * Adds and removes reservation pathSetup, teardowns and finish actions to the scheduling system.
 * The reservation is encapsulated within a ResDetails.
 * The ResourceManager is responsible for re-scheduling a reservation if its trigger times are changed
 * and removing any scheduled actions if it is canceled.
 */

public class RMReservationScheduler implements ReservationScheduler {
    
    private static RMReservationScheduler instance;
    private Scheduler quartzScheduler;
    private CoordClient coordClient;
    private int scanInterval;
    private int lookAhead;
    private ArrayList <ReservationHandler> pendingReservations = new ArrayList<ReservationHandler>();
    public static String schedLock = new String("unlocked");
    private static Logger LOG = null;
    private OSCARSNetLogger netLogger = null;
    private boolean processing = false;


    /**
     * Returns the singleton RMReservationScheduler class. Instantiates it, if necessary.
     * @param scanInterval Interval at which the resourceManager scans the reservations table to
     *           find reservations that  need to have actions scheduled
     * @param lookAhead interval within which a pending setup or teardown will be scheduled
     * @return
     */
    public static RMReservationScheduler getInstance(int scanInterval, int lookAhead) {
        if (instance == null) {
            instance = new RMReservationScheduler(scanInterval, lookAhead);
        }
        return instance;

    }
 
    public static RMReservationScheduler getInstance() {
        if (instance == null) {
            throw new RuntimeException ("RMReservationScheduler has not been instantied yet.");
        }
        return instance;

    }

    private RMReservationScheduler(int scanInterval, int lookAhead) {
        SchedulerFactory schedFact = new StdSchedulerFactory();
        try {
            String event = "InitReservationScheduler";
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_RM);
            System.out.println("cc context is " + cc.getContext());
            LOG = Logger.getLogger(RMReservationScheduler.class.getName());
            this.netLogger = OSCARSNetLogger.getTlogger();
            this.scanInterval =scanInterval;
            this.lookAhead = lookAhead;
            // Start Quartz
            this.quartzScheduler = schedFact.getScheduler();
            this.quartzScheduler.start();
            // Instantiate the Coordinator client
            this.initCoordClient();
            this.initNotifySender();
            // Create the background database query thread
            this.initBackgroundThread();
            
        } catch (SchedulerException e) {
            throw new RuntimeException ("Cannot start Quartz Scheduler.");
        } catch (OSCARSServiceException e) {
            System.out.println("RMReservationScheduler caught " + e.getMessage());
            throw new RuntimeException ("Cannot create Coordinator client.");
        }
    }
    
    /**
     * 
     */
    public void schedule (ResDetails resDetails) {
        // not used
    }
    /**
     * Schedules a PathSetup job
     * Called from ScanReservations.scan which holds the RMReservationScheduler.schedLock.
     * The ResourceManager is responsible for re-scheduling a reservation if its content is changed.
     * 
     * @param resDetails
     */
    public void scheduleSetup (ResDetails resDetails) {
        String event = "RMReservationScheduler.setup";
        this.netLogger = OSCARSNetLogger.getTlogger();
        this.netLogger.setGRI(resDetails.getGlobalReservationId());
        long now = (System.currentTimeMillis() / 1000);
        LOG.debug(this.netLogger.start(event));
        if (resDetails == null) {
            // Sanity check
            return;
        }
        // Check if we need to schedule a path setup
        if ((resDetails.getReservedConstraint().getStartTime() <= (now + this.lookAhead)) &&
            (resDetails.getStatus() != StateEngineValues.ACTIVE)) {
            this.setup (resDetails);
        } else {
            LOG.warn(this.netLogger.error(event, ErrSev.MINOR,
                                         "no job was scheduled" ));
        }
    }
    
    /**
     * Schedules a PathTeardown job
     * Called from ScanReservations.scan and
     * ResourceManager store and updateStatus when status changes to ACTIVE
     * The ResourceManager is responsible for re-scheduling a reservation if its content is changed.
     *
     * @param resDetails
     */
    public void scheduleTeardown (ResDetails resDetails) {

        String event = "RMReservationScheduler.teardown";
        this.netLogger = OSCARSNetLogger.getTlogger();
        this.netLogger.setGRI(resDetails.getGlobalReservationId());
        long now = (System.currentTimeMillis() / 1000);
        LOG.debug(this.netLogger.start(event));
        if (resDetails == null) {
            // Sanity check
            return;
        }
        // Check if we need to schedule a path teardown
        if (resDetails.getReservedConstraint().getEndTime() <= (now + this.lookAhead)) {
            this.teardown (resDetails);
        }
    }

    /**
     * Schedules a reservation finish job
     * Called from ScanReservations.scan which holds the RMReservationScheduler.schedLock.
     * The ResourceManager is responsible for re-scheduling a reservation if its content is changed.
     *
     * @param resDetails
     */
    public void scheduleFinish (ResDetails resDetails) {

        String event = "RMReservationScheduler.finish";
        this.netLogger = OSCARSNetLogger.getTlogger();
        this.netLogger.setGRI(resDetails.getGlobalReservationId());
        long now = (System.currentTimeMillis() / 1000);
        LOG.debug(this.netLogger.start(event));
        if (resDetails == null) {
            // Sanity check
            return;
        }
        // Check if we need to schedule a reservation finish job
        // use UserRequestConstraint here since there might not be a reservedConstraint
        if (resDetails.getUserRequestConstraint().getEndTime() <= (now + this.lookAhead)) {
            this.finish (resDetails);
        } else {
            LOG.warn(this.netLogger.error(event, ErrSev.MINOR, "no job was scheduled" ));
        }
    }
    /**
     * Remove any scheduled pathset or teardown for this reservation.
     * Called when a reservation is canceled and when a signal-xml reservation
     * is set to status RESERVED
     * @param resDetails the details of the reservation
     */
    public void forget (ResDetails resDetails) {
        // Sanity check
        String gri = resDetails.getGlobalReservationId();
        if (gri == null) {
            return; 
        }
        String event = "RMReservationScheduler.forget";
        this.netLogger = OSCARSNetLogger.getTlogger();
        this.netLogger.setGRI(gri);
        LOG.debug(this.netLogger.start(event));

        synchronized (this.pendingReservations) {
            // Is there any PATH SETUP pending job for that reservation ?
            ReservationHandler pending = this.getPendingReservationHandler(gri, ReservationHandler.PATHSETUP);
            if (pending != null) {
                LOG.debug(this.netLogger.getMsg(event,"canceling path setup" ));
                this.cancel (pending);
            }
            // Is there any TEAR DOWN pending for that reservation
            pending = this.getPendingReservationHandler(gri, ReservationHandler.TEARDOWN);
            if (pending != null) {
                // Coordinator.cancel will teardown an active reservation
                LOG.debug(this.netLogger.getMsg(event,"canceling path teardown" ));
                this.cancel (pending);
            }
        }
    }

    /**
     * Looks for a pendingReservationHandler and if one is found removes it from
     * the pendingReservations list.
     * @throws OSCARSServiceException
     */
    public void removePendingReservationHandler(String gri, String handlerType){
        synchronized (this.pendingReservations) {
            ReservationHandler handler = getPendingReservationHandler(gri, handlerType);
            if (handler != null){
                this.pendingReservations.remove(handler);
            }
        }
    }
 
    private void initCoordClient() throws OSCARSServiceException {
        String event = "initCoordClient";
        // Find the Coordinator wsdl file.
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_RM);
        HashMap<String, HashMap<String, HashMap<String, String>>> manifest = cc.getManifest();
        // assumes cooordService is running in same context as resourceManaager
        HashMap<String,String> coordServiceMap = manifest.get(ServiceNames.SVC_COORD).get(cc.getContext());
        if (coordServiceMap == null ) {
            LOG.debug(this.netLogger.error(event,ErrSev.MINOR,"no map found for " + cc.getContext()));
        }
        try {
            //get a URL to CoordinatorService wsdl
            String coordWsdlFilename = cc.getFilePath(ServiceNames.SVC_COORD,cc.getContext(),
                    ConfigDefaults.WSDL);
            LOG.debug(this.netLogger.getMsg(event,coordWsdlFilename));
            URL coordWsdl = new URL ("file:" + coordWsdlFilename);
            
            // find the Coordinator URL from its config.yaml file
            String coordYamlFilename = cc.getFilePath(ServiceNames.SVC_COORD,cc.getContext(),
                    ConfigDefaults.CONFIG);
            
            HashMap<String,Object> coordYaml = (HashMap<String,Object>)ConfigHelper.getConfiguration(coordYamlFilename);
            HashMap<String,String> soap = (HashMap<String,String>) coordYaml.get("soap");
            URL coordHost = new URL (soap.get("publishTo"));
            LOG.debug(this.netLogger.getMsg(event,"coordHost is " + coordHost.toString() +
                                            " coordWsdl is " + coordWsdl.toString()));
            this.coordClient = CoordClient.getClient(coordHost,coordWsdl);
        } catch (MalformedURLException e) {
            LOG.error(this.netLogger.error(event, ErrSev.MAJOR,
                                           "caught MalformedURLException " + e.getMessage()));
            throw new OSCARSServiceException (e);
        } catch (ConfigException e) {
            LOG.error(this.netLogger.error(event, ErrSev.MAJOR,
                    "caught ConfigException " + e.getMessage()));
            throw new OSCARSServiceException (e);
        }
    }

private void initNotifySender() throws OSCARSServiceException {
        String event = "initNotifyClient";
        // Find the Coordinator wsdl file.
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_RM);
        HashMap<String, HashMap<String, HashMap<String, String>>> manifest = cc.getManifest();
        try {
            //get a URL to NotificationBridge wsdl
            String notifyWsdlFilename = cc.getFilePath(ServiceNames.SVC_NOTIFY,cc.getContext(),
                    ConfigDefaults.WSDL);
            URL notifyWsdl = new URL ("file:" + notifyWsdlFilename);

            // find the NotificationBridge URL from its config.yaml file
            String notifyYamlFilename = cc.getFilePath(ServiceNames.SVC_NOTIFY,cc.getContext(),
                    ConfigDefaults.CONFIG);

            HashMap<String,Object> notifyYaml = (HashMap<String,Object>)ConfigHelper.getConfiguration(notifyYamlFilename);
            HashMap<String,String> soap = (HashMap<String,String>) notifyYaml.get("soap");
            URL notifyHost = new URL (soap.get("publishTo"));
            LOG.debug(this.netLogger.getMsg(event,"notifyHost is " + notifyHost.toString() +
                                            " notifyWsdl is " + notifyWsdl.toString()));
             NotifySender.init(notifyHost, notifyWsdl);
        } catch (MalformedURLException e) {
            LOG.error(this.netLogger.error(event, ErrSev.MAJOR,
                                           "caught MalformedURLException " + e.getMessage()));
            throw new OSCARSServiceException (e);
        } catch (ConfigException e) {
            LOG.error(this.netLogger.error(event, ErrSev.MAJOR,
                    "caught ConfigException " + e.getMessage()));
            throw new OSCARSServiceException (e);
        }
    }

    public CoordClient getCoordClient () {
        return this.coordClient;
    }
  
    private void initBackgroundThread() throws SchedulerException  {
        
        SimpleTrigger jobTrigger = new SimpleTrigger("Reservation Scheduler Background Thread",
                                                     null,
                                                     new Date(System.currentTimeMillis() + (this.scanInterval * 1000)),
                                                     null,
                                                     SimpleTrigger.REPEAT_INDEFINITELY,
                                                     (long) this.scanInterval * 1000L);
        
        JobDetail     jobDetail  = new JobDetail("Reservation Scheduler Background Thread", null,SchedulerJob.class);
        this.quartzScheduler.scheduleJob(jobDetail, jobTrigger);

    }

    public synchronized void scheduleNow () {
        if (this.processing) {
            // Already currently scanning the database. Ignore.
            return;
        }
        this.processing = true;

        try {
            // this.quartzScheduler.rescheduleJob("Reservation Scheduler Background Thread",null,jobTrigger);
            this.quartzScheduler.triggerJob("Reservation Scheduler Background Thread",null);
        } catch (SchedulerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public synchronized void processed() {
        this.processing = false;
    }
    /**
     * schedules a quartz job to setup a reservation path
     * Checks to see if a reservation has already been scheduled, and if so whether it 
     * needs to be canceled and rescheduled.
     * @param resDetails
     */
    private void setup (ResDetails resDetails) {
       
        String event = "doSetup";
        LOG.debug(this.netLogger.start(event));
        long now = (System.currentTimeMillis() / 1000);
        SimpleTrigger jobTrigger = null;
        
        synchronized(this.pendingReservations) {
            // Check if there is any SETUP already scheduled for this reservation.
            ReservationHandler pending = this.getPendingReservationHandler(resDetails.getGlobalReservationId(),
                                                                           ReservationHandler.PATHSETUP);

            if (resDetails.getReservedConstraint().getStartTime() <= now ) {
                // Schedule for immediate setup if not already scheduled.
                if ((pending != null) && (pending.getExecutionTime()/1000 > now )) {
                    // Reservation start time has been advanced. Cancel pending operation and re-schedule
                    LOG.debug(this.netLogger.getMsg(event,"Rescheduling setup: endTime: " + 
                                                    resDetails.getReservedConstraint().getEndTime() +
                                                    " now:  " + now +
                                                    " executionTime: " + pending.getExecutionTime()/1000 ));
                    this.cancel (pending);
                    pending = null;
                }
                // Create a new trigger for immediate execution
                if (pending == null){
                    jobTrigger = new SimpleTrigger(resDetails.getGlobalReservationId() + 
                                                   "-" + ReservationHandler.PATHSETUP,
                                                   null);
                }
            } else { // start time is > now
                if ((pending != null) && (pending.getExecutionTime()/1000) != resDetails.getReservedConstraint().getStartTime()) {
                    // Reservation start time has been changed. Cancel pending operation and re-schedule
                    LOG.debug(this.netLogger.getMsg(event,"Rescheduling setup: endTime: " + 
                                                    resDetails.getReservedConstraint().getEndTime() +
                                                    " executionTime: " + pending.getExecutionTime()/1000 ));
                    this.cancel (pending);
                    pending = null;
                }
                // Schedule setup at the provided date if not already scheduled.
                if (pending == null){
                    Date when = new Date (resDetails.getReservedConstraint().getStartTime() * 1000);
                    jobTrigger = new SimpleTrigger(resDetails.getGlobalReservationId() +
                                                   "-" + ReservationHandler.PATHSETUP,
                                                   null,
                                                   when);
               }
            }
            // need to schedule a job
            if (jobTrigger != null) {
                JobDetail jobDetail  = new JobDetail(resDetails.getGlobalReservationId() +
                                                     "-" + ReservationHandler.PATHSETUP,
                                                     null,
                                                     PathSetupJob.class);
                // Add the ResDetails into the JobDetail's context
                MessagePropertiesType msgProps = RMUtils.getMsgProps(resDetails);
                ReservationJob.setResDetails(jobDetail, resDetails,msgProps);
                try {
                    LOG.debug(this.netLogger.getMsg(event,"Calling quartz.scheduleJob"));
                    this.quartzScheduler.scheduleJob(jobDetail, jobTrigger);
                    ReservationHandler rh = new ReservationHandler(resDetails, 
                                                                   ReservationHandler.PATHSETUP,
                                                                   jobTrigger.getNextFireTime().getTime());
                    pendingReservations.add(rh);

                } catch (SchedulerException e) {
                    LOG.warn(this.netLogger.error(event, ErrSev.MINOR,
                            "failed to schedule setup job, exception was: "+ e.getMessage()));
                }
            }
            LOG.debug(this.netLogger.end(event));
        }
    }
    /**
     * schedules a quartz job to teardown a path
     * @param resDetails
     */
    private void teardown (ResDetails resDetails) {
        String event = "doTeardown";
        LOG.debug(this.netLogger.start(event ));
        
        long now = (System.currentTimeMillis() / 1000);
        SimpleTrigger jobTrigger = null;
        synchronized(this.pendingReservations){
            // Check if there is any TEARDOWN already scheduled for this reservation. 
            ReservationHandler pending = this.getPendingReservationHandler(resDetails.getGlobalReservationId(),
                    ReservationHandler.TEARDOWN);
            if (pending != null){
                LOG.debug(this.netLogger.getMsg(event,"found pending TEARDOWN"));
            }
            if (resDetails.getReservedConstraint().getEndTime() <= now) {
                // Schedule for immediate teardown if not already scheduled.
                if ((pending != null) && (pending.getExecutionTime()/1000 >  now )) {
                    // Reservation end time has been advanced. Cancel pending operation and re-schedule
                    LOG.debug(this.netLogger.getMsg(event,"Rescheduling teardown: endTime: " + 
                                                    resDetails.getReservedConstraint().getEndTime() +
                                                    " now:  " + now  +
                                                    " executionTime: " + pending.getExecutionTime()/1000 ));
                    this.cancel (pending);
                    pending = null;
                }
                if (pending == null){
                // Create a new trigger
                    jobTrigger = new SimpleTrigger(resDetails.getGlobalReservationId() + 
                                                   "-" + ReservationHandler.TEARDOWN,
                                                   null);
                }
            } else {
                if ((pending != null) && (pending.getExecutionTime()/1000 != resDetails.getReservedConstraint().getEndTime())) {
                    // Reservation end time has been changed. Cancel pending operation and re-schedule
                    LOG.debug(this.netLogger.getMsg(event, "Rescheduling teardown: endTime: endTime: " + 
                                                    resDetails.getReservedConstraint().getEndTime() +
                                                    " executionTime: " + pending.getExecutionTime()/1000 ));
                    this.cancel (pending);
                    pending = null;
                }
                if (pending == null) {
                // Schedule teardown at the provided date if not already scheduled.
                    Date when = new Date (resDetails.getReservedConstraint().getEndTime() * 1000);
                    jobTrigger = new SimpleTrigger(resDetails.getGlobalReservationId() + 
                                                   "-" + ReservationHandler.TEARDOWN,
                                                   null,
                                                   when);
                }
            }

            if (jobTrigger != null) {
                JobDetail     jobDetail  = new JobDetail(resDetails.getGlobalReservationId() + 
                        "-" + ReservationHandler.TEARDOWN,
                        null,
                        PathTeardownJob.class);
                // Add the ResDetails into the JobDetail's context
                MessagePropertiesType msgProps = RMUtils.getMsgProps(resDetails);
                ReservationJob.setResDetails(jobDetail, resDetails,msgProps);

                try {
                    LOG.debug(this.netLogger.getMsg(event, "scheduling path Teardown"));
                    this.quartzScheduler.scheduleJob(jobDetail, jobTrigger);
                    ReservationHandler rh = new ReservationHandler(resDetails, 
                                                                   ReservationHandler.TEARDOWN,
                                                                   jobTrigger.getNextFireTime().getTime());
                    pendingReservations.add(rh);
                } catch (SchedulerException e) {
                    LOG.warn(this.netLogger.error(event, ErrSev.MINOR,
                                                 "failed to schedule teardown job, exception was: "+ e.getMessage()));
                }
            }
            LOG.debug(this.netLogger.end(event));
        }
    }
    /**'
     * schedules a quartz job to  set reservation status to FINISHED
     * @param resDetails
     */
    private void finish (ResDetails resDetails) {
        String event = "doFinish";
        LOG.debug(this.netLogger.start(event));
        
        long now = (System.currentTimeMillis() / 1000);
        SimpleTrigger jobTrigger = null;
        synchronized(this.pendingReservations){
            // Check if there is a TEARDOWN already scheduled for this reservation. 
            ReservationHandler pending = this.getPendingReservationHandler(resDetails.getGlobalReservationId(),
                    ReservationHandler.TEARDOWN);
            if (pending != null) {
                return; // let the teardown complete. It will set status to FINISHED if appropriate
            }
            // check to see if a FINISH is already scheduled
            // use UserRequestConstraint here since there might not be a reservedConstraint
            pending = this.getPendingReservationHandler(resDetails.getGlobalReservationId(),
                    ReservationHandler.FINISH);
            if (resDetails.getUserRequestConstraint().getEndTime() <= now) {
                if ((pending != null) && (pending.getExecutionTime()/1000 >  now )) {
                    // Reservation end time has been advanced. Cancel pending operation and re-schedule
                    LOG.debug(this.netLogger.getMsg(event, "Rescheduling finish: endTime: " +
                                                    resDetails.getUserRequestConstraint().getEndTime() +
                                                    " now:  " + now +
                                                    " executionTime: " + pending.getExecutionTime()/1000 ));
                    this.cancel (pending);
                    pending = null;
                }
                if (pending == null){
                // Create a new trigger
                    jobTrigger = new SimpleTrigger(resDetails.getGlobalReservationId() + 
                                                   "-" + ReservationHandler.FINISH,
                                                   null);
                }
            } else {
                if ((pending != null) && (pending.getExecutionTime()/1000 != resDetails.getUserRequestConstraint().getEndTime())) {
                    // Reservation end time has been changed. Cancel pending operation and re-schedule
                    LOG.debug(this.netLogger.getMsg(event,"Rescheduling finish: endTime: " + 
                                                    resDetails.getUserRequestConstraint().getEndTime() +
                                                    " executionTime: " + pending.getExecutionTime()/1000 ));
                    this.cancel (pending);
                    pending = null;
                }
                if (pending == null) {
                // Schedule finish at the provided date if not already scheduled.
                    Date when = new Date (resDetails.getUserRequestConstraint().getEndTime() * 1000);
                    jobTrigger = new SimpleTrigger(resDetails.getGlobalReservationId() + 
                                                   "-" + ReservationHandler.FINISH,
                                                   null,
                                                   when);
                }
            }
            if (jobTrigger != null) {
                JobDetail     jobDetail  = new JobDetail(resDetails.getGlobalReservationId() + 
                                                         "-" + ReservationHandler.FINISH,
                                                         null,
                                                         ReservationFinishJob.class);
                // Add the ResDetails into the JobDetail's context
                MessagePropertiesType msgProps = RMUtils.getMsgProps(resDetails);
                ReservationJob.setResDetails(jobDetail, resDetails,msgProps);

                try {
                    LOG.debug(this.netLogger.getMsg(event,"scheduling reservation finish"));
                    this.quartzScheduler.scheduleJob(jobDetail, jobTrigger);
                    ReservationHandler rh = new ReservationHandler(resDetails, 
                                                                   ReservationHandler.FINISH,
                                                                   jobTrigger.getNextFireTime().getTime());
                    pendingReservations.add(rh);
                } catch (SchedulerException e) {
                    LOG.warn(this.netLogger.error(event,ErrSev.MINOR,
"                                                failed to schedule finish job, exception was: "+ e.getMessage()));
                }
            }
            LOG.debug(this.netLogger.end(event));
        }
    }

    
    /**
     * Remove a scheduled job from the quartz scheduler queue and the
     * pendingReservations list.
     * @param reservationHandler - Handler that was found in the pendingReservations list.
     */
    private void cancel (ReservationHandler reservationHandler) {
        String event = "doCancel";
        String jobName = reservationHandler.getResDetails().getGlobalReservationId() + "-" +
                         reservationHandler.getOperationType();
        try {
            this.quartzScheduler.deleteJob(jobName, null);
        } catch (SchedulerException e) {
            LOG.error(this.netLogger.error(event, ErrSev.MINOR,
                                          "failed to delete a quartz job, exception was: "+ e.getMessage()));
            e.printStackTrace();
        }
        synchronized (this.pendingReservations) {
            this.pendingReservations.remove(reservationHandler);
        }
    }
    /**
     * searches the pendingReservations list for a handler for the given gri and operation
     * @param gri GlobalReservationId of the reservation
     * @param operationType: ReservationHandler.PATHSETUP or ReservationHandler.TEARDOWN
     * @return Reservation if one was found, otherwise null
     */
    private ReservationHandler getPendingReservationHandler (String gri, String operationType) {
        
        synchronized (this.pendingReservations) {
            for (ReservationHandler reservationHandler : this.pendingReservations) {
                ResDetails resDetails = reservationHandler.getResDetails();
                if (resDetails == null) {
                    // No reservation
                    continue;
                }
                if ((resDetails.getGlobalReservationId().equals(gri)) &&
                    (reservationHandler.getOperationType().equals(operationType)) ){
                    // Return the reservationHandler
                    return reservationHandler;
                }
            }
        }
        return null;
    }
 
}
