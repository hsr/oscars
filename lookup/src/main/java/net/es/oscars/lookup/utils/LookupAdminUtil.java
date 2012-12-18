package net.es.oscars.lookup.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
import net.es.oscars.lookup.soap.gen.AddCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.AddRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.AdminViewRequestType;
import net.es.oscars.lookup.soap.gen.DeleteCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.DeleteRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.LookupFaultMessage;
import net.es.oscars.lookup.soap.gen.LookupPortType;
import net.es.oscars.lookup.soap.gen.ModifyCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.ModifyRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.Protocol;
import net.es.oscars.lookup.soap.gen.RegistrationType;
import net.es.oscars.lookup.soap.gen.Relationship;
import net.es.oscars.lookup.soap.gen.ServiceType;
import net.es.oscars.lookup.soap.gen.ViewCacheResponseType;
import net.es.oscars.lookup.soap.gen.ViewRegistrationsResponseType;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

public class LookupAdminUtil {
    private LookupPortType client;
    private PrintStream out;

    static {
        Logger.getRootLogger().setLevel(Level.OFF);
    }
    
    public LookupAdminUtil(String url, String context) throws MalformedURLException, OSCARSServiceException{
        this.out = System.out;
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
    }
    
    public List<ServiceType> viewCache(Integer numResults, Integer offset) throws LookupFaultMessage{
        AdminViewRequestType request = new AdminViewRequestType();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        request.setMaxResults(numResults);
        request.setOffset(offset);
        ViewCacheResponseType response = this.client.adminViewCache(request);
        
        if(response.getService().isEmpty()){
            return null;
        }
        
        return response.getService();
    }
    
    public void viewRegistrations(Integer numResults, Integer offset) throws LookupFaultMessage{
        AdminViewRequestType request = new AdminViewRequestType();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        request.setMaxResults(numResults);
        request.setOffset(offset);
        
        ViewRegistrationsResponseType response = this.client.adminViewRegistrations(request);
        if(response.getRegistration().isEmpty()){
            this.out.println("No registrations in database.");
            return;
        }
        
        for(RegistrationType reg : response.getRegistration()){
            this.out.println();
            this.out.println("ID: " + reg.getRegistrationId());
            this.out.println("Name: " + reg.getName());
            this.out.println("Published To: " + reg.getPublishUrl());
            this.out.println("Key: " + (reg.getKey() != null ? reg.getKey() : "NONE"));
        }
        this.out.println();
    }
    
    public void addCacheEntry(String type, boolean expires,
            List<String> protocols, List<String> relationships) throws LookupFaultMessage {
        
        AddCacheEntryRequestType request = new AddCacheEntryRequestType();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        
        if(type == null){
            System.err.println("Please specify a service type");
            System.exit(1);
        }
        
        request.setType(type);
        request.setExpires(expires);
        this.parseProtocols(protocols, request.getProtocol());
        this.parseRelationships(relationships, request.getRelationship());
        
        this.client.adminAddCacheEntry(request);
        System.out.println("Service added successfully!");
    }

    public void modCacheEntry(Integer id, String type, Boolean expires,
            List<String> protocols, List<String> relationships) throws LookupFaultMessage{
        if(id == null){
            System.out.println("Must specify an id of the service to modify");
            System.exit(1);
        }
        
        ModifyCacheEntryRequestType request = new ModifyCacheEntryRequestType();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        request.setServiceId(id);
        request.setType(type);
        request.setExpires(expires);
        this.parseProtocols(protocols, request.getProtocol());
        this.parseRelationships(relationships, request.getRelationship());
        
        this.client.adminModifyCacheEntry(request);
        System.out.println("Service modified successfully!");
    }
    
    public void delCacheEntry(Integer id) throws LookupFaultMessage{
        if(id == null){
            System.out.println("Must specify an id of the service to delete");
            System.exit(1);
        }
        
        DeleteCacheEntryRequestType request = new DeleteCacheEntryRequestType();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        request.setServiceId(id);
        this.client.adminDeleteCacheEntry(request);
        System.out.println("Service deleted successfully!");
    }
    
    public LookupPortType getClient(){
        return this.client;
    }
    
    public PrintStream getOutputStream() {
        return out;
    }

