package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.authN.soap.gen.AuthNPortType;
import net.es.oscars.authN.soap.gen.AuthNService;

@OSCARSNetLoggerize(moduleName = ModuleName.AUTHN)
@OSCARSService (
        implementor = "net.es.oscars.authN.soap.gen.AuthNService",
        namespace = "http://oscars.es.net/OSCARS/authN",
        serviceName = ServiceNames.SVC_AUTHN
)
public class AuthNClient extends OSCARSSoapService<AuthNService, AuthNPortType> {
    static private Logger LOG = Logger.getLogger(AuthNClient.class);

    private AuthNClient (URL host, URL wsdlFile) throws OSCARSServiceException {
        super (host, wsdlFile, AuthNPortType.class);
    }

    /**
     * Get a soapClient side for the AuthN service. Maybe be called by an OSCARS service
     * or a end user of authN.
     * @param host Host that the authN service is running on
     * @param wsdl Url to the authN wsdl
     * @return
     * @throws MalformedURLException
     * @throws OSCARSServiceException
     */
    static public AuthNClient getClient (URL host, URL wsdl)
            throws MalformedURLException, OSCARSServiceException {
        ContextConfig cc = ContextConfig.getInstance();
        String event = "authNGetClient";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        try {
            if (cc.getContext() != null ) {  // use new configuration method
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else {
                throw new ConfigException("ContextConfig not initialized");
            }
        } catch (ConfigException e) {
             LOG.error(netLogger.error(event,ErrSev.MAJOR, " caughtException: " + e.getMessage()));
             e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        AuthNClient client = new AuthNClient (host, wsdl);
        return client;
    }
}
