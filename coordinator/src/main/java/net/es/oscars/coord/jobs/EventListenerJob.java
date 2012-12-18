package net.es.oscars.coord.jobs;


import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;

import net.es.oscars.coord.events.CoordEvent;
import net.es.oscars.coord.events.CoordEventListener;

public class EventListenerJob implements Job {
    
    private static String EVENT_LISTENER = "CoordEventListener";
    private static String EVENT          = "CoordEvent";
    
    public EventListenerJob () {
        // Empty constructor to make Quartz happy
    }
 
    static private CoordEventStub getEventListenerStub (JobExecutionContext context) {
        if (context == null) {
            // No context, no ID
            return null;
        }
        JobDataMap map = context.getMergedJobDataMap();
        if (map == null) {
            // No map, no ID
            return null;
        }
        CoordEventListener listener = (CoordEventListener) map.get (EventListenerJob.EVENT_LISTENER); 
        CoordEvent event  = (CoordEvent) map.get(EventListenerJob.EVENT);
        if ((event == null) || (listener == null)) {
            return null;
        }
        return new CoordEventStub(event,listener);
    }

    static public void setEventListener (JobDetail jobDetail, CoordEvent event, CoordEventListener eventListener) {
        if (jobDetail == null) {
            Thread.dumpStack();
            throw new RuntimeException ("no JobDetail was provided");
        }
        JobDataMap map = new JobDataMap();
        map.put(EventListenerJob.EVENT_LISTENER, eventListener);
        map.put(EventListenerJob.EVENT, event);
        jobDetail.setJobDataMap(map);
    }
        
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        // Retrieve the event listener object this job must execute.
        CoordEventStub listenerStub = EventListenerJob.getEventListenerStub(context);
        if (listenerStub == null) {
            // No event listener is recorded. This should not happen.
            throw new JobExecutionException ("no listener");
        }
        listenerStub.listener.handleEvent(listenerStub.event);
    }

}
