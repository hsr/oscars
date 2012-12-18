package net.es.oscars.authZ.common;

import static java.util.Arrays.asList;
import java.net.URL;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.authZ.http.AuthZSoapServer;
import net.es.oscars.authZ.http.policy.AuthZPolicySoapServer;

public class Invoker {
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHZ);
    private static String context = ConfigDefaults.CTX_PRODUCTION;

    /**
     * Main program to start AuthZService and AuthZPolicyService
     * @param args [-h, ?] for help
     *              [-c PRODUCTION | SDK | DEVELOPMENT | INITTEST ] for context, defaults to PRODUCTION
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        cc.setServiceName(ServiceNames.SVC_AUTHZ);
        Logger log = null;

        parseArgs( args );

        System.out.println("starting authZ service with context " + context);
        cc.setContext(context);

        try {
            cc.loadManifest(ServiceNames.SVC_AUTHZ,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            log = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.AUTHZ,"0000");
        String event = "invoke";
        log.info(netLogger.start(event, "starting authZ"));
        OSCARSSoapService.setSSLBusConfiguration(
                new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        AuthZSoapServer server = AuthZSoapServer.getInstance();
        server.startServer(false);
        log.info(netLogger.start(event,"authZ service started with context " + context));
        
        log.info(netLogger.start(event,"starting authZPolicy"));
        OSCARSSoapService.setSSLBusConfiguration(
                new URL("file:" + cc.getFilePath(ServiceNames.SVC_AUTHZ_POLICY,ConfigDefaults.CXF_SERVER)));
        AuthZPolicySoapServer policyServer = AuthZPolicySoapServer.getInstance();
        policyServer.startServer(false);
        log.info(netLogger.end(event,"authZPolicyServer started with context " + context));
    }
    public static void parseArgs(String args[])  throws java.io.IOException {

        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        OptionSpec<String> CONTEXT = parser.accepts("c", "context:INITTEST,DEVELOPMENT,SDK,PRODUCTION").withRequiredArg().ofType(String.class);
        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has(CONTEXT) ){
            context = options.valueOf(CONTEXT);
            if (!context.equals("UNITTEST") &&
                    !context.equals("SDK") &&
                    !context.equals("DEVELOPMENT") &&
                    !context.equals("PRODUCTION") ) 
            {
                System.out.println("unrecognized CONTEXT value: " + context);
                System.exit(-1);
            } 
        } 
   }
}
