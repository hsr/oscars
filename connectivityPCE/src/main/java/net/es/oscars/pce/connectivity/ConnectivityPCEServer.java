package net.es.oscars.pce.connectivity;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.SimplePCEServer;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;

/**
 * Standalone server that reads in configuration file and publishes a 
 * connectivity PCE service.
 * 
 * @author Andy Lake <andy@es.net>
 */
@OSCARSService (
        serviceName = ServiceNames.SVC_PCE_CONN,
        config = ConfigDefaults.CONFIG,
        implementor = "net.es.oscars.pce.connectivity.ConnectivityPCEProtocolHandler"
)
@OSCARSNetLoggerize(moduleName = "net.es.oscars.pce.Connectivity")
public class ConnectivityPCEServer extends SimplePCEServer{
    static private ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PCE_CONN);
    
    final private Logger log = Logger.getLogger(this.getClass());
    private ConnectivityPCE pce;
    private String moduleName;
    private Map config =  null;
    private Map topoConfig = null;
    
    final private String PROP_TOPO_URL = "topoBridgeUrl";
    final private String PROP_TOPO_WSDL = "topoBridgeWsdl";
    final private String PROP_MAX_SEARCH_DEPTH = "maxSearchDepth";
    
    public ConnectivityPCEServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_PCE_CONN);
        String topoUrl =  null;
        String topoWsdl = null;
        //get the name of the module from netLogger.Use annotation to be 
        //consistent with protocol handler even though variable would probably do
        this.moduleName = 
            this.getClass().getAnnotation(OSCARSNetLoggerize.class).moduleName();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.info(netLogger.start("init"));
        try{
            String configAlias=
                this.getClass().getAnnotation(OSCARSService.class).config();
            String configFilename = cc.getFilePath(configAlias);
            this.log.debug("configFilename is " + configFilename);
            config = ConfigHelper.getConfiguration(configFilename);
            if(config.containsKey(PROP_TOPO_URL)){
                // if topo service runs on a different, put the url in ConnectivityPCS's config.yaml
                topoUrl = (String) config.get(PROP_TOPO_URL);
            } 
            else { // if topo service runs on same host, get the url from topo Service's config.yaml
                configFilename = cc.getFilePath(ServiceNames.SVC_TOPO,cc.getContext(),
                        ConfigDefaults.CONFIG);
                topoConfig = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
                Map soap = (HashMap<String,Object>) this.topoConfig.get("soap");
                if (soap == null ){
                    throw new ConfigException("soap stanza not found in Topology configuration");
                }
                topoUrl = (String)soap.get("publishTo");
            }
            if (topoUrl == null ){
                this.log.error(netLogger.error("initialization",ErrSev.MAJOR, "unable to find TopologyService URL"));
                throw new OSCARSServiceException("unable to find TopologyService URL");
            }
            this.log.debug(netLogger.start("init","topology service running on " + topoUrl));
            if (config.containsKey(PROP_TOPO_WSDL)) {
                topoWsdl = (String) config.get(PROP_TOPO_WSDL);
            } else { // get it from manifest
                topoWsdl = "file:" + cc.getFilePath(ServiceNames.SVC_TOPO,cc.getContext(),
                        ConfigDefaults.WSDL);
            }
            this.log.debug(netLogger.end("init", "topoUrl is " + topoUrl + " topoWsdl is " + topoWsdl));
            this.pce = new ConnectivityPCE(this.moduleName, topoUrl, topoWsdl);
            //set optional max search depth if configured
            if(config.containsKey(PROP_MAX_SEARCH_DEPTH)){
                this.pce.setMaxSearchDepth((Integer) config.get(PROP_MAX_SEARCH_DEPTH));
            }
            this.log.info(netLogger.end("init"));
        }catch(Exception e){
            this.log.error(netLogger.error("init", ErrSev.CRITICAL, e.getMessage()));
            System.err.println(e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException{
        OSCARSNetLogger netLogger = new OSCARSNetLogger(this.moduleName,query.getTransactionId());
        netLogger.setGRI(query.getGri());
        OSCARSNetLogger.setTlogger(netLogger);
        return this.pce.calculatePath(query);
    }
    
    public PCEDataContent commitPath(PCEMessage query) throws OSCARSServiceException{
        PCEDataContent pceData = null;
        OSCARSNetLogger netLogger = new OSCARSNetLogger(this.moduleName,query.getTransactionId());
        netLogger.setGRI(query.getGri());
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.debug(netLogger.start("commitPath"));
        try{
            pceData = this.pce.commitPath(query);
            this.log.debug(netLogger.end("commitPath"));
        }catch(OSCARSServiceException e){
            this.log.debug(netLogger.error("commitPath", ErrSev.MAJOR, e.getMessage()));
            throw e;
        }catch(Exception e){
            this.log.debug(netLogger.error("commitPath", ErrSev.MAJOR, e.getMessage()));
            throw new OSCARSServiceException(e);
        }
        
        return pceData;
    }
}
