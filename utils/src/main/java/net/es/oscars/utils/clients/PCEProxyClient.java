
package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pce.soap.gen.v06.PCEService;
import net.es.oscars.pce.soap.gen.v06.PCEPortType;
import org.apache.log4j.Logger;

@OSCARSNetLoggerize(moduleName=ModuleName.PCE)
@OSCARSService (
        implementor = "net.es.oscars.pce.soap.gen.v06.PCEService",
        namespace   = "http://oscars.es.net/OSCARS/PCE/20090922",
        serviceName = ServiceNames.SVC_PCE,
        config      = ConfigDefaults.CONFIG
)
public class PCEProxyClient extends OSCARSSoapService<PCEService, PCEPortType> {

    private static Logger LOG = Logger.getLogger(PCEProxyClient.class);

    private PCEProxyClient (URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, PCEPortType.class);
    }
    
    static public PCEProxyClient getClient (URL host, URL wsdl) throws MalformedURLException, OSCARSServiceException {
        ContextConfig cc = ContextConfig.getInstance();
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        try {
            String cxfClientPath = cc.getFilePath(cc.getServiceName(), cc.getContext(),
                                                  ConfigDefaults.CXF_CLIENT);
            OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
        } catch (ConfigException e) {
            LOG.error(netLogger.error("PCEProxyClient.getClient", ErrSev.MAJOR,
                                       " caughtException: " + e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }

        PCEProxyClient client = new PCEProxyClient (host, wsdl);
        return client;
    }

}
