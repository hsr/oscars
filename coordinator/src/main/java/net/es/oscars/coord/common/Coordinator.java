package net.es.oscars.coord.common;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

@OSCARSNetLoggerize(moduleName = ModuleName.COORD)
public class Coordinator {
    private URL authZHost = null;
    private URL rmHost    = null;
    private URL pssHost   = null;
    private URL notifyBridgeHost = null;
    private URL internalApiHost   = null;
    private String callbackEndpoint = null;
    private Scheduler scheduler = null; 
    private static  Logger LOG = null;
    private ContextConfig cc = null;
    private HashMap<String, HashMap<String, HashMap<String, String>>> manifest = null;
    private HashMap<String,Object> coordMap = null;
    private HashMap<String,Object> authzMap = null;
    private HashMap<String,Object> rmMap = null;
    private HashMap<String,Object> pssMap = null;
    private HashMap<String,Object> notifyBridgeMap = null;
    private HashMap<String,Object> internalApiMap = null;
    private boolean allowActiveModify = false;

    public boolean isAllowActiveModify() {
        return this.allowActiveModify;
    }
    /**
     * singleton instance
     */
    private static Coordinator instance;


    public static Coordinator getInstance() throws OSCARSServiceException {
        if (Coordinator.instance == null) {
            Coordinator.instance = new Coordinator();
        }
        return Coordinator.instance;
    }
    
    private Coordinator() throws OSCARSServiceException {
        LOG = Logger.getLogger(Coordinator.class.getName());
        cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
        this.loadConfigs();
        this.setModuleHosts();
    }

    public void start() throws OSCARSServiceException {
        
        // Start QUARTZ
        SchedulerFactory sf=new StdSchedulerFactory();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "Coordinator.start";
        try {
            this.scheduler =sf.getScheduler();
            this.scheduler.start();;
        } catch (SchedulerException e) {
            throw new OSCARSServiceException (e);
        }
 
        LOG.info(netLogger.getMsg(event,"started Quartz scheduler"));
 
        // Set SSL key-stores
        try {
            OSCARSSoapService.setSSLBusConfiguration(
                    new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e ) {
            throw new OSCARSServiceException (e);
        }
        CoordSoapServer s = CoordSoapServer.getInstance();
        s.startServer(false);
        LOG.info(netLogger.getMsg(event, "started CoordSoapServer"));
    }
    
    public Scheduler getScheduler () {
        return this.scheduler;
    }

    private void setModuleHosts () throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "setModuleHosts";
        HashMap<String,Object> soap = null;
        try { 
            // set callbackEnpoint for PSSReply
            soap = (HashMap<String,Object>) this.coordMap.get("soap");
            this.callbackEndpoint = (String)soap.get("publishTo");
            LOG.debug(netLogger.getMsg(event,"callbackEndpoint for PSSReply " + this.callbackEndpoint));
            
            //set whether we allow modification of active reservations
            if(this.coordMap.containsKey("allowActiveModify") && this.coordMap.get("allowActiveModify") != null){
                String propAllowActiveModify = this.coordMap.get("allowActiveModify") + "";
                if("1".equals(propAllowActiveModify) || "true".equals(propAllowActiveModify)){
                    this.allowActiveModify = true;
                }
            }
            
            // Retrieve AuthZ host 
            soap = (HashMap<String,Object>) this.authzMap.get("soap");
            if (soap == null ) {
                throw new ConfigException("soap stanza not found in authZ.yaml");
            }
            this.authZHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.getMsg(event,"authZ running on " + this.authZHost.toString()));
            
            // retrieve resourceManager host
            soap = (HashMap<String,Object>) this.rmMap.get("soap");
            if (soap == null ){
                throw new ConfigException("soap stanza not found in resourceManager.yaml");
            }
            this.rmHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.getMsg(event,"resourceManager running on " + this.rmHost.toString()));

            // retrieve pss host
            soap = (HashMap<String,Object>) this.pssMap.get("soap");
            if (soap == null ){
                throw new ConfigException("soap stanza not found in PSS.yaml");
            }
            this.pssHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.getMsg(event,"pssHost running on " + this.pssHost.toString()));

            // retrieve notificationBridgee host
            soap = (HashMap<String,Object>) this.notifyBridgeMap.get("soap");
            if (soap == null ){
                throw new ConfigException("soap stanza not found in notificationBridge.yaml");
            }
            this.notifyBridgeHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.getMsg(event,"notifyBridgeHost running on " + this.notifyBridgeHost.toString()));
                        // retrieve internal api host
            soap = (HashMap<String,Object>) this.internalApiMap.get("soap");
            if (soap == null ){
                throw new ConfigException("soap stanza not found in internal-api.yaml");
            }
            this.internalApiHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.getMsg(event,"internalApiHost running on " + this.internalApiHost.toString()));
            
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        } catch (ConfigException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void loadConfigs() throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "loadConfigs";
        this.manifest = cc.getManifest();
        String configFilename= null;
         try{
            LOG.debug(netLogger.start(event, "context is " + cc.getContext()));
            // assumes all the services are running in the same context
            configFilename = cc.getFilePath(ConfigDefaults.CONFIG);
            this.coordMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_AUTHZ,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.authzMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_RM,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.rmMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_PSS,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.pssMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
            
            configFilename = cc.getFilePath(ServiceNames.SVC_NOTIFY,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.notifyBridgeMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
            System.out.println("configFilename is " + configFilename);
            configFilename = cc.getFilePath(ServiceNames.SVC_API_INTERNAL,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.internalApiMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
        } catch (ConfigException e) {
            LOG.error(netLogger.error(event,ErrSev.MAJOR,"caught config exception " + e.getMessage()));
            throw new OSCARSServiceException (e);
        }
        LOG.debug(netLogger.end(event));
    }

    public String getCallbackEndpoint() {
        return this.callbackEndpoint;
    }
    
    public URL getRMHost () {
        return this.rmHost;
    }
    
    public URL getAuthZHost () {
        return this.authZHost;
    }
    
    public URL getPSSHost () {
        return this.pssHost;
    }
    public URL getNotifyBridgeHost () {
        return this.notifyBridgeHost;
    }
    public URL getInternalApiHost () {
        return this.internalApiHost;
    }

}
