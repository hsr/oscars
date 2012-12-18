package net.es.oscars.coord.workers;

import java.net.URL;

import org.apache.log4j.Logger;

import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

public class AuthZWorker extends ModuleWorker {

    private static Logger LOG = Logger.getLogger(AuthZWorker.class.getName());
    private AuthZClient    authZClient    = null;
    private URL            authZHost      = null;
    private Coordinator    coordinator    = null;
    
    private static AuthZWorker instance;
    
    public static AuthZWorker getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new AuthZWorker();
        }
        return instance;
    }
    
    private AuthZWorker () throws OSCARSServiceException {
        LOG =  Logger.getLogger(AuthZWorker.class.getName());
        this.reconnect();
    }
    
    
    public AuthZClient getAuthZClient() {
        return this.authZClient;
    }


    public void reconnect() throws OSCARSServiceException {
        this.coordinator = Coordinator.getInstance();
        this.authZHost = this.coordinator.getAuthZHost();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        // Instantiates AuthZ client
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
            URL authzWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHZ,null);
            LOG.debug (netLogger.getMsg("AuthZWorker.reconnect","AuthZ host= " + this.authZHost + " WSDL= " + authzWsdl));
            this.authZClient = AuthZClient.getClient(this.authZHost,authzWsdl);
        } catch (Exception e) {
            throw new OSCARSServiceException (e);
        }
    }

}
