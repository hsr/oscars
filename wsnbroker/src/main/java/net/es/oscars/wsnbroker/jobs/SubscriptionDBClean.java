package net.es.oscars.wsnbroker.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.wsnbroker.NotificationGlobals;
import net.es.oscars.wsnbroker.SubscriptionStatus;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SubscriptionDBClean implements Job{
    private Logger log = Logger.getLogger(SubscriptionDBClean.class);
    
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        OSCARSNetLogger netLogger = new OSCARSNetLogger(ModuleName.NOTIFY, UUID.randomUUID().toString());
        this.log.debug(netLogger.start("SubscriptionDBClean"));
        NotificationGlobals globals = null;
        Connection conn = null;
        try{
            globals = NotificationGlobals.getInstance();
            conn = globals.getConnection();
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE subscriptions SET STATUS=? WHERE terminationTime < ?");
            updateStmt.setInt(1, SubscriptionStatus.INACTIVE_STATUS);
            updateStmt.setLong(2, System.currentTimeMillis());
            updateStmt.executeUpdate();
        }catch(Exception e){
            this.log.debug(netLogger.error("SubscriptionDBClean", ErrSev.CRITICAL, e.getMessage()));
        }finally{
            globals.releaseDbConnection(conn);
        }
        
        this.log.debug(netLogger.end("SubscriptionDBClean"));
    }

}
