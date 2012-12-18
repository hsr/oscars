package net.es.oscars.authN.test.dao;


import java.util.List;
import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.authN.beans.Institution;
import net.es.oscars.authN.dao.InstitutionDAO;
import net.es.oscars.database.hibernate.HibernateUtil;

/**
 * This class tests access to the institutions table, which requires a working
 *     Institution.java and Institution.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authN", "institution" }, dependsOnGroups={ "create"})
public class InstitutionTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "testauthn";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void institutionQuery() {
        this.sf.getCurrentSession().beginTransaction();
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        Institution institution =
                (Institution) institutionDAO.queryByParam("name",
                                   CommonParams.getInstitutionName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert institution != null;
    }

  @Test
    public void institutionList() {
        this.sf.getCurrentSession().beginTransaction();
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        List<Institution> institutions = institutionDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !institutions.isEmpty();
    }
}
