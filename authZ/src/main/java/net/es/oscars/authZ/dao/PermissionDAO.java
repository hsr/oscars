package net.es.oscars.authZ.dao;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authZ.common.AuthZException;
import net.es.oscars.authZ.beans.Permission;

/** PermissionDAO is the data access object for the authZ.permissions table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PermissionDAO extends GenericHibernateDAO<Permission, Integer> {

    public PermissionDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /**
     * given an permission name, return the permission id
     *
     * @param permissionName String name of the permission
     * @return an Integer containing the permission id
     */
    public Integer getIdByName(String permissionName) throws AuthZException {
        Permission permission = super.queryByParam("name", permissionName);
        if (permission != null ) {
            return permission.getId();
        } else {
            throw new AuthZException ("No permission with name "+ permissionName);
        }
    }
}
