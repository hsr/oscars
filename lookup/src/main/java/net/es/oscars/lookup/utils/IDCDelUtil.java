package net.es.oscars.lookup.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import net.es.oscars.lookup.soap.gen.Relationship;
import net.es.oscars.lookup.soap.gen.ServiceType;
import net.es.oscars.utils.config.ConfigDefaults;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class IDCDelUtil {
    
    static {
        Logger.getRootLogger().setLevel(Level.OFF);
    }
    
    public static void main(String[] args){
        String url = null;
        String context = ConfigDefaults.CTX_PRODUCTION;
        
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS lookup module to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("d", "domain"), "required. the domain with the IDC to delete").withRequiredArg().ofType(String.class);
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
            
            String domain = (String)opts.valueOf("d");
            if(!domain.startsWith("urn:ogf:network:domain=")){
                domain = "urn:ogf:network:domain=" + domain;
            }
            
            //list service and filter out IDCs
            LookupAdminUtil util = new LookupAdminUtil(url, context);
            List<ServiceType> services = util.viewCache(null, null);
            if(services == null){
                System.out.println("Unable to find domain '"+domain+"'");
                System.exit(1);
            }
            Integer serviceId = null;
            for(ServiceType service : services){
                if(!"IDC".equals(service.getType())){
                    continue;
                }
                
                for(Relationship rel : service.getRelationship()){
                    if("controls".equals(rel.getType()) && 
                            domain.equals(rel.getRelatedTo())){
                        serviceId = Integer.parseInt(service.getServiceId());
                        break;
                    }
                }
            }
            
            if(serviceId == null){
                System.out.println("Unable to find domain '"+domain+"'");
                System.exit(1);
            }
            
            util.delCacheEntry(serviceId);
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
