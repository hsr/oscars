package net.es.oscars.utils.task.sched;

import net.es.oscars.utils.task.TaskException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

public class Schedule {
    private Schedule() {}
    private Scheduler scheduler;
    private static Schedule instance;
    public static Schedule getInstance() {
        if (instance == null) {
            instance = new Schedule();
        }
        return instance;
    }



    public void start() throws TaskException {
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            this.scheduler = schedFact.getScheduler();
            // look at the queue every second
            SimpleTrigger inspectorTrigger = new SimpleTrigger("TaskRunner", "TaskRunner");
            inspectorTrigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
            inspectorTrigger.setRepeatInterval(100);
            JobDetail inspectorJobDetail = new JobDetail("TaskRunner", "TaskRunner", TaskRunner.class);
            this.scheduler.scheduleJob(inspectorJobDetail, inspectorTrigger);
            scheduler.start();
        } catch (SchedulerException ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
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


}
