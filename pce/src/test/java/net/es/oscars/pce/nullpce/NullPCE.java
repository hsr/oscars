package net.es.oscars.pce.nullpce;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URL;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import net.es.oscars.pce.PCEProtocolServer;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.clients.PCERuntimeClient;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

@OSCARSService (
        implementor = "net.es.oscars.pce.nullpce.NullPCEProtocolHandler",
        serviceName = ServiceNames.SVC_PCE,
        config = ConfigDefaults.CONFIG
)
public class NullPCE extends PCEProtocolServer implements Runnable {
    private static ContextConfig cc = null;
    private static String context = ConfigDefaults.CTX_PRODUCTION;
    private static Logger log = null;
    private static NullPCE instance = null;
    private Thread processingThread;
    private ArrayList<PCEMessage> pceQueries = new ArrayList<PCEMessage>();

    public static NullPCE getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new NullPCE();
        }
        return instance;
    }

    public NullPCE () throws OSCARSServiceException {
        super(ServiceNames.SVC_PCE_NULLPCE);
        // Create and start the background thread
        this.processingThread = new Thread (this);
        this.processingThread.start();
    }
    
    public void addPceQuery(PCEMessage pceQuery) {
        synchronized (this.pceQueries) {
            this.pceQueries.add(pceQuery);
            this.pceQueries.notify();
        }
    }
    
    public PCEMessage getNextPceQuery () {
        synchronized (this.pceQueries) {
            if (this.pceQueries.size() > 0) {
                return this.pceQueries.remove(0);
            } else {
                try {
                    // no query in the list: ait for a query to be inserted into the list
                    this.pceQueries.wait();
                    return this.pceQueries.remove(0);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
    
    private void processQuery (PCEMessage query) throws OSCARSServiceException {
        // The NullPCE simply returns the same set of constraints (pass-thru PCE)
        // Use the PCERuntime client to send the PCEResponse back
        String callback = query.getCallBackEndpoint();
        // Get the client
        PCERuntimeClient client = null;
        try {
            client = PCERuntimeClient.getPCERuntimeClient(callback);
            PCEDataContent pceDataContent = query.getPCEDataContent();
            if (query.getMethod().equals(PCERequestTypes.PCE_CREATE)) {
                CtrlPlaneTopologyContent topology = new CtrlPlaneTopologyContent();
                PCEDataContent resultPceDataContent = new PCEDataContent();
                // Set the topology with the user constraint's path.
                UserRequestConstraintType userConstraint = pceDataContent.getUserRequestConstraint();
                if (userConstraint == null) {
                    // create a stub user constraint
                    userConstraint = new UserRequestConstraintType();
                    long currentTime = System.currentTimeMillis() / 1000;
                    userConstraint.setStartTime(currentTime + 3600); // One hour after now.
                    userConstraint.setEndTime(currentTime + 7200); // Two hours after now.
                    userConstraint.setPathInfo(new PathInfo());
                }
                PathInfo pathInfo = pceDataContent.getUserRequestConstraint().getPathInfo();
                if (pathInfo == null) {
                    pathInfo = new PathInfo();
                    CtrlPlanePathContent pathContent = new CtrlPlanePathContent();
                    pathContent.setId("fakeID");
                    pathInfo.setPath(pathContent);
                }
                topology.getPath().add (pathInfo.getPath());
    
                resultPceDataContent.setUserRequestConstraint(userConstraint);
                // set reservedConstraint as the userConstraint
                ReservedConstraintType reservedConstraint = new ReservedConstraintType();
                reservedConstraint.setBandwidth(userConstraint.getBandwidth());
                reservedConstraint.setStartTime(userConstraint.getStartTime());
                reservedConstraint.setEndTime(userConstraint.getEndTime());
                reservedConstraint.setPathInfo(pathInfo);
                resultPceDataContent.setReservedConstraint(reservedConstraint);
                resultPceDataContent.setTopology(topology);
    
                client.sendPceReply(query.getMessageProperties(),query.getGri(), query.getTransactionId(), query.getPceName(), resultPceDataContent);
            } else if (query.getMethod().equals(PCERequestTypes.PCE_CREATE_COMMIT)) {
                client.sendCreateCommitReply(query.getMessageProperties(),query.getGri(), query.getTransactionId(), query.getPceName(), pceDataContent);
            }  else if (query.getMethod().equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
                client.sendModifyCommitReply(query.getMessageProperties(),query.getGri(), query.getTransactionId(), query.getPceName(), pceDataContent);
            }
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
    }
    
    public static void main(String[] args) {

        try {
            cc = ContextConfig.getInstance(ServiceNames.SVC_PCE_NULLPCE);
            parseArgs(args);
            System.out.println("starting null PCE service with context "+ context);
            cc.setContext(context);
            cc.setServiceName(ServiceNames.SVC_PCE_NULLPCE);
            try {
                cc.loadManifest(ServiceNames.SVC_PCE,  "pce-" + ConfigDefaults.MANIFEST); // manifest.yaml
                cc.setLog4j();
                // need to do this after the log4j.properties file has been set
                log = Logger.getLogger(NullPCE.class);
            } catch (ConfigException ex) {
                System.out.println("caught ConfigurationException " + ex.getMessage());
                System.exit(-1);
            }
            // Set SSL keystores
            OSCARSSoapService.setSSLBusConfiguration(
                    new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
            
            NullPCE nullPCEServer = NullPCE.getInstance();
            nullPCEServer.startServer(false);
            log.info ("NullPCE Service has started with context " + context);
        } catch (Exception e) {
            log.error ("NullPCE Service caught Exception " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void run() {
        // Process queries
        for (;;) {
            PCEMessage query = this.getNextPceQuery();
            if (query == null) {
                // This should not happen unless this thread has been interrupted
                break;
            }
            // Process query
            try {
                this.processQuery (query);
            } catch (OSCARSServiceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
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