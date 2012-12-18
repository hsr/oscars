package net.es.oscars.database.hibernate;

import java.util.List;

import org.apache.log4j.*;

/**
 * Initializer handles Hibernate initialization.
 */
public class Initializer {
    private Logger log;

    public Initializer() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Initializes Hibernate. Called from Core modules getInstance methods
     */
    public void initDatabase(List<String> dbnames,
                             String username,
                             String password,
                             String monitor,
                             String serviceName) {

        // initializes session factories for given databases
        HibernateUtil.initSessionFactories(dbnames,
                                           username,
                                           password,
                                           monitor,
                                           serviceName);
    }
}
