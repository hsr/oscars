package net.es.oscars.authN.http;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.authN.soap.gen.AuthNService;
import net.es.oscars.authN.soap.gen.AuthNPortType;

@OSCARSNetLoggerize(moduleName=ModuleName.AUTHN)
@OSCARSService (
        implementor = "net.es.oscars.authN.http.AuthNSoapHandler",
        serviceName = ServiceNames.SVC_AUTHN,
        config = ConfigDefaults.CONFIG
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
        super(ServiceNames.SVC_AUTHN);
    }
}
