package net.es.oscars.utils.clients;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;
import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.notificationBridge.soap.gen.NotificationBridgePortType;
import net.es.oscars.notificationBridge.soap.gen.NotificationBridgeService;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

@OSCARSNetLoggerize(moduleName = ModuleName.NOTIFY)
@OSCARSService (
        implementor = "net.es.oscars.notificationBridge.soap.gen.NotificationBridgeService",
        namespace = "http://oscars.es.net/OSCARS/notificationBridge", 
        serviceName = ServiceNames.SVC_NOTIFY
)
public class NotificationBridgeClient extends OSCARSSoapService<NotificationBridgeService, NotificationBridgePortType>{

    static private Logger log = Logger.getLogger(NotificationBridgeClient.class);
    private String hostName = null;
    /**
     * Constructor. 
     * 
     * @param host The location of the host to contact
     * @param wsdl The location of the WSDL file for this service
     * @throws OSCARSServiceException
     */
    public NotificationBridgeClient(URL host, URL wsdl) throws OSCARSServiceException {
        super(host, wsdl, NotificationBridgePortType.class);
        this.hostName = host.toString();
    }
    
    /**
     * Creates a NotificationBridgeClient object that can be used to call the notification 
     * service given only the address of the service. The location of the WSDL
     * is assumed to be <i>host</i>?wsdl.
     * 
     * @param url The location of the service to contact as a URL string
     * @return A NotificationBridgeClient object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public NotificationBridgeClient getClient(String url) 
        throws OSCARSServiceException, MalformedURLException{
        return  getClient(new URL(url), new URL(url+"?wsdl"));
    }
    /**
     * Creates a NotificationBridgeClient object that can be used to call the Notification 
     * service.
     * 
     * @param url The location of the service to contact as a URL string
     * @param wsdl The location of the service wsdl as a URL string
     * @return A NotificationBridgeClient object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public NotificationBridgeClient getClient(String url, String wsdl) 
                throws OSCARSServiceException, MalformedURLException {
        
            return getClient (new URL(url), new URL(wsdl));
    }
    /**
     * Creates a NotificationBridgeClient object that can be used to call the Notification 
     * service.
     * 
     * @param url The location of the service to contact as a URL
     * @param wsdl The location of the service wsdl as a URL
     * @return A NotificationBridgeClient object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public NotificationBridgeClient getClient(URL url, URL wsdl) 
            throws OSCARSServiceException, MalformedURLException{
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "getNotificationBridgeClient";
        ContextConfig cc = ContextConfig.getInstance();
        try {
            String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
            OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
        } catch (ConfigException e) {
            log.error(netLogger.error(event,ErrSev.MAJOR, "NotificationBridgeClient caught ConfigException"));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        if(wsdl == null){
            wsdl = new URL (new String (url.toString()+"?wsdl"));
        }
        return new NotificationBridgeClient(url,wsdl);
    }
    /**
     * 
     * @return the hostName for the notificationBridge that is being called
     */
    public String getHostName() {
        return hostName;
    }
    /**
     * Method that sends a non-error event given the eventType, msgProps and resDetails
     * 
     * @param eventType the type of event being sent
     * @param msgProps message props of event
     * @param resDetails the details of the reservation affected by the event
     * @throws OSCARSServiceException
     */
    public void send(String eventType, MessagePropertiesType msgProps, 
            ResDetails resDetails) throws OSCARSServiceException{
        
        EventContent event = new EventContent();
        event.setMessageProperties(msgProps);
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(System.currentTimeMillis()/1000);
        event.setType(eventType);
        event.setResDetails(resDetails);
        this.send(event);
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
    public void sendError(String eventType, MessagePropertiesType msgProps, 
            String errMsg, String errSource, ResDetails resDetails) throws OSCARSServiceException{
        
        EventContent event = new EventContent();
        event.setMessageProperties(msgProps);
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(System.currentTimeMillis()/1000);
        event.setType(eventType);
        event.setErrorMessage(errMsg);
        event.setResDetails(resDetails);
        send(event);
    }
    
    
    /**
    * Sends an event to the notification bridge
    * 
    * @param event the event to send 
    * @throws OSCARSServiceException
    */
    public void send(EventContent event) throws OSCARSServiceException{
        
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        log.debug(netLog.start("NotifySender.send", this.hostName));
        
        try{
            this.getPortType().notify(event);
        }catch(Exception e){
            log.error(netLog.error("NotifySender.send", ErrSev.CRITICAL, e.getMessage(), this.hostName));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        log.debug(netLog.end("NotifySender.send", null, this.hostName));
    }

}
