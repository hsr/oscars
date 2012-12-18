package net.es.oscars.resourceManager.dao;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.beans.MPLSData;

import net.es.oscars.resourceManager.common.GlobalParams;

/**
 * This class tests access to the MPLSData table, which requires a working
 *     MPLSData.java and MPLSData.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "rm", "mplsData" }, dependsOnGroups={ "create" })
public class MPLSDataTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void  mplsDataQuery() {
        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO dao = new MPLSDataDAO(this.dbname);
        MPLSData mplsData = (MPLSData)
            dao.queryByParam("burstLimit", CommonParams.getMPLSBurstLimit());
        this.sf.getCurrentSession().getTransaction().commit();
        assert mplsData != null;
    }

  @Test
    public void mplsDataList() {
        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO dao = new MPLSDataDAO(this.dbname);
        List<MPLSData> mplsData = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !mplsData.isEmpty();
    }
}
