package net.es.oscars.api.compat;

import java.util.UUID;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SubscribeJob05 implements Job{
    private Logger log = Logger.getLogger(SubscribeJob05.class);
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        OSCARSNetLogger netLog = new OSCARSNetLogger(ModuleName.API, UUID.randomUUID()+"");
        OSCARSNetLogger.setTlogger(netLog);
        try {
            SubscribeManager05 subscribeMgr = SubscribeManager05.getInstance();
            subscribeMgr.renewAll();
        } catch (Exception e) {
            this.log.info(netLog.start("SubscribeJob05.execute"));//make sure you have matching start and end
            this.log.error(netLog.error("SubscribeJob05.execute", ErrSev.MAJOR, e.getMessage()));
        }
    }
}
