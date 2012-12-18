package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.logging.ModuleName;

@OSCARSNetLoggerize(moduleName = ModuleName.API)
@OSCARSService (
        implementor = "net.es.oscars.api.soap.gen.v05.OSCARSNotify_Service",
        namespace = "http://oscars.es.net/OSCARS",
        serviceName = "OSCARSNotify",
        config=ConfigDefaults.CONFIG
)
public class NotifyClient05 extends OSCARSSoapService<net.es.oscars.api.soap.gen.v05.OSCARSNotify_Service,
                                                   net.es.oscars.api.soap.gen.v05.OSCARSNotify> {


    private NotifyClient05 (URL host, URL wsdlFile) throws OSCARSServiceException {
        super (host, wsdlFile, net.es.oscars.api.soap.gen.v05.OSCARSNotify.class);
    }

    static public NotifyClient05 getClient (URL host, URL wsdl) throws MalformedURLException, OSCARSServiceException {

        // TODO: the following line is to force the class loader to load the Xalan's URI class, including its inner classes
        // which otherwise would not be loaded, and therefore, generate a Class Not Found exception.
        // Note that this is needed only for client that uses signing.
        org.apache.xml.utils.URI uri = new org.apache.xml.utils.URI();

        ContextConfig cc = ContextConfig.getInstance();
        try {
            String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
            OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
        } catch  (ConfigException e) {
            throw new OSCARSServiceException(e.getMessage());
        }
        
        return new NotifyClient05 (host, wsdl);
    }

}
