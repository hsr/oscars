package net.es.oscars.lookup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.lookup.jobs.CleanDBJob;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.internet2.perfsonar.PSLookupClient;
import edu.internet2.perfsonar.dcn.DCNLookupClient;

public class LookupGlobals {
    private Logger log = Logger.getLogger(LookupGlobals.class);
    private ComboPooledDataSource dataSource;
    private Scheduler scheduler;
    private Integer dataTTL;
    private Integer disableRegister;
    private String cacheCleanSched;
    
    final public static String JDBC_URL = "jdbc:derby:lookupCache";
    final private String PROP_DATA_TTL = "dataTTL";
    final private String PROP_GLOBAL_HINTS_FILE = "globalHintsFile";
    final private String PROP_GLOBAL_LOOKUP_SERVICES = "globalLookupServices";
    final private String PROP_HOME_LOOKUP_SERVICES = "homeLookupServices";
    final private String PROP_CACHE_CLEAN_SCHED = "cacheCleanSchedule";
    final private String PROP_DISABLE_REGISTER = "disableRegister";
    
    final private int DEFAULT_TTL = 3600; //1 hour
    final private int DEFAULT_DB_POOL_SIZE = 50;
    final private String C3P0_TEST_QUERY = "SELECT id FROM services";
    final private int C3P0_IDLE_TEST_PERIOD = 600;//10 minutes
    final private String DEFAULT_CACHE_CLEAN_SCHED = "0 0/5 * * * ?"; //5 minutes
    private String hintsFile;
    private List<String> globalLookupServices;
    private List<String> homeLookupServices;
    private DCNLookupClient perfsonarClient;
    
