package net.es.oscars.wbui.servlets;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.*;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.AuthZPolicyClient;
import net.es.oscars.utils.clients.CoordClient;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

/**
 * 
 * @author David Robertson, Eric Pouyoul, Evangelos Chaniotakis
 *
 * A singleton class that initializes and caches all the SOAP clients that the
 * servlets use.
 */
public class ServletCore {
    public static final String CORE = "net.es.oscars.servlet-core";
    private static ServletCore instance             = null;
    private static boolean firstInit                = true;
    private static  Logger LOG                      = Logger.getLogger(ServletCore.class);
    private static OSCARSNetLogger netLogger        = null;  // only used during initialization

    private static ContextConfig  cc                = null;
    private HashMap<String, HashMap<String, HashMap<String, String>>> manifest = null;
    private HashMap<String,Object> coordMap         = null;
    private HashMap<String,Object> authNMap         = null;
    private HashMap<String,Object> authNPolicyMap   = null;
    private HashMap<String,Object> authZMap         = null;
    private HashMap<String,Object> authZPolicyMap   = null;

    private Map config              = null;
    private URL authNHost           = null;
    private URL authNPolicyHost     = null;
    private URL authZHost           = null;
    private URL authZPolicyHost     = null;
    private URL coordHost           = null;
    private String userCookieName;
    private String sessionCookieName;
    private String secureCookie;
    private String guestLogin;
    private AuthNClient authNClient             = null;
    private AuthNPolicyClient authNPolicyClient = null;
    private AuthZClient authZClient             = null;
    private AuthZPolicyClient authZPolicyClient = null;
    private CoordClient coordClient             = null;
    private String transId                      = "0000";

    /**
     * Constructor - private because this is a Singleton
     */
    private ServletCore() {
    }

