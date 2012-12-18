package net.es.oscars.coord.common;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.apache.log4j.Logger;


public class SchedulerManager {
    private Logger LOG;

    private static Scheduler scheduler;
    private static SchedulerManager instance;

    public static SchedulerManager getInstance() {
        if (instance == null) {
            instance = new SchedulerManager();
        }
        return instance;

    }

    private SchedulerManager() {
        LOG = Logger.getLogger(this.getClass());
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "SchedulerManagerConstructor";
        SchedulerFactory schedFact = new StdSchedulerFactory();
        try {
            scheduler = schedFact.getScheduler();
        } catch (SchedulerException e) {
            LOG.error(netLogger.error(event, ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "SchedulerManagerStart";
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            LOG.error(netLogger.error(event,ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            LOG.error(netLogger.error("ScheduleManagerShutdown", ErrSev.MINOR, e.getMessage()));
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Scheduler getScheduler() {
        return scheduler;
    }

}
