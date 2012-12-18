package net.es.oscars.topoBridge.http;

import java.net.MalformedURLException;
import java.net.URL;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.topoBridge.soap.gen.TopoBridgePortType;
import net.es.oscars.topoBridge.soap.gen.TopoBridgeService;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;

@OSCARSNetLoggerize(moduleName = ModuleName.TOPO)
@OSCARSService (
        implementor = "net.es.oscars.topoBridge.http.TopoSoapHandler",
        serviceName = ServiceNames.SVC_TOPO,
        config = ConfigDefaults.CONFIG
)
public class TopoSoapServer extends OSCARSSoapService<TopoBridgeService, TopoBridgePortType> {

    private static TopoSoapServer instance;
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_TOPO);

    public static TopoSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new TopoSoapServer();
        }
        return instance;
    }

    private TopoSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_TOPO);
        // set cxf bus 
        try {
            OSCARSSoapService.setSSLBusConfiguration(
                    new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }  catch (ConfigException e ) {
            throw new OSCARSServiceException (e);
        }
    }
}
