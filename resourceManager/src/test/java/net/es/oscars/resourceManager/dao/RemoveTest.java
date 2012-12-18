package net.es.oscars.resourceManager.dao;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.beans.*;

import net.es.oscars.resourceManager.common.GlobalParams;

/**
 * This class tests removal of resource manager database entries
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "rm", "remove" }, dependsOnGroups={
    "mplsData", "layer2Data", "layer3Data", "pathElem", "path" ,"apiTest"})
public class RemoveTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void pathRemove() {
        this.sf.getCurrentSession().beginTransaction();
        // remove path by removing parent reservation
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation resv =
            (Reservation) dao.queryByParam("description", "path test");
        dao.remove(resv);
        // links created in pathCreate were deleted by cascade
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "pathRemove" })
  public void cascadingDeletedOptConstraint() {
      this.sf.getCurrentSession().beginTransaction();
      OptConstraintDAO optConstraintDAO = new OptConstraintDAO(this.dbname);
      OptConstraint constraint = (OptConstraint)
          optConstraintDAO.queryByParam("keyName", CommonParams.getConstraintCategory());
      this.sf.getCurrentSession().getTransaction().commit();
      // if cascading delete works with path testRemove, this will
      // be null
      assert constraint == null;
  }
  
  @Test(dependsOnMethods={ "pathRemove" })
    public void cascadingDeletedPathElem() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemDAO pathElemDAO = new PathElemDAO(this.dbname);
        String urn = CommonParams.getSrcEndpoint();
        PathElem ingressPathElem = (PathElem)
            pathElemDAO.queryByParam("urn", urn);
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with path testRemove, this will
        // be null
        assert ingressPathElem == null;
    }

  @Test(dependsOnMethods={ "pathRemove" })
    public void cascadingDeletedLayer2Data() {
        this.sf.getCurrentSession().beginTransaction();
        Layer2DataDAO layer2DataDAO = new Layer2DataDAO(this.dbname);
        Layer2Data layer2Data = (Layer2Data)
            layer2DataDAO.queryByParam("srcEndpoint",
                                       CommonParams.getSrcEndpoint());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with path testRemove, this will
        // be null
        assert layer2Data == null;
    }

  @Test(dependsOnMethods={ "pathRemove" })
    public void cascadingDeletedLayer3Data() {
        this.sf.getCurrentSession().beginTransaction();
        Layer3DataDAO layer3DataDAO = new Layer3DataDAO(this.dbname);
        Layer3Data layer3Data = (Layer3Data)
            layer3DataDAO.queryByParam("srcHost", CommonParams.getSrcHost());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with path testRemove, this will
        // be null
        assert layer3Data == null;
    }

  @Test(dependsOnMethods={ "pathRemove" })
    public void cascadingDeletedMplsData() {
        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO mplsDataDAO = new MPLSDataDAO(this.dbname);
        MPLSData mplsData = (MPLSData)
            mplsDataDAO.queryByParam("burstLimit",
                                     CommonParams.getMPLSBurstLimit());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with path testRemove, this will
        // be null
        assert mplsData == null;
    }
}
