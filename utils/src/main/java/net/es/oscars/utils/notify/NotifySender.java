package net.es.oscars.utils.notify;

import java.net.MalformedURLException;
import java.util.UUID;
import java.net.URL;
import org.apache.log4j.Logger;
import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.NotificationBridgeClient;
import net.es.oscars.utils.soap.OSCARSServiceException;

/**
 * Sends notification of an OSCARS event to the notification bridge.
 * It should be used by the coordinator to trigger events as they happen.
 */
public class NotifySender {
    static private Logger log = Logger.getLogger(NotifySender.class);
    private NotificationBridgeClient client;
    private String url;
    static private NotifySender instance;
    
    synchronized public static void init(String url) throws OSCARSServiceException{
        if(instance == null){
            instance = new NotifySender(url);
        }
    }

    synchronized public static void init(URL host, URL wsdl) throws OSCARSServiceException{
        if(instance == null){
            instance = new NotifySender(host, wsdl);
        }
    }
    
    private NotifySender(String url) throws OSCARSServiceException{
        try {
            this.client = NotificationBridgeClient.getClient(url);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException(e.getMessage());
        }
        this.url = url;
    }

    private NotifySender(URL host, URL wsdl) throws OSCARSServiceException{
        try {
            this.client = NotificationBridgeClient.getClient(host, wsdl);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException(e.getMessage());
        }
        this.url = host.toString();
    }
    
    /**
     * Method that sends a non-error event given the eventType, msgProps and resDetails
     * 
     * @param eventType the type of event being sent
     * @param msgProps message props of event
     * @param resDetails the details of the reservation affected by the event
     * @throws OSCARSServiceException
     */
    static public void send(String eventType,
                            MessagePropertiesType msgProps,
                            ResDetails resDetails) throws OSCARSServiceException {
        if(instance == null){
            throw new OSCARSServiceException("NotifySender has not been initialized.");
        }
        EventContent event = new EventContent();
        event.setMessageProperties(msgProps);
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(System.currentTimeMillis()/1000);
        event.setType(eventType);
        event.setResDetails(resDetails);
        NotifySender.send(event);
    }
    
    /**
     * Method that sends an error event to the notification bridge
     * 
     * @param eventType the type of event being sent
     * @param msgProps message props of event
     * @param errMsg a message describing the error
     * @param errSource the domain that caused the error
     * @param resDetails the details of the reservation affected by the event.
     * @throws OSCARSServiceException
     */
    static public void sendError(String eventType,
                                 MessagePropertiesType msgProps,
                                 String errMsg,
                                 String errSource,
                                 ResDetails resDetails) throws OSCARSServiceException{
        if(instance == null){
            throw new OSCARSServiceException("NotifySender has not been initialized.");
        }
        EventContent event = new EventContent();
        event.setMessageProperties(msgProps);
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(System.currentTimeMillis()/1000);
        event.setType(eventType);
        event.setErrorMessage(errMsg);
        event.setResDetails(resDetails);
        event.setErrorSource(errSource);
        NotifySender.send(event);
    }
    
    
    /**
    * Sends an event to the notification bridge
    * 
    * @param event the event to send 
    * @throws OSCARSServiceException
    */
    static public void send(EventContent event) throws OSCARSServiceException{
        if(instance == null){
            throw new OSCARSServiceException("NotifySender has not been initialized.");
        }
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        log.debug(netLog.start("NotifySender.send " + event.getType()));
        
        try{
            Object[] req = new Object[] {event};
            instance.getClient().invoke("Notify", req);
        }catch(Exception e){
            log.error(netLog.error("NotifySender.send", ErrSev.CRITICAL, e.getMessage(), instance.getUrl()));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        log.debug(netLog.end("NotifySender.send", null, instance.getUrl()));
    }

    /**
     * @return the client
     */
    public NotificationBridgeClient getClient() {
        return this.client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(NotificationBridgeClient client) {
        this.client = client;
    }

    /**
     * @return the url
     */
    private String getUrl() {
        return this.url;
    }

 }
