package net.es.oscars.resourceManager.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.database.hibernate.Initializer;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.scheduler.ReservationScheduler;
import net.es.oscars.resourceManager.scheduler.RMReservationScheduler;

/**
 *
 * @author Mary Thompson
 *
 * A singleton class that loads the configuration file, initializes the RM database. 
 * A ContextConfiguration must be initialized before this class is invoked. 
 * instantiates a ResourceManager and StateEngine, and opens and returns a Hibernate
 * session, and provides a method to shut down the session.
 */
public class RMCore {
    private static Logger log = Logger.getLogger(RMCore.class);

    private static RMCore        instance = null;
    private ResourceManager      resourceManager = null;
    private StateEngine          stateEngine = null;
    //private OSCARSNetLogger      netLogger = null;
    private ReservationScheduler reservationScheduler = null;
    private static String        dbname = null;
    private static String        username = null;
    private static String        password = null;
    private static String        monitor = null;
    private static String        localDomainId = "localDomain";
    private static Integer       scanInterval = null;
    private static Integer       lookAhead = null;

    static {
        // same properties for authZ and authZPolicy
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_RM);
            String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
            Map config = ConfigHelper.getConfiguration(configFile);
            assert config != null : "No configuration";
            Map RMconfig = (Map) config.get("resourceManager");
            assert RMconfig != null : "No resourceManager stanza in configuration";
            dbname = (String) RMconfig.get("dbname");
            assert dbname != null : "No dbname in configuration";
            username = (String) RMconfig.get("username");
            assert username != null : "No user name in configuration";
            password = (String) RMconfig.get("password");
            assert password != null : "No password in configuration";
            monitor = (String) RMconfig.get("monitor");
            scanInterval = Integer.valueOf((String) RMconfig.get("scanInterval"));
            lookAhead = Integer.valueOf((String) RMconfig.get("lookAhead"));

            configFile = cc.getFilePath(ServiceNames.SVC_UTILS,
                                        cc.getContext(),
                                        ConfigDefaults.CONFIG);
            Map utilConfig = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFile);
            Map localDomain = (Map) utilConfig.get("localDomain");
            localDomainId = (String) localDomain.get("id");
            System.out.println("localDomainId is " + localDomainId);
         } catch (ConfigException e){
            log.error("configurationException " + e.getMessage());
        }
    }

    /**
     * Constructor - private because this is a Singleton
     */
    private RMCore() {
        //this.netLogger = OSCARSNetLogger.getTlogger();
        // Start the Reservation Scheduler
        this.initDatabase();
        this.initReservationScheduler();
    }
    /**
     * Constructor - for unit tests
     */
    private RMCore(String dbname) {
        //this.netLogger = OSCARSNetLogger.getTlogger();
        this.dbname =dbname; // override production dbname
        // Start the Reservation Scheduler
        this.initDatabase();
        this.initReservationScheduler();
    }

    /**
     * constructor for unit tests. Sets dbname to test data base
     * @return the OSCARSCore singleton instance
     */
    public static RMCore getInstance(String dbname) {
        if (RMCore.instance == null) {
            RMCore.instance = new RMCore(dbname);
        }
        return RMCore.instance;
    }
    /**
     * @return the OSCARSCore singleton instance
     */
    public static RMCore getInstance() {
        if (RMCore.instance == null) {
            RMCore.instance = new RMCore();
        }
        return RMCore.instance;
    }
    /**
     * Shuts down database connections -- not called yet
     */
    public void shutdown() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger(); 
        log.info(netLogger.start("shutdown"));
        HibernateUtil.closeSessionFactory(dbname);
        log.info(netLogger.end("shutdown"));
    }


    /**
     * @return the current DB session for the current thread
     */
    public synchronized Session getSession() {
        String event = "getSession";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger(); 
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory(dbname);
        if (sessionFactory == null) {
            log.error(netLogger.error(event, ErrSev.MAJOR,"No Hibernate session factory"));
            return null;
        }
        Session session = sessionFactory.getCurrentSession();
        if (session == null || !session.isOpen()) {
            log.info(netLogger.getMsg(event,"opening resourceManager session"));
            sessionFactory.openSession();
            session = sessionFactory.getCurrentSession();
        }
        if (session == null || !session.isOpen()) {
            log.error(netLogger.error(event, ErrSev.MAJOR,"resourceManager session is still closed!"));
        }
        return session;
    }

    /**
     * Initializes the DB module. instantiates a singleton StateEngine
     * called only from the RMcore constructor
     */
    public synchronized void initDatabase() {
        String event = "initDatabase";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger(); 
        log.debug(netLogger.start(event));
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(dbname);
        Initializer dbInitializer = new Initializer();
        dbInitializer.initDatabase(dbnames, username, password, monitor, "ResourceManagerService");
        this.stateEngine =  new StateEngine(dbname);
        log.debug(netLogger.end(event));
    }

    /**
     * Initializes the ResourceManager module
     * called only from RMCore.getResourceManager
     */
    private void initResourceManager() {
        String event = "initRMManger";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger(); 
        log.debug(netLogger.start(event));
        this.resourceManager = new ResourceManager(dbname);
        log.debug(netLogger.end(event));
    }

    /**
     * Initializes the ResourceScheduler module
     * called only from RMCore.getResourceManager
     */
    private void initReservationScheduler() {
        String event = "initReservationScheduler";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger(); 
        log.debug(netLogger.start(event,"scanInterval= " + this.getScanInterval() + 
                " lookAhead= " + this.getLookAhead()));
        this.reservationScheduler = RMReservationScheduler.getInstance(this.getScanInterval(), this.getLookAhead());
        log.debug(netLogger.end(event));
    }

    /**
     * @return the resourceManager database name
     */
    public String getDbname() {
        return RMCore.dbname;
    }

    /**
     * @return the local domain name
     */
    public String getLocalDomainId() {
        return RMCore.localDomainId;
    }

    /**
     * @return the state engine
     */
    public StateEngine getStateEngine() {
        return this.stateEngine;
    }
    /**
     * @return the interval to scan the reservations Table for setup or teardown actions
     */
    public Integer getScanInterval() {
        return RMCore.scanInterval;
    }
    /**
     * @return the lookAhead value when scanning the reservations Table for setup or teardown actions
     */
    public Integer getLookAhead() {
        return RMCore.lookAhead;
    }

    /**
     * @return the "singleton' resourceManager
     * called by each method in RMSoapHandler
     */
    public ResourceManager getResourceManager() {
        if (this.resourceManager == null) {
            this.initResourceManager();
        }
        return this.resourceManager;
    }

    /**
     * @return the "singleton' ReservationScheduler
     */
    public ReservationScheduler getReservationScheduler() {
        if (this.reservationScheduler == null) {
            this.initReservationScheduler();
        }
        return this.reservationScheduler;
    }
}
