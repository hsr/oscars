package net.es.oscars.bootstrap;

import static java.util.Arrays.asList;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.bootstrap.BootClassLoader;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;


import org.apache.log4j.Logger;

public class Invoker {

    private static ContextConfig cc = ContextConfig.getInstance("BootStrap");
    private static String context = ConfigDefaults.CTX_PRODUCTION;
    private static  Logger LOG = null;

    public static net.es.oscars.coord.common.Invoker coordinator = null;
    public static net.es.oscars.pce.bandwidth.common.Invoker bwpce = null;
    public static net.es.oscars.pce.connectivity.common.Invoker conpce = null;
    public static net.es.oscars.pce.vlan.common.Invoker vlanpce = null;
    public static net.es.oscars.pce.dijkstra.common.Invoker dijpce = null;
    public static net.es.oscars.api.common.Invoker api = null;
    public static net.es.oscars.lookup.common.Invoker lookup = null;
    public static net.es.oscars.resourceManager.common.Invoker rm = null;
    public static net.es.oscars.authN.common.Invoker authN = null;
    public static net.es.oscars.authZ.common.Invoker authZ = null;
    public static net.es.oscars.pce.defaultagg.NullAgg nullAgg = null;
    // public static net.es.oscars.pss.eompls.common.Invoker pss = null;
    public static net.es.oscars.pss.stub.common.Invoker pss = null;
    public static net.es.oscars.notificationBridge.common.Invoker notification = null;
    public static net.es.oscars.topoBridge.common.Invoker topo = null;
    public static net.es.oscars.wbui.http.WebApp webapp = null;
    public static net.es.oscars.wsnbroker.common.Invoker wsbBroker = null;

    public static Thread coordinatorThread = null;
    public static Thread bwpceThread = null;
    public static Thread connpceThread = null;
    public static Thread vlanpceThread = null;
    public static Thread dijpceThread = null;
    public static Thread apiThread = null;
    public static Thread nullAggThread = null;
    public static Thread lookupThread = null;
    public static Thread rmThread = null;
    public static Thread authNThread = null;
    public static Thread authZThread = null;
    public static Thread pssThread = null;
    public static Thread notificationThread = null;
    public static Thread topoThread = null;
    public static Thread webappThread = null;
    public static Thread wsnBrokerThread = null;


    /**
     * Main program to start OSCARS
     * @param args [-h, ?] for help
     *              [-c PRODUCTION | SDK | DEVELOPMENT | INITTEST ] for context, defaults to PRODUCTION
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        parseArgs( args );
        System.out.println("starting OSCARS in context " + context);
        String[] params = new String[2];
        params [0] = "-c";
        params [1] = context;

        BootStrapJob moduleJob = new BootStrapJob (BootStrapJob.LOOKUP, params);
        lookupThread = new Thread (moduleJob);
        lookupThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_LOOKUP));
        // lookupThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.RM, params);
        rmThread = new Thread (moduleJob);
        rmThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_RM));
        rmThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.COORDINATOR, params);
        coordinatorThread = new Thread (moduleJob);
        coordinatorThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_COORD));
        coordinatorThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.BWPCE, params);
        bwpceThread = new Thread (moduleJob);
        bwpceThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_PCE_BW));
        bwpceThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.CONNPCE, params);
        connpceThread = new Thread (moduleJob);
        connpceThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_PCE_CONN));
        connpceThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.VLANPCE, params);
        vlanpceThread = new Thread (moduleJob);
        vlanpceThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_PCE_VLAN));
        vlanpceThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.DIJPCE, params);
        dijpceThread = new Thread (moduleJob);
        dijpceThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_PCE_DIJ));
        dijpceThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.NULLAGG, params);
        nullAggThread = new Thread (moduleJob);
        nullAggThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_PCE_NULLAGG));
        nullAggThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.AUTHN, params);
        authNThread = new Thread (moduleJob);
        authNThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_AUTHN));
        authNThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.AUTHZ, params);
        authZThread = new Thread (moduleJob);
        authZThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_AUTHZ));
        authZThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.PSS, params);
        pssThread = new Thread (moduleJob);
        pssThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_PSS));
        pssThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.NOTIFY, params);
        pssThread = new Thread (moduleJob);
        pssThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_PSS));
        pssThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.NOTIFY, params);
        notificationThread = new Thread (moduleJob);
        notificationThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_NOTIFY));
        notificationThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.NOTIFY, params);
        topoThread = new Thread (moduleJob);
        topoThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_TOPO));
        topoThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.API, params);
        apiThread = new Thread (moduleJob);
        apiThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_API));
        apiThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.WBUI, params);
        webappThread = new Thread (moduleJob);
        webappThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_WBUI));
        webappThread.start();

        moduleJob = new BootStrapJob (BootStrapJob.WSNBROKER, params);
        wsnBrokerThread = new Thread (moduleJob);
        wsnBrokerThread.setContextClassLoader(new BootClassLoader(ServiceNames.SVC_WSNBROKER));
        wsnBrokerThread.start();


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
