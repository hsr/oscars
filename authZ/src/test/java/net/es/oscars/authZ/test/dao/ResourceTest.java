package net.es.oscars.authZ.test.dao;


import java.util.List;

import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.authZ.beans.Resource;
import net.es.oscars.authZ.dao.ResourceDAO;

/**
 * This class tests access to the resources table, which requires a working
 *     Resource.java and Resource.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authz", "resource" }, dependsOnGroups={ "create" })
public class ResourceTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "testauthz";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void resourceQuery() {
        String rname = "Users";

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Resource resource = (Resource) resourceDAO.queryByParam("name",
                                     CommonParams.getResourceName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert resource != null;
    }

  @Test
    public void resourceList() {
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Resource> resources = resourceDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !resources.isEmpty();
    }
}
