package net.es.oscars.resourceManager.http;

import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;

import net.es.oscars.resourceManager.soap.gen.ResourceManagerService;
import net.es.oscars.resourceManager.soap.gen.RMPortType;

@OSCARSNetLoggerize(moduleName = ModuleName.RM)
@OSCARSService (
        implementor = "net.es.oscars.resourceManager.http.RMSoapHandler",
        serviceName = ServiceNames.SVC_RM,
        config = ConfigDefaults.CONFIG
)
public class RMSoapServer extends OSCARSSoapService<ResourceManagerService, RMPortType> {
 
    private static RMSoapServer instance;

    public static RMSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new RMSoapServer();
        }
        return instance;
    }

    private RMSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_RM);
    }
}
