package net.es.oscars.authZ.http;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.authZ.soap.gen.AuthZService;
import net.es.oscars.authZ.soap.gen.AuthZPortType;

@OSCARSNetLoggerize(moduleName=ModuleName.AUTHZ)
@OSCARSService (
        implementor = "net.es.oscars.authZ.http.AuthZSoapHandler",
        serviceName = ServiceNames.SVC_AUTHZ,
        config = ConfigDefaults.CONFIG
)
public class AuthZSoapServer extends OSCARSSoapService<AuthZService, AuthZPortType> {
    private static AuthZSoapServer instance;

    public static AuthZSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new AuthZSoapServer();
        }
        return instance;
    }

    private AuthZSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_AUTHZ);
    }
}
