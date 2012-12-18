package net.es.oscars.api.http;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.api.soap.gen.v06.*;

@OSCARSNetLoggerize(moduleName = ModuleName.API)
@OSCARSService (
		serviceName = ServiceNames.SVC_API,
		implementor = "net.es.oscars.api.http.OSCARSSoapHandler06",
		config = ConfigDefaults.CONFIG
)
public class OSCARSSoapServer06 extends OSCARSSoapService <OSCARSService, OSCARS> {

	private static OSCARSSoapServer06 instance;
	    
	public static OSCARSSoapServer06 getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new OSCARSSoapServer06();
        }
        return instance;
    }
	     
    public OSCARSSoapServer06() throws OSCARSServiceException {
        super(ServiceNames.SVC_API);
    }
}
