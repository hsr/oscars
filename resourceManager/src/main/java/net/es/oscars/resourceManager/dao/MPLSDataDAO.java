package net.es.oscars.resourceManager.dao;

import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.MPLSData;

/**
 * MPLSDataDAO is the data access object for the rm.mplsData table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class MPLSDataDAO extends GenericHibernateDAO<MPLSData, Integer> {

    public MPLSDataDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
