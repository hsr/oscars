package net.es.oscars.authZ.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authZ.beans.Attribute;
import net.es.oscars.authZ.beans.Authorization;
import net.es.oscars.authZ.beans.Constraint;
import net.es.oscars.authZ.beans.Permission;
import net.es.oscars.authZ.beans.Resource;
import net.es.oscars.authZ.common.AuthZException;

/**
 * AuthorizationDAO is the data access object for the authZ.authorizations
 * table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 * @author Mary Thompson (mrthompson@lbl.gov)
 */
public class AuthorizationDAO
    extends GenericHibernateDAO<Authorization, Integer> {

    String dbname;
    private Logger log;

    public AuthorizationDAO(String dbname) {
        this.setDatabase(dbname);
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Retrieves authorization, if any, based on presence of corresponding
     *    four-tuple in authorizations table.
     *
     * @param attrValue String value of attribute
     * @param resourceName String name of resource
     * @param permissionName String name of permission
     * @param constraintName String name of constraint
     * @return auths - list of the associated authorization instances, if any
     */
    public Authorization query(String attrValue, String resourceName,
            String permissionName, String constraintName) throws AuthZException {

        Authorization auth = null;
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        int attrId = attrDAO.getIdByValue(attrValue).intValue();
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        int resourceId = resourceDAO.getIdByName(resourceName).intValue();
        PermissionDAO permDAO = new PermissionDAO(this.dbname);
        int permId = permDAO.getIdByName(permissionName).intValue();
        if (constraintName == null) {
            constraintName = "none";
        }
        ConstraintDAO constrDAO = new ConstraintDAO(this.dbname);
        int constrId = constrDAO.getIdByName(constraintName).intValue();

        auth =  this.query(attrId,resourceId, permId, constrId);
        return auth;
    }

    /**
     * Retrieves authorization, if any, based on presence of corresponding
     *    four-tuple in authorizations table.
     *
     * @param attrId int with primary key of attribute
     * @param resourceId int with primary key of resource
     * @param permissionId int with primary key of permission
     * @param constraintId - int with primary key of constraint
     * @return auths - list of the associated authorization instances, if any
     */
    public Authorization query(int attrId, int resourceId, int permissionId,
                               int constraintId) {

        Authorization auth = null;

        String hsql = "from Authorization where attrId = :attrId and " +
            "resourceId = :resourceId and " +
            "permissionId = :permissionId and " +
            "constraintId = :constraintId";
        auth = (Authorization) this.getSession().createQuery(hsql)
            .setInteger("attrId", attrId)
            .setInteger("resourceId", resourceId)
            .setInteger("permissionId", permissionId)
            .setInteger("constraintId", constraintId)
            .setMaxResults(1)
            .uniqueResult();

        return auth;
    }

    /**
     * Retrieves authorization, if any, based on presence of corresponding
     *     triplet in authorizations table.
     *
     * @param attrId int with primary key of attribute
     * @param resourceId int with primary key of resource
     * @param permissionId int with primary key of permission
     * @return auths - list of the associated authorization instances, if any
     */
    public List<Authorization> query(int attrId, int resourceId,
                                     int permissionId) {

        List<Authorization> auths = null;

        String hsql = "from Authorization where attrId = :attrId and " +
                     "resourceId = :resourceId and " +
                     "permissionId = :permissionId";
        auths = this.getSession().createQuery(hsql)
                      .setInteger("attrId", attrId)
                      .setInteger("resourceId", resourceId)
                      .setInteger("permissionId", permissionId)
                      .list();

        return auths;
    }

    /**
     * Add a new authorization given the attrValue, resourceName, permisionName
     *    constraintName and value.
     *
     *    @param attrValue String name of attribute
     *    @param resourceName String name of resource
     *    @param permissionName String name of permission
     *    @param constraintName String name of constraint, may be  null
     *    @param constraintValue String value of constraint, if null map to "true"
     */

    public void create(String attrValue, String resourceName,
            String permissionName, String constraintName,
            String constraintValue) throws AuthZException {

        // check for an already existing authorization
        Authorization auth =
            this.query(attrValue, resourceName, permissionName, constraintName);
        if (auth != null) {
            throw new AuthZException("duplicate entry");
        }
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        Attribute attr = attrDAO.queryByParam("value", attrValue);
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        Resource resource = resourceDAO.queryByParam("name", resourceName);
        PermissionDAO permDAO = new PermissionDAO(this.dbname);
        Permission permission = permDAO.queryByParam("name", permissionName);
        if (constraintName == null) {
            constraintName = "none";
        }
        ConstraintDAO constrDAO = new ConstraintDAO(this.dbname);
        Constraint constraint = constrDAO.queryByParam("name", constraintName);
        auth = new Authorization();
        auth.setAttribute(attr);
        auth.setResource(resource);
        auth.setPermission(permission);
        auth.setConstraint(constraint);
        auth.setConstraintValue(constraintValue);
        super.create(auth);
    }

    /**
     * Update an authorization given the attrValue, resourceName, permisionName
     *    constraintName and value.
     *
     *    @param attrValue String name of attribute
     *    @param resourceName String name of resource
     *    @param permissionName String name of permission
     *    @param constraintName String name of constraint, may be  null
     *    @param constraintValue String value of constraint, if null map to "true"
     */

    public void update(Authorization auth, String attrValue, String resourceName,
            String permissionName, String constraintName,
            String constraintValue) throws AuthZException {

        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        Attribute attr = attrDAO.queryByParam("value", attrValue);
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        Resource resource = resourceDAO.queryByParam("name", resourceName);
        PermissionDAO permDAO = new PermissionDAO(this.dbname);
        Permission permission = permDAO.queryByParam("name", permissionName);
        if (constraintName == null) {
            constraintName = "none";
        }
        ConstraintDAO constrDAO = new ConstraintDAO(this.dbname);
        Constraint constraint = constrDAO.queryByParam("name", constraintName);

        auth.setAttribute(attr);
        auth.setResource(resource);
        auth.setPermission(permission);
        auth.setConstraint(constraint);
        auth.setConstraintValue(constraintValue);
        super.update(auth);
    }

    /**
     * Remove an authorization given the attrValue, resourceName, permisionName
     *    constraintName and value.
     *
     *    @param attrValue String name of attribute
     *    @param resourceName String name of resource
     *    @param permissionName String name of permission
     *    @param constraintName String name of constraint, may be  null
     */

    public void remove(String attrValue, String resourceName,
            String permissionName, String constraintName) throws AuthZException {

        Authorization auth =
            this.query(attrValue, resourceName, permissionName, constraintName);
        if (auth == null) {
            throw new AuthZException(
                    "Trying to remove non-existent authorization");
        }
        super.remove(auth);
    }

    /**
     * Retrieves all authorizations in alphabetical order on attribute name,
     * resource name, permission name, and constraint name.
     *
     * @return auths An ordered list of authorization instances.
     */
    public List<Authorization> orderedList() {
        // the corresponding command in MySQL will return all the names
        // as well, but Hibernate just gets the authorizations
        String sql = "select * from authorizations a " +
            "inner join attributes attr on a.attrId = attr.id " +
            "inner join resources r on a.resourceId = r.id " +
            "inner join permissions p on a.permissionId = p.id " +
            "inner join constraints c on a.constraintId = c.id " +
            "order by attr.value, r.name, p.name, c.name";
        List<Authorization> auths =
            (List<Authorization>) this.getSession().createSQLQuery(sql)
                                                  .addEntity(Authorization.class)
                                                  .list();
        return auths;
    }

    /**
     * Retrieves authorizations for a given user if userName is given.
     *     Otherwise, all authorizations are returned.
     * TODO:  need alternative
     * @param userName A string containing a user name
     * @return auths A list of authorization instances
     * @throws AuthZException.
     */
    public List<Authorization> listAuthByUser(String userName)
            throws AuthZException {

        /* currently not called - designed for use by Web interface to manage
           authorizations */

        List<Authorization> auths = new ArrayList<Authorization>();
        /*
        User user = null;
        UserDAO userDAO = new UserDAO(this.dbname);

        if (userName != null) {
            user = userDAO.query(userName);
            if (user == null)  {
                throw new AuthZException(
                      "AuthorizationDAO.listAuthByUser: User not found " +
                      userName + ".");
            }
            UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
            List <UserAttribute> userAttrs =
                    userAttrDAO.getAttributesByUser(user.getId());
            if (userAttrs.isEmpty()) {
                throw new AuthZException(
                    "AuthorizationDAO.listAuthByUser no attributes for user " +
                    userName + ".");
            }

            AttributeDAO attrDAO = new AttributeDAO(dbname);
            Iterator attrIter = userAttrs.iterator();
            UserAttribute currentAttr;
            currentAttr = (UserAttribute) attrIter.next();

            try {
                while (true ) {
                    String attributeName = currentAttr.getAttribute().getName();
                    auths.addAll(listAuthByAttr(attributeName));
                    currentAttr= (UserAttribute) attrIter.next();
                }
            } catch ( NoSuchElementException ex) {
                // end of loop over all the attributes for this user
            }
            return auths;
        }
        */
        auths = super.list();
        return auths;
        }

    /**
     * Retrieves authorizations for a given attribute if attrValue is given.
     *     Otherwise, all authorizations are returned.
     *
     * @param attrValue A string containing an attribute name
     * @return auths A list of authorization instances
     * @throws AuthZException.
     */
    public List<Authorization> listAuthByAttr(String attrValue)
            throws AuthZException {

        List<Authorization> auths = null;
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);

        if (attrValue != null)  {
            Attribute attr = attrDAO.queryByParam("value",attrValue);
            if (attr == null)  {
                throw new AuthZException(
                        "AuthorizationDAO.listAuthByAttr: Attr not found " +
                        attrValue + ".");
            }

            String sql = "select * from authorizations a " +
                "inner join attributes attr on a.attrId = attr.id " +
                "inner join resources r on a.resourceId = r.id " +
                "inner join permissions p on a.permissionId = p.id " +
                "inner join constraints c on a.constraintId = c.id " +
                "where attr.id = ? " +
                "order by attr.value, r.name, p.name, c.name";
            auths =
                (List<Authorization>) this.getSession().createSQLQuery(sql)
                                                .addEntity(Authorization.class)
                                                .setInteger(0, attr.getId())
                                                .list();
            return auths;
        }
        auths = super.list();
        return auths;
    }

    /**
     * getConstraintName returns the constraint name for an authorization
     *
     * @param auth an Authorization instance
     * @return constraintName string with name of constraint
     *
     */
    public String getConstraintName(Authorization auth) {
        return auth.getConstraint().getName();
    }
}
