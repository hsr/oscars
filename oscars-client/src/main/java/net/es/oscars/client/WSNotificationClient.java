package net.es.oscars.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.PauseSubscription;
import org.oasis_open.docs.wsn.b_2.PauseSubscriptionResponse;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.ResumeSubscription;
import org.oasis_open.docs.wsn.b_2.ResumeSubscriptionResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.w3c.dom.Document;

import net.es.oscars.wsnbroker.soap.gen.InvalidFilterFault;
import net.es.oscars.wsnbroker.soap.gen.InvalidMessageContentExpressionFault;
import net.es.oscars.wsnbroker.soap.gen.InvalidProducerPropertiesExpressionFault;
import net.es.oscars.wsnbroker.soap.gen.InvalidTopicExpressionFault;
import net.es.oscars.wsnbroker.soap.gen.NotifyMessageNotSupportedFault;
import net.es.oscars.wsnbroker.soap.gen.PauseFailedFault;
import net.es.oscars.wsnbroker.soap.gen.ResourceUnknownFault;
import net.es.oscars.wsnbroker.soap.gen.ResumeFailedFault;
import net.es.oscars.wsnbroker.soap.gen.SubscribeCreationFailedFault;
import net.es.oscars.wsnbroker.soap.gen.TopicExpressionDialectUnknownFault;
import net.es.oscars.wsnbroker.soap.gen.TopicNotSupportedFault;
import net.es.oscars.wsnbroker.soap.gen.UnableToDestroySubscriptionFault;
import net.es.oscars.wsnbroker.soap.gen.UnacceptableInitialTerminationTimeFault;
import net.es.oscars.wsnbroker.soap.gen.UnacceptableTerminationTimeFault;
import net.es.oscars.wsnbroker.soap.gen.UnrecognizedPolicyRequestFault;
import net.es.oscars.wsnbroker.soap.gen.UnsupportedPolicyRequestFault;
import net.es.oscars.wsnbroker.soap.gen.WSNBrokerPortType;
import net.es.oscars.wsnbroker.soap.gen.WSNBrokerService;

public class WSNotificationClient extends Client<WSNBrokerPortType>{
    
    //Constants useful for clients
    final public static String TOPIC_DIALECT_SIMPLE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";
    final public static String TOPIC_DIALECT_CONCRETE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";
    final public static String TOPIC_DIALECT_FULL = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full";
    final public static String TOPIC_DIALECT_XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    
    final private String NAMESPACE = "http://oscars.es.net/OSCARS/wsnbroker";
    final private String SERVICE_NAME = "WSNBrokerService";
    
    public WSNotificationClient(String serviceUrl, String wsdlUrl) throws OSCARSClientException{
        //Create PortType
        URL wsdlUrlObj = null;
        try {
            wsdlUrlObj = new URL(wsdlUrl);
        } catch (MalformedURLException e) {
            throw new OSCARSClientException("Malformed URL " + wsdlUrl);
        }
        this.prepareSSLForWSDL();
        WSNBrokerService service = new WSNBrokerService(wsdlUrlObj, new QName (NAMESPACE, SERVICE_NAME));
        this.portType = (WSNBrokerPortType) service.getPort(WSNBrokerPortType.class);
        this.setServiceEndpoint(serviceUrl);
    }
    
    public WSNotificationClient(String serviceUrl) throws OSCARSClientException{
        this(serviceUrl, serviceUrl + "?wsdl");
    }
    
    public SubscribeResponse subscribe(Subscribe request) throws OSCARSClientException, TopicExpressionDialectUnknownFault, SubscribeCreationFailedFault, UnacceptableInitialTerminationTimeFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault, UnrecognizedPolicyRequestFault, InvalidProducerPropertiesExpressionFault, UnsupportedPolicyRequestFault, InvalidTopicExpressionFault, ResourceUnknownFault, NotifyMessageNotSupportedFault, InvalidFilterFault{
        this.prepareClient();
        return this.portType.subscribe(request);
    }
    
    public RenewResponse renew(Renew request) throws OSCARSClientException, UnacceptableTerminationTimeFault, ResourceUnknownFault {
        this.prepareClient();
        return this.portType.renew(request);
    }
    public UnsubscribeResponse unsubscribe(Unsubscribe request) throws OSCARSClientException, UnableToDestroySubscriptionFault, ResourceUnknownFault{
        this.prepareClient();
        return this.portType.unsubscribe(request);
    }
    
    public PauseSubscriptionResponse pauseSubscription(PauseSubscription request) throws OSCARSClientException, ResourceUnknownFault, PauseFailedFault{
        this.prepareClient();
        return this.portType.pauseSubscription(request);
    }
    
    public ResumeSubscriptionResponse resumeSubscription(ResumeSubscription request) throws OSCARSClientException, ResumeFailedFault, ResourceUnknownFault{
        this.prepareClient();
        return this.portType.resumeSubscription(request);
    }
    
    public void notify(Notify request) throws OSCARSClientException{
        this.prepareClient();
        this.portType.notify(request);
    }
    
    //utility functions
    static public String getEprAddress(W3CEndpointReference epr){
        //Create instance of DocumentBuilderFactory
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         //Get the DocumentBuilder
         DocumentBuilder docBuilder = null;
         try {
             docBuilder = docBuilderFactory.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             return null;
         }
         Document doc = docBuilder.newDocument();
         DOMResult domResult = new DOMResult(doc);
         epr.writeTo(domResult);
         if(doc == null){
             return null;
         }
         if(doc.getElementsByTagName("Address") == null || 
                 doc.getElementsByTagName("Address").getLength() != 1){
             return null;
         }
         
         return doc.getElementsByTagName("Address").item(0).getTextContent();
     }
     
    static public W3CEndpointReference createEprAddress(String uri){
        return (new W3CEndpointReferenceBuilder()).address(uri).build();
    }
     
    static public TopicExpressionType createTopic(String topicString){
        TopicExpressionType topicExpr = new TopicExpressionType();
        topicExpr.setDialect(WSNotificationClient.TOPIC_DIALECT_FULL);
        topicExpr.setValue(topicString);
        return topicExpr;
    }
    
    static public TopicExpressionType createTopic(List<String> topicList){
        String topicString = "";
        boolean multiple = false;
        for(String topic : topicList){
            topicString += (multiple ? "|" : "");
            topicString += topic;
            multiple = true;
        }
        return createTopic(topicString);
    }
    
    static public QueryExpressionType createXPathFilter(String xpath){
        QueryExpressionType query = new QueryExpressionType();
        query.setDialect(WSNotificationClient.TOPIC_DIALECT_XPATH);
        query.setValue(xpath);
        return query;
    }
    
    static public QueryExpressionType createProducerProps(String producer){
        ArrayList<String> producers = new ArrayList<String>();
        producers.add(producer);
        return createProducerProps(producers);
    }
    
    static public QueryExpressionType createProducerProps(List<String> producers){
        boolean multiple = false;
        String xpath = "";
        for(String producer : producers){
            xpath += (multiple ? " or " : "");
            xpath += "/wsa:Address='" + producer + "'";
            multiple = true;
        }
        QueryExpressionType query = new QueryExpressionType();
        query.setDialect(WSNotificationClient.TOPIC_DIALECT_XPATH);
        query.setValue(xpath);
        
        return query;
    }
}
