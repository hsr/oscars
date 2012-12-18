package net.es.oscars.authZ.http.policy;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.authZ.soap.gen.policy.AuthZPolicyService;
import net.es.oscars.authZ.soap.gen.policy.AuthZPolicyPortType;

@OSCARSService (
        implementor = "net.es.oscars.authZ.http.policy.AuthZPolicySoapHandler",
        serviceName = ServiceNames.SVC_AUTHZ_POLICY,
        config = ConfigDefaults.CONFIG
)
public class AuthZPolicySoapServer extends OSCARSSoapService<AuthZPolicyService, AuthZPolicyPortType> {
    private static AuthZPolicySoapServer instance;

    public static AuthZPolicySoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new AuthZPolicySoapServer();
        }
        return instance;
    }

    private AuthZPolicySoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_AUTHZ);
    }
}
