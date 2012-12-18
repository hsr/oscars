package net.es.oscars.pss.dragon.common;

import static java.util.Arrays.asList;

import java.net.URL;

import org.apache.log4j.Logger;
import org.hibernate.exception.ExceptionUtils;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.dragon.soap.DRAGONPSSSoapServer;

import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class Invoker {

    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
    private static String context = ConfigDefaults.CTX_PRODUCTION;
    private static  Logger LOG = null;
    private static String mode = "server";

    public static void main(String[] args) throws Exception {

        parseArgs( args);
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_PSS); 
        try {
            System.out.println("loading manifest from " + ServiceNames.SVC_PSS + "/"+ConfigDefaults.MANIFEST);
            cc.loadManifest(ServiceNames.SVC_PSS, ConfigDefaults.MANIFEST); // manifest.yaml
            String configFilePath = cc.getFilePath(ConfigDefaults.CONFIG);
            System.out.println("loading config from "+configFilePath);
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            ConfigHolder.loadConfig(configFilePath);
            LOG = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            ex.printStackTrace();
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "DragonPSSinit";
        netLogger.init(ModuleName.PSS, "0000");
        LOG.debug("CXF config at: "+cc.getFilePath(ConfigDefaults.CXF_SERVER));
        DRAGONPSSSoapServer.setSSLBusConfiguration(
                new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));

        if (mode.equals("server")) {
            ClassFactory fac = ClassFactory.getInstance();
            fac.configure();
            try {
                LOG.info(netLogger.start(event));
                DRAGONPSSSoapServer server = DRAGONPSSSoapServer.getInstance();
                server.startServer(false);
                LOG.info(netLogger.end(event));
            } catch (Exception ex) {
                LOG.error(netLogger.error(event,ErrSev.MAJOR,"Caught Exception " + ex.toString()));
                LOG.debug(ExceptionUtils.getFullStackTrace(ex));
                ex.printStackTrace();
                System.exit(-1);
            }
        } else {
             System.out.println("client not implemented");
        }
    }
    public static void parseArgs(String args[])  throws java.io.IOException {

        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        parser.accepts( "mode" , "server / client mode" ).withRequiredArg().describedAs("client / server (default)").ofType(String.class);
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
        if (options.has("mode")) {
            String optVal = (String) options.valueOf("mode");
            if (optVal.equals("client")) {
                mode = "client";
            } else if (!optVal.equals("server")) {
                parser.printHelpOn( System.out );
                System.exit(1);
            }
        }
    }

}
