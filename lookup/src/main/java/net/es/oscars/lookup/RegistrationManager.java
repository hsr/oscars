package net.es.oscars.lookup;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.lookup.soap.gen.GeoLocation;
import net.es.oscars.lookup.soap.gen.Protocol;
import net.es.oscars.lookup.soap.gen.RegisterRequestContent;
import net.es.oscars.lookup.soap.gen.Relationship;
import edu.internet2.perfsonar.NodeRegistration;
import edu.internet2.perfsonar.PSException;
import edu.internet2.perfsonar.ServiceRegistration;

public class RegistrationManager {
    private Logger log = Logger.getLogger(RegistrationManager.class);
    private LookupGlobals globals;
    
    public RegistrationManager(){
        try {
            this.globals = LookupGlobals.getInstance();
        } catch (LookupException e) {}
    }
    
    public void register(RegisterRequestContent registerRequest, Connection conn) throws LookupException {
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        //make sure perfSONAR configured
        if(this.globals.getPerfsonarClient() == null || this.globals.getDisableRegister() == 1){
            //if not configured silently do nothing
            return;
        }
        
        //Get keys from db
        HashMap<String, HashMap<String,String>> serviceKeys = this.getServiceKeys(conn);

        //Set registration parameters
        ServiceRegistration serviceReg = new ServiceRegistration(registerRequest.getName(), registerRequest.getType());
        serviceReg.setDescription(registerRequest.getDescription());
        
        //Add protocols
        ArrayList<String> urls = new ArrayList<String>();
        for(Protocol proto : registerRequest.getProtocol()){
            String[] addrs = new String[1];
            addrs[0] = proto.getLocation();
            serviceReg.setPort(addrs, proto.getType(), null);
            //save urls for later
            urls.add(proto.getLocation());
        }
        
        //add relations
        for(Relationship relation : registerRequest.getRelationship()){
            List<String> addrs = new ArrayList<String>();
            addrs.add(relation.getRelatedTo());
            serviceReg.addRelation(relation.getType(), addrs);
        }
        
        //check if node registered
        HashMap<String, Boolean> regHosts = new HashMap<String, Boolean>();
        for(String urlStr : urls){
            URL urlObj = null;
            try{ 
                urlObj = new URL(urlStr); 
            }catch(Exception e){ 
                continue; 
            }
            String hostname = urlObj.getHost();
            if(regHosts.containsKey(hostname)){ continue; }
            Element node = null;
            try {
                this.log.debug(netLog.start("register.psLookupNode"));
                node = this.globals.getPerfsonarClient().lookupNode(hostname);
                this.log.debug(netLog.end("register.psLookupNode"));
            } catch (PSException e) {
                this.log.debug(netLog.error("register.psLookupNode", ErrSev.MAJOR, e.getMessage()));
                throw new LookupException("Error trying to find node in PS Lookup service");
            }
            
            //only register if not registered or if registered and have a key
            if(node == null){
                serviceKeys.put("node:"+hostname, new HashMap<String, String>());
                String nodeId = this.registerNode(hostname, 
                        registerRequest.getGeoLocation(),
                        serviceKeys.get("node:"+hostname));
                serviceReg.setNode(nodeId);
            }else if(serviceKeys.containsKey("node:"+hostname)){
                String nodeId = this.registerNode(hostname, 
                        registerRequest.getGeoLocation(), 
                        serviceKeys.get("node:"+hostname));
                serviceReg.setNode(nodeId);
            }else if(node.getAttributeValue("id") != null){
                serviceReg.setNode(node.getAttributeValue("id"));
            }
            regHosts.put(hostname, true);
        }
        
        /* REGISTER WITH LS */
        String servString = registerRequest.getType() + ":" + registerRequest.getName();
        if(!serviceKeys.containsKey(servString)){
            serviceKeys.put(servString, new HashMap<String, String>());
        }
        try {
            this.log.debug(netLog.start("register.psRegisterService"));
            this.globals.getPerfsonarClient().registerService(serviceReg, serviceKeys.get(servString));
            this.log.debug(netLog.end("register.psRegisterService"));
        } catch (PSException e) {
            this.log.debug(netLog.end("register.psRegisterService", ErrSev.MAJOR, e.getMessage()));
            throw new LookupException("Error trying to register service " + 
                    servString);
        }
        
        //Save keys
        try {
            this.log.debug(netLog.start("register.saveKeys", null, this.globals.JDBC_URL));
            this.saveKeys(serviceKeys, conn);
            this.log.debug(netLog.end("register.saveKeys", null, this.globals.JDBC_URL));
        } catch (SQLException e) {
            this.log.debug(netLog.error("register.saveKeys", ErrSev.MAJOR, e.getMessage(), this.globals.JDBC_URL));
            throw new LookupException("Error saving perfSONAR LS keys to the local database");
        }
    }
    
