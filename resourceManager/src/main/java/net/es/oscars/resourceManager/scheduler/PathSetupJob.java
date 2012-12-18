package net.es.oscars.resourceManager.scheduler;

import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.scheduler.RMReservationScheduler;
import net.es.oscars.resourceManager.scheduler.ReservationHandler;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;


public class PathSetupJob extends ReservationJob {
    private static final Logger LOG = Logger.getLogger(PathSetupJob.class.getName());

/**
 *  Called by the quartz schedule when a setup job is triggered
 *  Sends message to Coordinator to start a pathSetup
 */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String event = "pathSetupJob";
        MessagePropertiesType msgProps = ReservationJob.getMessageProperties(context);
        OSCARSNetLogger netLogger =  OSCARSNetLogger.getTlogger();
        String transId = PathTools.getLocalDomainId() + "-RM-" + UUID.randomUUID().toString();
        netLogger.init(ModuleName.RMSCHED, transId);
        if (msgProps == null) {
            LOG.warn(netLogger.error(event, ErrSev.MINOR, 
                                     "pathSetupJob found job with no msgProps"));
            return;
        }
        msgProps.setGlobalTransactionId(transId);
        ResDetails resDetails = ReservationJob.getResDetails(context);
        if (resDetails == null) {
            LOG.warn(netLogger.error(event, ErrSev.MINOR, 
                                     "pathSetupJob found job with no resDetails"));
            return;
        }
        netLogger.setGRI(resDetails.getGlobalReservationId());
        LOG.debug(netLogger.start(event));
        

        CreatePathContent cpContent = new CreatePathContent();
        cpContent.setMessageProperties(msgProps);
        cpContent.setGlobalReservationId(resDetails.getGlobalReservationId());
        CoordClient coordClient = RMReservationScheduler.getInstance().getCoordClient();

        Object[] req = new Object[]{null, cpContent, resDetails};
        Object[] res;
        try {
            res = coordClient.invoke("createPath",req);
        } catch (OSCARSServiceException e) {
            LOG.error(netLogger.error(event,ErrSev.MINOR,
                                        "pathSetupJob caught exception from Coordinator pathSetup call" +
                                        e.getMessage()));
            throw new JobExecutionException (e);
        } finally {
            RMReservationScheduler rmSched = RMReservationScheduler.getInstance();
            rmSched.removePendingReservationHandler(resDetails.getGlobalReservationId(),
                                ReservationHandler.PATHSETUP);
        }
        if ((res == null) || (res[0] == null)) {
            throw new JobExecutionException ("No response from Coordinator");
        }
        LOG.debug(netLogger.end(event));
    }
    
}
 