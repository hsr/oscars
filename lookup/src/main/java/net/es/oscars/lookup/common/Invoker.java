package net.es.oscars.lookup.common;

import static java.util.Arrays.asList;

import net.es.oscars.lookup.http.LookupSoapServer;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import java.net.URL;

import org.apache.log4j.Logger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class Invoker {
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_LOOKUP);
    private static String context = ConfigDefaults.CTX_PRODUCTION;

    /**
     * Main program to start LookupService 
     * @param args [-h, ?] for help
     *              [-c PRODUCTION | SDK | DEVELOPMENT | INITTEST ] for context, defaults to PRODUCTION
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Logger log = null;

        parseArgs( args );
        System.out.println("starting Lookup service in context " + context);
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_LOOKUP);
        try {
            cc.loadManifest(ServiceNames.SVC_LOOKUP,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            log = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        
        OSCARSSoapService.setSSLBusConfiguration(
                new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        
        LookupSoapServer server = LookupSoapServer.getInstance();
        server.startServer(false);
        log.info("lookup server has started with context: " + context);

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
