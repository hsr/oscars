package net.es.oscars.resourceManager.dao;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.PathElemParam;

/**
 * PathElemParamDAO is the data access object for the rm.pathElemParams table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathElemParamDAO extends GenericHibernateDAO<PathElemParam, Integer> {
    private Logger log;
    
    public PathElemParamDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }
}
