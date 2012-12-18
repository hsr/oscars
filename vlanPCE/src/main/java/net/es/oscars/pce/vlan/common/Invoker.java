package net.es.oscars.pce.vlan.common;

import static java.util.Arrays.asList;
import java.net.URL;
import java.util.HashMap;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.log4j.Logger;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.vlan.VlanPCEServer;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

public class Invoker {
    
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PCE_VLAN);
    private static String context = ConfigDefaults.CTX_PRODUCTION;

/**
 * Main program to start Vlan PCE server
 * @param args [-h, ?] for help
 *              [-c PRODUCTION | SDK | DEVELOPMENT | INITTEST ] for context, defaults to PRODUCTION
 * @throws Exception
 * 
 * @author Andy Lake <andy@es.net>
 */

    public static void main(String[] args) throws Exception {
        Logger log = null;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(Invoker.class.getName(),"0000");
        parseArgs( args );
        System.out.println("starting vlan PCE service with context "+ context);
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_PCE_VLAN);
        try {
            cc.loadManifest(ServiceNames.SVC_PCE_VLAN,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            log = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        
        log.info(netLogger.start("invoker"));
        OSCARSSoapService.setSSLBusConfiguration(
                new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        VlanPCEServer pceServer = new VlanPCEServer();
        pceServer.startServer(false);
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        netLogProps.put("context", cc.getContext());
        log.info(netLogger.end("invoker", null, null, netLogProps));
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
