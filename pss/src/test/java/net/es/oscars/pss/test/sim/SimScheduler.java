package net.es.oscars.pss.test.sim;


import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class SimScheduler {
    private Scheduler scheduler;
    private static SimScheduler instance;

    public static SimScheduler getInstance() {
        if (instance == null) {
            instance = new SimScheduler();
        }
        return instance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }


    public void start() {
        try {
            if (this.scheduler == null) {
                SchedulerFactory schedFact = new StdSchedulerFactory();
                this.scheduler = schedFact.getScheduler();
            }
            this.scheduler.start();
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

}
