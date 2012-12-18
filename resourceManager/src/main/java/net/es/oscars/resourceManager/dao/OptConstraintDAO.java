package net.es.oscars.resourceManager.dao;

import java.util.List;

import org.apache.log4j.Logger;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.OptConstraint;
import net.es.oscars.resourceManager.beans.Reservation;
import net.es.oscars.resourceManager.common.RMCore;
import net.es.oscars.resourceManager.common.StateEngine;
import net.es.oscars.utils.soap.OSCARSServiceException;

/**
 * OptConstraintDAO is the data access object for the rm.OptConstraint table.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Bharath@es.net  */

public class OptConstraintDAO extends GenericHibernateDAO<OptConstraint, Integer> {

	/*@S bhr*/
	
    private Logger log;
    private String dbname;

    public OptConstraintDAO(String dbname) {
    	this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }
    
    /**
     * Updates or creates a new persistent tuple in the rm.optConstraints 
     * for reservation requests to pick from the rm.optConstraints on its next use.
     * @param OptionalConstraint to be persisted
     */
    public void update(OptConstraint optcons) {
        super.update(optcons);
        
    }

    /*@E bhr*/
    
}
