package net.es.oscars.authZ.test.dao;


import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

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
import net.es.oscars.database.hibernate.HibernateUtil;

/**
 * This class tests creation of authZ database objects.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authz", "create" })
public class CreateTest {
    private final String FIRST_NAME = "test";
    private final String LAST_NAME = "suite";
    private final String EMAIL_PRIMARY = "user@yourdomain.net";
    private final String PHONE_PRIMARY = "777-777-7777";
    private final String PHONE_SECONDARY = "888-888-8888";
    private final String STATUS = "active";
    private final String DESCRIPTION = "test user";

    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    public void setUpClass() {
        this.dbname = "testauthz";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void attributeCreate() {
        Attribute attribute = new Attribute();
        String attrValue = CommonParams.getAttributeValue();
        String attrId = CommonParams.getAttributeId();
        attribute.setAttrId(attrId);
        attribute.setValue(attrValue);
        attribute.setDescription("test attribute");
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        attrDAO.create(attribute);
        this.sf.getCurrentSession().getTransaction().commit();
        assert attribute.getId() != null;
        assert attribute.getValue() != null;
    }

  @Test
    public void permissionCreate() {
        Permission permission = new Permission();
        permission.setName(CommonParams.getPermissionName());
        permission.setDescription("test permission");
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        permissionDAO.create(permission);
        this.sf.getCurrentSession().getTransaction().commit();
        assert permission.getId() != null;
        assert permission.getName() != null;
    }

  @Test
    public void resourceCreate() {
        Resource resource = new Resource();
        resource.setName(CommonParams.getResourceName());
        resource.setDescription("test resource");
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        resourceDAO.create(resource);
        this.sf.getCurrentSession().getTransaction().commit();
        assert resource.getId() != null;
        assert resource.getName() != null;
    }

  @Test
    public void constraintCreate() {
        Constraint constraint = new Constraint();
        constraint.setName(CommonParams.getConstraintName());
        constraint.setType("role");
        constraint.setDescription("test constraint");
        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        constraintDAO.create(constraint);
        this.sf.getCurrentSession().getTransaction().commit();
        assert constraint.getId() != null;
        assert constraint.getName() != null;
    }

  @Test(dependsOnMethods={ "attributeCreate", "permissionCreate",
                           "resourceCreate", "constraintCreate" })
    public void authorizationCreate() {
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        String attrValue = CommonParams.getAttributeValue();
        String resourceName = CommonParams.getResourceName();
        String permissionName = CommonParams.getPermissionName();
        String constraintName = CommonParams.getConstraintName();
        Authorization auth = new Authorization();
        this.sf.getCurrentSession().beginTransaction();
        Attribute attr = (Attribute) attrDAO.queryByParam("value", attrValue);
        Permission permission = (Permission) permissionDAO.queryByParam("name", permissionName);
        Resource resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        Constraint constraint = (Constraint) constraintDAO.queryByParam("name", constraintName);
        auth.setAttribute(attr);
        auth.setResource(resource);
        auth.setPermission(permission);
        auth.setConstraint(constraint);
        authDAO.create(auth);
        this.sf.getCurrentSession().getTransaction().commit();
        assert auth.getId() != null;
    }
}
