package edu.internet2.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.Map;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;

public class DBUtil {
    private static Logger log =  Logger.getLogger(DBUtil.class);
    private static String dburl = null;
    private static String dbname = null;
    private static String username = null;
    private static String password = null;
    private static String monitor = null;


    static {
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_IONUI);
            String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
            Map config = ConfigHelper.getConfiguration(configFile);
            assert config != null : "No configuration";
            Map iondb = (Map) config.get("iondb");
            assert iondb != null : "No iondb stanza in configuration";
            dburl  = (String) iondb.get("dburl");
            assert dbname != null : "No url in configuration";
            dbname = (String) iondb.get("dbname");
            assert dbname != null : "No database name in configuration";
            username = (String) iondb.get("username");
            assert username != null : "No user name in configuration";
            password = (String) iondb.get("password");
            assert password != null : "No password in configuration";
            monitor = (String) iondb.get("monitor");
        } catch (ConfigException e){
            log.error("configurationException " + e.getMessage());
        }
    }

    public static boolean loadJDBCDriver(){
	try {
            String driver = "com.mysql.jdbc.Driver";
            Class.forName(driver).newInstance();
            log.debug("Successfully connected to JDBC SQL " );
	    return true;
	    //end SQL trial
        } catch (InstantiationException e) {
            log.error("1:"+ e.getMessage());
            return false;
        } catch (IllegalAccessException e) {
            log.error("2:"+ e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            log.error("3:"+ e.getMessage());
            return false;
        }

    } //end method

    
    public static void closeConnection(Connection conn){
        try {
            if(conn != null && (!conn.isClosed())){
                conn.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    /* Method to get DB connection 
    */
    public static Connection getDBConnection() {
         Connection conn = null; 
	 try{
            conn = DriverManager.getConnection(dburl+dbname,username,password); 
            log.debug("-INFO: DB Connection obtained" + conn);
            //end SQL

        }catch(SQLException e){
            log.error("---Could not get DriverManager connection:"+ e.getMessage());
            closeConnection(conn);
        }
	return conn;
    } //end method getDBConn
}
