package net.es.oscars.authZ.common;

import java.util.ArrayList;
import java.util.Map;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.database.hibernate.Initializer;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.hibernate.Session;


/**
 *
 * @author Evangelos Chaniotakis, David Robertson
 * 
 * A singleton class that is used by both the AuthZService and the AuthZPolicyService.
 * A ContextConfiguration must be initialized before this class is invoked. 
 * The first getInstance call initializes the parameters of the database from the ContextConfig.
 * The class provides methods to open and return a Hibernate
 * session, and shut down the session. it also provides methods to intitialize and return instances
 * of an AuthZManager and a PolicyManager.
 */
public class AuthZCore {
    private static Logger log = Logger.getLogger(AuthZCore.class);

    private static AuthZCore instance = null;
    private AuthZManager authZManager = null;
    private PolicyManager policyManager = null;
    private static String dbname = null;
    private static String username = null;
    private static String password = null;
    private static String monitor = null;


    static {
        // same properties for authZ and authZPolicy
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHZ);
            String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
            Map config = ConfigHelper.getConfiguration(configFile);
            assert config != null : "No configuration";
            Map authZ = (Map) config.get("authZ");
            assert authZ != null : "No authZ stanza in configuration";
            dbname = (String) authZ.get("dbname");
            assert dbname != null : "No database name in configuration";
            username = (String) authZ.get("username");
            assert username != null : "No user name in configuration";
            password = (String) authZ.get("password");
            assert password != null : "No password in configuration";
            monitor = (String) authZ.get("monitor");
        } catch (ConfigException e){
            log.error("configurationException " + e.getMessage());
        }
    }

    /**
     * Constructor - private because this is a Singleton
     */
    private AuthZCore() {
    }

    /**
     * @return the OSCARSCore singleton instance
     */
    public static AuthZCore getInstance() {
        if (AuthZCore.instance == null) {
            AuthZCore.instance = new AuthZCore();
            instance.initDatabase();
        }
        return AuthZCore.instance;
    }

    /**
     * Shuts down database connections -- not called yet
     */
    public void shutdown() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event="shutdown";
        log.debug(netLogger.start(event));
        HibernateUtil.closeSessionFactory(dbname);
        log.debug(netLogger.end(event));
    }


    /**
     * @return the current DB session for the current thread
     */
    public Session getSession() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event="getSession";
        Session session = HibernateUtil.getSessionFactory(dbname).getCurrentSession();
        if (session == null || !session.isOpen()) {
            log.info(netLogger.getMsg(event,"opening authz session"));
            HibernateUtil.getSessionFactory(dbname).openSession();
            session = HibernateUtil.getSessionFactory(dbname).getCurrentSession();
        }
        if (session == null || !session.isOpen()) {
            log.error(netLogger.error(event,ErrSev.MAJOR,"authz session is still closed!"));
        }
        return session;
    }

    /**
     * Initializes the DB module
     */
    public void initDatabase() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event="initDataBase";
        log.debug(netLogger.start(event,dbname));
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(dbname);
        Initializer dbInitializer = new Initializer();
        dbInitializer.initDatabase(dbnames, username, password, monitor, ServiceNames.SVC_AUTHZ);
        log.debug(netLogger.end(event));
    }


    /**
     * Initializes the AuthZManager module
     */
    public void initAuthZManager() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event="initAuthManager";
        log.debug(netLogger.start(event));
        this.authZManager = new AuthZManager(dbname);
        log.debug(netLogger.end(event));
    }


    /**
     * Initializes the AuthZ PolicyManager module
     */
    public void initPolicyManager() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event="initPolicyManager";
        log.debug(netLogger.start(event));
        this.policyManager = new PolicyManager(dbname);
        log.debug(netLogger.end(event));
    }


    /**
     * @return the authz database name
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * @return the authZManager
     */
    public AuthZManager getAuthZManager() {
        if (this.authZManager == null) {
            this.initAuthZManager();
        }
        return authZManager;
    }

    /**
     * @return the policyManager
     */
    public PolicyManager getPolicyManager() {
        if (this.policyManager == null) {
            this.initPolicyManager();
        }
        return policyManager;
    }
}
