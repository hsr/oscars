package net.es.oscars.wsnbroker.utils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import net.es.oscars.utils.clients.WSNBrokerClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.notify.TopicDialect;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

public class SubscribeUtil {
    WSNBrokerClient client;
    
    public SubscribeUtil(String url) throws MalformedURLException, OSCARSServiceException{
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_WSNBROKER);
        cc.setContext("DEVELOPMENT");
        cc.setServiceName(ServiceNames.SVC_WSNBROKER);
        try {
            cc.loadManifest(ServiceNames.SVC_WSNBROKER,  ConfigDefaults.MANIFEST); // manifest.yaml
        } catch (ConfigException e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.client = WSNBrokerClient.getClient(url);
    }
    
    public SubscribeResponse subscribe(String consumerAddr, String topic, 
            List<String> producers, List<String> msgXpaths) throws OSCARSServiceException{
        
        Subscribe subscribeRequest = new Subscribe();
        
        //Set topic
        FilterType filter = new FilterType();
        TopicExpressionType topicExpr = new TopicExpressionType();
        topicExpr.setDialect(TopicDialect.FULL);
        topicExpr.setValue(topic);
        filter.getTopicExpression().add(topicExpr);
        subscribeRequest.setFilter(filter);
        
        //set producer
        if(!producers.isEmpty()){
            filter.getProducerProperties().add(NotificationXPathUtil.generateProducerProperties(producers));
        }
        
        //set message filter
        for(String msgXpath : msgXpaths){
            filter.getMessageContent().add(NotificationXPathUtil.generateQueryExpression(msgXpath));
        }
            
        //set subscriber reference
        W3CEndpointReference consumerRef = (new W3CEndpointReferenceBuilder()).address(consumerAddr).build();
        subscribeRequest.setConsumerReference(consumerRef);
        
        try {
            return this.client.getPortType().subscribe(subscribeRequest);
        } catch (Exception e){
            throw new OSCARSServiceException(e.getMessage());
        }
    }
    
    public static void main(String[] args){
        String url = "http://localhost:9013/OSCARS/wsnbroker";
        String topic = "INFO";
        String consumerAddr = "";
        List<String> msgXpaths = new ArrayList<String>();
        List<String> producers = new ArrayList<String>();
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS notification service to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("c", "consumer"), "the URL where notifications should be sent").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("t", "topic"), "the subscription topic").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("m", "message-filter"), "an xpath used to filter Notify messages sent for this subscription").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("p", "producer"), "the url of the producer to which you want to subscribe to messages").withRequiredArg().ofType(String.class);
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
        
        if(opts.has("t")){
            topic = (String) opts.valueOf("t");
        }
        if(opts.has("m")){
            msgXpaths.add((String) opts.valueOf("m"));
        }
        if(opts.has("p")){
            producers.add((String) opts.valueOf("p"));
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
        
        try {
            SubscribeUtil util = new SubscribeUtil(url);
            SubscribeResponse response = util.subscribe(consumerAddr, topic, producers, msgXpaths);
            System.out.println("Subscription Reference: " + WSAddrParser.getAddress(response.getSubscriptionReference()));
            System.out.println("Current Time: " + response.getCurrentTime());
            System.out.println("Termination Time: " + response.getTerminationTime());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }
        
    }
}
