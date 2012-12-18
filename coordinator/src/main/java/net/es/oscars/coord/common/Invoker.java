package net.es.oscars.coord.common;

import static java.util.Arrays.asList;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.log4j.Logger;

public class Invoker {
    
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
    private static String context = ConfigDefaults.CTX_PRODUCTION;
    private static  Logger LOG = null;
    
    /**
     * Main program to start CoordinatorService 
     * @param args [-h, ?] for help
     *              [-c PRODUCTION | SDK | DEVELOPMENT | INITTEST ] for context, defaults to PRODUCTION
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        parseArgs( args );
        System.out.println("starting Coordinator service in context " + context);
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_COORD);
        try {
            cc.loadManifest(ServiceNames.SVC_COORD,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            LOG = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.COORD,"0000");
        // Start Coordinator Engine
        Coordinator coordinator;
        try {
            LOG.info(netLogger.start("invoker"));
            coordinator = Coordinator.getInstance();
            coordinator.start();
            LOG.info (netLogger.end("invoker", "Service has started with context " + context));
        } catch (OSCARSServiceException e) {
            LOG.error(netLogger.error("invoker", ErrSev.MAJOR, "caught OSCARServiceException " + e.getMessage()));
            System.exit(-2);
        }

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
