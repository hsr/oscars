package net.es.oscars.utils.clients;

import java.net.MalformedURLException;
import java.net.URL;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.topoBridge.soap.gen.TopoBridgePortType;
import net.es.oscars.topoBridge.soap.gen.TopoBridgeService;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import org.apache.log4j.Logger;

/**
 * A helper class for generating clients that talk to the Topology Bridge Service. 
 */
@OSCARSNetLoggerize(moduleName = ModuleName.TOPO)
@OSCARSService (
        implementor = "net.es.oscars.topoBridge.soap.gen.TopoBridgeService",
        namespace = "http://oscars.es.net/OSCARS/topoBridge", 
        serviceName = ServiceNames.SVC_TOPO
)
public class TopoBridgeClient extends OSCARSSoapService<TopoBridgeService, TopoBridgePortType>{

    static private Logger LOG = Logger.getLogger(TopoBridgeClient.class);
    /**
     * Constructor. 
     * 
     * @param host The location of the host to contact
     * @param wsdl The location of the WSDL file for this service
     * @throws OSCARSServiceException
     */
    public TopoBridgeClient(URL host, URL wsdl) throws OSCARSServiceException {
        super(host, wsdl, TopoBridgePortType.class);
    }
    
    /**
     * Creates a LookupPortType object that can be used to call the lookup 
     * service.
     * 
     * @param host The location of the host to contact as a URL string
     * @param wsdl The location of the service wsdl as a URL string
     * @return A LookupPortType object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public TopoBridgeClient getClient(String host, String wsdl) throws OSCARSServiceException, MalformedURLException{
        ContextConfig cc = ContextConfig.getInstance();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        try {
            String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
            OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
        } catch (ConfigException e) {
            LOG.error(netLogger.error("TopoBridgeClient.getClient", ErrSev.MAJOR,
                    " caughtException: " + e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        if(wsdl == null){
            return TopoBridgeClient.getClient(host);
        }
        return new TopoBridgeClient(new URL(host), new URL(wsdl));
    }
    
    /**
     * Creates a LookupPortType object that can be used to call the lookup 
     * service given only the address of the service. The location of the WSDL
     * is assumed to be <i>host</i>?wsdl.
     * 
     * @param host The location of the host to contact as a URL string
     * @return A LookupPortType object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public TopoBridgeClient getClient(String host) throws OSCARSServiceException, MalformedURLException{
        return new TopoBridgeClient(new URL(host), new URL(host+"?wsdl"));
    }
}
