package net.es.oscars.client.examples;

import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;

/**
 * Example of listing all reservations with the RESERVED, ACTIVE and FINISHED statuses
 *
 */
public class ListReservations {
    public static void main(String[] args){
        try {
            /* Setup keystores using the ones created by the sampleDomain/bin/gencerts script */
            String oscarsHome = System.getenv("OSCARS_HOME");
            System.out.println("oscarsHome is " + oscarsHome);
            OSCARSClientConfig.setClientKeystore("mykey", oscarsHome + "/sampleDomain/certs/client.jks", "changeit");
            OSCARSClientConfig.setSSLKeyStore(oscarsHome +"/sampleDomain/certs/localhost.jks", "changeit");

            //initialize client with service URL
            OSCARSClient client = new OSCARSClient("http://localhost:9001/OSCARS");

            //Build request that asks for all ACTIVE, RESERVED and FINISHED reservations
            ListRequest request = new ListRequest();
            request.getResStatus().add(OSCARSClient.STATUS_ACTIVE);
            request.getResStatus().add(OSCARSClient.STATUS_RESERVED);
            request.getResStatus().add(OSCARSClient.STATUS_FINISHED);

            //send request
            ListReply reply = client.listReservations(request);

            //handle case where no reservations returned
            if(reply.getResDetails().size() == 0){
                System.out.println("No ACTIVE or RESERVED reservations found.");
                System.exit(0);
            }

            //print reservations
            for(ResDetails resDetails : reply.getResDetails() ){
                System.out.println("GRI: " + resDetails.getGlobalReservationId());
                System.out.println("Login: " + resDetails.getLogin());
                System.out.println("Status: " + resDetails.getStatus());
                System.out.println("Start Time: " + resDetails.getUserRequestConstraint().getStartTime());
                System.out.println("End Time: " + resDetails.getUserRequestConstraint().getEndTime());
                System.out.println("Bandwidth: " + resDetails.getUserRequestConstraint().getBandwidth());
                System.out.println();
            }
        } catch (OSCARSClientException e) {
            System.err.println("Error configuring client: " + e.getMessage());
            System.exit(1);
        } catch (OSCARSFaultMessage e) {
            System.err.println("Error returned from server: " + e.getMessage());
            System.exit(1);
        }
    }
}
