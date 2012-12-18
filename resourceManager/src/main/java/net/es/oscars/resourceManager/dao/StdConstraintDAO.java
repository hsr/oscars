package net.es.oscars.resourceManager.dao;

import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.StdConstraint;

/**
 * StdConstraintDAO is the data access object for the rm.stdConstraints table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class StdConstraintDAO extends GenericHibernateDAO<StdConstraint, Integer> {

    public StdConstraintDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
