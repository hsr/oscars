package net.es.oscars.lookup.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.lookup.LookupGlobals;
import net.es.oscars.lookup.soap.gen.AddCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.AddRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.AdminViewRequestType;
import net.es.oscars.lookup.soap.gen.DeleteCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.DeleteRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.ModifyCacheEntryRequestType;
import net.es.oscars.lookup.soap.gen.ModifyRegistrationRequestType;
import net.es.oscars.lookup.soap.gen.Protocol;
import net.es.oscars.lookup.soap.gen.RegistrationType;
import net.es.oscars.lookup.soap.gen.Relationship;
import net.es.oscars.lookup.soap.gen.ServiceType;
import net.es.oscars.lookup.soap.gen.ViewCacheResponseType;
import net.es.oscars.lookup.soap.gen.ViewRegistrationsResponseType;

public class AdminManager {
    private Logger log = Logger.getLogger(AdminManager.class);
    private LookupGlobals globals;
    
    public AdminManager(){
        try {
            this.globals = LookupGlobals.getInstance();
        } catch (LookupException e) {}
    }
    
    public void addCacheEntry(AddCacheEntryRequestType request, 
            Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        try {
            this.log.debug(netLog.start("addCacheEntry.updateDB", null, LookupGlobals.JDBC_URL));
            PreparedStatement servStmt = conn.prepareStatement("INSERT INTO services VALUES(DEFAULT, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            servStmt.setString(1, request.getType());
            servStmt.setLong(2, request.isExpires() ? System.currentTimeMillis()/1000 : 0);
            servStmt.executeUpdate();
            ResultSet genKeySet = servStmt.getGeneratedKeys();
            genKeySet.next();
            this.addProtocols(genKeySet.getInt(1), request.getProtocol(), conn);
            this.addRelationships(genKeySet.getInt(1), request.getRelationship(), conn);
            this.log.debug(netLog.end("addCacheEntry.updateDB", null, LookupGlobals.JDBC_URL));
        } catch (SQLException e) {
            this.log.debug(netLog.error("addCacheEntry.updateDB", ErrSev.MAJOR, e.getMessage(), LookupGlobals.JDBC_URL));
            throw new LookupException("SQL error occurred while trying to add the service.");
        }
    }

    public void addRegistration(AddRegistrationRequestType request, 
            Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        try{
            this.log.debug(netLog.start("addRegistration.updateDB", null, LookupGlobals.JDBC_URL));
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO registrations VALUES(DEFAULT, ?, ?, ?)");
            stmt.setString(1, request.getName());
            stmt.setString(2, request.getPublishUrl());
            stmt.setString(3, request.getKey());
            stmt.executeUpdate();
            this.log.debug(netLog.end("addRegistration.updateDB", null, LookupGlobals.JDBC_URL));
        }catch(SQLException e){
            this.log.debug(netLog.error("addRegistration.updateDB", ErrSev.MAJOR, 
                    "SQL error while trying to add registration: " + 
                    e.getMessage(), LookupGlobals.JDBC_URL));
            throw new LookupException("SQL error occurred while trying to add registration.");
        }
        
    }

    public void deleteRegistration(DeleteRegistrationRequestType request,
            Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        try{
            this.log.debug(netLog.start("deleteRegistration.updateDB", null, LookupGlobals.JDBC_URL));
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM registrations WHERE id=?");
            stmt.setInt(1, request.getRegistrationId());
            int rows = stmt.executeUpdate();
            if(rows == 0){
                this.log.debug(netLog.error("deleteRegistration.updateDB", ErrSev.MINOR,
                        "Unable to find registration with id " + 
                        request.getRegistrationId(), LookupGlobals.JDBC_URL));
                throw new LookupException("Unable to find registration with id " + request.getRegistrationId());
            }
            this.log.debug(netLog.end("deleteRegistration.updateDB", null, 
                    LookupGlobals.JDBC_URL));
        }catch(SQLException e){
            this.log.debug(netLog.error("deleteRegistration.updateDB", ErrSev.MAJOR, 
                    e.getMessage(), LookupGlobals.JDBC_URL));
            throw new LookupException("SQL error occurred while trying to delete registration.");
        }
        
    }

    public void deleteCacheEntry(DeleteCacheEntryRequestType request,
            Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        try {
            netLogParams.put("dbTable", "services");
            this.log.debug(netLog.start("deleteRegistration.updateDB", null, 
                    LookupGlobals.JDBC_URL, netLogParams));
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM services WHERE id=?");
            stmt.setInt(1, request.getServiceId());
            int rows = stmt.executeUpdate();
            if(rows == 0){
                this.log.debug(netLog.error("deleteRegistration.updateDB.services", ErrSev.MINOR, 
                        "Unable to find service with id " + request.getServiceId(), 
                        LookupGlobals.JDBC_URL, netLogParams));
                throw new LookupException("Unable to find service with id " + request.getServiceId());
            }
            this.log.debug(netLog.end("deleteRegistration.updateDBServices", 
                    null, LookupGlobals.JDBC_URL, netLogParams));
            
            netLogParams.put("dbTable", "protocols");
            this.log.debug(netLog.start("deleteRegistration.updateDB", null, 
                    LookupGlobals.JDBC_URL, netLogParams));
            PreparedStatement protoStmt = conn.prepareStatement("DELETE FROM protocols WHERE serviceId=?");
            protoStmt.setInt(1, request.getServiceId());
            protoStmt.execute();
            this.log.debug(netLog.end("deleteRegistration.updateDB", null, 
                    LookupGlobals.JDBC_URL, netLogParams));
            
            netLogParams.put("dbTable", "relationships");
            this.log.debug(netLog.start("deleteRegistration.updateDB", null, 
                    LookupGlobals.JDBC_URL, netLogParams));
            PreparedStatement relStmt = conn.prepareStatement("DELETE FROM relationships WHERE serviceId=?");
            relStmt.setInt(1, request.getServiceId());
            relStmt.execute();
            this.log.debug(netLog.end("deleteRegistration.updateDB", null, 
                    LookupGlobals.JDBC_URL, netLogParams));
        } catch (SQLException e) {
            this.log.debug(netLog.error("deleteRegistration.updateDB", ErrSev.MAJOR, 
                    e.getMessage(), LookupGlobals.JDBC_URL, netLogParams));
            throw new LookupException("SQL error occurred while trying to delete the service.");
        }
    }

    public void modifyCacheEntry(ModifyCacheEntryRequestType request,
            Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        boolean doUpdateService = false;
        int expPos = 1;
        int serviceId = request.getServiceId();
        if(serviceId <= 0){
            throw new LookupException("Service ID must be greater than 0");
        }
        
        String sql = "UPDATE services SET ";
        if(request.getType() != null){
            doUpdateService = true;
            expPos = 2;
            sql += "type=?";
        }
        if(request.isExpires() != null){
            if(doUpdateService){
                sql += ", ";
            }else{
                doUpdateService = true;
            }
            
            sql += "lastUpdated=?";
        }
        sql += " WHERE id="+serviceId;
        
        HashMap<String, String> netLogParams = new  HashMap<String, String>();
        try {
            if(doUpdateService){
                netLogParams.put("dbTable", "services");
                this.log.debug(netLog.start("modifyCacheEntry.updateDB", null, 
                        LookupGlobals.JDBC_URL, netLogParams));
                PreparedStatement servStmt = conn.prepareStatement(sql);
                if(expPos == 2){
                    servStmt.setString(1, request.getType());
                }
                if(request.isExpires() != null){
                    servStmt.setLong(expPos, (request.isExpires() ? System.currentTimeMillis()/1000 : 0));
                }
                int rows = servStmt.executeUpdate();
                if(rows == 0){
                    this.log.debug(netLog.error("modifyCacheEntry.updateDB", ErrSev.MINOR, 
                            "Unable to find service with id " + serviceId,
                            LookupGlobals.JDBC_URL, netLogParams));
                    throw new LookupException("Unable to find service with id " + serviceId);
                }
                this.log.debug(netLog.end("modifyCacheEntry.updateDB", null, 
                        LookupGlobals.JDBC_URL, netLogParams));
            }
            
            if(!request.getProtocol().isEmpty()){
                netLogParams.put("dbTable", "protocols");
                this.log.debug(netLog.start("modifyCacheEntry.updateDB", null, 
                        LookupGlobals.JDBC_URL, netLogParams));
                conn.prepareStatement("DELETE FROM protocols WHERE serviceId=" + serviceId).execute();
                this.addProtocols(serviceId, request.getProtocol(), conn);
                this.log.debug(netLog.end("modifyCacheEntry.updateDB", null, 
                        LookupGlobals.JDBC_URL, netLogParams));
            }
            
            if(!request.getRelationship().isEmpty()){
                netLogParams.put("dbTable", "relationships");
                this.log.debug(netLog.start("modifyCacheEntry.updateDB", null, 
                        LookupGlobals.JDBC_URL, netLogParams));
                conn.prepareStatement("DELETE FROM relationships WHERE serviceId=" + serviceId).execute();
                this.addRelationships(serviceId, request.getRelationship(), conn);
                this.log.debug(netLog.end("modifyCacheEntry.updateDB", null, 
                        LookupGlobals.JDBC_URL, netLogParams));
            }
        } catch (SQLException e) {
            this.log.debug(netLog.error("modifyCacheEntry.updateDB", ErrSev.MAJOR, e.getMessage(), 
                    LookupGlobals.JDBC_URL, netLogParams));
            throw new LookupException("SQL error occurred while trying to modify the service.");
        }
    }

    public void modifyRegistration(ModifyRegistrationRequestType request,
            Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        if(request.getRegistrationId() <= 0){
            throw new LookupException("Registration ID must be greater than 0");
        }
        String sql = "UPDATE registrations SET ";
        ArrayList<String> params = new ArrayList<String>();
        if(request.getName() != null){
            sql += "regElement=?";
            params.add(request.getName());
        }
        if(request.getPublishUrl() != null){
            sql += (params.isEmpty() ? "" : ", ");
            sql += "serviceUrl=?";
            params.add(request.getPublishUrl());
        }
        if(request.getKey() != null){
            sql += (params.isEmpty() ? "" : ", ");
            sql += "registrationKey=?";
            params.add(request.getKey());
        }
        sql += " WHERE id="+request.getRegistrationId();
        
        //no parameters provided so nothing to do 
        if(params.isEmpty()){
            return;
        }
        
        try{
            this.log.debug(netLog.start("modifyRegistration.updateDB", null, 
                    LookupGlobals.JDBC_URL));
            PreparedStatement stmt = conn.prepareStatement(sql);
            int i = 1;
            for(String param : params){
                stmt.setString(i, param);
                i++;
            }
            int rows = stmt.executeUpdate();
            if(rows == 0){
                String msg = "Unable to find registration with id " + 
                    request.getRegistrationId();
                this.log.debug(netLog.error("modifyRegistration.updateDB", ErrSev.MINOR, msg, 
                        LookupGlobals.JDBC_URL));
                throw new LookupException(msg);
            }
            this.log.debug(netLog.end("modifyRegistration.updateDB", null, 
                    LookupGlobals.JDBC_URL));
        }catch(SQLException e){
            this.log.debug(netLog.error("modifyRegistration.updateDB", 
                    ErrSev.MAJOR, e.getMessage(), LookupGlobals.JDBC_URL));
            throw new LookupException("SQL error occurred while trying to modify registrations.");
        }
    }

    public ViewCacheResponseType viewCache(AdminViewRequestType request, 
            Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        ViewCacheResponseType response = new ViewCacheResponseType();
        String sql = "SELECT id, type, lastUpdated FROM services";
        String event = null;
        try{
            PreparedStatement servStmt = null;
            PreparedStatement protoStmt = conn.prepareStatement("SELECT protocol, url FROM protocols WHERE serviceId=?");
            PreparedStatement relStmt = conn.prepareStatement("SELECT type, relatedTo FROM relationships WHERE serviceId=?");
            
            // Set offset in SQL
            if(request.getOffset() != null && request.getOffset() > 0){
                sql += " OFFSET " + request.getOffset() + " ROWS";
            }
            
            //Set max results with JDBC
            servStmt = conn.prepareStatement(sql);
            if(request.getMaxResults() != null && request.getMaxResults() > 0){
                servStmt.setMaxRows(request.getMaxResults());
            }
            event = "viewCache.queryServices";
            this.log.debug(netLog.start(event, null, LookupGlobals.JDBC_URL));
            ResultSet servResults = servStmt.executeQuery();
            this.log.debug(netLog.end(event, null, LookupGlobals.JDBC_URL));
            
            while(servResults.next()){
                ServiceType service = new ServiceType();
                service.setServiceId(servResults.getInt(1)+"");
                service.setType(servResults.getString(2));
                if(servResults.getLong(3) > 0){
                    service.setExpiration(servResults.getLong(3)+this.globals.getTTL());
                }else{
                    service.setExpiration(servResults.getLong(3));
                }
                protoStmt.setInt(1, servResults.getInt(1));
                event = "viewCache.queryProtocols";
                this.log.debug(netLog.start(event, null, LookupGlobals.JDBC_URL));
                ResultSet protoResults = protoStmt.executeQuery();
                this.log.debug(netLog.end(event, null, LookupGlobals.JDBC_URL));
                while(protoResults.next()){
                    Protocol proto = new Protocol();
                    proto.setType(protoResults.getString(1));
                    proto.setLocation(protoResults.getString(2));
                    service.getProtocol().add(proto);
                }
                
                relStmt.setInt(1, servResults.getInt(1));
                event = "viewCache.queryRelationships";
                this.log.debug(netLog.start(event, null, LookupGlobals.JDBC_URL));
                ResultSet relResults = relStmt.executeQuery();
                this.log.debug(netLog.end(event, null, LookupGlobals.JDBC_URL));
                while(relResults.next()){
                    Relationship rel = new Relationship();
                    rel.setType(relResults.getString(1));
                    rel.setRelatedTo(relResults.getString(2));
                    service.getRelationship().add(rel);
                }
                
                response.getService().add(service);
            }
        }catch(SQLException e){
            if(event != null){
                this.log.debug(netLog.error(event, ErrSev.MAJOR, 
                    "Database error listing services: " + e.getMessage()));
            }
            throw new LookupException("Request could not complete due to database error");
        }
        
        return response;
    }

    public ViewRegistrationsResponseType viewRegistrations(
            AdminViewRequestType request, Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        ViewRegistrationsResponseType response = new ViewRegistrationsResponseType();
        String sql = "SELECT id, regElement, serviceUrl, registrationKey FROM registrations";
        String event = "viewRegistrations.queryRegistartions";
        try{
            this.log.debug(netLog.start(event, null, LookupGlobals.JDBC_URL));
            // Set offset in SQL
            if(request.getOffset() != null && request.getOffset() > 0){
                sql += " OFFSET " + request.getOffset() + " ROWS";
            }
            
            //Set max results with JDBC
            PreparedStatement regStmt = conn.prepareStatement(sql);
            if(request.getMaxResults() != null && request.getMaxResults() > 0){
                regStmt.setMaxRows(request.getMaxResults());
            }
            
            ResultSet regResults = regStmt.executeQuery();
            this.log.debug(netLog.end(event, null, LookupGlobals.JDBC_URL));
            while(regResults.next()){
                RegistrationType reg = new RegistrationType();
                reg.setRegistrationId(regResults.getInt(1));
                reg.setName(regResults.getString(2));
                reg.setPublishUrl(regResults.getString(3));
                reg.setKey(regResults.getString(4));
                response.getRegistration().add(reg);
            }
        }catch(SQLException e){
            this.log.debug(netLog.error(event, ErrSev.MAJOR, e.getMessage(), LookupGlobals.JDBC_URL));
            throw new LookupException("Request could not complete due to database error");
        }
        
        return response;
    }
    
    private void addProtocols(int serviceId, List<Protocol> protocols, Connection conn) throws SQLException{
        PreparedStatement protoStmt = conn.prepareStatement("INSERT INTO protocols VALUES(DEFAULT, ?, ?, ?)");
        for(Protocol proto : protocols){
            protoStmt.setInt(1, serviceId);
            protoStmt.setString(2, proto.getType());
            protoStmt.setString(3, proto.getLocation());
            protoStmt.execute();
        }
        
    }
    
    private void addRelationships(int serviceId, List<Relationship> relationships, Connection conn) throws SQLException {
        PreparedStatement relStmt = conn.prepareStatement("INSERT INTO relationships VALUES(DEFAULT, ?, ?, ?)");
        for(Relationship rel : relationships){
            relStmt.setInt(1, serviceId);
            relStmt.setString(2, rel.getType());
            relStmt.setString(3, rel.getRelatedTo());
            relStmt.execute();
        }
    }
}
