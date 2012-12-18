package net.es.oscars.resourceManager.dao;

import java.util.*;
import org.apache.log4j.*;

import org.hibernate.*;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.resourceManager.beans.IdSequence;


/**
 * IdSequenceDAO is the data access object for the rm.idsequence table.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Andrew Lake (alake@internet2.edu)
 */
public class IdSequenceDAO
    extends GenericHibernateDAO<IdSequence, Integer> {

    private Logger log;
    private String dbname;

    public IdSequenceDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    /**
     * Creates a new entries in the table and returns an ID. This will create 
     * a lot of entries but makes locking issues easy.
     *
     * @return the new id as an int
     * @throws RMException
     */
    public int getNewId() throws RMException {
        IdSequence id = new IdSequence();
        this.getSession().save(id);
        int newId = id.getId().intValue();
        
        return newId;
    }
}
