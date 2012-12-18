package net.es.oscars.coord.workers;

import java.net.URL;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.clients.PSSClient;
import net.es.oscars.coord.common.Coordinator;

public class PSSWorker extends ModuleWorker {

    private PSSClient   pssClient    = null;
    private URL         pssHost      = null;
    private Coordinator coordinator = null;
    
    private static PSSWorker instance;
    
    private static Logger LOG = Logger.getLogger(PSSWorker.class.getName());

    public static PSSWorker getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new PSSWorker();
        }
        return instance;
    }

    private PSSWorker () throws OSCARSServiceException {
        LOG =  Logger.getLogger(PSSWorker.class.getName());
        this.reconnect();
    }
    
    public PSSClient getPSSClient() {
        return this.pssClient;
    }

    public void reconnect() throws OSCARSServiceException {
        this.coordinator = Coordinator.getInstance();
        this.pssHost = this.coordinator.getPSSHost();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        // Instantiates PSS client
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
            URL pssWsdl = cc.getWSDLPath(ServiceNames.SVC_PSS,null);
            LOG.debug(netLogger.getMsg("PSSWorker.reconnect", "pssHost is " + this.pssHost +
                                " pssWsdl is at " + pssWsdl.toString()));
            this.pssClient = PSSClient.getClient(this.pssHost,pssWsdl);
        } catch (Exception e) {
            LOG.error(netLogger.error("PSSWorker", ErrSev.MINOR, "caught exception " + e.getMessage()));
            throw new OSCARSServiceException (e);
        }        
    }
}
