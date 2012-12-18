package net.es.oscars.resourceManager.dao;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.beans.PathElem;
import net.es.oscars.resourceManager.beans.PathElemParam;

import net.es.oscars.resourceManager.common.GlobalParams;

/**
 * This class tests access to the pathElemParams table, which requires a working
 *     PathElemParam.java and PathElemParam.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "rm", "pathElemParam" }, dependsOnGroups={ "create" })
public class PathElemParamTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void pathElemParamQuery() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemDAO dao = new PathElemDAO(this.dbname);
        PathElem pathElem = (PathElem)
            dao.queryByParam("urn", CommonParams.getSrcEndpoint());
        PathElemParamDAO peParamDAO = new PathElemParamDAO(this.dbname);
        PathElemParam pathElemParam = (PathElemParam)
            peParamDAO.queryByParam("pathElemId", pathElem.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathElemParam != null;
    }

  @Test
    public void pathElemParamList() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemParamDAO dao = new PathElemParamDAO(this.dbname);
        List<PathElemParam> pathElemParams = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !pathElemParams.isEmpty();
    }
}
