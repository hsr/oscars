package net.es.oscars.authN.http;

import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;

import net.es.oscars.authN.soap.gen.AuthNService;
import net.es.oscars.authN.soap.gen.AuthNPortType;

@OSCARSService (
		implementor = "net.es.oscars.authN.http.AuthNSoapHandler",
		serviceName = "AuthNService",
		config = "authN.yaml"
)
public class AuthNSoapServer extends OSCARSSoapService<AuthNService, AuthNPortType> {
    private static AuthNSoapServer instance;

    public static AuthNSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new AuthNSoapServer();
        }
        return instance;
    }

    private AuthNSoapServer() throws OSCARSServiceException {
        super();
    }
}
