package net.es.oscars.lookup.http;

import net.es.oscars.lookup.soap.gen.LookupPortType;
import net.es.oscars.lookup.soap.gen.LookupService;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;

/**
 * The server object for the LookupService. 
 *
 */

@OSCARSService (implementor = "net.es.oscars.lookup.http.LookupSoapHandler",
                serviceName = ServiceNames.SVC_LOOKUP,
                config = ConfigDefaults.CONFIG
)
public class LookupSoapServer extends OSCARSSoapService <LookupService, LookupPortType>{
    private static LookupSoapServer instance;

    public static LookupSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new LookupSoapServer();
        }
        return instance;
    }

    private LookupSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_LOOKUP);
    }
}
