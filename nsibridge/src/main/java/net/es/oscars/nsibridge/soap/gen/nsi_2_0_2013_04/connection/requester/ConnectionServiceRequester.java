package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.requester;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.7.6
 * 2013-07-23T11:23:02.590-07:00
 * Generated source version: 2.7.6
 * 
 */
@WebServiceClient(name = "ConnectionServiceRequester", 
                  wsdlLocation = "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/2013_04/ConnectionService/ogf_nsi_connection_requester_v2_0.wsdl",
                  targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/requester") 
public class ConnectionServiceRequester extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://schemas.ogf.org/nsi/2013/04/connection/requester", "ConnectionServiceRequester");
    public final static QName ConnectionServiceRequesterPort = new QName("http://schemas.ogf.org/nsi/2013/04/connection/requester", "ConnectionServiceRequesterPort");
    static {
        URL url = null;
        try {
            url = new URL("file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/2013_04/ConnectionService/ogf_nsi_connection_requester_v2_0.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(ConnectionServiceRequester.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/2013_04/ConnectionService/ogf_nsi_connection_requester_v2_0.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public ConnectionServiceRequester(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ConnectionServiceRequester(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ConnectionServiceRequester() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ConnectionServiceRequester(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ConnectionServiceRequester(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ConnectionServiceRequester(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns ConnectionRequesterPort
     */
    @WebEndpoint(name = "ConnectionServiceRequesterPort")
    public ConnectionRequesterPort getConnectionServiceRequesterPort() {
        return super.getPort(ConnectionServiceRequesterPort, ConnectionRequesterPort.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ConnectionRequesterPort
     */
    @WebEndpoint(name = "ConnectionServiceRequesterPort")
    public ConnectionRequesterPort getConnectionServiceRequesterPort(WebServiceFeature... features) {
        return super.getPort(ConnectionServiceRequesterPort, ConnectionRequesterPort.class, features);
    }

}