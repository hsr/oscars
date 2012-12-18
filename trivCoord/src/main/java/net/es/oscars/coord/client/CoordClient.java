
package net.es.oscars.coord.client;

import java.net.URL;
import java.net.MalformedURLException;

import src.main.java.net.es.oscars.utils.clients.CoordClient;

import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;

import net.es.oscars.coord.soap.gen.CoordPortType;
import net.es.oscars.coord.soap.gen.CoordService;


@OSCARSService (
		implementor = "net.es.oscars.coord.soap.gen.CoordService",
		namespace = "http://oscars.es.net/OSCARS/coord",
		serviceName = "CoordService",
		config="coord.yaml"
)
public class CoordClient extends OSCARSSoapService<CoordService, CoordPortType> {

    private CoordClient (URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, CoordPortType.class);
    }
    
    static public CoordClient getClient (URL host, URL wsdl) throws MalformedURLException, OSCARSServiceException {
        OSCARSSoapService.setSSLBusConfiguration((
                new URL("file:" + (new SharedConfig ("CoordService")).getFilePath("client-cxf.xml"))));
        
        CoordClient client = new CoordClient (host, wsdl);
        return client;
    }

}
