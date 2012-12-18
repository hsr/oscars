package net.es.oscars.authN.common;

import static java.util.Arrays.asList;

import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.cxf.bus.spring.SpringBusFactory;

import net.es.oscars.authN.soap.gen.*;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.common.soap.gen.*;
import net.es.oscars.authN.http.AuthNSoapServer;
import net.es.oscars.utils.svc.ServiceNames;


@javax.jws.WebService(
        serviceName = ServiceNames.SVC_AUTHN,
        portName = "AuthNPort",
        targetNamespace = "http://oscars.es.net/OSCARS/authN",
        endpointInterface = "net.es.oscars.authN.soap.gen.AuthNPortType")
public class Invoker {
    public static void main(String[] args) throws Exception {
        // create a parser
        OptionParser parser = new OptionParser() {
            {
                acceptsAll( asList( "h", "?" ), "show help then exit" );
                accepts( "help", "show extended help then exit" );
                accepts( "mode" , "server / client mode" ).withRequiredArg().describedAs("client / server (default)").ofType(String.class);
            }
        };

        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        
        String mode = "server";
        if (options.has("mode")) {
            String optVal = (String) options.valueOf("mode");
            if (optVal.equals("client")) {
                mode = "client";
            } else if (!optVal.equals("server")) {
                parser.printHelpOn( System.out );
                System.exit(1);
            }
        }
        
        if (mode.equals("server")) {
            // Instantiate current version of the protocol
            AuthNSoapServer server = AuthNSoapServer.getInstance();
            server.startServer(false);
            // Instantiate 0.5 version of the protocol
            //AuthNSoapServer05 server05 = AuthNSoapServer05.getInstance();
            //server05.startServer();
            
        } else {
            new SpringBusFactory().createBus("client.xml");
            AuthNService ss = new AuthNService();
            AuthNPortType port = ss.getAuthNPort();  
            
            {
                System.out.println("Invoking verifyLogin...");
                VerifyLoginReqType verifyLoginReq = new VerifyLoginReqType();
                LoginId loginId = new LoginId();
                loginId.setLoginName("mrt");
                try {
                    VerifyReply reply = port.verifyLogin(verifyLoginReq);
                    SubjectAttributes subAttrs = reply.getSubjectAttributes();
                    printSubjectAttributes(subAttrs);
                } catch (OSCARSFaultMessage e) {
                    System.out
                            .println("Expected exception: OSCARSFaultMessage has occurred.");
                    System.out.println(e.toString());
                }
            }
            {
                System.out.println("Invoking verifyDN...");
                VerifyDNReqType verifyDNReq = new VerifyDNReqType();
                DNType dn= new DNType();
                dn.setSubjectDN("CN=Mary Thompson, DC=net, DC=es");
                dn.setIssuerDN("CN=esnetCA, DC=net, DC=es");
                verifyDNReq.setDN(dn);
                try {
                   VerifyReply reply = port.verifyDN(verifyDNReq);
                   SubjectAttributes subAttrs = reply.getSubjectAttributes();
                   printSubjectAttributes(subAttrs);
                } catch (OSCARSFaultMessage e) {
                    System.out
                            .println("Expected exception: OSCARSFaultMessage has occurred.");
                    System.out.println(e.toString());
                }
            }
        }
    }
    private static void printSubjectAttributes(SubjectAttributes subAttrs) {
        List<AttributeType> subjectAttributes = subAttrs.getSubjectAttribute();
        for (AttributeType subAttr: subjectAttributes) {
            System.out.println(subAttr.getName() + ":" + subAttr.getAttributeValue());
        }
    }
}
