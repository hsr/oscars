package net.es.oscars.pce.l3mpls;


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
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;

/**
 * Standalone server that reads in configuration file and publishes a 
 * MPLS PCE service.
 * 
 * @author Andy Lake <andy@es.net>
 */
@OSCARSService (
        serviceName = ServiceNames.SVC_PCE_L3MPLS,
        config = ConfigDefaults.CONFIG,
        implementor = "net.es.oscars.pce.l3mpls.L3MplsPCEProtocolHandler"
)
@OSCARSNetLoggerize(moduleName = "net.es.oscars.pce.L3MPLS", serviceName = ServiceNames.SVC_PCE_L3MPLS)
public class L3MplsPCEServer extends SimplePCEServer{
    static private ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PCE_L3MPLS);
    
    final private String PROP_RM_URL = "rmUrl";
    final private String PROP_RM_WSDL = "rmWsdl";
    
    final private Logger log = Logger.getLogger(this.getClass());
    private L3MplsPCE pce;
    private String moduleName;
    private Map config =  null;
    private Map rmConfig = null;
    
    public L3MplsPCEServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_PCE_L3MPLS);
        String rmUrl =  null;
        String rmWsdl = null;
        //get the name of the module from netLogger.Use annotation to be 
        //consistent with protocol handler even though variable would probably do
        this.moduleName = 
            this.getClass().getAnnotation(OSCARSNetLoggerize.class).moduleName();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.info(netLogger.start("init"));
        HashMap<String, String> netLogProps = new HashMap<String, String>();
        try{
            String configAlias=
                this.getClass().getAnnotation(OSCARSService.class).config();
            String configFilename = cc.getFilePath(configAlias);
            config = ConfigHelper.getConfiguration(configFilename);
            if(config.containsKey(PROP_RM_URL)){
                // if resourceManager service runs on a different, put the url in VlanPCE's config.yaml
                rmUrl = (String) config.get(PROP_RM_URL);
            } 
            else { // if RM service runs on same host, get the url from RM Service's config.yaml
                configFilename = cc.getFilePath(ServiceNames.SVC_RM,cc.getContext(),
                        ConfigDefaults.CONFIG);
                rmConfig = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
                Map soap = (HashMap<String,Object>) this.rmConfig.get("soap");
                if (soap == null ){
                    throw new ConfigException("soap stanza not found in resourceManager.yaml");
                }
                rmUrl = (String)soap.get("publishTo");
                netLogProps.put("rmUrl", rmUrl);
            }
            if (rmUrl == null ){
                this.log.error(netLogger.error("init",ErrSev.MAJOR, "unable to find resourceManager URL"));
                throw new OSCARSServiceException("unable to find resourceManager URL");
            }
            if (config.containsKey(PROP_RM_WSDL)) {
                rmWsdl = (String) config.get(PROP_RM_WSDL);
            } else { // get it from manifest
                rmWsdl = "file:" + cc.getFilePath(ServiceNames.SVC_RM,cc.getContext(),
                        ConfigDefaults.WSDL);
            }
            
            HashMap<String, Object> localDomainSettings = PathTools.getLocalDomainSettings();
            String localDomain = null;
            if(localDomainSettings != null){
                localDomain = (String) localDomainSettings.get("id");
            }
            netLogProps.put("localDomain", localDomain);
            this.pce = new L3MplsPCE(rmUrl, rmWsdl, localDomain);
            this.log.info(netLogger.end("init"));
        } catch (Exception e){
            this.log.error(netLogger.error("init", ErrSev.CRITICAL, e.getMessage()));
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException{
        PCEDataContent pceData = null;
        OSCARSNetLogger netLogger = new OSCARSNetLogger(this.moduleName, query.getTransactionId());
        netLogger.setGRI(query.getGri());
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.debug(netLogger.start("calculatePath"));
        try{
            pceData = this.pce.calculatePath(query, netLogger);
            this.log.debug(netLogger.end("calculatePath"));
        }catch(OSCARSServiceException e){
            this.log.debug(netLogger.error("calculatePath", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            throw e;
        }catch(Exception e){
            this.log.debug(netLogger.error("calculatePath", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException(e);
        }
        
        return pceData;
    }
}
