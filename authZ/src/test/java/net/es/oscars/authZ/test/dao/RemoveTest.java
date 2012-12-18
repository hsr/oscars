package net.es.oscars.authZ.test.dao;


import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.authZ.beans.Attribute;
import net.es.oscars.authZ.beans.Authorization;
import net.es.oscars.authZ.beans.Constraint;
import net.es.oscars.authZ.beans.Permission;
import net.es.oscars.authZ.beans.Resource;
import net.es.oscars.authZ.dao.AttributeDAO;
import net.es.oscars.authZ.dao.AuthorizationDAO;
import net.es.oscars.authZ.dao.ConstraintDAO;
import net.es.oscars.authZ.dao.PermissionDAO;
import net.es.oscars.authZ.dao.ResourceDAO;

/**
 * This class tests removal of authZ database entries.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authz" }, dependsOnGroups={ "resource", "permission", "authorization", "constraint" })
public class RemoveTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    public void setUpClass() {
        this.dbname = "testauthz";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void authorizationRemove() {
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Attribute attr =
                (Attribute) attrDAO.queryByParam("value",
                                     CommonParams.getAttributeValue());
        // remove an authorization
        // TODO:  change if add more than one authorization
        Authorization authorization =
            (Authorization) authDAO.queryByParam("attrId", attr.getId());
        authDAO.remove(authorization);
        authorization = (Authorization) authDAO.queryByParam("attrId",
                                             attr.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert authorization == null;
    }

  @Test(dependsOnMethods={ "authorizationRemove" })
    public void resourceRemove() {
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        // remove a resource
        Resource resource = (Resource) resourceDAO.queryByParam("name",
                                   CommonParams.getResourceName());
        resourceDAO.remove(resource);
        resource = (Resource) resourceDAO.queryByParam("name",
                                   CommonParams.getResourceName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert resource == null;
    }

  @Test(dependsOnMethods={ "authorizationRemove" })
    public void permissionRemove() {
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        // remove a permission
        Permission permission =
                (Permission) permissionDAO.queryByParam("name",
                                   CommonParams.getPermissionName());
        permissionDAO.remove(permission);
        permission = (Permission) permissionDAO.queryByParam("name",
                                   CommonParams.getPermissionName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert permission == null;
    }

  @Test(dependsOnMethods={ "authorizationRemove" })
    public void constraintRemove() {
        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        // remove a constraint
        Constraint constraint = (Constraint) constraintDAO.queryByParam("name",
                                   CommonParams.getConstraintName());
        constraintDAO.remove(constraint);
        constraint = (Constraint) constraintDAO.queryByParam("name",
                                   CommonParams.getConstraintName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert constraint == null;
    }

  @Test(dependsOnMethods={ "authorizationRemove" })
    public void attributeRemove() {
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        // remove an attribute
        Attribute attr = (Attribute) attrDAO.queryByParam("value",
                                   CommonParams.getAttributeValue());
        attrDAO.remove(attr);
        attr = (Attribute) attrDAO.queryByParam("value",
                                   CommonParams.getAttributeValue());
        this.sf.getCurrentSession().getTransaction().commit();
        assert attr == null;
    }
}
