package net.es.oscars.wsnbroker.utils;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;

import net.es.oscars.utils.clients.NotificationBridgeClient;
import net.es.oscars.utils.clients.WSNBrokerClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

public class RenewUtil {
    WSNBrokerClient client;
    
    public RenewUtil(String url) throws MalformedURLException, OSCARSServiceException{
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
    
    public RenewResponse renew(String subscriptionId, long relTermTime) throws OSCARSServiceException{
        Renew renew = new Renew();
        renew.setSubscriptionReference(WSAddrParser.createAddress(subscriptionId));
        if(relTermTime > 0){
            renew.setTerminationTime(this.generateDateTime(relTermTime));
        }
        
        try {
            return this.client.getPortType().renew(renew);
        } catch (Exception e){
            throw new OSCARSServiceException(e.getMessage());
        }
    }
    
    /**
     * Generates an xsd:datetime String that is X number of seconds in the future
     * where X is a supplied parameter.
     *
     * @param seconds the number of seconds in the future the xsd:datetime should represent
     * @return an xsd:datetime string that is the requested number of seconds in the future
     *
     */
     public String generateDateTime(long seconds){
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        cal.setTimeInMillis(System.currentTimeMillis() + (seconds*1000));
        return df.format(cal.getTime());
     }
    
    public static void main(String[] args){
        String url = "http://localhost:9013/OSCARS/wsnbroker";
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS notification service to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "id"), "the id of the subscription to renew").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("t", "termtime"), "the request time the subscription should expire").withRequiredArg().ofType(String.class);
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
        
        long relTermTime = -1;
        if(opts.has("t")){
            relTermTime = Long.parseLong((String) opts.valueOf("t"));
        }
        
        try {
            RenewUtil util = new RenewUtil(url);
            RenewResponse response = util.renew(id, relTermTime);
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
