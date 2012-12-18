package net.es.oscars.api.http;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.api.soap.gen.v06.*;


@OSCARSNetLoggerize(moduleName = ModuleName.INTAPI)
@OSCARSService (
        serviceName = ServiceNames.SVC_API_INTERNAL,
        implementor = "net.es.oscars.api.http.OSCARSInternalSoapHandler",
        config = ConfigDefaults.CONFIG
)
public class OSCARSInternalServer extends OSCARSSoapService <OSCARSInternalService, OSCARSInternalPortType> {

    private static OSCARSInternalServer instance;

    public static OSCARSInternalServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new OSCARSInternalServer();
        }
        return instance;
    }

    private OSCARSInternalServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_API);
    }
}
