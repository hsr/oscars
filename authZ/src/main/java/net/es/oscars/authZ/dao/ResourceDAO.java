package net.es.oscars.authZ.dao;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authZ.common.AuthZException;
import net.es.oscars.authZ.beans.Resource;

/**
 * ResourceDAO is the data access object for the aaa.resources table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class ResourceDAO extends GenericHibernateDAO<Resource, Integer> {

    public ResourceDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /** 
     * given an resource name, return the resource id
     * 
     * @param resourceName String name of the resource
     * @return an Integer containing the resource id
     */
    public Integer getIdByName(String resourceName) throws AuthZException {
        Resource resource = super.queryByParam("name", resourceName);
        if (resource != null ) {
            return resource.getId();
        } else {
            throw new AuthZException ("No resource with name "+ resourceName);
        }
    }
}
