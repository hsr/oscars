package net.es.oscars.resourceManager.common;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;
import org.testng.annotations.*;

import net.es.oscars.database.hibernate.Initializer;
import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.utils.config.*;
import net.es.oscars.utils.svc.ServiceNames;

/**
 * These must be run before and after all tests in the suite.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "init" })
public class InitTest {
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_RM);
    private static String context = ConfigDefaults.CTX_TESTING;
 
    private static String username = null;
    private static String password = null;
    private static String dbname = null;
    private static String monitor = null;
    private static Logger log = null;


  @BeforeSuite
    public void setUpTests() {
      System.out.println("starting resourceManager tests");
      cc.setContext(context);
      cc.setServiceName(ServiceNames.SVC_RM);
      try {
          cc.loadManifest(new File("config/"+ConfigDefaults.MANIFEST));
          cc.setLog4j();
          // need to do this after the log4j.properties file has been set
          log = Logger.getLogger(InitTest.class);
          log.debug("starting tests");
          String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
          Map config = ConfigHelper.getConfiguration(configFile);
          assert config != null : "No configuration";
          Map rm = (Map) config.get("resourceManager");
          assert rm != null : "No resourceManager stanza in configuration";
          username = (String) rm.get("username");
          assert username != null : "No user name in configuration";
          password = (String) rm.get("password");
          assert password != null : "No password in configuration";
          dbname = (String) rm.get("dbname");
          assert dbname != null: "No dbname in configuration";
          monitor = (String) rm.get("monitor");
      } catch (ConfigException ex) {
          System.out.println("caught ConfigurationException " + ex.getMessage());
          System.exit(-1);
      }
      Initializer initializer = new Initializer();
      List<String> dbnames = new ArrayList<String>();
      dbnames.add(dbname);
      initializer.initDatabase(dbnames, username, password, monitor, ServiceNames.SVC_RM);
  }

  @AfterSuite
    public void teardownTests() {
        /* If these run, there are error messages in the hibernate log
         * each time the tests are run.  If these are commented out, the
         * error messages don't appear, but the number of aborted clients
         * increases in MySQL.. */
        HibernateUtil.closeSessionFactory("testrm");
    }
}
