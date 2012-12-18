package net.es.oscars.authN.common;

import java.util.ArrayList;
import java.util.Map;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.database.hibernate.Initializer;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.hibernate.Session;


/**
 *
 * @author Evangelos Chaniotakis, David Robertson
 *
 * A singleton class that is used by both the AuthNService and the AuthNPolicyService.
 * A ContextConfiguration must be initialized before this class is invoked. 
 * The first getInstance call initializes the parameters of the database from the ContextConfig.
 * The class provides methods to open and return a Hibernate
 * session, and shut down the session. it also provides methods to intitialize and return instances
 * of an AuthNManager and a PolicyManager.
 */
@SuppressWarnings("unchecked")
public class AuthNCore {
    private static Logger log = Logger.getLogger(AuthNCore.class);

    private static AuthNCore instance = null;
    private AuthNManager authNManager = null;
    private PolicyManager policyManager = null;
    private static String dbname = null;
    private static String username = null;
    private static String password = null;
    private static String salt = null;
    private static String monitor = null;

    static {
        try {
            // same information for both authN and authNPolicy
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHN);
            cc.setServiceName(ServiceNames.SVC_AUTHN);
            String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
            Map config = ConfigHelper.getConfiguration(configFile);
            assert config != null : "No configuration";
            Map authN = (Map) config.get("authN");
            assert authN != null : "No authN stanza in configuration";
            dbname = (String) authN.get("dbname");
            assert dbname != null : "No dbname in configuration";
            username = (String) authN.get("username");
            assert username != null : "No user name in configuration";
            password = (String) authN.get("password");
            assert password != null : "No password in configuration";
            salt = (String) authN.get("salt");
            assert salt != null : "No salt in configuration";
            monitor = (String) authN.get("monitor");
        } catch (ConfigException e){
            log.error("configurationException " + e.getMessage());
        }
    }

    /**
     * Constructor - private because this is a Singleton
     */
    private AuthNCore() {
    }

    /**
     * returns the singleton instance, which it creates
     * and initializes the database on it first invocation.
     * 
     * @return the OSCARSCore singleton instance
     */
    public static AuthNCore getInstance() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        log.debug(netLogger.start("AuthNGetInstance"));
        if (AuthNCore.instance == null) {
            AuthNCore.instance = new AuthNCore();
            instance.initDatabase();
        }
        return AuthNCore.instance;
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
    public Session getSession() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "getSession";
        Session session = HibernateUtil.getSessionFactory(dbname).getCurrentSession();
        if (session == null || !session.isOpen()) {
            log.info(netLogger.start(event,"opening authn session"));
            HibernateUtil.getSessionFactory(dbname).openSession();
            session = HibernateUtil.getSessionFactory(dbname).getCurrentSession();
        }
        if (session == null || !session.isOpen()) {
            log.error(netLogger.error(event,ErrSev.MAJOR,"authn session is still closed!"));
        }
        return session;
    }

    /**
     * Initializes the DB module
     */
    public void initDatabase() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "initDatabase";
        log.debug(netLogger.start(event,dbname));
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(dbname);
        Initializer dbInitializer = new Initializer();
        dbInitializer.initDatabase(dbnames, username, password, monitor, ServiceNames.SVC_AUTHN);
        log.debug(netLogger.end(event));
    }

    /**
     * Initializes the AuthNManager module
     */
    public void initAuthNManager() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "initAuthManager";
        log.debug(netLogger.start(event));
        this.authNManager = new AuthNManager(dbname, salt);
        log.debug(netLogger.end(event));
    }

    /**
     * Initializes the AuthN PolicyManager module
     */
    public void initPolicyManager() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "initPolicyManager";
        log.debug(netLogger.start(event));
        this.policyManager = new PolicyManager(dbname, salt);
        log.debug(netLogger.end(event));
    }

    /**
     * @return the authn database name
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * @return the authn salt value
     */
    public String getSalt() {;
        return salt;
    }

    /**
     * @return the authNManager
     */
    public AuthNManager getAuthNManager() {
        if (this.authNManager == null) {
            this.initAuthNManager();
        }
        return authNManager;
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
