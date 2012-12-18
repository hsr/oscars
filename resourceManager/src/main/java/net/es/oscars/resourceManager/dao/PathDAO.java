package net.es.oscars.resourceManager.dao;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.Path;

/**
 * PathDAO is the data access object for the rm.Paths table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathDAO extends GenericHibernateDAO<Path, Integer> {
    private Logger log;

    public PathDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }

    /**
     * Retrieves a list of paths starting with the given urn.
     *
     * @param urn starting urn in path
     * @return list of paths starting with that urn
     */
    public List<Path> getPaths(String urn) {
        String sql = "select * from paths p " +
                     "inner join pathElems pe on p.id = pe.pathId " +
                     "where pe.urn = ?";
        List<Path> paths =
               (List<Path>) this.getSession().createSQLQuery(sql)
                                             .addEntity(Path.class)
                                             .setString(0, urn)
                                             .list();
        return paths;
    }
}
