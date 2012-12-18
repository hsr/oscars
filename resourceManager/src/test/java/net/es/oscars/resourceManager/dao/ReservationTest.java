package net.es.oscars.resourceManager.dao;

import org.testng.annotations.*;

import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.*;

import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.resourceManager.beans.*;

import net.es.oscars.resourceManager.common.GlobalParams;
import net.es.oscars.resourceManager.stubserver.ApiTest;

/**
 * This class tests access to the reservations table, which requires a working
 *     Reservation.java and Reservation.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "rm", "reservationTest" })
public class ReservationTest {
    private final String DESCRIPTION = "ReservationTest reservation";
    private final String TOPOLOGY_IDENT = "ReservationTest id";
    private SessionFactory sf;
    private String dbname;
    private static Logger LOG = Logger.getLogger(ReservationTest.class.getName());
  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getTestDbName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void reservationDAOCreate() throws RMException {
      LOG.debug("starting resesrvationDAOCreate");
        // have to build everything by hand for DAO test
        Reservation resv = new Reservation();
        // this is just testing the bean, so any value will do, as long as it
        // is the correct type and required ones are non-null
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        CommonParams.setParameters(resv, DESCRIPTION);
        resv.setGlobalReservationId("es-net-test1");

        Path path = new Path();

        // set up MPLS data
        MPLSData mplsData = new MPLSData();
        mplsData.setBurstLimit(10000000L);
        mplsData.setLspClass("4");
        path.setMplsData(mplsData);

        // set up layer 2 data
        Layer2Data layer2Data = new Layer2Data();
        layer2Data.setSrcEndpoint(CommonParams.getSrcEndpoint());
        layer2Data.setDestEndpoint(CommonParams.getDestEndpoint());
        path.setLayer2Data(layer2Data);

        // set up layer 3 data (just testing Hibernate structures,
        // won't be both layer 2 and layer 3 data in real path)
        Layer3Data layer3Data = new Layer3Data();
        layer3Data.setSrcHost(CommonParams.getSrcHost());
        layer3Data.setDestHost(CommonParams.getDestHost());
        path.setLayer3Data(layer3Data);

        // create ingress element in path
        // a few interdepencies to take care of...
        PathElem ingressPathElem = new PathElem();
        ingressPathElem.setUrn(CommonParams.getSrcEndpoint());
        List<PathElem> pathElems = new ArrayList<PathElem>();
        pathElems.add(ingressPathElem);
        path.setPathElems(pathElems);
        LOG.debug("setting pathType to " + PathType.STRICT);
        path.setPathType(PathType.STRICT);
        path.setPathSetupMode("timer-automatic");
        path.setPriority(1);
        /* create userConstraint */
        StdConstraint constraint = new StdConstraint();
        CommonParams.setParameters(constraint,ConstraintType.USER);
        constraint.setPath(path);
        resv.setConstraint(constraint);
        LOG.debug("pathType set to " + resv.getConstraint(ConstraintType.USER).getPath().getPathType());
        dao.create(resv);
        this.sf.getCurrentSession().getTransaction().commit();
        LOG.debug("finish resesrvationDAOCreate");
    }

  @Test(dependsOnMethods={ "reservationDAOCreate" })
    public void reservationDAOQuery() {
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation reservation =
            dao.queryByParam("description", DESCRIPTION);
        assert reservation != null;
        StdConstraint uc = reservation.getConstraintMap().get(ConstraintType.USER);
        assert uc != null;
        Path path = uc.getPath();
        assert path != null;
        assert path.getPathType().equals(PathType.STRICT);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "reservationDAOCreate" })
    public void reservationDAOAuthList() throws RMException {
      LOG.debug("starting resesrvationDAOListAllUsers");

        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String login = CommonParams.getLogin();
        List<String> logins = null;
        try {
             // if null, list all reservations by all users
            reservations = dao.list(10, 0, logins, null, null, null, null,
                                    null);
        } catch (RMException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }

        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
        LOG.debug("finish resesrvationDAOListAllUsers");

    }

  @Test(dependsOnMethods={ "reservationDAOCreate" })
    public void reservationDAOUserList() throws RMException {
      LOG.debug("starting resesrvationDAOListLoginOnly");

        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<String> logins = new ArrayList<String>();
        String login = CommonParams.getLogin();
        logins.add(login);
        try {
            reservations = dao.list(10, 0, logins, null, null, null, null,
                                    null);
        } catch (RMException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
        LOG.debug("finish resesrvationDAOListAllUsers");

    }
  
  @Test(dependsOnMethods={ "reservationDAOQuery", "reservationDAOUserList",
                           "reservationDAOAuthList" })
    public void reservationDAORemove() {
      LOG.debug("starting resesrvationDAORemove");

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation reservation =
                (Reservation) dao.queryByParam("description", DESCRIPTION);
        dao.remove(reservation);
        this.sf.getCurrentSession().getTransaction().commit();
        LOG.debug("finish resesrvationDAORemove");
    }
}
