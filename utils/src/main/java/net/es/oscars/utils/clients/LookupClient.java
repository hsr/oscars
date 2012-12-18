package net.es.oscars.utils.clients;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;
import java.util.List;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.lookup.soap.gen.GeoLocation;
import net.es.oscars.lookup.soap.gen.LookupPortType;
import net.es.oscars.lookup.soap.gen.LookupRequestContent;
import net.es.oscars.lookup.soap.gen.LookupResponseContent;
import net.es.oscars.lookup.soap.gen.LookupService;
import net.es.oscars.lookup.soap.gen.RegisterRequestContent;
import net.es.oscars.lookup.soap.gen.RegisterResponseContent;

import net.es.oscars.lookup.soap.gen.Protocol;
import net.es.oscars.lookup.soap.gen.Relationship;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;



/**
 * A helper class for generating clients that talk to the LookupService. 
 */
@OSCARSNetLoggerize(moduleName = ModuleName.LOOKUP)
@OSCARSService (
        implementor = "net.es.oscars.lookup.soap.gen.LookupService",
        namespace = "http://oscars.es.net/OSCARS/lookup", 
        serviceName = ServiceNames.SVC_LOOKUP
)
public class LookupClient extends OSCARSSoapService<LookupService, LookupPortType>{

    static private final String TYPE = "IDC";
    
    /**
     * Constructor. 
     * 
     * @param host The location of the host to contact
     * @param wsdl The location of the WSDL file for this service
     * @throws OSCARSServiceException
     */
    public LookupClient(URL host, URL wsdl) throws OSCARSServiceException {
        super(host, wsdl, LookupPortType.class);
    }
    
    /**
     * Creates a LookupPortType object that can be used to call the lookup 
     * service.
     * 
     * @param host The location of the host to contact as a URL string
     * @param wsdl The location of the service wsdl as a URL string
     * @return A LookupPortType object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public LookupClient getClient(String host, String wsdl) throws OSCARSServiceException, MalformedURLException{
        return new LookupClient(new URL(host), new URL(wsdl));
    }
    
    /**
     * Creates a LookupPortType object that can be used to call the lookup 
     * service.
     * 
     * @param host The location of the host to contact as a URL string
     * @param wsdl The location of the service wsdl as a URL string
     * @return A LookupPortType object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public LookupClient getClient(URL host, URL wsdl) throws OSCARSServiceException, MalformedURLException{
        return new LookupClient(host,wsdl);
    }
    
    /**
     * Creates a LookupPortType object that can be used to call the lookup 
     * service given only the address of the service. The location of the WSDL
     * is assumed to be <i>host</i>?wsdl.
     * 
     * @param host The location of the host to contact as a URL string
     * @return A LookupPortType object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public LookupClient getClient(String host) throws OSCARSServiceException, MalformedURLException{
        return new LookupClient(new URL(host), new URL(host+"?wsdl"));
    }
    
    public List<Protocol> lookup (String domainId) throws OSCARSServiceException {
        // LookupResponseContent lookup(LookupRequestContent request)
        LookupRequestContent request = new LookupRequestContent ();
     
        Relationship rel = new Relationship();
        
        rel.setType("controls");
        rel.setRelatedTo(domainId);
        
        request.setType(LookupClient.TYPE);
        request.setHasRelationship(rel);
        request.setHasLocation(null);
        
        Object[] req = new Object[] {request};
        
        Object[] res = this.invoke("lookup", req);
 
        if ((res == null) || (res[0] == null)) {
            throw new OSCARSServiceException ("no response from Lookup Module. query= lookup domain= " + domainId,"system");
        }
        
        LookupResponseContent result = (LookupResponseContent) res[0];
 
        return result.getProtocol();
    }
    
    public void register (String domainId,
            String name,
            HashMap<String, String> protocolMap,
            String description) throws OSCARSServiceException {
        this.register(domainId, name,protocolMap, description, null);
    }
    
    public void register (String domainId,
                          String name,
                          HashMap<String, String> protocolMap,
                          String description,
                          GeoLocation geoLocation) throws OSCARSServiceException {        
        
        RegisterRequestContent request = new RegisterRequestContent(); 
        request.setName(name);
        request.setType(LookupClient.TYPE); 
        request.setDescription(description);
        request.setGeoLocation(geoLocation);
        
        //indicate what protocol it speaks
	//        Protocol protocol = new Protocol(); //comment out decl to include inside loop. issue 324
        for(String version : protocolMap.keySet()){
	    Protocol protocol = new Protocol(); //for issue 324
            protocol.setType(version);
            protocol.setLocation(protocolMap.get(version));
            request.getProtocol().add(protocol);
        }
        Relationship rel = new Relationship();
        rel.setType("controls");
        rel.setRelatedTo(domainId);
        request.getRelationship().add(rel);
        
        Object[] req = new Object[] {request};
        Object[] res = this.invoke("register", req);
        if ((res == null) || (res[0] == null)) {
            throw new OSCARSServiceException ("no response from Lookup Module to the register request.","system");
        }
       
        RegisterResponseContent result = (RegisterResponseContent) res[0];
        if (! result.isSuccess()) {
            throw new OSCARSServiceException ("register request failed","system");
        }
    }
}
