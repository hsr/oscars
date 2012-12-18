package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType;
import net.es.oscars.api.soap.gen.v06.OSCARSInternalService;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
@OSCARSNetLoggerize(moduleName = ModuleName.INTAPI)
@OSCARSService (
        serviceName = ServiceNames.SVC_API_INTERNAL,
        namespace = "http://oscars.es.net/OSCARS/06",
        implementor = "net.es.oscars.api.soap.gen.v06.OSCARSInternalService",
        config="config-internal.yaml"
)
public class InternalAPIClient extends OSCARSSoapService<OSCARSInternalService, OSCARSInternalPortType> {
    private static ContextConfig cc = ContextConfig.getInstance();
    private static Logger LOG = Logger.getLogger(InternalAPIClient.class);

    
    private InternalAPIClient (URL host, URL wsdlFile) throws OSCARSServiceException {
        super (host, wsdlFile, OSCARSInternalPortType.class);
    }
    
    static public InternalAPIClient getClient (URL host, URL wsdl) throws MalformedURLException, OSCARSServiceException {
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        try {
            if (cc.getContext() != null ) {  // use new configuration method
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else {
                throw new ConfigException("ContextConfig not set");
            }
        } catch  (ConfigException e) {
            LOG.error(netLogger.error("InternalAPIClient.getClient", ErrSev.MAJOR,
                                      " caughtException: " + e.getMessage()));
            throw new OSCARSServiceException(ErrorCodes.CONFIG_ERROR,e.getMessage(), ErrorReport.SYSTEM);
        }

        InternalAPIClient client = new InternalAPIClient (host, wsdl);
        return client;
    }

}
