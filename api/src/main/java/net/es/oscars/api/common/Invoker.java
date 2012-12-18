package net.es.oscars.api.common;

import static java.util.Arrays.asList;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

@OSCARSNetLoggerize(moduleName = ModuleName.API)
public class Invoker {

    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_API);
    private static String context = ConfigDefaults.CTX_PRODUCTION;
    private static  Logger LOG = null;
    private static OSCARSNetLogger netLogger = null;

    /**
     * Main program to start OSCARSService 
     * @param args [-h, ?] for help
     *              [-c PRODUCTION | SDK | DEVELOPMENT | INITTEST ] for context, defaults to PRODUCTION
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {

        parseArgs( args );
        System.out.println("starting OSCARS service in context " + context);
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_API);
        try {
            System.out.println("loading manifest from ./config/"+ConfigDefaults.MANIFEST);
            cc.loadManifest(ServiceNames.SVC_API,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            LOG = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        String event = "Invoker";
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.API, "0000");
        LOG.info(netLogger.start(event,"Starting OSCARSService with context " + context));
        OSCARSIDC service = OSCARSIDC.getInstance();
        service.startService();
        LOG.info(netLogger.end(event, "OSCARSService started with context " + context));
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

