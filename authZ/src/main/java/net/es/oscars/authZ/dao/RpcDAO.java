package net.es.oscars.authZ.dao;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authZ.beans.Rpc;

/**
 * RpcDAO is the data access object for the authZ.rpcs table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class RpcDAO extends GenericHibernateDAO<Rpc, Integer> {

    public RpcDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
