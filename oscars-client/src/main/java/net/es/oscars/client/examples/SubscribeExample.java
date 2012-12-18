package net.es.oscars.client.examples;

import java.net.URL;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.client.WSNotificationClient;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;

/**
 * Example that uses more complex command-line argument processes to
 * subscribe to notifications of a NotificationBroker.
 *
 */
public class SubscribeExample {
    public static void main(String[] args){
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS service to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("k", "keystore"), "the keystore that contains the client certificate").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "user-key"), "the name of the private key in the keystore to use for message signing").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("p", "keystorepass"), "the keystore password").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("keypass"), "the private key password to use (defaults to keystore password)").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("sslkeystore"), "the keystore containing trusted certificates used to validate server https certificates").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("sslkeystorepass"), "the password of the ssl keystore").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("c", "consumer"), "the URL where notifications should be sent").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("t", "topic"), "the subscription topic").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("m", "message-filter"), "an xpath used to filter Notify messages sent for this subscription").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("p", "producer"), "the url of the producer to which you want to subscribe to messages").withRequiredArg().ofType(String.class);
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
        String topic = "";
        String consumerAddr = "";
        String msgXPath = "";
        String producer = "";
        
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
            
            if(opts.has("t")){
                topic = (String) opts.valueOf("t");
            }else{
                System.err.println("Missing required argument 't'");
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
            
            if(opts.has("m")){
                msgXPath = (String) opts.valueOf("m");
            }
            if(opts.has("p")){
                producer = (String) opts.valueOf("p");
            }
            if(opts.has("c")){
                consumerAddr = (String) opts.valueOf("c");
            }else{
                System.err.println("Must provide a consumer address");
                try{
                    parser.printHelpOn(System.out);
                }catch(Exception e){}
                System.exit(1);
            }
            
        }catch(Exception e){
            System.err.println("Error parsing command line: " + e.getMessage());
        }
        
        //Create client and send request
        try {
            //init client
            OSCARSClientConfig.setClientKeystore(keystoreUser, keystore, keystorePass, keyPass);
            if(!"".equals(sslKeystore)){
                OSCARSClientConfig.setSSLKeyStore(sslKeystore, sslKeystorePass);
            }
            
            //create request
            WSNotificationClient client = new WSNotificationClient(url);
            Subscribe subscribeRequest = new Subscribe();
            FilterType filter = new FilterType();
            filter.getTopicExpression().add(WSNotificationClient.createTopic(topic));
            if(!"".equals(producer)){
                filter.getProducerProperties().add(WSNotificationClient.createProducerProps(producer));
            }
            if(!"".equals(msgXPath)){
                filter.getMessageContent().add(WSNotificationClient.createXPathFilter(msgXPath));
            }
            subscribeRequest.setFilter(filter);
            subscribeRequest.setConsumerReference(WSNotificationClient.createEprAddress(consumerAddr));
            
            //send message and print response
            SubscribeResponse response = client.subscribe(subscribeRequest);
            System.out.println("Subscription Reference: " + WSNotificationClient.getEprAddress(response.getSubscriptionReference()));
            System.out.println("Current Time: " + response.getCurrentTime());
            System.out.println("Termination Time: " + response.getTerminationTime());
        } catch (OSCARSClientException e) {
            System.err.println("Error creating oscars client: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error returned trying to contact server: " + e.getMessage());
            System.exit(1);
        }
    }
}
