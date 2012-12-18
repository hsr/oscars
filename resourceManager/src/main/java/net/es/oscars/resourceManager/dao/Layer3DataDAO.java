package net.es.oscars.resourceManager.dao;

import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.Layer3Data;

/**
 * Layer3DataDAO is the data access object for the rm.layer3Data table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class Layer3DataDAO extends GenericHibernateDAO<Layer3Data, Integer> {

    public Layer3DataDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
