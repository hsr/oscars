package net.es.oscars.resourceManager.dao;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.beans.Path;

import net.es.oscars.resourceManager.common.GlobalParams;

/**
 * This class tests methods in PathDAO.java, which requires a working
 *     Path.java and Path.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "rm", "path" }, dependsOnGroups={ "create" })
public class PathTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void pathQuery() {
        this.sf.getCurrentSession().beginTransaction();
        String urn = CommonParams.getSrcEndpoint();
        String sql = "select * from paths p " +
                     "inner join pathElems pe on p.id = pe.pathId " +
                     "where pe.urn = ?";

        Path path = (Path) this.sf.getCurrentSession().createSQLQuery(sql)
                                                    .addEntity(Path.class)
                                                    .setString(0, urn)
                                                    .setMaxResults(1)
                                                    .uniqueResult();
        this.sf.getCurrentSession().getTransaction().commit();
        assert path != null;
    }

  @Test
    public void pathList() {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO dao = new PathDAO(this.dbname);
        List<Path> paths = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !paths.isEmpty();
    }
}
