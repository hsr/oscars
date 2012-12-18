package net.es.oscars.lookup.http;

import java.sql.Connection;
import java.util.UUID;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.lookup.LookupGlobals;
import net.es.oscars.lookup.LookupManager;
import net.es.oscars.lookup.RegistrationManager;
import net.es.oscars.lookup.admin.AdminManager;
import net.es.oscars.lookup.soap.gen.AddCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.AddRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.AdminSuccessResponseType;
import net.es.oscars.lookup.soap.gen.AdminViewRequestType;
import net.es.oscars.lookup.soap.gen.DeleteCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.DeleteRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.LookupFaultMessage;
import net.es.oscars.lookup.soap.gen.LookupPortType;
import net.es.oscars.lookup.soap.gen.LookupRequestContent;
import net.es.oscars.lookup.soap.gen.LookupResponseContent;
import net.es.oscars.lookup.soap.gen.ModifyCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.ModifyRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.RegisterRequestContent;
import net.es.oscars.lookup.soap.gen.RegisterResponseContent;
import net.es.oscars.lookup.soap.gen.ViewCacheResponseType;
import net.es.oscars.lookup.soap.gen.ViewRegistrationsResponseType;

import org.apache.log4j.Logger;

/**
 * Contains the logic of the Lookup Service.
 * 
 */

