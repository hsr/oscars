package net.es.oscars.lookup.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import net.es.oscars.utils.config.ConfigDefaults;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class IDCAddUtil {
    final static private String defaultType = "IDC";
    final static private String defaultRelType = "controls";
    final static private String defaultProtocolType = "OSCARS6";
    
    static {
        Logger.getRootLogger().setLevel(Level.OFF);
    }
    
    public static void main(String[] args){
        String url = null;
        String context = ConfigDefaults.CTX_PRODUCTION;
        
        String proto = defaultProtocolType;
        HashMap<String, String> protoMap = new HashMap<String, String>();
        protoMap.put("OSCARS5", "https://oscars.es.net/OSCARS");
        protoMap.put("OSCARS6", "http://oscars.es.net/OSCARS/06");
        
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS lookup module to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("d", "domain"), "required. the domain to add").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("l", "location"), "required. the URL of the IDC").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("p", "protocol"), "the protocol spoken by the IDC. May be OSCARS5, OSCARS6, or a namespace URL. Defaults to OSCARS6.").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("c", "context"), "context in which to run the client").withRequiredArg().ofType(String.class);
            }
        };
        try {
            OptionSet opts = parser.parse(args);
            
            if(opts.has("h")){
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            
            if(opts.has("u")){
                url = (String)opts.valueOf("u");
                new URL(url);
            }
            if(opts.has("c")){
                context = (String)opts.valueOf("c");
            }
            
            if(!opts.has("d")){
                System.out.println("A domain must me specified");
                parser.printHelpOn(System.out);
                System.exit(1);
            }
            
            if(!opts.has("l")){
                System.out.println("A IDC URL must me specified");
                parser.printHelpOn(System.out);
                System.exit(1);
            }
            
            if(opts.has("p")){
                proto = (String) opts.valueOf("p");
            }
            if(protoMap.containsKey(proto)){
                proto = protoMap.get(proto);
            }
            
            String domain = (String)opts.valueOf("d");
            if(!domain.startsWith("urn:ogf:network:domain=")){
                domain = "urn:ogf:network:domain=" + domain;
            }
            
            LookupAdminUtil util = new LookupAdminUtil(url, context);
            util.addCacheEntry(defaultType, false, Arrays.asList(proto + "=" + opts.valueOf("l")), Arrays.asList(defaultRelType+"="+domain));
        } catch (OptionException e) {
            System.out.println(e.getMessage());
            try{
                parser.printHelpOn(System.out);
            }catch(IOException e1){}
            System.exit(1);
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL provided for OSCARS Lookup module");
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
    
    
}
