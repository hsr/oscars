package net.es.oscars.client.examples;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;

import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.client.WSNotificationClient;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.wsnbroker.soap.gen.SubscribeCreationFailedFault;


/**
 * Example of how to subscribe to notifications and send request a
 * createReservation request. The handling of the message callback is not shown.
 *
 *  The reservation assumes the IDC is handling reservations for testdomain-3.
 *  Thus $OSCARS_HOME/Utils/conf/config.yaml must specify the localDomain:id as testdomaain-3
 */
public class CreateReservationNotify {
    public static void main(String[] args){
        try {
            /* Setup keystores using the ones created by the sampleDomain/bin/gencerts script */
            String oscarsHome = System.getenv("OSCARS_HOME");
            System.out.println("oscarsHome is " + oscarsHome);
            OSCARSClientConfig.setClientKeystore("mykey", oscarsHome + "/sampleDomain/certs/client.jks", "changeit");
            OSCARSClientConfig.setSSLKeyStore(oscarsHome +"/sampleDomain/certs/localhost.jks", "changeit");

            //initialize client with service URL
            OSCARSClient client = new OSCARSClient("http://localhost:9001/OSCARS");
            WSNotificationClient notifyClient = new WSNotificationClient("http://localhost:9013/OSCARS/wsnbroker");
            
            //Subscribe to Notifications
            Subscribe subscribeRequest = new Subscribe();
            FilterType filter = new FilterType();
            filter.getTopicExpression().add(WSNotificationClient.createTopic(OSCARSClient.TOPIC_RESERVATION));
            subscribeRequest.setFilter(filter);
            subscribeRequest.setConsumerReference(
                    WSNotificationClient.createEprAddress("http://localhost:9999/mycallback"));
            
            //send subscribe message and print response
            SubscribeResponse response = notifyClient.subscribe(subscribeRequest);
            System.out.println("Created subscription with ID " + WSNotificationClient.getEprAddress(response.getSubscriptionReference()));
            
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
            System.out.println("Client returned status: " + reply.getStatus());
        } catch (OSCARSClientException e) {
            System.err.println("Error configuring client: " + e.getMessage());
            System.exit(1);
        } catch (OSCARSFaultMessage e) {
            System.err.println("Error returned from OSCARS server: " + e.getMessage());
            System.exit(1);
        } catch (SubscribeCreationFailedFault e) {
            System.err.println("Error returned from Notification server: " + e.getMessage());
            System.exit(1);
        } catch(Exception e){
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
