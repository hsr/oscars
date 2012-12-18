package net.es.oscars.resourceManager.scheduler;

import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.notify.NotifySender;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.common.RMCore;
import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.resourceManager.common.ResourceManager;
import net.es.oscars.resourceManager.scheduler.RMReservationScheduler;
import net.es.oscars.resourceManager.scheduler.ReservationHandler;

/**
 * tells the Reservation manager that the reservation has expired
 * 
 * @author mrt
 */
public class ReservationFinishJob extends ReservationJob {
    private static final Logger LOG = Logger.getLogger(ReservationFinishJob.class.getName());

    /**
     *  Called by the quartz scheduler when a reservationFinish job is triggered
     *  Calls resourceManger to update the status to FINISHED
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        String event = "reservationFinishJob";
        ResDetails resDetails = ReservationJob.getResDetails(context);
        MessagePropertiesType msgProps = ReservationJob.getMessageProperties(context);
        OSCARSNetLogger netLogger =  OSCARSNetLogger.getTlogger();
        String transId = PathTools.getLocalDomainId() + "-RM-" + UUID.randomUUID().toString();
        netLogger.init(ModuleName.RMSCHED, transId);
 
        if (resDetails == null) {
            LOG.warn(netLogger.error(event, ErrSev.MINOR,
                                    "reservationFinishJob found job with no resDetails"));
            return;
        }
        netLogger.setGRI(resDetails.getGlobalReservationId());
        LOG.debug(netLogger.start(event));
        
        // if the RMScheduler gets run as a separate service, the following will need to be
        // replaced by an UpdateStatus message sent to the ResourceManager.
        RMCore core = RMCore.getInstance();
        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            mgr.updateStatus(resDetails.getGlobalReservationId(),
                    StateEngineValues.FINISHED, false, null);
        } catch (Exception ex) {
            // might be an OSCARSServiceException if notify failed
            LOG.error(netLogger.error(event, ErrSev.MINOR,
                                      "caught RMException" + ex.getMessage()));
        } finally {
            RMReservationScheduler rmSched = RMReservationScheduler.getInstance();
            rmSched.removePendingReservationHandler(resDetails.getGlobalReservationId(),
                                ReservationHandler.FINISH);
        }
        session.getTransaction().commit();
        LOG.debug(netLogger.end(event));
    }   
    
}
 