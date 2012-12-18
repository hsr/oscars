package net.es.oscars.wsnbroker.utils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.utils.clients.NotificationBridgeClient;
import net.es.oscars.utils.clients.WSNBrokerClient;
import net.es.oscars.utils.soap.OSCARSServiceException;

public class SubscribeListUtil {
    WSNBrokerClient client;
    
    public SubscribeListUtil(String url) throws MalformedURLException, OSCARSServiceException{
        this.client = WSNBrokerClient.getClient(url);
    }
    
   /* public OscarsListSubscriptionsResponse list(List<String> statuses, List<String> users, List<String> ids) throws OSCARSServiceException{
        
        
        try {
            OscarsListSubscriptions request = new OscarsListSubscriptions();
            MessagePropertiesType msgProps = new MessagePropertiesType();
            msgProps.setGlobalTransactionId(UUID.randomUUID().toString());
            request.setMessageProperties(msgProps);
            request.getStatus().addAll(statuses);
            request.getUser().addAll(users);
            request.getSubscriptionId().addAll(ids);
            return this.client.getPortType().listSubscriptions(request);
        } catch (Exception e){
            throw new OSCARSServiceException(e.getMessage());
        }
    }
    
    public static void main(String[] args){
        String url = "http://localhost:9013/OSCARS/wsnbroker";
        
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS notification service to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("s", "status"), "the status of subscriptions to return. Can be ACTIVE, INACTIVE, PAUSED, or ALL. Defaults to ALL.").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("l", "login"), "the login of the user that created the subscriptions to be returned").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "id"), "the id of the subscription to return").withRequiredArg().ofType(String.class);
            }
        };
        
        OptionSet opts = parser.parse(args);
        if(opts.has("h")){
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e){}
            System.exit(0);
        }
        
        if(opts.has("u")){
            url = (String)opts.valueOf("u");
        }
        
        List<String> statuses = new ArrayList<String>();
        if(opts.has("s")){
            String status = (String) opts.valueOf("s");
            statuses.add(status);
        }else{
            statuses.add(SubscriptionStatus.ALL_STRING);
        }
        
        List<String> logins = new ArrayList<String>();
        if(opts.has("l")){
            String login = (String) opts.valueOf("l");
            logins.add(login);
        }
        
        List<String> ids = new ArrayList<String>();
        if(opts.has("i")){
            String id = (String) opts.valueOf("i");
            ids.add(id);
        }
        
        try {
            SubscribeListUtil util = new SubscribeListUtil(url);
            OscarsListSubscriptionsResponse response = util.list(statuses, logins, ids);
            for(OscarsSubscription subscrip : response.getSubscription()){
                System.out.println("ID: " + WSAddrParser.getAddress(subscrip.getSubscriptionReference()));
                System.out.println("Status: " + subscrip.getStatus());
                System.out.println("User: " + subscrip.getUser());
                System.out.println("Created Time: " + subscrip.getCreatedTime());
                System.out.println("Termination Time: " + subscrip.getTerminationTime());
                System.out.println("Consumer URL: " + WSAddrParser.getAddress(subscrip.getConsumerReference()));
                for(TopicExpressionType topic : subscrip.getFilter().getTopicExpression()){
                    System.out.println("Topic: " + topic.getValue());
                }
                for(QueryExpressionType prodFilter : subscrip.getFilter().getProducerProperties()){
                    System.out.println("Producer: " + prodFilter.getValue());
                }
                for(QueryExpressionType msgFilter : subscrip.getFilter().getMessageContent()){
                    System.out.println("Message Filter: " + msgFilter.getValue());
                }
                for(String authzDomain : subscrip.getAuthorizedDomain()){
                    System.out.println("Authorized Domain: " + authzDomain);
                }
                for(String authzLogin : subscrip.getAuthorizedLogin()){
                    System.out.println("Authorized Login: " + authzLogin);
                }
                
                System.out.println();
            }
            System.out.println("Request returned " + response.getSubscription().size() + " results.");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }
        
    } */
}
