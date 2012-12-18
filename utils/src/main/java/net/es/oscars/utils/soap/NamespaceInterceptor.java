package net.es.oscars.utils.soap;

import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.AbstractEndpointSelectionInterceptor;
import org.apache.cxf.interceptor.Fault;

import org.apache.log4j.Logger;
import net.es.oscars.logging.OSCARSNetLogger;

public class NamespaceInterceptor  extends AbstractEndpointSelectionInterceptor {

    private static final Logger LOG = Logger.getLogger(NamespaceInterceptor.class.getName());

    public NamespaceInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    protected Endpoint selectEndpoint(Message message, Set<Endpoint> eps)  {
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "selectEndpoint";
        LOG.debug(netLogger.start(event));
        if (eps == null) {
            LOG.debug(netLogger.end(event, "returns null"));
            return null;
        }

        QName serviceName = (QName) message.get("javax.xml.ws.wsdl.service");
        try {
            String ns = serviceName.getNamespaceURI();
            //LOG.info("service name " + ns);
            for (Endpoint ep : eps) {   
                //LOG.info("ProtocolVersion " + ep.get("ProtocolVersion"));
                if (ns.equals(ep.get("ProtocolVersion"))) {
                	LOG.info(netLogger.getMsg( "NamespaceInterceptor.selectEndpoint",  "returns " +
                             ep.getEndpointInfo().getName().getNamespaceURI()));
                    return ep;
                } 
            }
            
        } catch (Exception e) {
            throw new Fault(e);
        }  
        return null;
    }	

}
