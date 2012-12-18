package net.es.oscars.notificationBridge.observers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.ObjectFactory;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.notificationBridge.NotificationBridgeObservable;
import net.es.oscars.utils.clients.WSNBrokerClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.notify.FilterNamespaceContext;
import net.es.oscars.utils.notify.NotifyNSUtil;
import net.es.oscars.utils.notify.TopicDialect;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.MessageType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WSNObserver implements Observer{
    Logger log = Logger.getLogger(WSNObserver.class);
    private WSNBrokerClient client = null;
    private String brokerUrl;
    private String brokerWsdl;
    private String producerUrl;
    private HashMap<String, String> topics = null;
    private Map<String, String> namespaceMap = null;
    
    final private String PROP_BROKER_URL = "wsn.url";
    final private String PROP_BROKER_WSDL_URL = "wsn.wsdl";
    final private String PROP_PRODUCER_URL = "wsn.producer";
    
    final private String TOPIC_SET_FILE = "topicset.xml";
    final private String TOPIC_NAMESPACE_FILE = "topicnamespace.xml";
    final private String TOPIC_NS_URI = "http://docs.oasis-open.org/wsn/t-1";
    final private String EVENT_CLASS = "net.es.oscars.api.soap.gen.v06";
    
    public WSNObserver() throws OSCARSServiceException{
        this.loadTopics();
        this.namespaceMap = NotifyNSUtil.getNamespaceMap();
        System.out.println("this.namespaceMap");
        for (String key : this.namespaceMap.keySet()){
            System.out.println(key + " " + this.namespaceMap.get(key));
        }
    }
    
    public void update(Observable obs, Object eventObj) {
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.info(netLog.start("WSNObserver.update"));
        try{
            EventContent inEvent =  (EventContent) eventObj;
            this.initClient((NotificationBridgeObservable) obs);
            Notify notifyRequest = new Notify();
            NotificationMessageHolderType notifyMsgHolder = new NotificationMessageHolderType();
            
            //convert EventContent to idc:event DOM element
            MessageType msg = new MessageType();
            ObjectFactory objFactory = new ObjectFactory();
            Document eventDoc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            eventDoc = db.newDocument();
            //TODO: Should be a better way than hard-coding class
            JAXBContext jaxbContext = JAXBContext.newInstance(EVENT_CLASS);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(objFactory.createEvent(inEvent), eventDoc);
            msg.getAny().add(eventDoc.getDocumentElement());
            
            //set the message, topic and producer reference
            notifyMsgHolder.setMessage(msg);
            notifyMsgHolder.setTopic(this.generateTopicExpression(eventDoc.getDocumentElement()));
            notifyMsgHolder.setProducerReference(
                    (new W3CEndpointReferenceBuilder()).address(this.producerUrl).build());
            notifyRequest.getNotificationMessage().add(notifyMsgHolder);
            //send request
            Object[] req = new Object[] {notifyRequest};
            this.client.invoke("Notify", req);
        }catch(Exception e){
            e.printStackTrace();
            this.log.info(netLog.error("WSNObserver.update", ErrSev.CRITICAL, e.getMessage()));
            return;
        }
        this.log.info(netLog.end("WSNObserver.update"));
    }
    
    synchronized private void initClient(NotificationBridgeObservable obs) throws OSCARSServiceException{
        if(this.client != null){
            return;
        }
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.info(netLog.start("initClient"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        try{
            //init broker URL and WSDL
            Map config = obs.getConfig();
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_NOTIFY);
            
            /* The broker URL specifies the URL of the external NotificationBroker 
             * to contact. If it is not explicitly defined in teh file then the code 
             * looks to see if the WSNBroker service is on the same machine, and reads 
             * the publishTo property from its config file.
             */
            if(config.containsKey(PROP_BROKER_URL)){
                this.brokerUrl = (String) config.get(PROP_BROKER_URL);
            }else{
                HashMap<String,Object> oscarsConfig = (HashMap<String,Object>)ConfigHelper.getConfiguration(cc.getFilePath(ServiceNames.SVC_WSNBROKER, cc.getContext(),
                        ConfigDefaults.CONFIG));
                Map soap = (HashMap<String,Object>) oscarsConfig.get("soap");
                if (soap == null ){
                    throw new ConfigException("The notificationBridge cannot find " +
                            "the required property " + PROP_BROKER_URL);
                }
                this.brokerUrl = (String)soap.get("publishTo");
            }
            
            /* Get the WSDL URL since CXF needs it. Defaults to the broker URL 
             * appended with '?wsdl'
             */
            if(config.containsKey(PROP_BROKER_WSDL_URL)){
                this.brokerWsdl = (String) config.get(PROP_BROKER_WSDL_URL);
            }else {
                //default to service URL plus ?wsdl since thats the cxf default
                this.brokerWsdl = cc.getWSDLPath(ServiceNames.SVC_WSNBROKER, null) + "";
            }
            netLogProps.put("wsdl", this.brokerWsdl);
            
            /* The producer URL is a property that identifies who sent the event.
             * It can be explicitly set in the properties file but if it is not found then 
             * the service tries to use the OSCARS name as the default. We made need to 
             * set this differently.
             */
            if(config.containsKey(PROP_PRODUCER_URL)){
                this.producerUrl = (String) config.get(PROP_PRODUCER_URL);
            }else {
                HashMap<String,Object> oscarsConfig = (HashMap<String,Object>)ConfigHelper.getConfiguration(cc.getFilePath(ServiceNames.SVC_API,cc.getContext(),
                        ConfigDefaults.CONFIG));
                Map soap = (HashMap<String,Object>) oscarsConfig.get("soap");
                if (soap == null ){
                    throw new ConfigException("The notificationBridge cannot find " +
                            "the required property " + PROP_PRODUCER_URL);
                }
                this.producerUrl = (String)soap.get("publishTo");
            }
            netLogProps.put("producer", this.producerUrl);
            //set client 
            this.client = WSNBrokerClient.getClient(this.brokerUrl, this.brokerWsdl);
        }catch(Exception e){
            this.log.info(netLog.error("initClient", ErrSev.CRITICAL, e.getMessage(), 
                    this.brokerUrl, netLogProps));
            throw new OSCARSServiceException(e.getMessage());
        }
        this.log.info(netLog.end("initClient", null, this.brokerUrl, netLogProps));
    }
    
    /**
    * Loads Topics from an XML file. The file must follow the WS-Topics standard
    * listed by OASIS (http://docs.oasis-open.org/wsn/wsn-ws_topics-1.3-spec-os.pdf).
    * It does NOT support the option "Extension Topics" listed in section 6.1 of the
    * WS-Topics specification. This is intended to allow for lightweight activation/deactivation
    * of notifications about certain topics.
    *
    * @throws OSCARSServiceException 
    */
   private void loadTopics() throws OSCARSServiceException{
       topics = new HashMap<String, String>();
       
       //Step 0: Load topic config file names and namespaces
       ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_NOTIFY);
       cc.setServiceName(ServiceNames.SVC_NOTIFY);
       String topicSetFilename = null;
       String topicNsFilename = null;
       try {
           topicSetFilename = cc.getFilePath(ServiceNames.SVC_NOTIFY,cc.getContext(),
                   TOPIC_SET_FILE);
           topicNsFilename = cc.getFilePath(ServiceNames.SVC_NOTIFY,cc.getContext(),
                   TOPIC_NAMESPACE_FILE);
       } catch (ConfigException e) {
           throw new OSCARSServiceException("Unable to load topic set XML file: " + e.getMessage());
       }
       Map<String, String> prefixMap = NotifyNSUtil.getPrefixMap();
       
       //Step 1: Figure out which Topics are supported
       //open topicset
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       dbf.setNamespaceAware(true);
       DocumentBuilder db = null;
       Document topicSetDoc = null;
       try {
           db = dbf.newDocumentBuilder();
           topicSetDoc = db.parse(new File(topicSetFilename));
       } catch (Exception e) {
           e.printStackTrace();
           throw new OSCARSServiceException(e.getMessage());
       }
       
       Element topicSetRoot = topicSetDoc.getDocumentElement();
       ArrayList<Element> topicSetElems = new ArrayList<Element>();
       ArrayList<String> topicSetParents = new ArrayList<String>();
       HashMap<String, Boolean> supportedTopics = new HashMap<String, Boolean>();
       //initialize topic set
       topicSetElems.addAll(0, this.nodesToList(topicSetRoot.getChildNodes()));
       //load this.topics with name,xpath entries
       while(!topicSetElems.isEmpty()){
           Element currElem = topicSetElems.get(0);
           String name = currElem.getLocalName();
           NodeList children = currElem.getChildNodes();
           //check if working way back up tree
           if((!topicSetParents.isEmpty()) && name.equals(topicSetParents.get(0))){
               topicSetElems.remove(0);
               topicSetParents.remove(0);
               continue;
           }
           String namespaceURI = currElem.getNamespaceURI();
           String prefix = prefixMap.get(namespaceURI);
           String isTopic = currElem.getAttributeNS(TOPIC_NS_URI, "topic");
           if("true".equals(isTopic)){
               String completeName = prefix + ":";
               for(int i = (topicSetParents.size() - 1); i >= 0; i--){
                   completeName += (topicSetParents.get(i) + "/");
               }
               completeName += name;
               supportedTopics.put(completeName, true);
           }
           
           if(children.getLength() == 0){
               topicSetElems.remove(0);
           }else{
               topicSetParents.add(0, name);
               topicSetElems.addAll(0, this.nodesToList(children));
           }
       }

       //Step 2: Find supported topics in topic namespace
       //load file
       Document topicNSDoc = null;
       try {
           topicNSDoc = db.parse(new File(topicNsFilename));
       } catch (Exception e) {
           e.printStackTrace();
           throw new OSCARSServiceException(e.getMessage());
       }
       Element topicNSRoot = topicNSDoc.getDocumentElement();
       String targetNamespace = topicNSRoot.getAttribute("targetNamespace");
       String prefix = prefixMap.get(targetNamespace);
       ArrayList<Element> topicNSElems = new ArrayList<Element>();
       ArrayList<String> parents = new ArrayList<String>();
       //topicNSElems.addAll(0, topicNSRoot.getChildren("Topic", wstop));
       topicNSElems.addAll(0, this.getChildren(topicNSRoot, TOPIC_NS_URI, "Topic"));
       while(!topicNSElems.isEmpty()){
           Element currElem = topicNSElems.get(0);
           String completeName = prefix + ":";
           String name = currElem.getAttributeNS(TOPIC_NS_URI, "name");
           if(name == null){
               topicNSElems.remove(0);
               continue;
           }
           
           List<Element> children = this.getChildren(currElem, TOPIC_NS_URI, "Topic");
           //check if working way back up tree
           if((!parents.isEmpty()) && name.equals(parents.get(0))){
               topicNSElems.remove(0);
               parents.remove(0);
               continue;
           }

           for(int i = (parents.size() - 1); i >= 0; i--){
               completeName += (parents.get(i) + "/");
           }
           completeName += name;
           if(!supportedTopics.containsKey(completeName)){
               topicNSElems.remove(0);
               continue;
           }
           if(children.isEmpty()){
               topicNSElems.remove(0);
           }else{
               parents.add(0, name);
               topicNSElems.addAll(0, children);
           }
           
           List<Element> msgChilds = this.getChildren(currElem, TOPIC_NS_URI, "MessagePattern");
           if(msgChilds == null || msgChilds.isEmpty()){
               continue;
           }
           String dialect = msgChilds.get(0).getAttributeNS(TOPIC_NS_URI, "Dialect");
           if(TopicDialect.XPATH.equals(dialect)){
               String xpath = msgChilds.get(0).getTextContent();
               topics.put(completeName, xpath);
           }
       }
   }

   private List<Element> getChildren(Element parentElem, String namespaceUri,
           String tagName) {
       ArrayList<Element> tmpList = new ArrayList<Element>();
       NodeList childNodes =  parentElem.getChildNodes();
       for(int i = 0; i < childNodes.getLength(); i++){
           //ignore text nodes and extraneous data
           if(!(childNodes.item(i) instanceof Element)){
               continue;
           }
           Element child = (Element) childNodes.item(i);
           if(tagName.equals(child.getLocalName()) && 
               namespaceUri.equals(child.getNamespaceURI())){
               tmpList.add(child);
           }
       }
       return tmpList;
   }

   private List<Element> nodesToList(NodeList childNodes) {
       ArrayList<Element> tmpList = new ArrayList<Element>();
       for(int i = 0; i < childNodes.getLength(); i++){
           //ignore text nodes and extraneous data
           if(childNodes.item(i) instanceof Element){
               tmpList.add((Element) childNodes.item(i));
           }
       }
       return tmpList;
   }
   
   /**
    * Matches an event to a topic then returns a TopicExpression
    *
    * @param event the event to classify as belonging to a topic
    * @return a topic expression for the given event
    */
   private TopicExpressionType generateTopicExpression(Element event)
                   throws OSCARSServiceException{
       TopicExpressionType topicExpr = new TopicExpressionType();
       topicExpr.setDialect(TopicDialect.FULL);
       String topicString = "";
       boolean firstMatch = true;
       for(String topic : topics.keySet()){
           String xpathStr = topics.get(topic);
           /* Prepare message for parsing by adding outer element to appease axis2 */
           XPath xpath = XPathFactory.newInstance().newXPath();
           xpath.setNamespaceContext(new FilterNamespaceContext(this.namespaceMap));
           XPathExpression xpathExpr = null;
           Boolean xpathResult = null;
           try {
               xpathExpr = xpath.compile(xpathStr);
               xpathResult = (Boolean) xpathExpr.evaluate(event, XPathConstants.BOOLEAN);
           } catch (XPathExpressionException e) {
               e.printStackTrace();
               throw new OSCARSServiceException(e.getMessage());
           }
           if(xpathResult){
               topicString += (firstMatch ? "" : "|");
               topicString += (topic);
               firstMatch = false;
           }
       }
       if("".equals(topicString)){
           return null;
       }
       topicExpr.setValue(topicString);

       return topicExpr;
   }

}
