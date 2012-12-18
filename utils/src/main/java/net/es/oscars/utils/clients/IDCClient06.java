package net.es.oscars.utils.clients;

import org.apache.log4j.Logger;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.WSConstants;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;

@OSCARSNetLoggerize(moduleName = ModuleName.API)
@OSCARSService (
        implementor = "net.es.oscars.api.soap.gen.v06.OSCARSService",
        namespace = "http://oscars.es.net/OSCARS/06",
        serviceName = ServiceNames.SVC_API,
        config=ConfigDefaults.CONFIG
)
public class IDCClient06 extends OSCARSSoapService<net.es.oscars.api.soap.gen.v06.OSCARSService,
                                                   net.es.oscars.api.soap.gen.v06.OSCARS> {
    private static Logger LOG = Logger.getLogger(IDCClient06.class);

    private IDCClient06 (URL host, URL wsdlFile) throws OSCARSServiceException {
        super (host, wsdlFile, net.es.oscars.api.soap.gen.v06.OSCARS.class);
    }
    
    static public IDCClient06 getClient (URL host, URL wsdl, String connType) throws MalformedURLException, OSCARSServiceException {

        // TODO: the following line is to force the class loader to load the Xalan's URI class, including its inner classes
        // which otherwise would not be loaded, and therefore, generate a Class Not Found exception.
        // Note that this is needed only for client that uses signing.
        org.apache.xml.utils.URI uri = new org.apache.xml.utils.URI();
        
        ContextConfig cc = ContextConfig.getInstance();
        LOG = Logger.getLogger(IDCClient06.class);
        String event = "IDCGetClient";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        if (netLogger.getModuleName() == null) {
            netLogger.init(ModuleName.API,"0000");
        }
        if (connType.equals("UT")) {
            try {
                if (cc.getContext() != null ) {  // use new configuration method
                    String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                    LOG.debug(netLogger.start(event,"setting BusConfiguration from " + cxfClientPath));
                    OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
                } else { 
                 // deprecated
                    String protocol = host.getProtocol();
                    String clientCxf = "client-cxf-http.xml";
                    if (protocol.equals("https")){
                        clientCxf = "client-cxf-ssl.xml";
                    }
                    OSCARSSoapService.setSSLBusConfiguration((
                            new URL("file:" + (new SharedConfig (ServiceNames.SVC_API)).getFilePath(clientCxf))));
                }
            } catch  (ConfigException e) {
                LOG.error(netLogger.error(event,ErrSev.MAJOR,"IDCClient06 caught ConfigException"));
                throw new OSCARSServiceException(e.getMessage());
            }
            IDCClient06 client = new IDCClient06 (host, wsdl);
            
            // User/Password configuration

            /**/
            net.es.oscars.api.soap.gen.v06.OSCARS port  = client.getPortType();
            org.apache.cxf.endpoint.Client tmpClient = org.apache.cxf.frontend.ClientProxy.getClient(port);
            org.apache.cxf.endpoint.Endpoint cxfEndpoint = tmpClient.getEndpoint(); 
            Map<String,Object> outProps = new HashMap<String, Object>();
            
            outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN );
            
            outProps.put(WSHandlerConstants.USER, "mykey");   

            outProps.put(WSHandlerConstants.SIG_PROP_FILE, "clientKeystore.properties");
            
            outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
            outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordCallback.class.getName());

            WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
            cxfEndpoint.getOutInterceptors().add(wssOut);
            /**/
            
            return client;
            
        } else if (connType.equals("x509")) {
            try {
                if (cc.getContext() != null ) {  // use new configuration method
                    String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                    LOG.debug(netLogger.start(event,"setting BusConfiguration from " + cxfClientPath));
                    OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
                } else { 
                    String protocol = host.getProtocol();
                    String clientCxf = "client-cxf-http.xml";
                    if (protocol.equals("https")){
                        clientCxf = "client-cxf-ssl.xml";
                    }
                    OSCARSSoapService.setSSLBusConfiguration((
                            new URL("file:" + (new SharedConfig (ServiceNames.SVC_COORD)).getFilePath(clientCxf))));
                }
            } catch  (ConfigException e) {
                LOG.error(netLogger.error(event,ErrSev.MAJOR,"IDCClient06 caught ConfigException"));
                throw new OSCARSServiceException(e.getMessage());
            }
            IDCClient06 client = new IDCClient06 (host, wsdl);
            return client;
        }
        
        throw new RuntimeException (connType + " is invalid.");
    }

}
