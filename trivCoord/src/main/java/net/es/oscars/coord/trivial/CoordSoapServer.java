package net.es.oscars.coord.trivial;

import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;

import net.es.oscars.coord.soap.gen.CoordService;
import net.es.oscars.coord.soap.gen.CoordPortType;

@OSCARSService (
		implementor = "net.es.oscars.coord.trivial.CoordImpl",
		serviceName = "CoordService",
		config = "coord.yaml"
)
public class CoordSoapServer extends OSCARSSoapService  <CoordService, CoordPortType>{
 
    private static CoordSoapServer instance;

    public static CoordSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new CoordSoapServer();
        }
        return instance;
    }

    private CoordSoapServer() throws OSCARSServiceException {
        super();
    }
}
