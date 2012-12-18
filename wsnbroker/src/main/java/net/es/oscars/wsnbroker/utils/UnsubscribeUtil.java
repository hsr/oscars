package net.es.oscars.wsnbroker.utils;

import java.net.MalformedURLException;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;

import net.es.oscars.utils.clients.WSNBrokerClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

public class UnsubscribeUtil {
    WSNBrokerClient client;
    
    public UnsubscribeUtil(String url) throws MalformedURLException, OSCARSServiceException{
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
    
    public UnsubscribeResponse unsubscribe(String id) throws OSCARSServiceException{
        Unsubscribe unsubRequest = new Unsubscribe();
        unsubRequest.setSubscriptionReference(WSAddrParser.createAddress(id));
        try {
            return this.client.getPortType().unsubscribe(unsubRequest);
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
                acceptsAll(Arrays.asList("i", "id"), "the id of the subscription to cancel").withRequiredArg().ofType(String.class);
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
        
        String id = null;
        if(opts.has("i")){
            id = (String) opts.valueOf("i");
        }else{
            System.err.println("Must provide a subscription id");
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e){}
            System.exit(1);
        }
        
        try {
            UnsubscribeUtil util = new UnsubscribeUtil(url);
            util.unsubscribe(id);
            System.out.println("Successfully cancelled subscription " + id);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }
    }
}
