package net.es.oscars.api.http;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.api.soap.gen.v05.*;


@OSCARSService (
		serviceName = ServiceNames.SVC_API,
		implementor = "net.es.oscars.api.http.OSCARSSoapHandler05"
)
public class OSCARSSoapServer05 extends OSCARSSoapService <OSCARSService, OSCARS> {

	private static OSCARSSoapServer05 instance;
	    
	public static OSCARSSoapServer05 getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new OSCARSSoapServer05();
        }
        return instance;
    }
	     
    private OSCARSSoapServer05() throws OSCARSServiceException {
        super(ServiceNames.SVC_API);
    }
}
