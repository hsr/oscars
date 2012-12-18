package net.es.oscars.resourceManager.scheduler;

import java.util.UUID;

import net.es.oscars.resourceManager.common.RMCore;
import net.es.oscars.resourceManager.common.ResourceManager;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.TeardownPathContent;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
/**
 * Job that is called by the Quartz scheduler to send a message to the coordinator
 * to teardown a path.
 * 
 * @author lomax
 */
public class PathTeardownJob extends ReservationJob {
    private static final Logger LOG = Logger.getLogger(PathTeardownJob.class.getName());

    /**
     *  Called by the quartz scheduler when a teardown job is triggered
     *  Sends message to Coordinator to start a pathTeardown
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        String event = "pathTeardownJob";
        ResDetails resDetails = ReservationJob.getResDetails(context);
        MessagePropertiesType msgProps = ReservationJob.getMessageProperties(context);
        OSCARSNetLogger netLogger =  OSCARSNetLogger.getTlogger();
        String transId = PathTools.getLocalDomainId() + "-RM-" + UUID.randomUUID().toString();
        netLogger.init(ModuleName.RMSCHED, transId);
        if (msgProps == null) {
            LOG.warn(netLogger.error(event, ErrSev.MINOR, 
                                          "pathTeardownJob found job with no msgProps"));
            return;
        }
        msgProps.setGlobalTransactionId(transId);
        if (resDetails == null) {
            LOG.warn(netLogger.error(event,ErrSev.MINOR,
                                          "pathTeardownJob found job with no resDetails"));
            return;
        }
        netLogger.setGRI(resDetails.getGlobalReservationId());
        LOG.debug(netLogger.start(event));

        TeardownPathContent tdContent = new TeardownPathContent();
        tdContent.setMessageProperties(msgProps);
        tdContent.setGlobalReservationId(resDetails.getGlobalReservationId());
        CoordClient coordClient = RMReservationScheduler.getInstance().getCoordClient();

        Object[] req = new Object[]{null, tdContent, resDetails};
        Object[] res;
        try {
            res = coordClient.invoke("teardownPath",req);
        } catch (OSCARSServiceException e) {
            LOG.error(netLogger.error(event,ErrSev.MINOR,
                                          "pathTeardownJob caught exception from Coordinator pathTeardown call" +
                                          e.getMessage()));
            throw new JobExecutionException (e);
        } finally {
            RMReservationScheduler rmSched = RMReservationScheduler.getInstance();
            rmSched.removePendingReservationHandler(resDetails.getGlobalReservationId(),
                                ReservationHandler.TEARDOWN);
        }
        if ((res == null) || (res[0] == null)) {
            throw new JobExecutionException ("No response from ResourceManager");
        }
        LOG.debug(netLogger.end(event));
    }
    
}
 