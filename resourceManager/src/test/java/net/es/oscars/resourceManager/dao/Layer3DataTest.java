package net.es.oscars.resourceManager.dao;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.beans.Layer3Data;

import net.es.oscars.resourceManager.common.GlobalParams;


/**
 * This class tests access to the Layer3Data table, which requires a working
 *     Layer3Data.java and Layer3Data.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "rm", "layer3Data" },
               dependsOnGroups={ "create" })
public class Layer3DataTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void  layer3DataQuery() {
        this.sf.getCurrentSession().beginTransaction();
        Layer3DataDAO dao = new Layer3DataDAO(this.dbname);
        Layer3Data layer3Data = (Layer3Data)
            dao.queryByParam("srcHost", CommonParams.getSrcHost());
        this.sf.getCurrentSession().getTransaction().commit();
        assert layer3Data != null;
    }

  @Test
    public void layer3DataList() {
        this.sf.getCurrentSession().beginTransaction();
        Layer3DataDAO dao = new Layer3DataDAO(this.dbname);
        List<Layer3Data> layer3Data = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !layer3Data.isEmpty();
    }
}
