package net.es.oscars.coord.runtimepce;

import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pce.soap.gen.v06.PCEPortType;
import net.es.oscars.pce.soap.gen.v06.PCEService;

@OSCARSNetLoggerize(moduleName=ModuleName.PCERUNTIME)
@OSCARSService (
		implementor = "net.es.oscars.coord.runtimepce.PCERuntimeSoapHandler",
		serviceName = ServiceNames.SVC_PCE,
		config = ConfigDefaults.CONFIG
)
public class PCERuntimeSoapServer extends OSCARSSoapService  <PCEService, PCEPortType> {
 
    private static PCERuntimeSoapServer instance;

    public static PCERuntimeSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new PCERuntimeSoapServer();
        }
        return instance;
    }

    private PCERuntimeSoapServer() throws OSCARSServiceException {
        // Uses the Coordinator's ContextConfig
        super(ServiceNames.SVC_COORD);
    }
}
