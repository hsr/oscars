package net.es.oscars.topoBridge.common;

import static java.util.Arrays.asList;

import org.apache.log4j.Logger;
import java.util.HashMap;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.topoBridge.http.TopoSoapServer;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Invoker {

    static private ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_TOPO);
    static private String context = "PRODUCTION";
    static private Logger LOG = null;
    
    public static void main(String[] args) throws Exception {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.TOPO,"0000");
        
        // create a parser
        OptionParser parser = new OptionParser() {
            {
                acceptsAll( asList( "h", "?" ), "show help then exit" );
                accepts( "help", "show extended help then exit" );
                accepts( "mode" , "server / client mode" ).withRequiredArg().describedAs("client / server (default)").ofType(String.class);
                accepts( "c" , "context" ).withRequiredArg().describedAs("TESTING,DEVELOPMENT,SDK,PRODUCTION(default)").ofType(String.class);
            }
        };

        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has("c") ){
            context = (String) options.valueOf("c");
            if (!context.equals("UNITTEST") &&
                    !context.equals("SDK") &&
                    !context.equals("DEVELOPMENT") &&
                    !context.equals("PRODUCTION") ) 
            {
                System.out.println("unrecognized CONTEXT value: " + context);
                System.exit(-1);
            } 
        } 
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_TOPO);
        System.out.println("starting TopoBridgeService with context "+ context);
        try {
            cc.loadManifest(ServiceNames.SVC_TOPO,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            LOG = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        String mode = "server";
        if (options.has("mode")) {
            String optVal = (String) options.valueOf("mode");
            if (optVal.equals("client")) {
                mode = "client";
            } else if (!optVal.equals("server")) {
                parser.printHelpOn( System.out );
                System.exit(1);
            }
        }

        if (mode.equals("server")) {
            TopoSoapServer server = TopoSoapServer.getInstance();
            server.startServer(false);

        } else {
            System.out.println("do client");
        }
        // grab the local topology immediately when starting up
        LOG.info(netLogger.start("Invoker.init"));
        TopoBridgeCore core = TopoBridgeCore.getInstance();
        core.getLocalTopology();
        HashMap<String, String> netLoggerProps = new HashMap<String, String>();
        netLoggerProps.put("context", context);
        LOG.info(netLogger.end("Invoker.init", null, null, netLoggerProps));
    }
}
