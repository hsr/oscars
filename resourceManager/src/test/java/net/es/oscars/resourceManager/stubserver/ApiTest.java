package net.es.oscars.resourceManager.stubserver;

import org.testng.annotations.*;

import java.util.*;
import org.apache.log4j.Logger;
import org.hibernate.*;

import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.Layer3Info;
import net.es.oscars.api.soap.gen.v06.MplsInfo;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.VlanTag;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.beans.*;
import net.es.oscars.resourceManager.dao.*;
import net.es.oscars.resourceManager.common.RMCore;
import net.es.oscars.resourceManager.common.ResourceManager;
import net.es.oscars.resourceManager.common.ScanReservations;
import net.es.oscars.resourceManager.common.StateEngine;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.soap.OSCARSServiceException;

import net.es.oscars.resourceManager.common.GlobalParams;

/**
 * This class tests access to the reservations table, which requires a working
 *     Reservation.java and Reservation.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "rm", "apiTest" }, dependsOnGroups={"create" })
public class ApiTest {
    private final String DESCRIPTION = "api test";
    private final String TOPOLOGY_IDENT = "ReservationTest id";
    private SessionFactory sf;
    private String dbname;
    private String GRI;
    private static Logger LOG = Logger.getLogger(ApiTest.class.getName());

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
       // this.core = RMCore.getInstance();
    }

/* tests the ResourceMananger store function */
  @Test 
    public void reservationStore() throws OSCARSServiceException, RMException {
      LOG.debug("\nstarting api tests");
      LOG.debug("store reservation");

        ResDetails resDetails = new ResDetails ();
        UserRequestConstraintType uc = new UserRequestConstraintType();
        PathInfo pathInfo = new PathInfo();
        this.sf.getCurrentSession().beginTransaction();
        ResourceManager mgr = new ResourceManager(this.dbname);

        CommonParams.setParameters(resDetails, DESCRIPTION);
        CommonParams.setParameters(uc);
        /**
         * Generates the next Global Resource Identifier, created from the local
         *  Domain's topology identifier and the next unused index in the IdSequence table.
        */
        GRI = mgr.generateGRI();
        resDetails.setGlobalReservationId(GRI);

        // set up MPLS Info
        MplsInfo mplsInfo = new MplsInfo();
        mplsInfo.setBurstLimit(10000000);
        mplsInfo.setLspClass("4");
        pathInfo.setMplsInfo(mplsInfo);

        // set up layer 2 Info
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(CommonParams.getSrcEndpoint());
        layer2Info.setDestEndpoint(CommonParams.getDestEndpoint());
        pathInfo.setLayer2Info(layer2Info);

        // set up layer 3 Info (just testing Hibernate structures,
        // won't be both layer 2 and layer 3 Info in real path)
        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(CommonParams.getSrcHost());
        layer3Info.setDestHost(CommonParams.getDestHost());
        pathInfo.setLayer3Info(layer3Info);

        // create PathContent consisting of the ingress and egress
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        path.setId("userPath");//id doesn't matter in this context
        CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
        hop.setId("1");
        hop.setLinkIdRef(CommonParams.getSrcEndpoint());
        path.getHop().add(hop);
        hop.setId("2");
        hop.setLinkIdRef(CommonParams.getDestEndpoint());
        path.getHop().add(hop);
        pathInfo.setPath(path);
        pathInfo.setPathType(PathType.LOOSE);
        pathInfo.setPathSetupMode("timer-automatic");
        uc.setPathInfo(pathInfo);
        resDetails.setUserRequestConstraint(uc);
        mgr.store(resDetails);
        this.sf.getCurrentSession().getTransaction().commit();

    }
  
  /* tests the resourceManager's implementation of storing over an existing reservation 
   * Change status to "PENDING" and add a reservedConstraint 
   * */
  @Test( dependsOnMethods={ "reservationStore" })
  public void reservationStore2() throws OSCARSServiceException, RMException {

      LOG.debug("second store reservation");

        ResDetails resDetails = new ResDetails ();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo = new PathInfo();
        this.sf.getCurrentSession().beginTransaction();
        ResourceManager mgr = new ResourceManager(this.dbname);

       CommonParams.setParameters(rc); // changes the start and end time
        
        /* change status so that scan reservations will find it */
        resDetails.setStatus(StateEngineValues.RESERVED);
        resDetails.setGlobalReservationId(this.GRI);

        // set up MPLS Info
        MplsInfo mplsInfo = new MplsInfo();
        mplsInfo.setBurstLimit(10000000);
        mplsInfo.setLspClass("4");
        pathInfo.setMplsInfo(mplsInfo);

        // set up layer 2 Info
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(CommonParams.getSrcEndpoint());
        layer2Info.setDestEndpoint(CommonParams.getDestEndpoint());
        pathInfo.setLayer2Info(null);

        // create PathContent consisting of the ingress and egress
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        path.setId("userPath");//id doesn't matter in this context
        CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
        hop.setId("1");
        hop.setLinkIdRef(CommonParams.getSrcEndpoint());
        path.getHop().add(hop);
        hop.setId("2");
        hop.setLinkIdRef(CommonParams.getDestEndpoint());
        path.getHop().add(hop);
        pathInfo.setPath(path);
        pathInfo.setPathType(PathType.STRICT);
        pathInfo.setPathSetupMode("timer-automatic");
        rc.setPathInfo(pathInfo);
        resDetails.setReservedConstraint(rc);
        mgr.store(resDetails);
        this.sf.getCurrentSession().getTransaction().commit();
  }
  
  /* tests the resourceManager's implementation of limit the list by linkId */
  @Test( dependsOnMethods={ "reservationStore" })
    public void reservationLinkIdList() throws OSCARSServiceException {
        LOG.debug("reservationLinkIdList");
        List<ResDetails> reservations = null;
        AuthConditions authCond = new AuthConditions();
        this.sf.getCurrentSession().beginTransaction();
        ResourceManager mgr = new ResourceManager(this.dbname);
        List<String> linkIds = new ArrayList<String>();
        linkIds.add(CommonParams.getSrcEndpoint());
        try {
            /*list(authConditions, numRequested, offset, 
            List<String> statuses, String description, Strin userName, List<String> linkIds,
            List<String> vlanTags,  Long startTime, Long endTime) */
            reservations = mgr.list(authCond,0,0, null, null, null, linkIds, null, null, null);
 
        } catch (OSCARSServiceException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }

        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  /* test the scanning of reservations for setup and teardown actions */
  @Test(dependsOnMethods ={ "reservationStore2" })
    public void scanReservations() throws RMException, SchedulerException {
      LOG.debug("scanning reservations in database " + this.dbname);

      // the following in needed because we have a different dbname than is in RMcore
      ScanReservations scanner = ScanReservations.getInstance(this.dbname);
      scanner.scan();
      LOG.debug("finished scanning reservations");
  }
  
/* test the mapping of Reservation to ResDetails by the ResourceManager */
  @Test( dependsOnMethods={ "reservationStore2" })
    public void reservationQuery() throws RMException {
        LOG.debug("reservationQuery");
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation reservation =
            dao.queryByParam("description", DESCRIPTION);
        assert reservation != null;
        AuthConditions authCond = new AuthConditions();
        ResourceManager mgr = new ResourceManager(this.dbname);
        try {
            ResDetails resDetails = mgr.query(authCond, reservation.getGlobalReservationId());
            assert resDetails.getGlobalReservationId().equals(reservation.getGlobalReservationId());
            assert resDetails.getDescription().equals(DESCRIPTION);
            assert resDetails.getLogin().equals(reservation.getLogin());
            assert resDetails.getCreateTime() == reservation.getCreatedTime();
            UserRequestConstraintType constraint = resDetails.getUserRequestConstraint();
            assert constraint != null;
            assert constraint.getBandwidth() == CommonParams.getBandwidth()/ 1000000L;
            PathInfo pathInfo = null;
            ReservedConstraintType resConstraint = resDetails.getReservedConstraint();
            if (resConstraint != null) {
                pathInfo = resConstraint.getPathInfo();
            } else {
                pathInfo = constraint.getPathInfo();
            }
            assert pathInfo != null;
            if (resDetails.getStatus().equals("COMMITTED")){
                assert pathInfo.getPathType().equals(PathType.LOOSE);
            } else {
                assert pathInfo.getPathType().equals(PathType.STRICT);
            }
        }catch (Exception ex) {
            throw new RMException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
  
  /* remove the reservation that was created  so that it doesn't interfere with removeTest*/
  @Test( dependsOnMethods={ "reservationStore", "reservationStore2","reservationLinkIdList", "reservationQuery",
          "scanReservations"})
      public void reservationDelete() throws RMException {
      LOG.debug("reservationDelete");
      this.sf.getCurrentSession().beginTransaction();
      ReservationDAO dao = new ReservationDAO(this.dbname);
      Reservation resv =
          (Reservation) dao.queryByParam("description", DESCRIPTION);
      dao.remove(resv);
      this.sf.getCurrentSession().getTransaction().commit();
  }
}
