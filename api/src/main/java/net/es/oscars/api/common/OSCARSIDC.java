package net.es.oscars.api.common;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.MalformedURLException;

import net.es.oscars.logging.ModuleName;
import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import net.es.oscars.api.http.OSCARSInternalServer;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.lookup.soap.gen.GeoLocation;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.validator.DataValidator;
import net.es.oscars.utils.validator.EndpointValidator;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.LookupClient;


/**
 * 
 * Singleton class that loads the configuration  information, initializes
 * and saves the clients that are needed.
 * 
 * @author lomax
 *
 */
public class OSCARSIDC implements Runnable {

    private static  Logger         LOG                = null;
    private ContextConfig          cc                 = null;
    private HashMap<String, HashMap<String, HashMap<String, String>>> manifest = null;
    private HashMap<String,Object> coordMap           = null;
    private HashMap<String,Object> lookupMap          = null;
    private HashMap<String,Object> authNMap           = null;
    private HashMap<String,Object> IDCMap             = null;
    private HashMap<String,Object> IDC_INTMap         = null;
    private CoordClient            coordClient        = null;
    private AuthNClient            authNClient        = null;
    private LookupClient           lookupClient       = null;
    private URL                    coordHost          = null;
    private URL                    authNHost          = null;
    private URL                    lookupHost         = null;
    private Map                    config             = null;
    private static boolean         internalAPIStarted = false;
    private static Thread          serverThread       = null;
                         
    private static OSCARSIDC instance;
    
