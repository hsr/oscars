package net.es.oscars.resourceManager.dao;

import org.testng.annotations.*;

import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.*;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.resourceManager.common.GlobalParams;
import net.es.oscars.resourceManager.dao.CommonParams;
import net.es.oscars.resourceManager.beans.*;

/**
 * This class tests creating RM database entries having to do with paths.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */

@Test(groups={ "rm", "create" })
public class CreateTest {
    private SessionFactory sf;
    private String dbname;
    private static Logger LOG = Logger.getLogger(CreateTest.class.getName());

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  // Sets up all path structure in this method to test cascading save
  // as well as path creation.  Note that path has to be created as part
  // of reservation.
  @Test
    public void pathCreate() throws RMException {
      LOG.debug("starting createTest:pathCreate");

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO reservationDAO = new ReservationDAO(this.dbname);
        Reservation resv = new Reservation();
        CommonParams.setParameters(resv, "path test");
        resv.setGlobalReservationId("es-net-test2");
        Path path = new Path();

        // set up MPLS data
        MPLSData mplsData = new MPLSData();
        mplsData.setBurstLimit(CommonParams.getMPLSBurstLimit());
        mplsData.setLspClass("4");
        path.setMplsData(mplsData);

        // set up layer 2 data
        Layer2Data layer2Data = new Layer2Data();
        layer2Data.setSrcEndpoint(CommonParams.getSrcEndpoint());
        layer2Data.setDestEndpoint(CommonParams.getDestEndpoint());
        path.setLayer2Data(layer2Data);

        // set up layer 3 data (just testing Hibernate structures,
        // won't be both layer 2 and layer 3 data in real path
        Layer3Data layer3Data = new Layer3Data();
        layer3Data.setSrcHost(CommonParams.getSrcHost());
        layer3Data.setDestHost(CommonParams.getDestHost());
        path.setLayer3Data(layer3Data);

        List<PathElem> pathElems = new ArrayList<PathElem>();

        // create ingress and egress elements in path
        PathElem ingressPathElem = new PathElem();
        PathElemParam pathElemParam = new PathElemParam();
        pathElemParam.setSwcap("test");
        pathElemParam.setType("test");
        pathElemParam.setValue("test");
        ingressPathElem.addPathElemParam(pathElemParam);
        ingressPathElem.setUrn(CommonParams.getSrcEndpoint());
        pathElems.add(ingressPathElem);

        PathElem egressPathElem = new PathElem();
        egressPathElem.setUrn(CommonParams.getDestEndpoint());
        pathElems.add(egressPathElem);

        path.setPathElems(pathElems);
        /* create userConstraint */
        StdConstraint constraint = new StdConstraint();
        CommonParams.setParameters(constraint,ConstraintType.USER);
        constraint.setPath(path);
        resv.setConstraint(constraint);
        OptConstraint optConstraint = new OptConstraint();
        optConstraint.setKeyName(CommonParams.getConstraintCategory());
        optConstraint.setValue("this should be an xml string");
        resv.addOptConstraint(optConstraint);
        reservationDAO.create(resv);
        this.sf.getCurrentSession().getTransaction().commit();
        assert path.getId() != null;
        LOG.debug("finish createTest:pathCreate");

    }
 @Test(dependsOnMethods={ "pathCreate" })
     public void cascadingSavedOptConstraint(){
         this.sf.getCurrentSession().beginTransaction();
         OptConstraintDAO constraintDAO = new OptConstraintDAO(this.dbname);
         OptConstraint constraint = (OptConstraint)
             constraintDAO.queryByParam("keyName",CommonParams.getConstraintCategory());
         this.sf.getCurrentSession().getTransaction().commit();
         // if cascading save worked with Reservation create, this will
         // not be null
         assert constraint != null;
 }
 
 @Test(dependsOnMethods={ "pathCreate" })
    public void cascadingSavedLayer2Data() {
        this.sf.getCurrentSession().beginTransaction();
        Layer2DataDAO layer2DataDAO = new Layer2DataDAO(this.dbname);
        Layer2Data layer2Data = (Layer2Data)
            layer2DataDAO.queryByParam("srcEndpoint",
                                       CommonParams.getSrcEndpoint());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading save worked with path create, this will
        // not be null
        assert layer2Data != null;
    }

  @Test(dependsOnMethods={ "pathCreate" })
    public void cascadingSavedLayer3Data() {
        this.sf.getCurrentSession().beginTransaction();
        Layer3DataDAO layer3DataDAO = new Layer3DataDAO(this.dbname);
        Layer3Data layer3Data = (Layer3Data)
            layer3DataDAO.queryByParam("srcHost",
                                       CommonParams.getSrcHost());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading save worked with path create, this will
        // not be null
        assert layer3Data != null;
    }


  @Test(dependsOnMethods={ "pathCreate" })
    public void cascadingSavedMPLSData() {
      LOG.debug("starting createTest:cacasdingSavedMPLS");

        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO mplsDataDAO = new MPLSDataDAO(this.dbname);
        MPLSData mplsData = (MPLSData)
            mplsDataDAO.queryByParam("burstLimit",
                                     CommonParams.getMPLSBurstLimit());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading save worked with path create, this will
        // not be null
        assert mplsData != null;
        LOG.debug("finish createTest:cacasdingSavedMPLS");

    }
}
