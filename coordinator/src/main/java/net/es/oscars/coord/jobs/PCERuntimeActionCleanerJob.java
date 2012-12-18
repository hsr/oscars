package net.es.oscars.coord.jobs;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


import net.es.oscars.coord.runtimepce.PCERuntimeAction;

public class PCERuntimeActionCleanerJob implements Job {
    
    public PCERuntimeActionCleanerJob () {
        
    }
    
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        
        // Call the event cleaning operation
        PCERuntimeAction.gc();
    }

}
