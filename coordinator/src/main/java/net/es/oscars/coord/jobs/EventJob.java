package net.es.oscars.coord.jobs;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.apache.log4j.Logger;

import net.es.oscars.coord.events.CoordEvent;

public class EventJob implements Job {
    
    public EventJob () {
        
    }
    
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        
        // Call the event cleaning operation
        CoordEvent.cleanupEvents();
    }

}
