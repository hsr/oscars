package net.es.oscars.coord.workers;

import java.net.URL;

import net.es.oscars.api.soap.gen.v06.ResDetails;

import org.apache.log4j.Logger;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.notify.NotifySender;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.coord.req.CoordRequest;

public class NotifyWorker extends ModuleWorker {

    private NotifySender notifySender                = null;
    private URL notifyHost                           = null;
    private Coordinator coordinator                  = null;
    private static  Logger LOG = Logger.getLogger(NotifyWorker.class.getName());
    private static NotifyWorker instance;
    
    public static NotifyWorker getInstance() {
        if (instance == null) {
            instance = new NotifyWorker();
        }
        return instance;
    }

    // only called once from getInstance
    private NotifyWorker () {
        LOG =  Logger.getLogger(NotifyWorker.class.getName());
        try {
            this.reconnect();
        } catch (OSCARSServiceException e) {
            throw new RuntimeException (e);
        }
    }

    public NotifySender getClient() {
        return this.notifySender;
    }
    
    public void reconnect() throws OSCARSServiceException {
        this.coordinator = Coordinator.getInstance();
        this.notifyHost = this.coordinator.getNotifyBridgeHost();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        // Instantiates NotifyBridge client
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
            URL notifyWsdl = cc.getWSDLPath(ServiceNames.SVC_NOTIFY,null);
            LOG.info(netLogger.getMsg("NotifyWorker.reconnect", "NotifyBridge host= " +
                     this.notifyHost + " WSDL= " + notifyWsdl));
            NotifySender.init(this.notifyHost, notifyWsdl);
        } catch (Exception e) {
            throw new OSCARSServiceException (e);
        }
    }

    public void sendInfo (CoordRequest request, String eventType, ResDetails resDetails)  {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG = Logger.getLogger(NotifyWorker.class.getName());
        LOG.debug(netLogger.start("notify.sendInfo", eventType));
        try {
            NotifySender.send (eventType, request.getMessageProperties(), resDetails);
        } catch (OSCARSServiceException e) {

            LOG.error(netLogger.getMsg("NotifyWorker.sendInfo", e.getMessage()));
        }
    }

    public void sendError (CoordRequest request,
                           String eventType,
                           String error,
                           String source,
                           ResDetails resDetails) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG = Logger.getLogger(NotifyWorker.class.getName());
        LOG.debug(netLogger.start("notify.sendError", eventType));
        try {
            NotifySender.sendError (eventType, request.getMessageProperties(), error, source, resDetails);
        } catch (OSCARSServiceException e) {
            LOG.error(netLogger.getMsg("NotifyWorker.sendError", e.getMessage()));
        }
    }

}
