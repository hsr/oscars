package net.es.oscars.client.examples;

import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.TeardownPathContent;
import net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;

/**
 * Example of how to send a teardownPath request and poll until 
 * the path is down.
 * 
 * usage: java net.es.oscars.client.examples.TeardownPath <GRI>
 *
 */
public class TeardownPath {
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
            
            //create request
            TeardownPathContent request = new TeardownPathContent();
            request.setGlobalReservationId(gri);
            
;           //send request
            TeardownPathResponseContent response = client.teardownPath(request);
            
            //display result
            if(OSCARSClient.STATUS_OK.equals(response.getStatus())){
                System.out.println("The teardown request was received");
            }else{
                System.err.println("The request returned status " + response.getStatus());
                System.exit(1);
            }
            
            //poll until reservation is down
            String resvStatus = "";
            while(resvStatus.equals(OSCARSClient.STATUS_INTEARDOWN)){
                //send query
                QueryResContent queryRequest = new QueryResContent();
                queryRequest.setGlobalReservationId(gri);
                QueryResReply queryResponse = client.queryReservation(queryRequest);
                resvStatus = queryResponse.getReservationDetails().getStatus();
                System.out.println("Reservation status is " + resvStatus);
                
                //sleep for 10 seconds
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.err.println("Sleep interrupted");
                    break;
                } 
            }
            if(OSCARSClient.STATUS_RESERVED.equals(resvStatus) || 
                    OSCARSClient.STATUS_FINISHED.equals(resvStatus)){
                System.out.println("Circuit" + gri + " teardown succeeded");
            }else{
                System.out.println("Circuit" + gri + " teardown failed");
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
