package net.es.oscars.authZ.test.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.authZ.beans.Permission;
import net.es.oscars.authZ.dao.PermissionDAO;

/**
 * This class tests access to the permissions table, which requires a working
 *     Permission.java and Permission.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authz", "permission" }, dependsOnGroups={ "create" })
public class PermissionTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "testauthz";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void permissionQuery() {
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Permission permission =
                (Permission) permissionDAO.queryByParam("name",
                                    CommonParams.getPermissionName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert permission != null;
    }

  @Test
    public void permissionList() {
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Permission> perms = permissionDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !perms.isEmpty();
    }
}
