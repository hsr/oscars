package net.es.oscars.wsnbroker.utils;

import java.net.MalformedURLException;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.notify.NotifySender;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

public class NotifySenderTest {
    NotifySender sender;
    
    public NotifySenderTest(String url) throws MalformedURLException, OSCARSServiceException{
       // this.sender = new NotifySender(url);
    }
    
    public void sendNotify(String eventType) throws OSCARSServiceException{
      // this.sender.send(eventType.toString(), "user", null);
    }
    
    public static void main(String[] args){
        String url = "http://localhost:9013/OSCARS/wsnbroker";
        String eventType = "RESERVATION_CREATE_COMPLETED";
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_WSNBROKER);
        cc.setServiceName(ServiceNames.SVC_WSNBROKER);
        cc.setContext("DEVELOPMENT");
        try {
            cc.loadManifest(ServiceNames.SVC_WSNBROKER,  ConfigDefaults.MANIFEST); // manifest.yaml
        } catch (ConfigException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        
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
        
        
        try {
            NotifySenderTest util = new NotifySenderTest(url);
            util.sendNotify(eventType);
            System.out.println("Notification sent");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }
        
    }
}
