package net.es.oscars.notificationBridge.http;

import net.es.oscars.notificationBridge.soap.gen.NotificationBridgePortType;
import net.es.oscars.notificationBridge.soap.gen.NotificationBridgeService;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.svc.ServiceNames;
/**
 * The server object for the NotificationBridgeService. 
 *
 */

@OSCARSService (implementor = "net.es.oscars.notificationBridge.http.NotificationBridgeSoapHandler",
                serviceName = ServiceNames.SVC_NOTIFY,
                config = ConfigDefaults.CONFIG
)
public class NotificationBridgeServer extends OSCARSSoapService <NotificationBridgeService, NotificationBridgePortType>{
    private static NotificationBridgeServer instance;
    
    public static NotificationBridgeServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new NotificationBridgeServer();
        }
        return instance;
    }

    private NotificationBridgeServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_NOTIFY);
    }
}
