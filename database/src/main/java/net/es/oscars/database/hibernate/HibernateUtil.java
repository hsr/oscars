package net.es.oscars.database.hibernate;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.io.File;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.jmx.StatisticsService;

/**
 * HibernateUtil is adapted from the tutorial in the Hibernate 3.2
 * distribution.
 * It maintains a hash map of session factories with one entry for each item
 * in the list of db names given to initSessionFactories.
 *
 * @author (besides Hibernate developers) dwrobertson@lbl.gov, mrthompson@lbl.gov
 */
public class HibernateUtil {

    private static final Map<String, SessionFactory> sessionFactories =
        new HashMap<String,SessionFactory>();
    private static Logger LOG =  Logger.getLogger(HibernateUtil.class);
    
    /**
     * Called from Initializer which is called by Core modules getInstance methods and test setup. 
     *
     * @param dbnames list of db names to build session factories for
     * @param username database user name
     * @param password database password
     * @param monitor whether to monitor statistics
     * @param serviceName name of service from utils.svc.ServiceNames module
     */
    public static void
        initSessionFactories(List<String> dbnames,
                             String username,
                             String password,
                             String monitor,
                             String serviceName) {
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "initHibernateSessionFactories";
        try {
            Properties props = new Properties();
            props.setProperty("hibernate.connection.username", username);
            props.setProperty("hibernate.connection.password", password);
            Configuration cfg = new Configuration();
            cfg.setProperties(props);
            ContextConfig cc = ContextConfig.getInstance();
            if (cc.getServiceName() == null){
                System.out.println("initSessionFactories: cc.serviceName not set");
            }
            if (!serviceName.equals(cc.getServiceName())) {
                System.out.println("cc.serviceName is " + cc.getServiceName() + 
                        " but input service  name is " + serviceName);
            }

            for (String dbname: dbnames) {
                if (sessionFactories.get(dbname) == null) {
                    SessionFactory sessionFactory = null;
                    String fileAlias = dbname + ".cfg.xml";
                    String fileName = cc.getFilePath(fileAlias);
                    File cfgFile = new File(fileName);
                    if (cfgFile.exists()) {
                        LOG.debug(netLogger.start(event,"Initializing data base from " + fileName));
                        sessionFactory =
                            cfg.configure(cfgFile).buildSessionFactory();
                    } else { // try resource
                        LOG.debug(netLogger.start(event,"Initializing data base from " + dbname + ".cfg.xml"));
                        sessionFactory =
                            cfg.configure(dbname + ".cfg.xml").buildSessionFactory();
                    }
                    putSessionFactory(dbname, sessionFactory);
                    if ((monitor != null) && ("1".equals(monitor))) {
                        MBeanServer mbeanServer =
                            ManagementFactory.getPlatformMBeanServer();
                        final ObjectName objectName = new ObjectName(
                            "Hibernate:name=statistics,Type=" + dbname +
                            System.currentTimeMillis());
                        final StatisticsService mBean =
                            new StatisticsService();
                        mBean.setStatisticsEnabled(true);
                        mBean.setSessionFactory(sessionFactory);
                        mbeanServer.registerMBean(mBean, objectName);
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.out.println("error: "+ex.getMessage());
            throw new ExceptionInInitializerError(ex.getMessage());
        }
    }

    public static void destroySessionFactories() {
        Iterator<String> dbNamesIt = sessionFactories.keySet().iterator();
        while (dbNamesIt.hasNext()) {
            String dbName = dbNamesIt.next();
            HibernateUtil.closeSessionFactory(dbName);
        }
    }

    public static void closeSessionFactory(String factoryName) {
        SessionFactory sessionFactory = sessionFactories.get(factoryName);
        if (sessionFactory != null) {
            Session session = sessionFactory.getCurrentSession();
            if (session != null) {
                session.close();
            }
            sessionFactory.close();
        }
    }

    public static SessionFactory getSessionFactory(String factoryName) {
        return sessionFactories.get(factoryName);
    }

    public static void putSessionFactory (String name, SessionFactory SF) {
        sessionFactories.put(name, SF);
    }
}
