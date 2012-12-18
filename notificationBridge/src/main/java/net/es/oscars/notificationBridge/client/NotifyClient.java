package net.es.oscars.notificationBridge.client;

import java.util.Arrays;
import java.util.UUID;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.utils.notify.NotifySender;
import net.es.oscars.utils.soap.OSCARSServiceException;

public class NotifyClient {
    public static void main(String[] args){
        String url = "http://localhost:9012/notificationBridge";
        String eventType = "RESERVATION_CREATE_COMPLETED";
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS notification service to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("e", "event"), "the event type to throw").withRequiredArg().ofType(String.class);
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
        
        if(opts.has("e")){
            eventType = (String)opts.valueOf("e");
        }else{
            System.err.println("Event type must be specified");
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e){}
            System.exit(1);
        }
        
        
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setGlobalTransactionId(UUID.randomUUID().toString());
        try {
            NotifySender.init(url);
            NotifySender.send(eventType, msgProps, null);
            System.out.println("Notification sent");
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }
    }
}
