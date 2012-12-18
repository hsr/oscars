package net.es.oscars.coord.jobs;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;

import net.es.oscars.coord.actions.CoordAction;

public class CoordJob implements Job {

    private static String COORDACTION_ID = "CoordActionID";
    // A Long object is used so it can be synchronized (a long primitive type can't be synchronized)
    private static Long idCounter = new Long(0L); 

    
    public CoordJob() {
        
    }
    
    static public String getActionId (JobExecutionContext context) {
        if (context == null) {
            // No context, no ID
            return null;
        }
        JobDataMap map = context.getMergedJobDataMap();
        if (map == null) {
            // No map, no ID
            return null;
        }
        String id = map.getString(CoordJob.COORDACTION_ID);    
        return id;
    }
    
    @SuppressWarnings("unchecked")
    static public void setRequestId (JobDetail jobDetail, CoordAction action) {
        if (jobDetail == null) {
            Thread.dumpStack();
            throw new RuntimeException ("no JobDetail was provided");
        }
        JobDataMap map = new JobDataMap();
        map.put(CoordJob.COORDACTION_ID, action.getId());
        jobDetail.setJobDataMap(map);
    }
    
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Retrieve the action object this job must execute.
        String coordActionId = CoordJob.getActionId(context);
        if (coordActionId == null) {
            // No action is recorded. This means the action was canceled.
            throw new JobExecutionException ("no action to execute for this job");
        }
        CoordAction action = CoordAction.getCoordAction (coordActionId);
        if (action == null) {
            // No action object is recorded. The action was cancelled
            throw new JobExecutionException ("no action object to execute for this job");
        }
        if (action.getState() == CoordAction.State.UNPROCESSED) {
            // This action needs to be executed. Set state to PROCESSING
            action.setState(CoordAction.State.PROCESSING);
            action.execute();
        }
    }  
    
    public static String createId() {
        long id=0L;
        synchronized (CoordJob.idCounter) {
            id = ++CoordJob.idCounter;
        }
        return Long.toString(id);
    }
    
    
} 
