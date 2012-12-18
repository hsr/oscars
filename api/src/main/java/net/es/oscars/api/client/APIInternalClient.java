package net.es.oscars.api.client;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType;
import net.es.oscars.api.soap.gen.v06.OSCARSInternalService;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;


import net.es.oscars.api.soap.gen.v06.OSCARSInternalService;
import net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType;

@OSCARSService (
        serviceName = ServiceNames.SVC_API_INTERNAL,
        namespace = "http://oscars.es.net/OSCARS/06",
        implementor = "net.es.oscars.api.http.OSCARSInternalSoapHandler",
        config="config-internal.yaml"
)
public class APIInternalClient extends OSCARSSoapService<OSCARSInternalService, OSCARSInternalPortType> {
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_API);
    private static Logger LOG = Logger.getLogger(APIInternalClient.class);

    
    private APIInternalClient (URL host, URL wsdlFile) throws OSCARSServiceException {
        super (host, wsdlFile, OSCARSInternalPortType.class);
    }
    
    static public APIInternalClient getClient (URL host, URL wsdl) throws MalformedURLException, OSCARSServiceException {
        try {
            if (cc.getContext() != null ) {  // use new configuration method
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                LOG.debug("APIInternalClient setting BusConfiguration from " + cxfClientPath);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else { 
                LOG.debug("ContextConfig not set");
                System.exit(-1);
            }
        } catch  (ConfigException e) {
            LOG.error("APIInternalClient caught ConfigException");
            throw new OSCARSServiceException(e.getMessage());
        }

        APIInternalClient client = new APIInternalClient (host, wsdl);
        return client;
    }

}