    public void setOutputStream(PrintStream out) {
        this.out = out;
    }
    
    private void parseProtocols(List<String> protocols, List<Protocol> request){
        if(protocols == null){
            return;
        }
        for(String protoStr : protocols){
            String[] protoParts = protoStr.split("=", 2);
            if(protoParts.length < 2){
                System.out.println("Please specify protocols as <Type>=<Location>");
                System.exit(1);
            }
            Protocol proto = new Protocol();
            proto.setType(protoParts[0]);
            proto.setLocation(protoParts[1]);
            request.add(proto);
        }
    }
    
    private void parseRelationships(List<String> relationships,
            List<Relationship> request) {
        if(relationships == null){
            return;
        }
        for(String relStr : relationships){
            String[] relParts = relStr.split("=", 2);
            if(relParts.length < 2){
                System.out.println("Please specify relationships as <Type>=<RelatedToId>");
                System.exit(1);
            }
            Relationship rel = new Relationship();
            rel.setType(relParts[0]);
            rel.setRelatedTo(relParts[1]);
            request.add(rel);
        }
    }
    
    public void addRegistration(String name, String pubUrl,
            String key) throws LookupFaultMessage {
        AddRegistrationRequestType request = new AddRegistrationRequestType();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        if(name == null){
            this.out.println("Must specify the registration name");
            System.exit(1);
        }
        if(pubUrl == null){
            this.out.println("Must specify the publisher URL");
            System.exit(1);
        }
        request.setName(name);
        request.setPublishUrl(pubUrl);
        request.setKey(key);
        
        this.client.adminAddRegistration(request);
        this.out.println("Registration added.");
    }
    
    public void modRegistration(Integer regId, String name, String pubUrl,
            String key) throws LookupFaultMessage {
        if(regId == null || regId <= 0){
            this.out.println("Must specify registration ID");
            System.exit(1);
        }
        
        ModifyRegistrationRequestType request = new ModifyRegistrationRequestType();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        request.setRegistrationId(regId);
        request.setName(name);
        request.setPublishUrl(pubUrl);
        request.setKey(key);
        
        this.client.adminModifyRegistration(request);
        this.out.println("Registration modified.");
    }
    
    public void delRegistration(Integer regId) throws LookupFaultMessage {
        if(regId == null || regId <= 0){
            this.out.println("Must specify registration ID");
            System.exit(1);
        }
        DeleteRegistrationRequestType request = new DeleteRegistrationRequestType();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        request.setRegistrationId(regId);
        
        this.client.adminDeleteRegistration(request);
        this.out.println("Registration deleted.");
    }
    
    public void printServices(List<ServiceType> services){
        if(services == null){
            this.out.println("No services in cache.");
            return; 
        }
        
        for(ServiceType service : services){
            this.out.println();
            this.out.println("ID: " + service.getServiceId());
            this.out.println("Type: " + service.getType());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            this.out.println("Expiration: " + (service.getExpiration() > 0 ? df.format(new Date(service.getExpiration()*1000L)) : "NEVER"));
            
            this.out.println("Protocols:");
            int i = 0;
            for(Protocol protocol : service.getProtocol()){
                if(i != 0){
                    this.out.println();
                }else{
                    i++;
                }
                this.out.println("    Type: " + protocol.getType());
                this.out.println("    Location: " + protocol.getLocation());
            }
            this.out.println("Relationships:");
            for(Relationship relationship : service.getRelationship()){
                this.out.println("    [" + relationship.getType() + "] " + 
                        relationship.getRelatedTo());
            }
        }
        this.out.println();
    }
    
