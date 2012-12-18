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
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.authZ.soap.gen.AuthZPortType;
import net.es.oscars.authZ.soap.gen.AuthZService;

@OSCARSNetLoggerize(moduleName = ModuleName.AUTHZ)
@OSCARSService (
        implementor = "net.es.oscars.authZ.soap.gen.AuthZService",
        namespace = "http://oscars.es.net/OSCARS/authZ",
        serviceName = ServiceNames.SVC_AUTHZ
)
public class AuthZClient extends OSCARSSoapService<AuthZService, AuthZPortType> {

    static private Logger LOG = Logger.getLogger(AuthZClient.class);
    private AuthZClient (URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, AuthZPortType.class);
    }
    
    static public AuthZClient getClient (URL host, URL wsdl)
            throws MalformedURLException, OSCARSServiceException {
        ContextConfig cc = ContextConfig.getInstance();
        boolean loggingInit = false;
        String event = "authZGetClient";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        try {
            if (cc.getContext() != null) {
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
        AuthZClient client = new AuthZClient (host, wsdl);
        return client;
    }
}
