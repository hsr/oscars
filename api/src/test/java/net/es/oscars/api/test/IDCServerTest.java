
package net.es.oscars.api.test;

import java.net.MalformedURLException;
import java.net.URL;

import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;

import net.es.oscars.api.soap.gen.v06.*;

@OSCARSService (
        serviceName = ServiceNames.SVC_API,
        implementor = "net.es.oscars.api.http.OSCARSSoapHandler06",
        config = ConfigDefaults.CONFIG
)
public class IDCServerTest extends OSCARSSoapService<OSCARSService,OSCARS> {
    public static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_API);
    private static String context = ConfigDefaults.CTX_TESTING;
    protected IDCServerTest() throws Exception {
        super(ServiceNames.SVC_API);
        System.out.println("Starting Server");
        // new SpringBusFactory().createBus("server.xml");
        /*Object implementor = new OSCARSAuthNImpl();
        String address = "http://localhost:9090/OSCARSAuthN";
        Endpoint.publish(address, implementor);
        */
    }
    
    public static void main(String args[]) throws Exception, OSCARSServiceException { 
        
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_API);
        try {
            System.out.println("loading manifest from ./config/"+ConfigDefaults.MANIFEST);
            cc.loadManifest(ServiceNames.SVC_API,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }

        try {
            OSCARSSoapService.setSSLBusConfiguration(
                    new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e ) {
            throw new OSCARSServiceException (e);
        }
        IDCServerTest server = new IDCServerTest();
        server.startServer(true);
        System.out.println("Server ready..."); 
        
        Thread.sleep(5 * 60 * 1000); 
        System.out.println("Server exiting");
        System.exit(0);
    }
}
