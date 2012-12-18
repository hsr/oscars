package net.es.oscars.pss.eompls.common;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.database.hibernate.Initializer;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;

import net.es.oscars.utils.svc.ServiceNames;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;



public class EoMPLSPSSCore {
    private static Logger log = Logger.getLogger(EoMPLSPSSCore.class);

    private static EoMPLSPSSCore instance = null;

    public String getDbname() {
        return dbname;
    }



    private String dbname;
    private String password;
    private String username;


    /**
     * Constructor - private because this is a Singleton
     */
    private EoMPLSPSSCore() {
    }

    /**
     * returns the singleton instance, which it creates
     * and initializes the database on it first invocation.
     * 
     * @return the OSCARSCore singleton instance
     */
    public static EoMPLSPSSCore getInstance() {
        if (EoMPLSPSSCore.instance == null) {
            EoMPLSPSSCore.instance = new EoMPLSPSSCore();
            instance.initDatabase();
        }
        return EoMPLSPSSCore.instance;
    }


    /**
     * @return the current DB session for the current thread
     */
    public Session getSession() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "getSession";
        Session session = HibernateUtil.getSessionFactory(dbname).getCurrentSession();
        if (session == null || !session.isOpen()) {
            log.info(netLogger.start(event,"opening eomplspss session"));
            HibernateUtil.getSessionFactory(dbname).openSession();
            session = HibernateUtil.getSessionFactory(dbname).getCurrentSession();
        }
        if (session == null || !session.isOpen()) {
            log.error(netLogger.error(event,ErrSev.MAJOR,"EoMPLSPSSCore is still closed!"));
        }
        return session;
    }

    /**
     * Initializes the DB module
     */
    public void initDatabase() {
        dbname = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getDbname();
        password = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getPassword();
        username = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getUsername();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();

        String event = "initDatabase";
        log.debug(netLogger.start(event,dbname));
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(dbname);
        Initializer dbInitializer = new Initializer();
        dbInitializer.initDatabase(dbnames, username, password, null, ServiceNames.SVC_PSS);
        log.debug(netLogger.end(event));
    }

}
