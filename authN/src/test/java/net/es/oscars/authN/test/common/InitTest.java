package net.es.oscars.authN.test.common;

import java.io.File;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

import net.es.oscars.authN.common.AuthNCore;
import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

/**
 * These must be run before and after all tests in the suite.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "init" })
public class InitTest {
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHN);
    private static String context = ConfigDefaults.CTX_TESTING;
    private static Logger log = null;

@SuppressWarnings("unchecked")
@BeforeSuite
public void setUpTests() {
    OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
    String event = "setupTests";
    netLogger.init(ModuleName.AUTHN,"0000");
    //System.out.println(netLogger.start(event));
    System.out.println("AuthN setupTests");
    cc.setContext(context);
    cc.setServiceName(ServiceNames.SVC_AUTHN);
    try {
      //System.out.println("loading manifest from ./config/"+ConfigDefaults.MANIFEST)
       cc.loadManifest(new File("config/"+ConfigDefaults.MANIFEST));
      //cc.loadManifestFromSrc(ServiceNames.SVC_AUTHN,  ConfigDefaults.MANIFEST); // manifest.yaml
      cc.setLog4j();
      // need to do this after the log4j.properties file has been set
      log = Logger.getLogger(InitTest.class);
    } catch (ConfigException ex) {
      System.out.println("caught ConfigurationException " + ex.getMessage());
      System.exit(-1);
    }
    AuthNCore core = AuthNCore.getInstance();
    log.debug(netLogger.end( event, "using database " + core.getDbname()));
  }

  @AfterSuite
    public void teardownTests() {
        HibernateUtil.closeSessionFactory("testauthn");
    }
}
