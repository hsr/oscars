package net.es.oscars.authN.common;

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
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.authN.http.AuthNSoapServer;
import net.es.oscars.authN.http.policy.AuthNPolicySoapServer;

public class Invoker {


    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHN);
    private static String context = ConfigDefaults.CTX_PRODUCTION;

    /**
     * Main program to start AuthNService and AuthNPolicyService
     * @param args [-h, ?] for help
     *              [-c PRODUCTION | DEVELOPMENT | TESTING ] for context, defaults to PRODUCTION
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        cc.setServiceName(ServiceNames.SVC_AUTHN);
        Logger log =null;
        parseArgs( args);

        System.out.println("starting authN service with context " + context);
        cc.setContext(context);
        try {
            cc.loadManifest(ServiceNames.SVC_AUTHN,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            log = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "invoke";
        netLogger.init(ModuleName.AUTHN,"0000");
        log.info(netLogger.start(event, "starting authN with context " + context));
        OSCARSSoapService.setSSLBusConfiguration(
                new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        AuthNSoapServer server = AuthNSoapServer.getInstance();
        server.startServer(false);
        log.info(netLogger.end(event, "AuthN started with context is " + context));
        
        log.info(netLogger.start(event, "starting authNPolicy with context " + context));
        OSCARSSoapService.setSSLBusConfiguration(
                new URL("file:" + cc.getFilePath(ServiceNames.SVC_AUTHN_POLICY,ConfigDefaults.CXF_SERVER)));
        AuthNPolicySoapServer policyServer = AuthNPolicySoapServer.getInstance();
        policyServer.startServer(false);
        log.info(netLogger.end(event,"authNPolicy started with context " + context));
    }
    
    public static void parseArgs(String args[])  throws java.io.IOException {

        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        OptionSpec<String> CONTEXT = parser.accepts("c", "context:UNITTEST,DEVELOPMENT,SDK,PRODUCTION").withRequiredArg().ofType(String.class);
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
