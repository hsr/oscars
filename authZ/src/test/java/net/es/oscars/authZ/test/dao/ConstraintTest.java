package net.es.oscars.authZ.test.dao;


import java.util.List;

import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.authZ.beans.Constraint;
import net.es.oscars.authZ.dao.ConstraintDAO;

/**
 * This class tests access to the constraints table, which requires a working
 *     Constraint.java and Constraint.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authz", "constraint" }, dependsOnGroups={ "create" })
public class ConstraintTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "testauthz";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void constraintQuery() {
        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Constraint constraint =
                (Constraint) constraintDAO.queryByParam("name",
                                    CommonParams.getConstraintName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert constraint != null;
    }

  @Test
    public void constraintList() {
        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Constraint> constraints = constraintDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !constraints.isEmpty();
    }
}
