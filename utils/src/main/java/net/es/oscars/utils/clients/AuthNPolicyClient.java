package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType;
import net.es.oscars.authN.soap.gen.policy.AuthNPolicyService;
import org.apache.log4j.Logger;


@OSCARSService (
        implementor = "net.es.oscars.authN.soap.gen.policy.AuthNPolicyService",
        namespace = "http://oscars.es.net/OSCARS/authNPolicy",
        serviceName = "AuthNPolicyService"
)
public class AuthNPolicyClient extends OSCARSSoapService<AuthNPolicyService, AuthNPolicyPortType> {
    static private Logger LOG = Logger.getLogger(AuthNPolicyClient.class);

    private AuthNPolicyClient (URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, AuthNPolicyPortType.class);
    }
    
    static public AuthNPolicyClient getClient (URL host, URL wsdl)
            throws MalformedURLException, OSCARSServiceException {
        ContextConfig cc = ContextConfig.getInstance();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        try {
            if (cc.getContext() != null ) {  // use new configuration method
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else {
                throw new ConfigException("ContextConfig not initialized");
            }
        } catch (ConfigException e) {
            LOG.error(netLogger.error("AuthNPolicyClient.getClient", ErrSev.MAJOR,
                    " caughtException: " + e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        AuthNPolicyClient client = new AuthNPolicyClient (host, wsdl);
        return client;
    }
}