    public static void main(String[] args){
        String url = null;
        String context = ConfigDefaults.CTX_PRODUCTION;
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("help", "h"), "prints this help message");
                acceptsAll(Arrays.asList("url", "u"), "the URL of the OSCARS lookup module to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("c", "context"), "context in which to run the client").withRequiredArg().ofType(String.class);
                accepts("cache-view", "display services currently in lookup cache");
                accepts("cache-add", "add an entry to the cache");
                accepts("cache-mod", "modify an entry in the cache");
                accepts("cache-del", "delete an entry from the cache");
                accepts("reg-view", "display registrations managed by this lookup module");
                accepts("reg-add", "add a registration to the server database");
                accepts("reg-mod", "add a registration to the server database");
                accepts("reg-del", "delete a registration from the server database");
                
                //view opts
                acceptsAll(Arrays.asList("num-results", "n"), "(cache-view, reg-view) the number of results to display").withRequiredArg().ofType(Integer.class);
                acceptsAll(Arrays.asList("offset", "o"), "(cache-view, reg-view) show results starting at position specified").withRequiredArg().ofType(Integer.class);
                
                //cache-add opts
                acceptsAll(Arrays.asList("service-type", "t"), "(cache-add*, cache-mod*) the type of service such as IDC").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("expires", "x"), "(cache-add, cache-mod) indicates that this netry will expire");
                acceptsAll(Arrays.asList("protocols", "p"), "(cache-add, cache-mod) the protocols supported by this " +
                        "service. Takes form <Type>=<Location> separated by commas.")
                        .withRequiredArg().ofType(String.class).withValuesSeparatedBy(',');
                acceptsAll(Arrays.asList("relationships", "r"), "(cache-add, cache-mod) the relationship of this service to other elements " +
                    "Takes form <Type>=<RelativeId> separated by commas.")
                    .withRequiredArg().ofType(String.class).withValuesSeparatedBy(',');
                
                //service ids
                acceptsAll(Arrays.asList("service-id", "s"), "(cache-del*, cache-mod*) the id of the service to modify/delete").withRequiredArg().ofType(Integer.class);
                
                //reg opts
                acceptsAll(Arrays.asList("reg-id", "i"), "(reg-del*, reg-mod*) the id of the registrations to modify/delete").withRequiredArg().ofType(Integer.class);
                acceptsAll(Arrays.asList("reg-name", "g"), "(reg-add*, reg-mod) the name of this registration").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("reg-publishto", "l"), "(reg-add*, reg-mod) the URL of the perfSONAR LS where this registration exists").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("reg-key", "k"), "(reg-add, reg-mod) the optional registration key received from the perfSONAR LS").withRequiredArg().ofType(String.class);
            }
        };
        
        try {
            OptionSet opts = parser.parse(args);
            
            //print help message
            if(opts.has("h")){
                try {
                    parser.printHelpOn(System.out);
                } catch (IOException e1) {}
                System.exit(0);
            }
            
            if(opts.has("u")){
                url = (String) opts.valueOf("u");
            }
            
            if(opts.has("context")){
                context = (String)opts.valueOf("c");
            }
            
            LookupAdminUtil util =  new LookupAdminUtil(url, context); 
            if(opts.has("cache-view")){
                util.printServices(
                        util.viewCache((Integer)opts.valueOf("n"), (Integer)opts.valueOf("o"))
                );
            }else if(opts.has("cache-add")){
                util.addCacheEntry((String)opts.valueOf("t"), opts.has("x"), (List<String>) opts.valuesOf("p"), (List<String>) opts.valuesOf("r"));
            }else if(opts.has("cache-del")){
                util.delCacheEntry((Integer)opts.valueOf("s"));
            }else if(opts.has("cache-mod")){
                util.modCacheEntry((Integer)opts.valueOf("s"), (String)opts.valueOf("t"), opts.has("x"), (List<String>) opts.valuesOf("p"), (List<String>) opts.valuesOf("r"));
            }else if(opts.has("reg-add")){
                util.addRegistration((String)opts.valueOf("g"), (String)opts.valueOf("p"), (String)opts.valueOf("k"));
            }else if(opts.has("reg-mod")){
                util.modRegistration((Integer)opts.valueOf("i"), (String)opts.valueOf("g"), (String)opts.valueOf("p"), (String)opts.valueOf("k"));
            }else if(opts.has("reg-del")){
                util.delRegistration((Integer)opts.valueOf("i"));
            }else if(opts.has("reg-view")){
                util.viewRegistrations((Integer)opts.valueOf("n"), (Integer)opts.valueOf("o"));
            }else{
                parser.printHelpOn(System.out);
                System.exit(1);
            }
        } catch(OptionException e){
            System.out.println(e.getMessage());
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e1) {}
            System.exit(1);
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL specified");
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e1) {}
            System.exit(1);
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
    }
}
