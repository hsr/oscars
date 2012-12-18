package net.es.oscars.notificationBridge.http;

import java.util.UUID;

import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.notificationBridge.NotificationBridgeGlobals;
import net.es.oscars.notificationBridge.soap.gen.NotificationBridgePortType;
import net.es.oscars.utils.svc.ServiceNames;

@javax.jws.WebService(
        serviceName = ServiceNames.SVC_NOTIFY,
        portName = "NotificationBridgePort",
        targetNamespace = "http://oscars.es.net/OSCARS/notificationBridge",
        endpointInterface = "net.es.oscars.notificationBridge.soap.gen.NotificationBridgePortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class NotificationBridgeSoapHandler implements NotificationBridgePortType{
    Logger log = Logger.getLogger(NotificationBridgeSoapHandler.class);
    private NotificationBridgeGlobals globals;
   
    public NotificationBridgeSoapHandler(){
        OSCARSNetLogger netLogger = this.initNetLogger(null);
        this.log.info(netLogger.start("init"));
        
        //init globals
        try {
            this.globals = NotificationBridgeGlobals.getInstance();
        } catch (Exception e) {
            this.log.info(netLogger.error("init", ErrSev.FATAL, e.getMessage()));
            System.exit(1);
        }
        
        this.log.info(netLogger.end("init"));
    }
    
    public void notify(EventContent event) {
        OSCARSNetLogger netLogger = this.initNetLogger(event.getMessageProperties());
        this.log.info(netLogger.start("notify"));
        try{
            this.globals.getObservable().fireEvent(event);
        }catch(Exception e){
            this.log.info(netLogger.error("notify", ErrSev.CRITICAL, e.getMessage()));
        }
        this.log.info(netLogger.end("notify"));
    }

    private OSCARSNetLogger initNetLogger(MessagePropertiesType msgProps) {
        String guid = null;
        if(msgProps != null && msgProps.getGlobalTransactionId() != null){
            guid = msgProps.getGlobalTransactionId();
        }else{
            guid = UUID.randomUUID().toString();
        }
        OSCARSNetLogger netLogger = new OSCARSNetLogger(ModuleName.NOTIFY, guid);
        OSCARSNetLogger.setTlogger(netLogger);
        
        return netLogger;
    }
}