    static private ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_LOOKUP);
    static private LookupGlobals instance = null;
    
    public LookupGlobals() throws LookupException{
        //load configuration
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.loadConfig();
        
        //create perfsonar lookup client
        try{
            this.log.debug(netLog.start("initPerfsonarClient"));
            this.initPerfsonarClient();
            this.log.debug(netLog.end("initPerfsonarClient"));
        }catch(Exception e){
            this.log.debug(netLog.error("initPerfsonarClient", ErrSev.CRITICAL, e.getMessage()));
            throw new LookupException("Error creating perfSONAR client: " + e.getMessage());
        }
        this.dataSource = new ComboPooledDataSource();
        
        //init DB
        try {
            System.setProperty("derby.system.home", cc.getFilePath("derby.home"));
            this.log.debug(netLog.start("initJDBC"));
            this.dataSource.setDriverClass("org.apache.derby.jdbc.EmbeddedDriver");
            this.dataSource.setJdbcUrl(JDBC_URL);
            this.dataSource.setMaxPoolSize(DEFAULT_DB_POOL_SIZE);
            this.dataSource.setIdleConnectionTestPeriod(C3P0_IDLE_TEST_PERIOD);
            this.dataSource.setPreferredTestQuery(C3P0_TEST_QUERY);
            this.dataSource.setConnectionCustomizerClassName(LookupDBConnectionCustomizer.class.getName());
            
            /*
             * dataSource.setIdleConnectionTestPeriod(C3P0_IDLE_TEST_PERIOD);
            //set query used to test stale connection
            dataSource.setPreferredTestQuery(C3P0_TEST_QUERY);
             */
            
            this.log.debug(netLog.end("initJDBC"));
        } catch (Exception e) {
            this.log.debug(netLog.end("initJDBC", ErrSev.CRITICAL, e.getMessage()));
            throw new LookupException("Error initializing lookup module due to JDBC error");
        }
        
        //init Scheduler
        SchedulerFactory schedFactory = new StdSchedulerFactory();
        try {
            this.log.debug(netLog.start("initScheduler"));
            this.scheduler = schedFactory.getScheduler();
            this.scheduler.start();
            this.log.debug(netLog.end("initScheduler"));
        } catch (SchedulerException e) {
            this.log.debug(netLog.end("initScheduler", ErrSev.CRITICAL, e.getMessage()));
            throw new LookupException("Error initializing lookup module: Failed to initialize scheduler");
        }

        //Add job to clean-up cache if perfsonar configured
        if(this.perfsonarClient != null){
            this.log.debug(netLog.start("initCacheClean"));
            CronTrigger cronTrigger = null;
            try {
                cronTrigger = new CronTrigger("LSCleanDBTrigger", "LOOKUP", cacheCleanSched);
            } catch (ParseException e) {
                this.log.debug(netLog.end("initCacheClean", ErrSev.CRITICAL, e.getMessage()));
                throw new LookupException("Error initializing lookup module");
            }
            JobDetail jobDetail = new JobDetail("LSCleanDBJob", "LOOKUP", CleanDBJob.class);
            try {
                this.scheduler.scheduleJob(jobDetail, cronTrigger);
            } catch (SchedulerException e) {
                this.log.debug(netLog.end("initCacheClean", ErrSev.CRITICAL, e.getMessage()));
                throw new LookupException("Error initializing lookup module");
            }
            this.log.debug(netLog.end("initCacheClean"));
        }
    }
    
    private void initPerfsonarClient() throws HttpException, IOException {
        //no perfsonar client used so rely on database
        if(this.hintsFile == null && this.globalLookupServices == null && 
                this.homeLookupServices == null){
            return;
        }
        
        boolean useGlobals = false;
        ArrayList<String> globalList = new ArrayList<String>();
        
        //get URLs from hints file
        if(this.hintsFile != null){
            useGlobals = true;
            for(String url : PSLookupClient.getGlobalHints(this.hintsFile)){
                globalList.add(url);
            }
        }
        
        //get manual URLs
        if(this.globalLookupServices != null){
            useGlobals = true;
            globalList.addAll(this.globalLookupServices);
        }
        
        //convert to array
        String[] hlsArray = new String[0];
        if(this.homeLookupServices != null){
            hlsArray = this.homeLookupServices.toArray(
                    new String[this.homeLookupServices.size()]);
        }
        String[] glsArray = globalList.toArray(new String[globalList.size()]);
        
        //Create DCN client
        this.perfsonarClient = new DCNLookupClient(glsArray, hlsArray);
    }

    private void loadConfig() throws LookupException {
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        Map config = null;
        String configFile = null;
        try {
            configFile = cc.getFilePath(ConfigDefaults.CONFIG);
            this.log.debug(netLog.start("loadConfig", null, configFile));
            config = ConfigHelper.getConfiguration(configFile);
        }
        catch (ConfigException e) {
            log.error("loadConfig caught ConfigException " + e.getMessage());
            throw new LookupException(e.getMessage());
        }
        HashMap<String, String> logFieldMap = new HashMap<String, String>();
        
        //if no perfSONAR settings then just use the DB
        if (config.get("perfsonar") == null){
            this.log.debug(netLog.end("loadConfig", null, configFile));
            return;
        }
        
        Map perfsonar = (Map) config.get("perfsonar");
        if(perfsonar.containsKey(PROP_DISABLE_REGISTER)){
            this.disableRegister = (Integer) perfsonar.get(PROP_DISABLE_REGISTER);
        }else{
            this.disableRegister = 0;
        }
        logFieldMap.put("disableRegister", this.disableRegister+"");
        
        if(perfsonar.containsKey(PROP_DATA_TTL)){
            this.dataTTL = (Integer) perfsonar.get(PROP_DATA_TTL);
        }else{
            this.dataTTL = DEFAULT_TTL;
        }
        logFieldMap.put("dataTTL", this.dataTTL+"");
        
        if(perfsonar.containsKey(PROP_CACHE_CLEAN_SCHED)){
            this.cacheCleanSched = (String) perfsonar.get(PROP_CACHE_CLEAN_SCHED);
        }else{
            this.cacheCleanSched = DEFAULT_CACHE_CLEAN_SCHED;
        }
        logFieldMap.put("cacheCleanSched", this.cacheCleanSched);
        
        
        if(perfsonar.containsKey(PROP_GLOBAL_HINTS_FILE)){
            this.hintsFile = (String) perfsonar.get(PROP_GLOBAL_HINTS_FILE);
            logFieldMap.put("hints", this.hintsFile);
        }
        
        if(perfsonar.containsKey(PROP_GLOBAL_LOOKUP_SERVICES)){
            this.globalLookupServices = (List<String>) perfsonar.get(PROP_GLOBAL_LOOKUP_SERVICES);
        }
        
        if(perfsonar.containsKey(PROP_HOME_LOOKUP_SERVICES)){
            this.homeLookupServices = (List<String>) perfsonar.get(PROP_HOME_LOOKUP_SERVICES);
        }
        this.log.debug(netLog.end("loadConfig", null, configFile, logFieldMap));
    }
    
    static public LookupGlobals getInstance() throws LookupException{
        if(instance == null){
           instance = new LookupGlobals();
        }
        return instance;
    }
    
    public ComboPooledDataSource getDataSource(){
        return this.dataSource;
    }
    
    public Connection getDbConnection() throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        Connection conn = null;
        try{
            this.log.debug(netLog.start("getDbConnection"));
            conn = this.dataSource.getConnection();
        }catch(SQLException e){
            this.log.debug(netLog.end("getDbConnection", ErrSev.CRITICAL, e.getMessage()));
            throw new LookupException("Error connecting to the local lookup database.");
        }
        this.log.debug(netLog.end("getDbConnection"));
        return conn;
    }
    
    public void releaseDbConnection(Connection conn){
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e1) {}
        }
    }
    
    public Scheduler getScheduler(){
        return this.scheduler;
    }
    
    public long getTTL(){
        return this.dataTTL;
    }
    public long getDisableRegister(){
        return this.disableRegister;
    }
    
    public DCNLookupClient getPerfsonarClient(){
        return this.perfsonarClient;
    }
}
