package net.es.oscars.coord.workers;

import java.net.URL;

import org.apache.log4j.Logger;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.coord.common.Coordinator;

public class RMWorker extends ModuleWorker {

    private RMClient    rmClient    = null;
    private URL         rmHost      = null;
    private Coordinator coordinator = null;
    private static  Logger LOG = Logger.getLogger(RMWorker.class.getName());

    private static RMWorker instance;
    public static RMWorker getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new RMWorker();
        }
        return instance;
    }

    private RMWorker () throws OSCARSServiceException {
        LOG =  Logger.getLogger(RMWorker.class.getName());
        this.reconnect();
    }
    
    public RMClient getRMClient() {
        return this.rmClient;
    }

    public void reconnect() throws OSCARSServiceException {
        this.coordinator = Coordinator.getInstance();
        this.rmHost = this.coordinator.getRMHost();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        // Instantiates Resource Manager client
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
            URL rmWsdl = cc.getWSDLPath(ServiceNames.SVC_RM,null);
            LOG.info (netLogger.getMsg("RMWorker.reconnect", "ResourceManager host= " + 
                                        this.rmHost + " WSDL= " + rmWsdl));
            this.rmClient = RMClient.getClient(this.rmHost,rmWsdl);
        } catch (Exception e) {
            throw new OSCARSServiceException (e);
        }        
    }
}