@javax.jws.WebService(
                      serviceName = ServiceNames.SVC_LOOKUP,
                      portName = "LookupPort",
                      targetNamespace = "http://oscars.es.net/OSCARS/lookup",
                      endpointInterface = "net.es.oscars.lookup.soap.gen.LookupPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class LookupSoapHandler implements LookupPortType {
    private Logger log = Logger.getLogger(LookupSoapHandler.class.getName());
    private  LookupGlobals globals;
    private LookupManager lookupManager;
    private RegistrationManager regManager;
    private AdminManager adminManager;
    
    public LookupSoapHandler(){
        OSCARSNetLogger netLog = this.initNetLogger(null);
        this.log.info(netLog.start("init"));
        
        //Initialize global objects (db pool, etc)
        try {
            this.globals = LookupGlobals.getInstance();
        } catch (LookupException e) {
            this.log.error(netLog.error("init", ErrSev.FATAL, 
                    "Unable to initialize global environment: " + 
                        e.getMessage()));
            System.exit(1);
        }
        
        this.lookupManager = new LookupManager();
        this.regManager = new RegistrationManager();
        this.adminManager = new AdminManager();
        
        this.log.info(netLog.end("init"));
    }
    
    /**
     * Registers a service with the perfSONAR Lookup Service. This method 
     * should be called by the coordinator on start-up to publish the existence 
     * and capabilities of the local OSCARS instance.
     * 
     * @param request the SOAP request containing registration information
     * @return an object with a boolean set to true indicating success. 
     * @throws LookupFaultMessage
     */
    public RegisterResponseContent register(RegisterRequestContent request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("register"));
        
        Connection conn = null;
        RegisterResponseContent response = new RegisterResponseContent();
        response.setMessageProperties(request.getMessageProperties());
        try{
            conn = this.globals.getDbConnection();
            regManager.register(request, conn);
            response.setSuccess(true);
        }catch(Exception e){
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.error("register", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        
        this.log.info(netLog.end("register"));
        return response;
    }
    
    /**
     * Finds a service by first looking in the local Derby database. If no 
     * matches are found then the perfSONAR this module contacts the perfSONAR 
     * Lookup Service. Successful results from the perfSONAR lookup
     * service are cached in the DB. If nothing is returned then an exception 
     * is thrown indicating no matches found.
     * 
     * @param request a profile of the service to find
     * @return the matching service
     * @throws LookupFaultMessage
     */
    public LookupResponseContent lookup(LookupRequestContent request) throws LookupFaultMessage    { 
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("lookup"));
        
        Connection conn = null;
        LookupResponseContent response = null;

        try {
            conn = this.globals.getDbConnection();
            response = lookupManager.lookup(request, conn);
            response.setMessageProperties(request.getMessageProperties());
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.error("lookup", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        
        this.log.info(netLog.end("lookup"));
        return response;
    }
    
    /**
     * Manually adds a service to the local Derby database. This method should 
     * be used to manually add a service not in the perfSONAR LS or 
     * for scenarios where LS access is inhibited (i.e. the perfSONAR LS has 
     * gone down for an extended period of time, internal firewalls, etc).
     * NOTE: This is an administrative function and should NEVER be called by 
     * the coordinator.
     * 
     * @param request the service to add
     * @return a boolean set to true for success. 
     * @throws LookupFaultMessage
     */
    public AdminSuccessResponseType adminAddCacheEntry(AddCacheEntryRequestType request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("adminAddCacheEntry"));
        
        Connection conn = null;
        AdminSuccessResponseType response = null;
        
        try {
            conn = this.globals.getDbConnection();
            adminManager.addCacheEntry(request, conn);
            response = new AdminSuccessResponseType();
            response.setMessageProperties(request.getMessageProperties());
            response.setSuccess(true);
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.error("adminAddCacheEntry", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        
        this.log.info(netLog.end("adminAddCacheEntry"));
        return response;
    }
    
    /**
     * Adds registration information to the local database. This does NOT 
     * register a service with the perfSONAR lookup service. It is intended to 
     * be used in troubleshooting situations or situations where you want to transfer 
     * a registration key from another installation.
     * NOTE: This is an administrative function and should NEVER be called by 
     * the coordinator.
     * 
     * @param request the registration information to add
     * @return a boolean set to true for success. 
     * @throws LookupFaultMessage
     */
    public AdminSuccessResponseType adminAddRegistration(AddRegistrationRequestType request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("adminAddRegistration"));
        
        Connection conn = null;
        AdminSuccessResponseType response = null;

        try {
            conn = this.globals.getDbConnection();
            adminManager.addRegistration(request, conn);
            response = new AdminSuccessResponseType();
            response.setMessageProperties(request.getMessageProperties());
            response.setSuccess(true);
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.error("adminAddRegistration", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        this.log.info(netLog.end("adminAddRegistration"));
        
        return response;
    }
    
    /**
     * Manually removes a service cached from a previous lookup that's in the 
     * local database. This should be used when a neighbor changes their IDC URL or protocol 
     * and one would like to use the new URL prior to the scheduled expiration of the 
     * old cache entry.
     * NOTE: This is an administrative function and should NEVER be called by 
     * the coordinator.
     * 
     * @param request an id of the entry to delete. the id is found in the viewCache operation. 
     * @return a boolean set to true for success. 
     * @throws LookupFaultMessage
     */
    public AdminSuccessResponseType adminDeleteCacheEntry(DeleteCacheEntryRequestType request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("adminDeleteCacheEntry"));
        
        Connection conn = null;
        AdminSuccessResponseType response = null;

        try {
            conn = this.globals.getDbConnection();
            adminManager.deleteCacheEntry(request, conn);
            response = new AdminSuccessResponseType();
            response.setMessageProperties(request.getMessageProperties());
            response.setSuccess(true);
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.info(netLog.error("adminDeleteCacheEntry", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        this.log.info(netLog.end("adminDeleteCacheEntry"));
        
        return response;
    }
    
    /**
     * Manually removes registration information such as registration keys. 
     * This may be useful if a perfSONAR lookup service were to lose its key data.
     * NOTE: This is an administrative function and should NEVER be called by 
     * the coordinator.
     *  
     * @param request the id of the entry to delete. the id is found in the viewRegistartions operation. 
     * @return a boolean set to true for success. 
     * @throws LookupFaultMessage
     */
    public AdminSuccessResponseType adminDeleteRegistration(DeleteRegistrationRequestType request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("adminDeleteRegistration"));
        
        Connection conn = null;
        AdminSuccessResponseType response = null;

        try {
            conn = this.globals.getDbConnection();
            adminManager.deleteRegistration(request, conn);
            response = new AdminSuccessResponseType();
            response.setMessageProperties(request.getMessageProperties());
            response.setSuccess(true);
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.error("adminDeleteRegistration", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        this.log.info(netLog.end("adminDeleteRegistration"));
        
        return response;
    }
    
    /**
     * Manually modifies an entry for a service in the local database. This 
     * may be useful if the lookup service returns data different than that desired, 
     * to update a manually added entries or to change the expiration time of data.
     * NOTE: This is an administrative function and should NEVER be called by 
     * the coordinator.
     * 
     * @param request an id of the entry to modify and the parameters to change 
     * @return a boolean set to true for success. 
     * @throws LookupFaultMessage
     */
    public AdminSuccessResponseType adminModifyCacheEntry(ModifyCacheEntryRequestType request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("adminModifyCacheEntry"));
        
        Connection conn = null;
        AdminSuccessResponseType response = null;

        try {
            conn = this.globals.getDbConnection();
            adminManager.modifyCacheEntry(request, conn);
            response = new AdminSuccessResponseType();
            response.setMessageProperties(request.getMessageProperties());
            response.setSuccess(true);
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.error("adminModifyCacheEntry", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        this.log.info(netLog.end("adminModifyCacheEntry"));
        
        return response;
    }
    
    /**
     * Manually modifies registration information. May be useful if key 
     * information for the LS changes or needs to be transferred.
     * NOTE: This is an administrative function and should NEVER be called by 
     * the coordinator.
     * 
     * @param request an id of the entry to modify and the parameters to change
     * @return a boolean set to true for success. 
     * @throws LookupFaultMessage
     */
    public AdminSuccessResponseType adminModifyRegistration(ModifyRegistrationRequestType request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("adminModifyRegistration"));
        
        Connection conn = null;
        AdminSuccessResponseType response = null;

        try {
            conn = this.globals.getDbConnection();
            adminManager.modifyRegistration(request, conn);
            response = new AdminSuccessResponseType();
            response.setMessageProperties(request.getMessageProperties());
            response.setSuccess(true);
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.end("adminModifyRegistration", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        this.log.info(netLog.end("adminModifyRegistration"));
        
        return response;
    }
    
    /**
     * Returns all the services in the local database. Useful for debugging.
     * NOTE: This is an administrative function and should NEVER be called by 
     * the coordinator.
     * 
     * @param request request containing option parameters to limit how many results are shown
     * @return the services in the local database. 
     * @throws LookupFaultMessage
     */
    public ViewCacheResponseType adminViewCache(AdminViewRequestType request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("adminViewCache"));
        
        Connection conn = null;
        ViewCacheResponseType response = null;

        try {
            conn = this.globals.getDbConnection();
            response = adminManager.viewCache(request, conn);
            response.setMessageProperties(request.getMessageProperties());
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.error("adminViewCache", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        this.log.info(netLog.end("adminViewCache"));
        
        return response;
    }
    
    /**
     * Returns all the services in the local database. Useful for debugging.
     * NOTE: This is an administrative function and should NEVER be called by 
     * the coordinator.
     * 
     * @param request request containing optional parameters to limit how many results are shown
     * @return the services in the local database. 
     * @throws LookupFaultMessage
     */
    public ViewRegistrationsResponseType adminViewRegistrations(AdminViewRequestType request) throws LookupFaultMessage {
        OSCARSNetLogger netLog = this.initNetLogger(request.getMessageProperties());
        this.log.info(netLog.start("adminViewRegistrations"));
        
        Connection conn = null;
        ViewRegistrationsResponseType response = null;

        try {
            conn = this.globals.getDbConnection();
            response = adminManager.viewRegistrations(request, conn);
            response.setMessageProperties(request.getMessageProperties());
        } catch (Exception e) {
            this.globals.releaseDbConnection(conn);
            this.log.error(netLog.error("adminViewRegistrations", ErrSev.MAJOR, e.getMessage()));
            throw new LookupFaultMessage(e.getMessage());
        }finally{
            this.globals.releaseDbConnection(conn);
        }
        this.log.info(netLog.end("adminViewRegistrations"));
        
        return response;
    }
    
    
    private OSCARSNetLogger initNetLogger(MessagePropertiesType msgProps) {
        String guid = null;
        if(msgProps != null && msgProps.getGlobalTransactionId() != null){
            guid = msgProps.getGlobalTransactionId();
        }else{
            guid = UUID.randomUUID().toString();
        }
        OSCARSNetLogger netLogger = new OSCARSNetLogger(ModuleName.LOOKUP, guid);
        OSCARSNetLogger.setTlogger(netLogger);
        
        return netLogger;
    }
}
