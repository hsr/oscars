package net.es.oscars.lookup.utils;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.utils.clients.LookupClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.lookup.soap.gen.LookupFaultMessage;
import net.es.oscars.lookup.soap.gen.LookupPortType;
import net.es.oscars.lookup.soap.gen.LookupRequestContent;
import net.es.oscars.lookup.soap.gen.LookupResponseContent;
import net.es.oscars.lookup.soap.gen.Protocol;
import net.es.oscars.lookup.soap.gen.Relationship;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

public class LookupUtil {
    private LookupPortType client;
    private PrintStream out;
    
    final static private String defaultType = "IDC";
    final static private String defaultRelType = "controls";
    
    static {
        Logger.getRootLogger().setLevel(Level.OFF);
    }
    
    public LookupUtil(String url, String context) throws MalformedURLException, OSCARSServiceException{
        //Set the context
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_LOOKUP);
        cc.setServiceName(ServiceNames.SVC_LOOKUP);
        cc.setContext(context);
        
        //load the manifest
        try {
            cc.loadManifest(ServiceNames.SVC_LOOKUP,  ConfigDefaults.MANIFEST);
        } catch (ConfigException e) {
            throw new OSCARSServiceException(e.getMessage());
        }
        
        //Set the url of the server
        if(url == null){
            try {
                String configFile = cc.getFilePath(ConfigDefaults.CONFIG);
                Map config = ConfigHelper.getConfiguration(configFile);
                if(!config.containsKey("soap") || config.get("soap") == null){
                    throw new OSCARSServiceException("Unable to determine URL of server. " +
                        "No soap block in lookup config file");
                }
                HashMap<String,Object> soap = (HashMap<String,Object>) config.get("soap");
                if(!soap.containsKey("publishTo") || soap.get("publishTo") == null){
                    throw new OSCARSServiceException("Unable to determine URL of server. " +
                        " No publishTo in lookup config file.");
                }
                url = (String) soap.get("publishTo");
            } catch (ConfigException e) {
                throw new OSCARSServiceException(e.getMessage());
            }
        }
        
        //set the wsdl
        String wsdl = url + "?wsdl";
        try{
            wsdl = "file:" + cc.getFilePath(ConfigDefaults.WSDL);
        }catch(Exception e){}
        
        //set the client cxf config file
        try {
            OSCARSSoapService.setSSLBusConfiguration(new URL("file:"+ cc.getFilePath(ConfigDefaults.CXF_CLIENT)));
            this.client = LookupClient.getClient(url, wsdl).getPortType();
            ClientProxy.getClient(this.client).getRequestContext().put("org.apache.cxf.message.Message.ENDPOINT_ADDRESS", url);
        } catch (ConfigException e) {
            throw new OSCARSServiceException(e.getMessage());
        }
        this.out = new PrintStream(System.out);
    }
    
    public void lookup(String type, String relationship, String url) throws LookupFaultMessage{
        LookupRequestContent request = new LookupRequestContent();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        if(type == null){
            this.out.println("Must specify service type");
            System.exit(1);
        }
        request.setType(type);
        
        if(relationship != null){
            String[] relParts = relationship.split("=", 2);
            if(relParts.length < 2){
                System.out.println("Please specify relationships as <Type>=<RelatedToId>");
                System.exit(1);
            }
            Relationship rel = new Relationship();
            rel.setType(relParts[0]);
            rel.setRelatedTo(relParts[1]);
            request.setHasRelationship(rel);
        }
        
        request.setHasLocation(url);
        
        LookupResponseContent response = this.client.lookup(request);
        System.out.println();
        System.out.println("Type: " + response.getType());
        System.out.println("Protocols:");
        for(Protocol protocol : response.getProtocol()){
            this.out.println("    Type: " + protocol.getType());
            this.out.println("    Location: " + protocol.getLocation());
            this.out.println();
        }
        System.out.println("Relationships:");
        for(Relationship rel : response.getRelationship()){
            System.out.println("    [" + rel.getType() + "] " + 
                    rel.getRelatedTo());
        }
        System.out.println();
    }
    
    public static void main(String[] args){
        String helpMsg = "\nUsage: oscars-lookup [opts] <domain>\n\n";
        helpMsg += "The optional <domain> argument is the name " +
                "of the domain with the IDC you wish to find. Alternatively you may " +
                "use the options below to perform more advanced lookups. The " +
                "default behavior is equivalenet to '-t IDC -r " +
                "controls=urn:ogfnetwork:domain=<domain>'.\n\n";
        
        String url = null;
        String context = ConfigDefaults.CTX_PRODUCTION;
        String type = defaultType;
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS lookup module to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("t", "type"), "the type of service to lookup").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("l", "location"), "the URL of the service to lookup").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("relationship", "r"), "lookup a service with the given relationship" +
                "Takes form <Type>=<RelativeId>.").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("c", "context"), "context in which to run the client").withRequiredArg().ofType(String.class);
            }
        };
        
        try {
            OptionSet opts = parser.parse(args);
            if(opts.has("h")){
                System.out.println(helpMsg);
                try{
                    parser.printHelpOn(System.out);
                }catch(Exception e){}
            }
            List<String> nonOpts = opts.nonOptionArguments();
            if(nonOpts.size() > 0 && (opts.has("r") || opts.has("l"))){
                System.out.println("Do not pass bare argument if specifying -r or -l.");
                System.exit(1);
            }
            
            if(opts.has("u")){
                url = (String)opts.valueOf("u");
            }
            if(opts.has("c")){
                context = (String)opts.valueOf("c");
            }
            
            if(opts.has("t")){
                type = (String) opts.valueOf("t");
            }
            
            LookupUtil util = new LookupUtil(url, context);
            if(nonOpts.size() == 1){
                util.lookup(type, defaultRelType + "=urn:ogf:network:domain=" + 
                        nonOpts.get(0), null);
            }else{
                util.lookup(type, (String)opts.valueOf("r"), (String)opts.valueOf("l"));
            }
        }catch(OptionException e){
            System.out.println(e.getMessage());
            System.out.println(helpMsg);
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e1){}
            System.exit(1);
        }catch (MalformedURLException e) {
            System.out.println("Invalid URL provided for OSCARS lookup module");
            System.exit(1);
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