    /**
     * @return an initialized ServletCore singleton instance
     */
    public static ServletCore getInstance() {
        // not thread-safe, only call in one init method
        netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_WBUI,"0000");
        OSCARSNetLogger.setTlogger(netLogger);
        String event = "getInstance";
        if (ServletCore.instance == null) {
            // only can call once
            if (!firstInit) {
                String msg = "fatal exception in ServletCore setup, server restart required";
                LOG.fatal(netLogger.error(event,ErrSev.CRITICAL,msg));
                throw new RuntimeException(msg);
            }
            try {
                cc = ContextConfig.getInstance(ServiceNames.SVC_WBUI);
                if (cc.getContext() == null){
                    LOG.info (netLogger.start(event,"starting wbui in context " + System.getProperty("context")));
                    cc.setContext(System.getProperty("context"));
                    cc.setServiceName(ServiceNames.SVC_WBUI);
                    cc.loadManifest(ServiceNames.SVC_WBUI,  ConfigDefaults.MANIFEST); // manifest.yaml
                }
                ServletCore.instance = new ServletCore();
                firstInit = false;

                instance.initAll();
            } catch (Exception e) {
                instance = null;
                LOG.fatal(netLogger.error(event,ErrSev.CRITICAL,
                                          "fatal exception in client setup, server restart required: " + e));
                throw new RuntimeException(e);
            }
        }
        LOG.info(netLogger.end("wbui started in context " + System.getProperty("context")));
        return ServletCore.instance;
    }

    /**
     * Closes all clients
     */
    public void shutdown() {
        LOG.info("shutdown.start");
        LOG.info("shutdown.end");
    }

    /**
     * Initializes all the clients
     */
    public void initAll() throws OSCARSServiceException {

        String event = "initAll";
        LOG.debug(netLogger.start(event));
        // Load configuration file
        this.loadConfig();
        this.setAuthNHost();
        this.setAuthNPolicyHost();
        this.setAuthZHost();
        this.setAuthZPolicyHost();
        this.setCoordHost();
        this.initAuthNClient();
        this.initAuthNPolicyClient();
        this.initAuthZClient();
        this.initAuthZPolicyClient();
        this.initCoordClient();
        LOG.debug(netLogger.end(event));
    }

    public String getUserCookieName() {
        return this.userCookieName;
    }

    public String getSessionCookieName() {
        return this.sessionCookieName;
    }

    public String getGuestLogin() {
        return this.guestLogin;
    }

    public String getSecureCookie() {
        return this.secureCookie;
    }

    /**
     * @return the authNClient
     */
    public AuthNClient getAuthNClient() {
        return authNClient;
    }

    /**
     * @return the authNPolicyClient
     */
    public AuthNPolicyClient getAuthNPolicyClient() {
        return authNPolicyClient;
    }

    /**
     * @return the authZClient
     */
    public AuthZClient getAuthZClient() {
        return authZClient;
    }
    /**
     * @return the authZPolicyClient
     */
    public AuthZPolicyClient getAuthZPolicyClient() {
        return authZPolicyClient;
    }
    /**
     * @return the coordClient
     */
    public CoordClient getCoordClient() {
        return coordClient;
    }

    /**
     * @return the transactionId
     */
    public String getTransId() {
        return transId;
    }
    
    /**
     * set the transactionId
     * @param transactionId for current servlet
     */
    public void setTransId (String transId) {
        this.transId = transId;
    }
    private void loadConfig() throws OSCARSServiceException {
        String configFile = null;
        try {
            this.manifest = cc.getManifest();
            String configFilename= null;
            // assumes all the services are running in the same context
            configFilename = cc.getFilePath(ConfigDefaults.CONFIG);
            this.config = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_AUTHN,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.authNMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_AUTHN_POLICY,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.authNPolicyMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_AUTHZ,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.authZMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            configFilename = cc.getFilePath(ServiceNames.SVC_AUTHZ_POLICY,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.authZPolicyMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

            // not used yet, will be when reservations are managed
            configFilename = cc.getFilePath(ServiceNames.SVC_COORD,cc.getContext(),
                    ConfigDefaults.CONFIG);
            this.coordMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);

        } catch (Exception e) {
            throw new OSCARSServiceException (e);
        }

        assert config != null : "No configuration";		
        Map wbui = (Map) config.get("wbui");
        assert wbui != null : "No wbui stanza in configuration";
        userCookieName = (String) wbui.get("userName");
        assert userCookieName != null : "No user cookie name in configuration";
        sessionCookieName = (String) wbui.get("sessionName");
        assert sessionCookieName != null : "No session cookie name in configuration";
        guestLogin = (String) wbui.get("guestLogin");
        secureCookie = (String) wbui.get("secureCookie");
        assert secureCookie != null : "No secureCookie setting in configuration";
    }

    private void setAuthNHost () throws OSCARSServiceException {
        // Retrieve AuthN host 
        assert authNMap != null : "No AuthN config provided in manifest";
        try {
            Map soap = (HashMap<String,Object>) this.authNMap.get("soap");
            if (soap == null ) {
                throw new ConfigException("soap stanza not found in authN.yaml");
            }
            this.authNHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.end("setAuthNHost","authN running on " + this.authNHost.toString()));

        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void setAuthNPolicyHost () throws OSCARSServiceException {
        // Retrieve AuthNPolicy host 
        assert authNPolicyMap != null : "No AuthNPolicy config provided in manifest";
        try {
            Map soap = (HashMap<String,Object>) this.authNPolicyMap.get("soap");
            if (soap == null ) {
                throw new ConfigException("soap stanza not found in authNolicy.yaml");
            }
            this.authNPolicyHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.end("setAuthNPolicy","authNPolicy running on " + 
                                    this.authNPolicyHost.toString()));
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void setAuthZHost () throws OSCARSServiceException {
        // Retrieve AuthZ host 
        assert authZMap != null : "No AuthZ config provided in manifest";
        try {
            Map soap = (HashMap<String,Object>) this.authZMap.get("soap");
            if (soap == null ) {
                throw new ConfigException("soap stanza not found in authZ.yaml");
            }
            this.authZHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.end("setAuthZHost","authZ running on " + this.authZHost.toString()));

        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void setAuthZPolicyHost () throws OSCARSServiceException {
        // Retrieve AuthZPolicy host 
        assert authZPolicyMap != null : "No AuthZPolicy config provided in manifest";
        try {
            Map soap = (HashMap<String,Object>) this.authZPolicyMap.get("soap");
            if (soap == null ) {
                throw new ConfigException("soap stanza not found in authZ.yaml");
            }
            this.authZPolicyHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.end("authZPolicyHost","authZPolicy running on " + 
                                     this.authZPolicyHost.toString()));
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void setCoordHost () throws OSCARSServiceException {
        // Retrieve AuthZPolicy host 
        assert coordMap != null : "No Coordinator config provided in manifest";
        try {
            Map soap = (HashMap<String,Object>) this.coordMap.get("soap");
            if (soap == null ) {
                throw new ConfigException("soap stanza not found in coord.yaml");
            }
            this.coordHost = new URL ((String)soap.get("publishTo"));
            LOG.debug(netLogger.end("setCoordHost","coodinator running on " + this.coordHost.toString()));
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void initAuthNClient() throws OSCARSServiceException {
        // Instantiates AuthN client
        try {
            URL authNWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHN,null);
            LOG.info (netLogger.end("initAuthNClient","AuthNhost= " + this.authNHost + " WSDL= " + authNWsdl));
            this.authNClient = AuthNClient.getClient(this.authNHost,authNWsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void initAuthNPolicyClient() throws OSCARSServiceException {
        // Instantiates AuthNPolicy client
        try {
            URL authNPolicyWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHN_POLICY,null);
            this.authNPolicyClient =
                AuthNPolicyClient.getClient(this.authNPolicyHost, authNPolicyWsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void initAuthZClient() throws OSCARSServiceException {
        // Instantiates AuthZ client
        try {
            URL authZWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHZ,null);
            this.authZClient = AuthZClient.getClient(this.authZHost, authZWsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
    }

    private void initAuthZPolicyClient() throws OSCARSServiceException {
        // Instantiates AuthZPolicy client
        try {
            URL authZPolicyWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHZ_POLICY,null);
            this.authZPolicyClient =
                AuthZPolicyClient.getClient(this.authZPolicyHost, authZPolicyWsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
    }
    private void initCoordClient() throws OSCARSServiceException {
        // Instantiates Coordinator client
        try {
            URL coordWsdl = cc.getWSDLPath(ServiceNames.SVC_COORD,null);
            this.coordClient =
                CoordClient.getClient(this.coordHost, coordWsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
    }
}
