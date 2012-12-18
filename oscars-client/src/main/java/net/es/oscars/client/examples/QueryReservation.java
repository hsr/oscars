package net.es.oscars.client.examples;

import java.net.URL;
import java.util.Arrays;

import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;


import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Example that uses more complex command line processing, queries a 
 * reservation based on the input provided, and prints the results to 
 * the screen.
 *
 */
public class QueryReservation {
    public static void main(String[] args){
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS service to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("k", "keystore"), "the keystore that contains the client certificate").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "user-key"), "the name of the private key in the keystore to use for message signing").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("p", "keystorepass"), "the keystore password").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("r", "gri"), "the ID of the reservation to query").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("keypass"), "the private key password to use (defaults to keystore password)").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("sslkeystore"), "the keystore containing trusted certificates used to validate server https certificates").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("sslkeystorepass"), "the password of the ssl keystore").withRequiredArg().ofType(String.class);
            }
        };
        
        //Set defaults
        String url = "";
        String keystore = "";
        String keystoreUser = "";
        String keystorePass = "";
        String keyPass = "";
        String sslKeystore = "";
        String sslKeystorePass = "";
        String gri = "";
        
        try{
            //parse options
            OptionSet opts = parser.parse(args);
            
            if(opts.has("h")){
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            
            //Required arguments
            if(opts.has("u")){
                url = (String)opts.valueOf("u");
                new URL(url);
            }else{
                System.err.println("Missing required argument 'u'");
                System.exit(1);
            }
            
            if(opts.has("i")){
                keystoreUser = (String)opts.valueOf("i");
            }else{
                System.err.println("Missing required argument 'i'");
                System.exit(1);
            }
            
            if(opts.has("k")){
                keystore = (String)opts.valueOf("k");
            }else{
                System.err.println("Missing required argument 'k'");
                System.exit(1);
            }
            
            if(opts.has("r")){
                gri = (String)opts.valueOf("r");
            }else{
                System.err.println("Missing required argument 'r'");
                System.exit(1);
            }
            
            //optional arguments
            if(opts.has("p")){
                keystorePass = (String)opts.valueOf("p");
            }
            
            if(opts.has("keypass")){
                keyPass = (String)opts.valueOf("keypass");
            }else{
                keyPass = keystorePass;
            }
            
            if(opts.has("sslkeystore")){
                sslKeystore = (String)opts.valueOf("sslkeystore");
            }
            
            if(opts.has("sslkeystorepass") && (!sslKeystore.equals(""))){
                System.err.println("Provided SSL keystore password but no SSL keystore.");
                System.exit(1);
            }
            
        }catch(Exception e){
            System.err.println("Error parsing command line: " + e.getMessage());
        }
        
        //Create client and send request
        try {
            OSCARSClientConfig.setClientKeystore(keystoreUser, keystore, keystorePass, keyPass);
            if(!"".equals(sslKeystore)){
                OSCARSClientConfig.setSSLKeyStore(sslKeystore, sslKeystorePass);
            }
            OSCARSClient client = new OSCARSClient(url);
            QueryResContent request = new QueryResContent();
            request.setGlobalReservationId(gri);
            QueryResReply response = client.queryReservation(request);
            System.out.println("GRI: " + response.getReservationDetails().getGlobalReservationId());
            ResDetails resDetails = response.getReservationDetails();
            OSCARSClientUtils.printResDetails(resDetails);
        } catch (OSCARSClientException e) {
            System.err.println("Error creating oscars client: " + e.getMessage());
            System.exit(1);
        } catch (OSCARSFaultMessage e) {
            System.err.println("Error returned from server: " + e.getMessage());
            System.exit(1);
        }
    }

}
