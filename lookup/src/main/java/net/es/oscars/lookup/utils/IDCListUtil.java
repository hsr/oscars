package net.es.oscars.lookup.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.es.oscars.lookup.soap.gen.ServiceType;
import net.es.oscars.utils.config.ConfigDefaults;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class IDCListUtil {    
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
            
            //list service and filter out IDCs
            LookupAdminUtil util = new LookupAdminUtil(url, context);
            List<ServiceType> services = util.viewCache(null, null);
            if(services == null){
                System.out.println("No IDCs in database.");
                System.exit(0);
            }
            ArrayList<ServiceType> idcs = new ArrayList<ServiceType>();
            for(ServiceType service : services){
                if("IDC".equals(service.getType())){
                    idcs.add(service);
                }
            }
            
            if(idcs.isEmpty()){
                System.out.println("No IDCs in database.");
                System.exit(0);
            }
            
            util.printServices(idcs);
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
