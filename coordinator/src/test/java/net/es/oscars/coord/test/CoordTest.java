package net.es.oscars.coord.test;

import org.apache.log4j.Logger;
import org.testng.annotations.*;

import java.io.File;
import java.util.List;
import java.lang.Exception;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

@Test
public class CoordTest {
    private static boolean coordStarted = false;
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
    private static String context = ConfigDefaults.CTX_TESTING;
    private static Logger log = null;
    
    // TIMEOUT is the time in second for which the tests are going to wait for replies from the 
    // coordinator before assuming that it failed. Depending on the performance of the host, this
    // timeout may have to be raised to several minutes
    private static long TIMEOUT = (4 * 60);
    
    @BeforeSuite
    public void setUpTests() {
      OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
      netLogger.init(ModuleName.COORD,"0000");
      System.out.println("starting coordinator tests");
      cc.setContext(context);
      cc.setServiceName(ServiceNames.SVC_COORD);
      try {
          cc.loadManifest(new File("config/"+ConfigDefaults.MANIFEST));
          cc.setLog4j();
          // need to do this after the log4j.properties file has been set
          log = Logger.getLogger(CoordTest.class);
          log.debug(netLogger.start("setupTests"));
      } catch (ConfigException ex) {
          System.out.println("caught ConfigurationException " + ex.getMessage());
          System.exit(-1);
      }
  }
    public class TestException extends Exception {
        private static final long serialVersionUID = 1439115737928915954L;
        
        public TestException (String reason) {
            super (reason);
        }
        public TestException (Exception e) {
            super (e);
        }
    }
    
    public class TestFailureRequest extends CoordRequest <String,String> {
        private static final long serialVersionUID = 1L;
        private boolean failMode = false;
        
        public TestFailureRequest (String name, boolean failMode) {
            super (name, null);
            this.failMode = failMode;
        }

        public TestFailureRequest(String gri, String name, boolean failMode) {
            super (gri, name);
            this.failMode = failMode;
        }
        
        public void execute()  {
            StubAction action1 = new StubAction ("action1", this, this.failMode);
            this.add (action1);
            
            // CoordRequests must start the processing of the graph of CoordActions.
            super.process();
            
            // The CoordAction part of this CoordRequest is now executed itself.
            this.executed();
        }
        
    }
    
    private static void startCoordinator() {
        if (CoordTest.coordStarted) {
            return;
        }
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "startCoordinator";
        CoordTest.coordStarted = true;
        log.info(netLogger.start(event));
        Coordinator c;
        try {
            c = Coordinator.getInstance();
            c.start();
        } catch (OSCARSServiceException e) {
            log.error(netLogger.error(event,ErrSev.MAJOR, "Coordinator cannot be started."));
            e.printStackTrace();
        }
    }
     
