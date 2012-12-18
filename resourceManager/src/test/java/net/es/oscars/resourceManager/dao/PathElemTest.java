package net.es.oscars.resourceManager.dao;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.beans.PathElem;

import net.es.oscars.resourceManager.common.GlobalParams;

/**
 * This class tests access to the pathElems table, which requires a working
 *     PathElem.java and PathElem.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "rm", "pathElem" }, dependsOnGroups={ "create" })
public class PathElemTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void pathElemQuery() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemDAO dao = new PathElemDAO(this.dbname);
        String urn = CommonParams.getSrcEndpoint();
        PathElem pathElem = (PathElem)
            dao.queryByParam("urn", urn);
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathElem != null;
    }

  @Test
    public void pathElemList() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemDAO dao = new PathElemDAO(this.dbname);
        List<PathElem> pathElems = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !pathElems.isEmpty();
    }
}
