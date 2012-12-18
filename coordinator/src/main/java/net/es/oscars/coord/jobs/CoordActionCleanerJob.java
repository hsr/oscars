package net.es.oscars.coord.jobs;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;

import net.es.oscars.coord.actions.CoordAction;

public class CoordActionCleanerJob implements Job {
    
    public CoordActionCleanerJob () {
        
    }
    
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        
        // Call the event garbage collection
        CoordAction.gc();
    }

}
