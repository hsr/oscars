package net.es.oscars.wsnbroker.utils;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.oasis_open.docs.wsn.b_2.MessageType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.w3c.dom.Document;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.ObjectFactory;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.utils.clients.WSNBrokerClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.notify.TopicDialect;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

public class NotifyUtil {
    WSNBrokerClient client;
    
    public NotifyUtil(String url) throws MalformedURLException, OSCARSServiceException{
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
    
    public void notify(String topic, String producerAddr) throws OSCARSServiceException{
       Notify request = new Notify();
       NotificationMessageHolderType notifyMsgType = new NotificationMessageHolderType();
       TopicExpressionType topicExpr = new TopicExpressionType();
       topicExpr.setDialect(TopicDialect.FULL);
       topicExpr.setValue(topic);
       notifyMsgType.setTopic(topicExpr);
       notifyMsgType.setProducerReference(WSAddrParser.createAddress(producerAddr));
       MessageType msg = new MessageType();
       EventContent event = new EventContent();
       event.setType("RESERVATION_CREATE_COMPLETED");
       event.setTimestamp(System.currentTimeMillis()/1000);
       MessagePropertiesType msgProps = new MessagePropertiesType();
       msgProps.setGlobalTransactionId(UUID.randomUUID().toString());
       event.setMessageProperties(msgProps);
       ObjectFactory objFactory = new ObjectFactory();
       try {
           DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
           dbf.setNamespaceAware(true);
           DocumentBuilder db = dbf.newDocumentBuilder();
           Document doc = db.newDocument();
           JAXBContext jaxbContext = JAXBContext.newInstance("net.es.oscars.api.soap.gen.v06");
           Marshaller marshaller = jaxbContext.createMarshaller();
           marshaller.marshal(objFactory.createEvent(event), doc);
           msg.getAny().add(doc.getDocumentElement());
       } catch (JAXBException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
           System.exit(1);
       } catch (ParserConfigurationException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
           System.exit(1);
       }
       
       notifyMsgType.setMessage(msg);
       request.getNotificationMessage().add(notifyMsgType);
       this.client.getPortType().notify(request);
    }
    
    public static void main(String[] args){
        String url = "http://localhost:9013/OSCARS/wsnbroker";
        String topic = "INFO";
        String producer = "http://localhost:9001/OSCARS";
        
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS notification service to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("t", "topic"), "the subscription topic").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("p", "producer"), "the url of the producer sending the message").withRequiredArg().ofType(String.class);
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
        
        if(opts.has("p")){
            producer = (String) opts.valueOf("p");
        }
        
        try {
            NotifyUtil util = new NotifyUtil(url);
            util.notify(topic, producer);
            System.out.println("Notification sent");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }
        
    }
}
