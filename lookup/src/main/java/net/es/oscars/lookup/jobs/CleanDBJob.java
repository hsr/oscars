package net.es.oscars.lookup.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.lookup.LookupGlobals;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CleanDBJob implements Job{
    Logger log = Logger.getLogger(CleanDBJob.class);
    
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        OSCARSNetLogger netLog = new OSCARSNetLogger(ModuleName.LOOKUP);
        netLog.setGUID(UUID.randomUUID().toString());
        boolean error = false;
        this.log.debug(netLog.start("CleanDBJob"));
        
        Connection conn = null;
        LookupGlobals globals = null;
        try {
            globals = LookupGlobals.getInstance();
            conn = globals.getDbConnection();
        } catch (LookupException e) {
            this.log.debug(netLog.error("CleanDBJob", ErrSev.CRITICAL, 
                    "Unable to get connection to clean-up DB: " + 
                    e.getMessage()));
            return;
        }
        
        long expTime = System.currentTimeMillis()/1000 - globals.getTTL();
        try {
            //delete protocol entries
            PreparedStatement protoStmt = conn.prepareStatement("DELETE FROM protocols " +
                    "WHERE serviceId IN (SELECT id FROM services WHERE " +
                    "lastUpdated > 0 AND lastUpdated <= ?)");
            protoStmt.setLong(1, expTime);
            protoStmt.execute();
            protoStmt.close();
            
            //delete relation entries
            PreparedStatement relationStmt = conn.prepareStatement("DELETE FROM relationships " +
                    "WHERE serviceId IN (SELECT id FROM services WHERE " +
                    "lastUpdated > 0 AND lastUpdated <= ?)");
            relationStmt.setLong(1, expTime);
            relationStmt.execute();
            relationStmt.close();
            
            //delete service entries
            PreparedStatement serviceStmt = conn.prepareStatement("DELETE FROM services WHERE " +
                    "lastUpdated > 0 AND lastUpdated <= ?");
            serviceStmt.setLong(1, expTime);
            serviceStmt.execute();
            serviceStmt.close();
            globals.releaseDbConnection(conn);
        } catch (Exception e) {
        	if(conn != null){
        		try {
					conn.close();
				} catch (SQLException e1) {}
        	}
            this.log.debug(netLog.error("CleanDBJob", ErrSev.MAJOR, 
                    "Error cleaning lookup tables: " + e.getMessage()));
            return;
        } finally {
        	globals.releaseDbConnection(conn);
        }
        

        this.log.debug(netLog.end("CleanDBJob"));
    }
}
