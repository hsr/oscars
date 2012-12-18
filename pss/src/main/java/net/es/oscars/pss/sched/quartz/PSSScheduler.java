package net.es.oscars.pss.sched.quartz;

import net.es.oscars.pss.beans.PSSException;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

public class PSSScheduler {
    private Scheduler scheduler;

    private static PSSScheduler instance;

    @SuppressWarnings("rawtypes")
    private Class workflowInspectorJobClass = null;

    private PSSScheduler() {
    }

    public static PSSScheduler getInstance() {
        if (instance == null) {
            instance = new PSSScheduler();
        }
        return instance;
    }


    public void start() throws PSSException {
        if (workflowInspectorJobClass == null) {
            throw new PSSException("No workflow inspector class set!");
        }
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            this.scheduler = schedFact.getScheduler();

            // look at the queue every second
            SimpleTrigger inspectorTrigger = new SimpleTrigger("WFInspectorTrigger", "WFInspector");
            inspectorTrigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
            inspectorTrigger.setRepeatInterval(1000);
            JobDetail inspectorJobDetail = new JobDetail("WFInspector", "WFInspector", workflowInspectorJobClass);
            this.scheduler.scheduleJob(inspectorJobDetail, inspectorTrigger);

            scheduler.start();

        } catch (SchedulerException ex) {
            ex.printStackTrace();
            throw new PSSException(ex.getMessage());
        }

    }


    public void stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
    
    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    @SuppressWarnings("rawtypes")
    public void setWorkflowInspector(Class workflowInspector) {
        this.workflowInspectorJobClass = workflowInspector;
    }

    @SuppressWarnings("rawtypes")
    public Class getWorkflowInspector() {
        return workflowInspectorJobClass;
    }

}
