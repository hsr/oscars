package net.es.oscars.coord.jobs;

import java.util.HashSet;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.topology.PathTools;



public class RequestTimerJob implements Job {

    private static String COORDREQUEST_ID = "CoordRequestID";
    private static final Logger LOG = Logger.getLogger(RequestTimerJob.class.getName());
    // A Long object is used so it can be synchronized

    public RequestTimerJob() {
        
    }
    
    static public String getRequestId (JobExecutionContext context) {
        if (context == null) {
            // No context, no ID
            return null;
        }
        JobDataMap map = context.getMergedJobDataMap();
        if (map == null) {
            // No map, no ID
            return null;
        }
        String id = map.getString(RequestTimerJob.COORDREQUEST_ID);    
        return id;
    }
    
    @SuppressWarnings("unchecked")
    static public void setRequestId(JobDetail jobDetail, String key) {
        if (jobDetail == null) {
            Thread.dumpStack();
            throw new RuntimeException ("no JobDetail was provided");
        }
        JobDataMap map = new JobDataMap();
        map.put(RequestTimerJob.COORDREQUEST_ID, key);
        jobDetail.setJobDataMap(map);
    }
    
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.COORD,"null");
        String event = "RequestTimerJob";
        
        // Retrieve the CoordRequest object this job must execute.
        String requestId = RequestTimerJob.getRequestId(context);
        LOG.debug(netLogger.start(event,"requestId is " + requestId));
        if (requestId == null) {
            // No action is recorded. This means the action was canceled.
            LOG.error (netLogger.error(event, ErrSev.MAJOR, "watchdog - did not find id for request"));
            throw new JobExecutionException ("no request to execute for this job");
        }
        CoordRequest request = CoordRequest.getCoordRequestById (requestId);
        if (request == null) {
            // No action object is recorded. The action was canceled
            LOG.debug (netLogger.end(event,"watchdog - did not find CoordRequest for id " + requestId));
            return;
            //throw new JobExecutionException ("no action object to execute for this job");
        }
        if (request.getCoordRequest() != null ) {
            netLogger.init(ModuleName.COORD,request.getCoordRequest().getTransactionId());
        }
        //LOG.debug(netLogger.getMsg(event,"checking on " + request.getName()));

        if ( ! request.isRequestComplete()) {
            // This request has not yet been completed. Assume an error/problem has occurred. Fail the request
	        LOG.debug(netLogger.getMsg(event,request.getName() + " not completed, calling fail"));
            String resStatus = "";
            if (request.getGRI() != null) {
                resStatus = request.getResvStatus();
            }
            ErrorReport errRep = new ErrorReport(ErrorCodes.REQUEST_TIMEOUT,
                                                 "Watchdog Terminated " + request.getName() + " in status " + resStatus,
                                                  ErrorReport.SYSTEM,
                                                  request.getGRI(),
                                                  request.getTransactionId(),
                                                  request.getReceivedTime(),
                                                  ModuleName.COORD,
                                                  PathTools.getLocalDomainId());
            request.fail(new OSCARSServiceException (errRep));
        }

        // Force the CoordRequest to be removed from the pending request list (allowing this request to be GC'ed)
        PCERuntimeAction.releaseMutex(request.getGRI());
        CoordRequest.forget(requestId);
    }
 
} 
