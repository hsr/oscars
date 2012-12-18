package net.es.oscars.resourceManager.dao;

import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.Layer2Data;

/**
 * Layer2DataDAO is the data access object for the rm.layer2Data table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class Layer2DataDAO extends GenericHibernateDAO<Layer2Data, Integer> {

    public Layer2DataDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