    private void saveKeys(HashMap<String, HashMap<String, String>> serviceKeys, Connection conn) throws SQLException {
        PreparedStatement updateStmt = conn.prepareStatement("UPDATE registrations SET registrationKey=? WHERE regElement=? AND serviceUrl=?"); 
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO registrations VALUES(DEFAULT, ?, ?, ?)");
        for(String regElement : serviceKeys.keySet()){
            for(String serviceUrl : serviceKeys.get(regElement).keySet()){
                String key = serviceKeys.get(regElement).get(serviceUrl);
                updateStmt.setString(1, key);
                updateStmt.setString(2, regElement);
                updateStmt.setString(3, serviceUrl);
                int rowCount = updateStmt.executeUpdate();
                if(rowCount == 0){
                    insertStmt.setString(1, regElement);
                    insertStmt.setString(2, serviceUrl);
                    insertStmt.setString(3, key);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    private HashMap<String, HashMap<String,String>> getServiceKeys(Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        HashMap<String,HashMap<String,String>> serviceKeys = new HashMap<String,HashMap<String,String>>();
        this.log.debug(netLog.start("register.getServiceKeys"));
        ResultSet regResults;
        try {
            regResults = conn.prepareStatement("SELECT regElement, serviceUrl, registrationKey FROM registrations").executeQuery();
            while(regResults.next()){
                String regElem = regResults.getString(1);
                if(!serviceKeys.containsKey(regElem)){
                    serviceKeys.put(regElem, new HashMap<String, String>());
                }
                serviceKeys.get(regElem).put(regResults.getString(2), regResults.getString(3));
            }
        } catch (SQLException e) {
            this.log.debug(netLog.end("register.getServiceKeys", ErrSev.CRITICAL, 
                    "Registration failed due to local database error: " + 
                    e.getMessage()));
            throw new LookupException("Unable to register service because error accessing local database");
        }
        this.log.debug(netLog.end("register.getServiceKeys"));

        return serviceKeys;
    }
    
    private String registerNode(String hostname, GeoLocation geoLocation, 
            HashMap<String, String> nodeKeys) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("register.registerNode"));
        String id = "";
        InetAddress[] nodeIPs = null;
        try {
            nodeIPs = InetAddress.getAllByName(hostname);
        } catch (UnknownHostException e) {
            this.log.debug(netLog.end("register.registerNode", ErrSev.MAJOR, e.getMessage()));
            throw new LookupException("Unknown host exception while trying to " +
                    "register node with name '"+hostname+"'. Make sure the " +
                    "name is a valid IP or DNS name.");
        }
        String name = null;
        String ip = null;
        for(InetAddress nodeIP : nodeIPs){
            if(nodeIP.isLoopbackAddress()){
                continue;
            }
            if(name == null){
                name = nodeIP.getCanonicalHostName();
            }
            ip = nodeIP.getHostAddress();
            if(name == null || ip == null){
                continue;
            }else if(ip.equals(name)){
                name = null;
            }else{
                break;
            }
        }
        
        if(ip == null){
            this.log.debug(netLog.end("register.registerNode", ErrSev.MAJOR, 
                    "No non-loopback IP address found for " + hostname));
            throw new LookupException("No non-loopback IP address found for " + hostname);
        }
        
        if(name == null){
            id = ip;
        }else if (name.indexOf('.') > -1){
            int dotIndex = name.indexOf('.');
            id += "urn:ogf:network:domain=" + name.substring(dotIndex+1);
            id += ":node=" + name.substring(0, dotIndex);
        }

        NodeRegistration nodeReg = new NodeRegistration(id);
        if(name != null){
            nodeReg.setName(name, "dns");
        }
        for(InetAddress nodeIP : nodeIPs){
            if(nodeIP.isLoopbackAddress()){ continue; }
            boolean isIPv6 = nodeIP.getClass().getName().equals("java.net.Inet6Address");
            String currIp = nodeIP.getHostAddress();
            if(currIp == null){ continue; }
            nodeReg.setL3Address(currIp, isIPv6);
        }
        
        //Set location information
        if(geoLocation != null){
            HashMap<String, String> locationInfo = new HashMap<String, String>();
            this.setLocField("cage", geoLocation.getCage(), locationInfo);
            this.setLocField("city", geoLocation.getCity(), locationInfo);
            this.setLocField("continent", geoLocation.getContinent(), locationInfo);
            this.setLocField("country", geoLocation.getCountry(), locationInfo);
            this.setLocField("floor", geoLocation.getFloor(), locationInfo);
            this.setLocField("institution", geoLocation.getInstitution(), locationInfo);
            this.setLocField("latitude", geoLocation.getLatitude(), locationInfo);
            this.setLocField("longitude", geoLocation.getLongitude(), locationInfo);
            this.setLocField("rack", geoLocation.getRack(), locationInfo);
            this.setLocField("room", geoLocation.getRoom(), locationInfo);
            this.setLocField("shelf", geoLocation.getShelf(), locationInfo);
            this.setLocField("state", geoLocation.getState(), locationInfo);
            this.setLocField("streetAddress", geoLocation.getStreetAddress(), locationInfo);
            this.setLocField("zipcode", geoLocation.getZipCode(), locationInfo);
            if(!locationInfo.isEmpty()){
                nodeReg.setLocation(locationInfo);
            }
        }
        
        //register the node
        try {
            this.globals.getPerfsonarClient().registerNode(nodeReg, nodeKeys);
        } catch (PSException e) {
            this.log.error(netLog.end("register.registerNode", ErrSev.MAJOR, e.getMessage()));
            throw new LookupException("Errow while registering node with PS Lookup service");
        }
        this.log.debug(netLog.end("register.registerNode"));
        
        return id;
    }
    
    private void setLocField(String field, String value, HashMap<String, String> locationInfo){
        if(field != null){ 
            locationInfo.put(field, value); 
        } 
    }
}
