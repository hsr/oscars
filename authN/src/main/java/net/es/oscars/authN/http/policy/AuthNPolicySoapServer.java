package net.es.oscars.authN.http.policy;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;

import net.es.oscars.authN.soap.gen.policy.AuthNPolicyService;
import net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType;

@OSCARSNetLoggerize(moduleName=ModuleName.AUTHNP)
@OSCARSService (
		implementor = "net.es.oscars.authN.http.policy.AuthNPolicySoapHandler",
		serviceName = ServiceNames.SVC_AUTHN_POLICY,
		config = ConfigDefaults.CONFIG
)
public class AuthNPolicySoapServer extends OSCARSSoapService<AuthNPolicyService, AuthNPolicyPortType> {
    private static AuthNPolicySoapServer instance;

    public static AuthNPolicySoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new AuthNPolicySoapServer();
        }
        return instance;
    }

    private AuthNPolicySoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_AUTHN);
    }
}
