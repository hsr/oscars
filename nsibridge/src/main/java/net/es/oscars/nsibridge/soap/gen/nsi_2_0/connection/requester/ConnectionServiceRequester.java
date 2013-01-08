
/*
 * 
 */

package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.7
 * Wed Nov 07 14:42:37 PST 2012
 * Generated source version: 2.2.7
 * 
 */


@WebServiceClient(name = "ConnectionServiceRequester", 
                  wsdlLocation = "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_requester_v2_0.wsdl",
                  targetNamespace = "http://schemas.ogf.org/nsi/2012/03/connection/requester") 
public class ConnectionServiceRequester extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://schemas.ogf.org/nsi/2012/03/connection/requester", "ConnectionServiceRequester");
    public final static QName ConnectionServiceRequesterPort = new QName("http://schemas.ogf.org/nsi/2012/03/connection/requester", "ConnectionServiceRequesterPort");
    static {
        URL url = null;
        try {
            url = new URL("file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_requester_v2_0.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_requester_v2_0.wsdl");
            // e.printStackTrace();
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