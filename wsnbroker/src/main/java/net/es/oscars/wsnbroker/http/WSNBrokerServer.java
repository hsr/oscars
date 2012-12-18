package net.es.oscars.wsnbroker.http;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.wsnbroker.soap.gen.WSNBrokerPortType;
import net.es.oscars.wsnbroker.soap.gen.WSNBrokerService;

/**
 * The server object for the LookupService. 
 *
 */

@OSCARSService (implementor = "net.es.oscars.wsnbroker.http.WSNBrokerSoapHandler",
                serviceName = ServiceNames.SVC_WSNBROKER,
                config = ConfigDefaults.CONFIG
)
public class WSNBrokerServer extends OSCARSSoapService <WSNBrokerService, WSNBrokerPortType>{
    private static WSNBrokerServer instance;
    
    public static WSNBrokerServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new WSNBrokerServer();
        }
        return instance;
    }

    private WSNBrokerServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_WSNBROKER);
    }
}