    public void testFailure() throws TestException {
        CoordTest.startCoordinator();
        try {           
            TestFailureRequest failureRequest1 = new TestFailureRequest ("No failure1", false);
            failureRequest1.processAndWait(CoordTest.TIMEOUT);
            if (failureRequest1.getState() != CoordAction.State.PROCESSED) {
                throw new TestException ("testFailure without failure has failed.");
            }
            if (! failureRequest1.isFullyCompleted()) {
                throw new TestException ("testFailure without failure has failed. not fully completed.");
            }
            TestFailureRequest failureRequest2 = new TestFailureRequest ("No failure2", true);
            failureRequest2.processAndWait(CoordTest.TIMEOUT);
            if (failureRequest2.getState() != CoordAction.State.FAILED) {
                throw new TestException ("testFailure with failure has failed. Incorrect state: " + failureRequest2.getState());
            }            
            List<CoordAction> failedActions = failureRequest2.getFailedCoordActions();
            if ((failedActions == null) || (failedActions.size() == 0)) {
                throw new TestException ("testFailure with failure has failed. No action has failed.");
            }
            for (CoordAction<String,String> action : failedActions) {
                if (action.getState() != CoordAction.State.FAILED ) {
                    throw new TestException ("testFailure with failure has failed. action " + action.getName() + " has state " + action.getState());
                }
            }
            if (! failureRequest2.isFullyCompleted()) {
                throw new TestException ("testFailure with failure has failed. not fully completed.");
            }
            
        } catch (Exception e) {
            //e.printStackTrace();
            throw new TestException (e);
        }
    }
    
    
    public void testNoCoordRequest() throws TestException {
        CoordTest.startCoordinator();
        try {         
            // Single CoordAction
            StubAction action = new StubAction("action",null,false);
            action.processAndWait(CoordTest.TIMEOUT);
            if (action.getState() != CoordAction.State.PROCESSED) {
                throw new TestException ("testAction/single action has failed. state= " + action.getState());
            }
            if (! action.isFullyCompleted()) {
                throw new TestException ("testAction/single action has failed. not fully completed.");
            }
            // Two CoordAction's
            StubAction action1 = new StubAction("action1",null,false);
            StubAction action2 = new StubAction("action2",null,false);
            action1.add(action2);
            action1.processAndWait(CoordTest.TIMEOUT);
            if (action1.getState() != CoordAction.State.PROCESSED) {
                throw new TestException ("testAction/two actions has failed. state= " + action1.getState());
            } 
            if (! action1.isFullyCompleted()) {
                throw new TestException ("testAction/two actions has failed. not fully completed.");
            }
            
            // Two CoordAction's with failure
            StubAction action3 = new StubAction("action3",null,false);
            StubAction action4 = new StubAction("action4",null,true);
            action3.add(action4);
            action3.processAndWait(CoordTest.TIMEOUT);
            if (action3.getState() != CoordAction.State.PROCESSED) {
                throw new TestException ("testAction/two actions/failure has failed. state= " + action3.getState());
            }
            if (! action3.isFullyCompleted()) {
                throw new TestException ("testAction/two actions/failure has failed. not fully completed.");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw new TestException (e);
        }
    }
    /* the following method talks to the real ResoueceManager service which changed the
     *  rm data base
    
    public void testGenerateGRI() throws TestException {
        CoordTest.startCoordinator();
        try {         
            RMGenerateGRIAction genGRIAction = new RMGenerateGRIAction ("testGenerateGRI", null);
            genGRIAction.execute();
            String gri = genGRIAction.getResultData();
            System.out.println ("test GRI is: " + gri);
            if (gri == null) {
                throw new TestException ("Returned GRI is null");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw new TestException (e);
        }
    }
*/
    /* the following method talks to the real ResoueceManager service which changed the
     *  rm data base
    public void testCreateReservation() throws TestException {
        CoordTest.startCoordinator();
        try {         
            ResCreateContent createResvReq = new ResCreateContent();
            UserRequestConstraintType userConstraint = new UserRequestConstraintType();
            long currentTime = System.currentTimeMillis() / 1000;
            userConstraint.setStartTime(currentTime + 3600); // One hour after now.
            userConstraint.setEndTime(currentTime + 7200); // Two hours after now.
            userConstraint.setBandwidth(1); // in Mbps
            Layer2Info layer2Info = new Layer2Info();
            //layer2Info.setSrcEndpoint("urn:ogf:network:es.net:anl-mr1:TenGigabitEthernet3/3:*");
            //layer2Info.setDestEndpoint("urn:ogf:network:es.net:albu-sdn1:xe-1/3/0:*");
            layer2Info.setSrcEndpoint("urn:ogf:network:testdomain-2:node-2-1:port1:link1");
            layer2Info.setDestEndpoint("urn:ogf:network:testdomain-2:node-2-2:port1:link2");
            PathInfo pathInfo = new PathInfo ();
            pathInfo.setLayer2Info(layer2Info);
            CtrlPlanePathContent pathContent = new CtrlPlanePathContent();
            pathContent.setId("fakeID");
            pathInfo.setPathType("strict");
            pathInfo.setPath(pathContent);
            userConstraint.setPathInfo(pathInfo);
            createResvReq.setDescription("IDCTest");
            createResvReq.setUserRequestConstraint(userConstraint);
            
            String loginName = "testClient";            
            CreateReservationRequest createReservation = new CreateReservationRequest ("testCreateReservation",
                                                                                        loginName,
                                                                                        createResvReq);
            createReservation.processAndWait(40000); 
            
            if ( ! createReservation.isFullyCompleted()) {
                throw new TestException ("createReservation query has not completed.");
            }
            if ( createReservation.getState() == CoordAction.State.FAILED) {
                Exception ex = createReservation.getException();
                String exMsg = null;
                if (ex != null) {
                    exMsg = ex.getMessage();
                }
                throw new TestException ("createReservation query failed. Exception found: " + exMsg);
            }
            
        } catch (Exception e) {
            //e.printStackTrace();
            throw new TestException (e);
        }
    }
    */
}