    public static OSCARSIDC getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new OSCARSIDC();
        }
        return instance;
    }
         
    private OSCARSIDC () throws OSCARSServiceException {
        
        LOG = Logger.getLogger(OSCARSIDC.class.getName());
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.API,"0001");
        String event = "OSCARSIDC";
        cc = ContextConfig.getInstance(ServiceNames.SVC_API);
        // Load configuration file
        this.loadConfigs();
        
        // Retrieve the hosts URL where the AuthNService and Coordinator service are running
        this.setModuleHosts();
        
        //initialize perfSONAR lookup settings for finding host names
        this.initPSHostLookup();
        
        // Instantiates the CXF bus
        try {
            OSCARSSoapService.setSSLBusConfiguration(
                    new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e ) {
            throw new OSCARSServiceException (e);
        }
        
        // Make sure that the Internal API Service is running
        if (OSCARSIDC.serverThread == null) {
            OSCARSIDC.serverThread = new Thread (this);
            OSCARSIDC.serverThread.start();
        }
        
        // Instantiates Coordinator client
        try {           
            URL coordWsdl = cc.getWSDLPath(ServiceNames.SVC_COORD,null);
            LOG.info (netLogger.start(event,"Coordinator host= " + this.coordHost + " WSDL= " + coordWsdl));
            this.coordClient = CoordClient.getClient(this.coordHost,coordWsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
        // Instantiates AuthNStub client
        try {
            URL authNWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHN,null);
            LOG.info (netLogger.start(event,"AuthNhost= " + this.authNHost + " WSDL= " + authNWsdl));
            this.authNClient = AuthNClient.getClient(this.authNHost,authNWsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
        // Instantiates lookup client
        try {           
            URL lookupWsdl = cc.getWSDLPath(ServiceNames.SVC_LOOKUP,null);
            LOG.info (netLogger.start(event, "Lookup host= " + this.lookupHost + " WSDL= " + lookupWsdl));
            this.lookupClient = LookupClient.getClient(this.lookupHost,lookupWsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
        
        // Register this IDC
        try {
            this.registerIDC();
        } catch (OSCARSServiceException e) {
            // Ignore but log the error
            LOG.error("Cannot register to the LookupService");
        }
    }

    public void startService() throws OSCARSServiceException {
        
    }
    
    private void loadConfigs() throws OSCARSServiceException {
        String configFile = null;

        try {
            this.manifest = cc.getManifest();
            String configFilename= null;
            // assumes all the services are running in the same context
            configFilename = cc.getFilePath(ConfigDefaults.CONFIG);
            this.IDCMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_AUTHN,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.authNMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_COORD,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.coordMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_API_INTERNAL,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.IDC_INTMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
            
            configFilename = cc.getFilePath(ServiceNames.SVC_LOOKUP,cc.getContext(),
                    ConfigDefaults.CONFIG);
            
            this.lookupMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
            
        } catch (Exception e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void setModuleHosts () throws OSCARSServiceException {
        HashMap<String,Object> soap = null;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "setHosts";
        try { 
            // Retrieve AuthN host 
            soap = (HashMap<String,Object>) this.authNMap.get("soap");
            if (soap == null ) {
                throw new ConfigException("soap stanza not found in authN.yaml");
            }
            this.authNHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.getMsg(event,"authN running on " + this.authNHost.toString()));
            
            // retrieve coodinator host
            soap = (HashMap<String,Object>) this.coordMap.get("soap");
            if (soap == null ){
                throw new ConfigException("soap stanza not found in coordinator.yaml");
            }
            this.coordHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.getMsg(event,"coodinator running on " + this.coordHost.toString()));
            
            // retrieve lookup host
            soap = (HashMap<String,Object>) this.lookupMap.get("soap");
            if (soap == null ){
                throw new ConfigException("soap stanza not found in lookup.yaml");
            }
            this.lookupHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.getMsg(event,"lookup running on " + this.lookupHost.toString()));
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        } catch (ConfigException e) {
            throw new OSCARSServiceException (e);
        }
    }
    
    private void initPSHostLookup() throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG.debug(netLogger.start("initPSHostLookup"));
        
        //get perfSONAR lookup settings from lookup module
        Map perfsonar = (Map) this.lookupMap.get("perfsonar");
        if(perfsonar == null){
            //no host lookup, so server only accepts URNs
            LOG.debug(netLogger.end("initPSHostLookup"));
            return;
        }
        HashMap<String, String> logFieldMap = new HashMap<String, String>();
        String hintsFile = null;
        List<String> globalLookupServices = null;
        List<String> homeLookupServices = null;
        
        if(perfsonar.containsKey("globalHintsFile")){
            hintsFile = (String) perfsonar.get("globalHintsFile");
            logFieldMap.put("hints", hintsFile);
        }
        
        if(perfsonar.containsKey("globalLookupServices")){
            globalLookupServices = (List<String>) perfsonar.get("globalLookupServices");
            logFieldMap.put("gLS", OSCARSNetLogger.serializeList(globalLookupServices));
        }
        
        if(perfsonar.containsKey("homeLookupServices")){
            homeLookupServices = (List<String>) perfsonar.get("homeLookupServices");
            logFieldMap.put("hLS", OSCARSNetLogger.serializeList(homeLookupServices));
        }
        
        try {
           EndpointValidator.init(hintsFile, globalLookupServices, homeLookupServices);
        } catch (HttpException e) {
            LOG.debug(netLogger.error("initPSHostLookup", ErrSev.CRITICAL, e.getMessage(), null, logFieldMap));
            throw new OSCARSServiceException (e);
        } catch (IOException e) {
            LOG.debug(netLogger.error("initPSHostLookup",ErrSev.CRITICAL, e.getMessage(), null, logFieldMap));
            throw new OSCARSServiceException (e);
        }
        
        LOG.debug(netLogger.end("initPSHostLookup", null, null, logFieldMap));
        
    }
    
    public CoordClient getCoordClient() {
        return this.coordClient;
    }
    

    public AuthNClient getAuthNClient() {
        return this.authNClient;
    }
    
    public LookupClient getLookupClient() {
        return this.lookupClient;
    }
    
    private void registerIDC () throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG.info(netLogger.start("registerIDC"));
        
        LookupClient client = this.getLookupClient();
        if (client == null) {
            LOG.warn(netLogger.end("registerIDC","Cannot register this IDC to the lookup service: no lookup service client"));
            return;
        }
        //Verify public block is configured
        if(!this.IDCMap.containsKey("public") || this.IDCMap.get("public") == null){
            LOG.warn(netLogger.end("registerIDC","Cannot register this IDC to the lookup service: No 'public' block defined in config file"));
            return;
        }
        
        //Set defaults
        String domainId = PathTools.getLocalDomainId();
        String namespace = "http://oscars.es.net/OSCARS/06";
        String namespace05 = "http://oscars.es.net/OSCARS";
        String name = "OSCARS IDC";
        String description = "OSCARS IDC";
        GeoLocation geoLocation = new GeoLocation();
        boolean hasGeoLocation = false;
        
        //verify that we have a valid 0.6 url to register
        HashMap<String, String> protocolMap = new HashMap<String, String>();
        HashMap<String,String> publicInfo = (HashMap<String,String>) this.IDCMap.get("public");
        try{
            protocolMap.put(namespace, this.getPublicUrl("publishTo", publicInfo));
        }catch(Exception e){
            LOG.warn(netLogger.end("registerIDC","Cannot register this IDC to the lookup service: " + e.getMessage()));
            return;
        }
        
        //Check if we have a valid 0.5 ur to register
        try{
            protocolMap.put(namespace05, this.getPublicUrl("publishTo05", publicInfo));
        }catch(Exception e){
            LOG.warn(netLogger.end("registerIDC","Cannot register 0.5 address to the lookup service: " + e.getMessage()));
        }
        
        //get optional name and description
        if(publicInfo.containsKey("name") && publicInfo.get("name") != null){
            name = (String) publicInfo.get("name");
        }
        
        if(publicInfo.containsKey("description") && publicInfo.get("description") != null){
            description = (String) publicInfo.get("description");
        }
        
        //Get optional geographic information
        if(publicInfo.containsKey("street") && publicInfo.get("street") != null){
           hasGeoLocation = true;
           geoLocation.setStreetAddress((String) publicInfo.get("street"));
        }
        
        if(publicInfo.containsKey("city") && publicInfo.get("city") != null){
            hasGeoLocation = true;
            geoLocation.setCity((String) publicInfo.get("city"));
        }
        
        if(publicInfo.containsKey("state") && publicInfo.get("state") != null){
            hasGeoLocation = true;
            geoLocation.setState((String) publicInfo.get("state"));
        }
        
        if(publicInfo.containsKey("zipcode") && publicInfo.get("zipcode") != null){
            hasGeoLocation = true;
            geoLocation.setZipCode((String) publicInfo.get("zipcode"));
        }
        
        if(publicInfo.containsKey("country") && publicInfo.get("country") != null){
            hasGeoLocation = true;
            geoLocation.setCountry((String) publicInfo.get("country"));
        }
        
        if(publicInfo.containsKey("continent") && publicInfo.get("continent") != null){
            hasGeoLocation = true;
            geoLocation.setContinent((String) publicInfo.get("continent"));
        }
        
        if(publicInfo.containsKey("institution") && publicInfo.get("institution") != null){
            hasGeoLocation = true;
            geoLocation.setInstitution((String) publicInfo.get("institution"));
        }
        
        if(publicInfo.containsKey("floor") && publicInfo.get("floor") != null){
            hasGeoLocation = true;
            geoLocation.setFloor((String) publicInfo.get("floor"));
        }
        
        if(publicInfo.containsKey("rack") && publicInfo.get("rack") != null){
            hasGeoLocation = true;
            geoLocation.setRack((String) publicInfo.get("rack"));
        }
        
        if(publicInfo.containsKey("room") && publicInfo.get("room") != null){
            hasGeoLocation = true;
            geoLocation.setRoom((String) publicInfo.get("room"));
        }
        
        if(publicInfo.containsKey("cage") && publicInfo.get("cage") != null){
            hasGeoLocation = true;
            geoLocation.setCage((String) publicInfo.get("cage"));
        }
        
        if(publicInfo.containsKey("shelf") && publicInfo.get("shelf") != null){
            hasGeoLocation = true;
            geoLocation.setShelf((String) publicInfo.get("shelf"));
        }
        
        if(publicInfo.containsKey("latitude") && publicInfo.get("latitude") != null){
            hasGeoLocation = true;
            geoLocation.setLatitude((String) publicInfo.get("latitude"));
        }
        
        if(publicInfo.containsKey("longitude") && publicInfo.get("longitude") != null){
            hasGeoLocation = true;
            geoLocation.setLongitude((String) publicInfo.get("longitude"));
        }
        
        if(!hasGeoLocation){
            geoLocation = null;
        }
        
        LOG.info(netLogger.end("registerIDC", null, null, publicInfo));
        client.register (domainId, name, protocolMap, description, geoLocation);
    }
    
    private String getPublicUrl(String property, HashMap<String,String> publicInfo){
        if(!publicInfo.containsKey(property) || publicInfo.get(property) == null){
            throw new RuntimeException("No " + property + 
                    " property under the 'public' block in the config file");
        }
        String idcUrl = (String) publicInfo.get(property);
        URL tmpUrl = null;
        try {
            tmpUrl = new URL(idcUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("The " + property + " property is not a valid URL");
        }
        if(tmpUrl.getHost().equals("localhost") || tmpUrl.getHost().equals("127.0.0.1")){
            throw new RuntimeException("The " + property + " property cannot be local address");
        }
        
        return idcUrl;
    }

    public void run() {
        try {
            this.startInternalAPI ();
        } catch (OSCARSServiceException e) {
            LOG.error("OSCARSIDC.run caught exception:" + e.getMessage());
        }
    }

    private synchronized void startInternalAPI () throws OSCARSServiceException {
        if (! OSCARSIDC.internalAPIStarted) {
            OSCARSInternalServer.getInstance().startServer(false);
            OSCARSIDC.internalAPIStarted = true;
        }
    }
}
