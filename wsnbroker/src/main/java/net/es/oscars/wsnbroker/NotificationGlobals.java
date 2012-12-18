package net.es.oscars.wsnbroker;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.wsnbroker.jobs.SubscriptionDBClean;
import net.es.oscars.wsnbroker.policy.NotifyPEP;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.notify.NotifyNSUtil;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class NotificationGlobals {
    private Logger log = Logger.getLogger(NotificationGlobals.class);
    static private NotificationGlobals instance = null;
    static private ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_WSNBROKER);
    
    private Map<String,String> filterNamespaceMap;
    private ComboPooledDataSource dataSource;
    private Scheduler scheduler;
    private SubscribeManager subscribeMgr;
    private NotifyManager notifyMgr;
    private AuthNClient authnClient;
    private String authNUrl;
    private String authNWsdl;
    private AuthZClient authzClient;
    private String authZUrl;
    private String authZWsdl;
    private List<NotifyPEP> pepList;
    
    final private String PROP_EXPIRATION = "expiration";
    final private String PROP_DB_CLEAN_SCHED = "dbCleanSchedule";
    final private String PROP_AUTHN_URL = "authNUrl";
    final private String PROP_AUTHN_WSDL_URL = "authNWsdlUrl";
    final private String PROP_AUTHZ_URL = "authZUrl";
    final private String PROP_AUTHZ_WSDL_URL = "authZWsdlUrl";
    final private String PROP_PEPS = "peps";
    final private String PROP_PEP_CLASS = "class";
    final private long DEFAULT_EXPIRATION = 3600; //1 hour
    final private String DEFAULT_DB_CLEAN_SCHED = "0 * * * * ?"; //every minute
    final static public String JDBC_URL = "jdbc:derby:notifyDb";
    final static public String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    
    public NotificationGlobals() throws OSCARSServiceException {
        cc.setServiceName(ServiceNames.SVC_WSNBROKER);
        //load config file
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        Map config = null;
        String configFile = null;
        this.log.debug(netLog.start("loadConfig"));
        try {
            configFile = cc.getFilePath(ConfigDefaults.CONFIG);
            config = ConfigHelper.getConfiguration(configFile);
        } catch (ConfigException e) {
            this.log.debug(netLog.error("loadConfig", ErrSev.FATAL, e.getMessage()));
            throw new OSCARSServiceException(e.getMessage());
        }
        HashMap<String, String> logFieldMap = new HashMap<String, String>();
        long expTime = DEFAULT_EXPIRATION;
        if(config.containsKey(PROP_EXPIRATION) && config.get(PROP_EXPIRATION) != null){
            try{
                expTime = Long.parseLong(config.get(PROP_EXPIRATION)+"");
            }catch(Exception e){
                this.log.debug(netLog.error("loadConfig", ErrSev.FATAL, "Non-integer value specified as expiration"));
            }
        }
        String cleanDbSched = DEFAULT_DB_CLEAN_SCHED;
        if(config.containsKey(PROP_DB_CLEAN_SCHED) && config.get(PROP_DB_CLEAN_SCHED) != null){
            cleanDbSched = config.get(PROP_DB_CLEAN_SCHED)+"";
        }
        this.filterNamespaceMap = NotifyNSUtil.getNamespaceMap();

        this.subscribeMgr = new SubscribeManager(expTime * 1000);
        this.notifyMgr = new NotifyManager();
        logFieldMap.put(PROP_EXPIRATION, expTime+"");
        this.log.debug(netLog.end("loadConfig", null, configFile, logFieldMap));
        
        //init database
        try {
            this.log.debug(netLog.start("initDatabase"));
            //set Derby Home
            System.setProperty("derby.system.home", cc.getFilePath("derby.home"));
            //init jdbc
            dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass(JDBC_DRIVER);
            dataSource.setJdbcUrl(JDBC_URL);
            HashMap<String,String> netLogProps = new HashMap<String,String>();
            netLogProps.put("driver", JDBC_DRIVER);
            this.log.debug(netLog.end("initDatabase", null, JDBC_URL, netLogProps));
        } catch (Exception e) {
            this.log.debug(netLog.error("initDatabase", ErrSev.FATAL, e.getMessage()));
            throw new OSCARSServiceException("Error initializing notification module: " +
                    "Failed to initialize database");
        }
        
        //init scheduler
        SchedulerFactory schedFactory = new StdSchedulerFactory();
        try {
            this.log.debug(netLog.start("initScheduler"));
            this.scheduler = schedFactory.getScheduler();
            this.scheduler.start();
            //add job to clean db
            CronTrigger cronTrigger = new CronTrigger("SubCleanDBTrigger", "SUBSCRIPTIONS", cleanDbSched);
            JobDetail jobDetail = new JobDetail("SubCleanDBJob", "SUBSCRIPTIONS", SubscriptionDBClean.class);
            this.scheduler.scheduleJob(jobDetail, cronTrigger);
            this.log.debug(netLog.end("initScheduler"));
        } catch (Exception e) {
            this.log.debug(netLog.error("initScheduler", ErrSev.FATAL, e.getMessage()));
            throw new OSCARSServiceException("Error initializing notification module: Failed to initialize scheduler");
        }
        
        //init PEPs
        try{
            this.log.debug(netLog.start("initPEPs"));
            HashMap<String,String> netLogProps = new HashMap<String,String>();
            this.pepList = new ArrayList<NotifyPEP>();
            List<Map> peps = (List<Map>) config.get(PROP_PEPS);
            ArrayList<String> pepClasses = new ArrayList<String>();
            if(peps != null){
                ClassLoader classLoader = this.getClass().getClassLoader();
                for(Map pep : peps){
                    if(pep.containsKey(PROP_PEP_CLASS) && pep.get(PROP_PEP_CLASS) != null){
                        Class pepClass = classLoader.loadClass((String)pep.get(PROP_PEP_CLASS));
                        this.pepList.add((NotifyPEP)pepClass.newInstance());
                        pepClasses.add(pep.get(PROP_PEP_CLASS)+"");
                    }
                }
            }
            netLogProps.put("pepClasses", OSCARSNetLogger.serializeList(pepClasses));
            this.log.debug(netLog.end("initPEPs", null, null, netLogProps));
        }catch(Exception e){
            this.log.debug(netLog.error("initPEPs", ErrSev.FATAL, e.getMessage()));
            throw new OSCARSServiceException("Error initializing notification module: Failed to initialize PEPs");
        }
        
        //init authn URL and WSDL
        if(config.containsKey(PROP_AUTHN_URL)){
            this.authNUrl = (String) config.get(PROP_AUTHN_URL);
        }else{
            //otherwise look for authN config
            try {
                String configFilename = cc.getFilePath(ServiceNames.SVC_AUTHN,cc.getContext(),
                        ConfigDefaults.CONFIG);
                HashMap<String,Object> authNMap = 
                    (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
                HashMap<String,Object> soap = (HashMap<String,Object>) authNMap.get("soap");
                if (soap == null ) {
                    throw new ConfigException("soap stanza not found in authN yaml");
                }
                this.authNUrl = (String)soap.get("publishTo");
            } catch (ConfigException e) {
               //allow to run without auth module
                this.authNUrl = null;
            }
        }
        if(config.containsKey(PROP_AUTHN_WSDL_URL)){
            this.authNWsdl = (String) config.get(PROP_AUTHN_WSDL_URL);
        }else{
            try {
                this.authNWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHN,null) + "";
            } catch (MalformedURLException e) {
                this.authNWsdl = this.authNUrl != null ? this.authNUrl + "?wsdl" : null;
            }
        }
        
        //init authZ url and WSDL
        if(config.containsKey(PROP_AUTHZ_URL)){
            this.authZUrl = (String) config.get(PROP_AUTHZ_URL);
        }else{
           //otherwise look for authZ config
            try {
                String configFilename = cc.getFilePath(ServiceNames.SVC_AUTHZ,cc.getContext(),
                        ConfigDefaults.CONFIG);
                HashMap<String,Object> authZMap = 
                    (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
                HashMap<String,Object> soap = (HashMap<String,Object>) authZMap.get("soap");
                if (soap == null ) {
                    throw new ConfigException("soap stanza not found in authZ yaml");
                }
                this.authZUrl = (String)soap.get("publishTo");
            } catch (ConfigException e) {
               //allow to run without authZ module
                this.authZUrl = null;
            }
        }
        if(config.containsKey(PROP_AUTHZ_WSDL_URL)){
            this.authZWsdl = (String) config.get(PROP_AUTHZ_WSDL_URL);
        }else if(this.authZUrl != null){
            try {
                this.authZWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHZ,null) + "";
            } catch (MalformedURLException e) {
                this.authZWsdl = this.authZUrl + "?wsdl";
            }
        }else{
            //allow to run without authZ module
            this.authZWsdl = null;
        }
    }
    
    static public NotificationGlobals getInstance() throws OSCARSServiceException {
        if(instance == null){
           instance = new NotificationGlobals();
        }
        return instance;
    }
    
    public Connection getConnection() throws OSCARSServiceException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        Connection conn = null;
        try{
            this.log.debug(netLog.start("getConnection"));
            conn = this.dataSource.getConnection();
        }catch(SQLException e){
            this.log.debug(netLog.end("getConnection", ErrSev.CRITICAL, e.getMessage()));
            throw new OSCARSServiceException("Error connecting to the local subscription database.");
        }
        this.log.debug(netLog.end("getConnection"));
        return conn;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public SubscribeManager getSubscribeMgr() {
        return this.subscribeMgr;
    }

    /**
     * @return the notifyMgr
     */
    public NotifyManager getNotifyMgr() {
        return this.notifyMgr;
    }

    /**
     * @return the filterNamespaceMap
     */
    public Map<String, String> getFilterNamespaceMap() {
        return this.filterNamespaceMap;
    }
    
    /**
     * @return the authN service URL
     */
    public String getAuthNUrl(){
        return this.authNUrl;
    }
    
    /**
     * @return the authZ service URL
     */
    public String getAuthZUrl(){
        return this.authZUrl;
    }
    
    /**
     * 
     * @return the list of policy enforcement classes
     */
    public List<NotifyPEP > getPEPList(){
        return this.pepList;
    }
    
    /**
     * @return the authnClient
     * @throws OSCARSServiceException 
     */
    synchronized public AuthNClient getAuthNClient() throws OSCARSServiceException {
        if(this.authnClient == null){
            this.initAuthNClient();
        }
        return this.authnClient;
    }
    
    synchronized private void initAuthNClient() throws OSCARSServiceException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("initAuthNClient"));
        //exit if no URL set for authN service
        if(this.authNUrl == null){
            this.log.debug(netLog.end("initAuthNClient",
                    "No " + PROP_AUTHN_URL + " property provided. " +
                    "Proceeding with no authentication."));
            return;
        }
        
        try {
            this.authnClient = AuthNClient.getClient(new URL(this.authNUrl),
                    new URL(this.authNWsdl));
        } catch (Exception e) {
            this.log.debug(netLog.error("initAuthNClient", ErrSev.FATAL, "Error contacting AuthN service. It may not be running at given url."));
            throw new OSCARSServiceException(e.getMessage());
        }
        this.log.debug(netLog.end("initAuthNClient"));
    }
    
    /**
     * @return the authZClient
     * @throws OSCARSServiceException 
     */
    synchronized public AuthZClient getAuthZClient() throws OSCARSServiceException {
        if(this.authzClient == null){
            this.initAuthZClient();
        }
        return this.authzClient;
    }
    
    synchronized private void initAuthZClient() throws OSCARSServiceException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("initAuthZClient"));
        //exit if no URL set for authZ service
        if(this.authZUrl == null){
            this.log.debug(netLog.end("initAuthZClient",
                    "No " + PROP_AUTHZ_URL + " property provided. " +
                    "Proceeding with no authorization."));
            return;
        }
        
        try {
            this.authzClient = AuthZClient.getClient(new URL(this.authZUrl),
                    new URL(this.authZWsdl));
        } catch (Exception e) {
            this.log.debug(netLog.error("initAuthZClient", ErrSev.FATAL, 
                    "Error contacting AuthZ service. It may not be running at given url."));
            throw new OSCARSServiceException(e.getMessage());
        }
        this.log.debug(netLog.end("initAuthZClient"));
    }
    
    /**
     * Closes a DB connection when finished with it.
     * @param conn
     */
    public void releaseDbConnection(Connection conn){
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e1) {}
        }
    }
}