package net.es.oscars.client.examples;

import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
/**
 * Example of sending a createReservationRequest and polling until 
 * the resources are reserved or the request fails.
 *
 */
public class CreateReservationPoll {
    public static void main(String[] args){
        try {
            /* Setup keystores using the ones created by the sampleDomain/bin/gencerts script */
            String oscarsHome = System.getenv("OSCARS_HOME");
            System.out.println("oscarsHome is " + oscarsHome);
            OSCARSClientConfig.setClientKeystore("mykey", oscarsHome + "/sampleDomain/certs/client.jks", "changeit");
            OSCARSClientConfig.setSSLKeyStore(oscarsHome +"/sampleDomain/certs/localhost.jks", "changeit");

            //initialize client with service URL
            OSCARSClient client = new OSCARSClient("http://localhost:9001/OSCARS");
            
            //Build request for 100Mbps circuit for 1 hour between two points
            ResCreateContent request = new ResCreateContent();
            request.setDescription("Example circuit request");
            UserRequestConstraintType userConstraint = new UserRequestConstraintType();
            userConstraint.setStartTime(System.currentTimeMillis()/1000); //start immediately
            userConstraint.setEndTime(System.currentTimeMillis()/1000 + 3600); //1 hour in the future
            userConstraint.setBandwidth(100);//bandwidth in Mbps
            PathInfo pathInfo = new PathInfo();
            Layer2Info layer2Info = new Layer2Info();
            layer2Info.setSrcEndpoint("urn:ogf:network:domain=testdomain-3:node=switch1:port=3:link=11.2.1.2");
            layer2Info.setDestEndpoint("urn:ogf:network:domain=testdomain-3:node=switch3:port=3:link=11.2.5.1");
            pathInfo.setLayer2Info(layer2Info );
            userConstraint.setPathInfo(pathInfo);
            request.setUserRequestConstraint(userConstraint);
            
            //send request
            CreateReply reply = client.createReservation(request);
            if(!reply.getStatus().equals(OSCARSClient.STATUS_OK)){
                System.err.println("OSCARS returned non-OK status: " + reply.getStatus());
                System.exit(1);
            }
            
            //poll until circuit is ACTIVE
            String resvStatus = "";
            String gri = reply.getGlobalReservationId();
            System.out.println("Global Reservation ID is " + gri);
            while(!resvStatus.equals(OSCARSClient.STATUS_ACTIVE)){
                //send query
                QueryResContent queryRequest = new QueryResContent();
                queryRequest.setGlobalReservationId(gri);
                QueryResReply queryResponse = client.queryReservation(queryRequest);
                
                //check status
                resvStatus = queryResponse.getReservationDetails().getStatus();
                if(resvStatus.equals(OSCARSClient.STATUS_FAILED)){
                    System.err.println("Circuit reservation failed");
                    System.exit(1);
                }
                System.out.println("Reservation status is " + resvStatus);
                
                //sleep for 10 seconds
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.err.println("Sleep interrupted");
                    break;
                } 
            }
            System.out.println("Circuit " + gri + " has been reserved");
        } catch (OSCARSClientException e) {
            System.err.println("Error configuring client: " + e.getMessage());
            System.exit(1);
        } catch (OSCARSFaultMessage e) {
            System.err.println("Error returned from server: " + e.getMessage());
            System.exit(1);
        }
    }
}
