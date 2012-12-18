package net.es.oscars.authZ.test.stubserver;

import org.apache.cxf.bus.spring.SpringBusFactory;

import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;


import net.es.oscars.authZ.soap.gen.AuthZService;
import net.es.oscars.authZ.soap.gen.AuthZPortType;

@OSCARSService (
		implementor = "net.es.oscars.authZ.test.stubserver.AuthZStubSoapHandler",
		serviceName = "AuthZService",
		config = "authZ.yaml"
)
public class AuthZStubSoapServer extends OSCARSSoapService<AuthZService, AuthZPortType> {
 
    private static AuthZStubSoapServer instance;

    public static AuthZStubSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new AuthZStubSoapServer();
        }
        return instance;
    }

    private AuthZStubSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_AUTHZ);
    }
    
    public static void main(String[] args) throws Exception {
        
        // Instantiates the CXF bus
        try {
            String cxfServerFile = (new SharedConfig("AuthZService")).getFilePath("server-cxf.xml");
            new SpringBusFactory().createBus("file:" + cxfServerFile);
        } catch (Exception ee) {
            throw new RuntimeException (ee);
        }
        
        AuthZStubSoapServer server = AuthZStubSoapServer.getInstance();
        server.startServer(false);       
    }
}
