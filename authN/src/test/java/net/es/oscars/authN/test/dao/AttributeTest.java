package net.es.oscars.authN.test.dao;


import java.util.List;

import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.authN.dao.AttributeDAO;

/**
 * This class tests access to the attributes table, which requires a working
 *     Attribute.java and Attribute.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authN", "attribute" }, dependsOnGroups={ "create" })
public class AttributeTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "testauthn";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void attributeQuery() {
        AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Attribute attribute =
                (Attribute) attributeDAO.queryByParam("value",
                                    CommonParams.getAttributeValue());
        this.sf.getCurrentSession().getTransaction().commit();
        assert attribute != null;
    }

  @Test
    public void attributeList() {
        AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Attribute> attrs = attributeDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !attrs.isEmpty();
    }
}
