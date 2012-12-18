package net.es.oscars.authZ.test.dao;


import java.util.List;
import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.authZ.beans.Attribute;
import net.es.oscars.authZ.beans.Authorization;
import net.es.oscars.authZ.beans.Constraint;
import net.es.oscars.authZ.beans.Permission;
import net.es.oscars.authZ.beans.Resource;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.authZ.common.AuthZException;
import net.es.oscars.authZ.dao.AttributeDAO;
import net.es.oscars.authZ.dao.AuthorizationDAO;
import net.es.oscars.authZ.dao.ConstraintDAO;
import net.es.oscars.authZ.dao.PermissionDAO;
import net.es.oscars.authZ.dao.ResourceDAO;


/**
 * This class tests methods in AuthorizationDAO.java, which requires a working
 *     Authorization.java and Authorization.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authz", "authorization" }, dependsOnGroups={ "create" })
public class AuthorizationTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    public void setUpClass() {
        this.dbname = "testauthz";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void authorizationQuery() throws AuthZException {
        Attribute attr = null;
        Resource resource = null;
        Permission permission = null;

        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        String attrValue = CommonParams.getAttributeValue();
        String resourceName = CommonParams.getResourceName();
        String permissionName = CommonParams.getPermissionName();

        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        attr = (Attribute) attrDAO.queryByParam("value", attrValue);
        if (attr == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthZException("Attribute " + attrValue + " does not exist");
        }

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        if (resource == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthZException("Resource " + resourceName + " does not exist");
        }

        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        permission = (Permission)
                permissionDAO.queryByParam("name", permissionName);
        if (permission == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthZException("permission " + permissionName + " does not exist");
        }

        List<Authorization> auths =
            authDAO.query(attr.getId(), resource.getId(), permission.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert !auths.isEmpty();
    }

  @Test
    public void authorizationQueryByName() throws AuthZException {

        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        String attrValue = CommonParams.getAttributeValue();
        String resourceName = CommonParams.getResourceName();
        String permissionName = CommonParams.getPermissionName();
        String constraintName = CommonParams.getConstraintName();

        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Attribute attr = (Attribute) attrDAO.queryByParam("value", attrValue);
        if (attr == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthZException("Attribute " + attrValue + " does not exist");
        }

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        Resource resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        if (resource == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthZException("Resource " + resourceName + " does not exist");
        }

        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        Permission permission = (Permission)
                permissionDAO.queryByParam("name", permissionName);
        if (permission == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthZException("permission " + permissionName + " does not exist");
        }

        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        Constraint constraint = (Constraint)
                constraintDAO.queryByParam("name", constraintName);
        if (constraint == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthZException("constraint " + constraintName + " does not exist");
        }

        Authorization auth = authDAO.query(attr.getValue(), resource.getName(),
                permission.getName(), constraint.getName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert auth != null;
    }

  @Test
    public void authorizationList() throws AuthZException {

        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Authorization> auths = authDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !auths.isEmpty();
    }

  /*
  @Test
    public void authorizationListByUser() throws AuthZException {
        String login = CommonParams.getLogin();
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Authorization> auths = authDAO.listAuthByUser(login);
        this.sf.getCurrentSession().getTransaction().commit();
        assert !auths.isEmpty();
    }
  */

  @Test
    public void authorizationListByAttribute() throws AuthZException {
        String attrValue = CommonParams.getAttributeValue();
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Authorization> auths = authDAO.listAuthByAttr(attrValue);
        this.sf.getCurrentSession().getTransaction().commit();
        assert !auths.isEmpty();
    }

  @Test(dependsOnMethods={ "authorizationQueryByName" })
    public void authorizationGetConstraintName() throws AuthZException {
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        String attrValue = CommonParams.getAttributeValue();
        String resourceName = CommonParams.getResourceName();
        String permissionName = CommonParams.getPermissionName();
        String constraintName = CommonParams.getConstraintName();

        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Attribute attr = (Attribute) attrDAO.queryByParam("value", attrValue);

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        Resource resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        Permission permission = (Permission)
                permissionDAO.queryByParam("name", permissionName);

        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        Constraint constraint = (Constraint)
                constraintDAO.queryByParam("name", constraintName);

        Authorization auth = authDAO.query(attr.getValue(), resource.getName(),
                permission.getName(), constraint.getName());
        String authConstraintName = authDAO.getConstraintName(auth);
        this.sf.getCurrentSession().getTransaction().commit();
        assert authConstraintName.equals(constraintName);
    }
}
