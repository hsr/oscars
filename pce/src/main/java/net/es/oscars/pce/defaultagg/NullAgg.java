package net.es.oscars.pce.defaultagg;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.pce.soap.gen.v06.TagDataContent;

import net.es.oscars.pce.PCEProtocolServer;
import net.es.oscars.pce.AggMessage;
import net.es.oscars.utils.clients.PCERuntimeClient;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

@OSCARSService (
        implementor = "net.es.oscars.pce.defaultagg.NullAggProtocolHandler",
        serviceName = ServiceNames.SVC_PCE_NULLAGG,
        config = ConfigDefaults.CONFIG
)
public class NullAgg extends PCEProtocolServer implements Runnable {
    private static ContextConfig cc = null;
    private static String context = ConfigDefaults.CTX_PRODUCTION;
    private static Logger log = null;
    private static NullAgg instance = null;
    private Thread processingThread;
    private ArrayList<AggMessage> pceQueries = new ArrayList<AggMessage>();

    public static NullAgg getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new NullAgg();
        }
        return instance;
    }

    public NullAgg () throws OSCARSServiceException {
        super(ServiceNames.SVC_PCE_NULLAGG);
        // Create and start the background thread
        this.processingThread = new Thread (this);
        this.processingThread.start();
    }
    
    public void addPceQuery(AggMessage pceQuery) {
        synchronized (this.pceQueries) {
            this.pceQueries.add(pceQuery);
            this.pceQueries.notify();
        }
    }
    
    public AggMessage getNextPceQuery () {
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
    
    private void processQuery (AggMessage query) throws OSCARSServiceException {
        String callback = query.getCallBackEndpoint();

        // Get the client
        PCERuntimeClient client = null;
        try {
            client = PCERuntimeClient.getPCERuntimeClient(callback);
            
            log.info ("NullAGG received query");
            
            log.debug ("GRI= " + query.getGri() + 
                                "\nName= " + query.getPceName() +
                                "\ncallback URL= " + query.getCallBackEndpoint());
            
            PCEDataContent pceData = new PCEDataContent();

            List<TagDataContent> tagPCEDataList = query.getPCETagDataContent();
                
            boolean found = false;
            for (TagDataContent tagPCEData : tagPCEDataList) {
                log.debug("PCEData Tag= " + tagPCEData.getTag());
                if (! found) {
                    found = true;
                    
                    // The NullAGG retrieves the first TagPCEData and selects it as the result
                    pceData.setUserRequestConstraint(tagPCEData.getConstraints().getUserRequestConstraint());
                    pceData.setReservedConstraint(tagPCEData.getConstraints().getReservedConstraint());
                    pceData.setTopology(tagPCEData.getConstraints().getTopology());
                    pceData.getOptionalConstraint().addAll(tagPCEData.getConstraints().getOptionalConstraint());
                }
            }
            if (query.getMethod().equals(PCERequestTypes.PCE_CREATE)) {
                client.sendPceReply(query.getMessageProperties(),query.getGri(), query.getTransactionId(), query.getPceName(), pceData);
            } else if (query.getMethod().equals(PCERequestTypes.PCE_CREATE_COMMIT)) {
                client.sendCreateCommitReply(query.getMessageProperties(),query.getGri(), query.getTransactionId(), query.getPceName(), pceData);
            } else if (query.getMethod().equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
                client.sendModifyCommitReply(query.getMessageProperties(),query.getGri(), query.getTransactionId(), query.getPceName(), pceData);
            } else if (query.getMethod().equals(PCERequestTypes.PCE_CANCEL)) {
                client.sendCancelReply(query.getMessageProperties(),query.getGri(), query.getTransactionId(), query.getPceName(), pceData, null);
            } else if (query.getMethod().equals(PCERequestTypes.PCE_MODIFY)) {
                client.sendModifyReply(query.getMessageProperties(),query.getGri(), query.getTransactionId(), query.getPceName(), pceData, null);
            }
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
    }
    
    public static void main(String[] args) {

        try {
            cc = ContextConfig.getInstance(ServiceNames.SVC_PCE_NULLAGG);
            cc.setServiceName(ServiceNames.SVC_PCE_NULLAGG);
            parseArgs(args);
            cc.setContext(context);
            try {
                cc.loadManifest(ServiceNames.SVC_PCE,  ConfigDefaults.MANIFEST); // manifest.yaml
                cc.setLog4j();
                // need to do this after the log4j.properties file has been set
                log = Logger.getLogger(NullAgg.class);
            } catch (ConfigException ex) {
                System.out.println("caught ConfigurationException " + ex.getMessage());
                System.exit(-1);
            }
            OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
            netLogger.init("NullAgg", "0000");
            // Set SSL keystores
            OSCARSSoapService.setSSLBusConfiguration(
                    new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
            
            NullAgg nullPCEServer = NullAgg.getInstance();
            nullPCEServer.startServer(false);
            log.info (netLogger.start("main","NullAggregator Service has started with context " + context));
        } catch (Exception e) {
            log.error ("NullAggregator Service failed" + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public void run() {
        // Process queries
        for (;;) {
            AggMessage query = this.getNextPceQuery();
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
