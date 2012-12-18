package net.es.oscars.client.examples;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;

import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.client.WSNotificationClient;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.wsnbroker.soap.gen.SubscribeCreationFailedFault;

/**
 * Example of how to subscribe to notifications and send a ModifyReservation
 * request that extends the startTime by 1 hour. The handling of the message 
 * callback is not show.
 * 
 * usage: java net.es.oscars.client.examples.ModifyReservation <GRI>
 *
 */
public class ModifyReservation {
    public static void main(String[] args){
        try {
            //get the gri from the command-line
            if(args == null || args.length == 0){
                System.err.println("Please provide a GRI.");
                System.exit(1);
            }
            String gri = args[0];
            
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
            SubscribeResponse subResponse = notifyClient.subscribe(subscribeRequest);
            System.out.println("Created subscription with ID " + WSNotificationClient.getEprAddress(subResponse.getSubscriptionReference()));
            
            //create modify request
            long endTime = System.currentTimeMillis()/1000 + 3600; //extend 1 hour
            ModifyResContent request = new ModifyResContent();
            UserRequestConstraintType userConstraints = new UserRequestConstraintType();
            request.setGlobalReservationId(gri);
            userConstraints.setEndTime(endTime);
            userConstraints.setPathInfo(new PathInfo());
            request.setUserRequestConstraint(userConstraints);
            
            //send modify request
            client.modifyReservation(request);
            
        } catch (OSCARSClientException e) {
            System.err.println("Error configuring client: " + e.getMessage());
            System.exit(1);
        } catch (OSCARSFaultMessage e) {
            System.err.println("Error returned from server: " + e.getMessage());
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
