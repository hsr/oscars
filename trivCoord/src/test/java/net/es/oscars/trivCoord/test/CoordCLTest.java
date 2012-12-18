
package net.es.oscars.trivCoord.test;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.*;
import net.es.oscars.common.soap.gen.*;
import net.es.oscars.coord.client.CoordClient;
import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.CreatePathResponseContent;
import oasis.names.tc.saml._2_0.assertion.AttributeType;


import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.Bus;

import net.es.oscars.api.soap.gen.v06.OSCARSService;
import net.es.oscars.api.soap.gen.v06.OSCARS;

public final class CoordCLTest {

    /**
     * Coordinator Test client.
     * Arguments are: <IDC host:port> <protocol version> <auth type>
     * 
     * @param args
     */
    public static void main(String args[]) {
        URL coordWsdl;
        String GRI = "DEADBEEF";
        try {
            URL coordHost = new URL("https://localhost:9003/OSCARS/Coord");
            coordWsdl = (new SharedConfig ("CoordService")).getWSDLPath(null);
            if (args.length > 1 ){
                GRI = args [2];
            }
            {
                System.out.println("Invoking createPath...");

                try {
                    CoordClient coordClient = CoordClient.getClient(coordHost,coordWsdl);
                    SubjectAttributes subjectAttrs = new SubjectAttributes();
                    AttributeType at = new AttributeType();
                    at.setName("role");
                    at.getAttributeValue().add("OSCARS-Engineer");
                    subjectAttrs.getSubjectAttribute().add(at);
                    CreatePathContent query = new CreatePathContent();
                    query.setGlobalReservationId(GRI);
                    query.setToken("TestToken");
                    Object[] req = new Object[]{subjectAttrs, query};
                    Object[] res = coordClient.invoke("createPath",req);
                    CreatePathResponseContent response = (CreatePathResponseContent) res[0];
                    System.out.println("Response: " + response.getGlobalReservationId() + " , " + response.getStatus());


                } catch (OSCARSServiceException ex) { 
                    System.out.println("OSCARSServiceExeption: " + ex.getMessage());
                    if (ex.getFaultInfo() != null) {
                        OSCARSFault faultInfo = (OSCARSFault) ex.getFaultInfo();
                        if (faultInfo != null) {
                            System.out.println(faultInfo.getMsg() + " " + faultInfo.getDetails());
                        }
                    }
                }
            }
            System.exit(0);
        } catch (MalformedURLException e1) {
            System.out.println("Caught MalformedURLException");
            e1.printStackTrace();
        }
    }
}

