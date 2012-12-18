package net.es.oscars.lookup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

import edu.internet2.perfsonar.PSException;
import edu.internet2.perfsonar.PSNamespaces;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.lookup.soap.gen.LookupRequestContent;
import net.es.oscars.lookup.soap.gen.LookupResponseContent;
import net.es.oscars.lookup.soap.gen.Protocol;
import net.es.oscars.lookup.soap.gen.Relationship;

public class LookupManager {
    private Logger log = Logger.getLogger(LookupManager.class);
    private LookupGlobals globals;
    private PSNamespaces psNs;
    
    public LookupManager(){
        try {
            this.globals = LookupGlobals.getInstance();
        } catch (LookupException e) {}
        psNs = new PSNamespaces();
    }
    
    public LookupResponseContent lookup(LookupRequestContent lookupRequest, 
            Connection conn) throws LookupException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        LookupResponseContent response = null;
        String query = "SELECT s.id, s.type FROM services AS s ";
        ArrayList<String> sqlParams = new ArrayList<String>();

        //error checking
        if(lookupRequest.getHasRelationship() != null && 
                lookupRequest.getHasLocation() != null){
            throw new LookupException("Request must contain either " +
                    "hasRelationship or hasLocation field, not both.");
        }else if(lookupRequest.getHasRelationship() == null && 
                lookupRequest.getHasLocation() == null){
            throw new LookupException("Request does not contain a " +
                    "hasRelationship or hasLocation field");
        }else if(lookupRequest.getHasRelationship() != null){
            query += "INNER JOIN relationships AS r ON " +
                    "r.serviceId = s.id WHERE " +
                    "s.type = ? AND r.type=? AND " +
                    "r.relatedTo=?";
            sqlParams.add(lookupRequest.getType());
            sqlParams.add(lookupRequest.getHasRelationship().getType());
            sqlParams.add(lookupRequest.getHasRelationship().getRelatedTo());
        }else if(lookupRequest.getHasLocation() != null){
            query += "INNER JOIN protocols AS p ON p.serviceId = s.id " +
                "WHERE s.type=? AND p.url=?";
            sqlParams.add(lookupRequest.getType());
            sqlParams.add(lookupRequest.getHasLocation());
        }
        
        //query local database
        try {
            this.log.debug(netLog.start("lookup.databaseLookup", null, LookupGlobals.JDBC_URL));
            response = this.databaseLookup(query, sqlParams, conn);
            this.log.debug(netLog.end("lookup.databaseLookup", null, LookupGlobals.JDBC_URL));
        } catch (Exception e) {
            this.log.debug(netLog.error("lookup.databaseLookup", 
                    ErrSev.CRITICAL, e.getMessage(), LookupGlobals.JDBC_URL));
            throw new LookupException("Error querying lookup database");
        }

        //query perfSONAR LS if not in DB
        //NOTE: Currently only lookups by relationship work to LS since URLs are not summarized
        if(response == null && lookupRequest.getHasRelationship() != null && 
                this.globals.getPerfsonarClient() != null){
            this.log.debug(netLog.start("lookup.perfsonarLookup"));
            try{
                response = this.perfsonarLookup(lookupRequest);
            }catch(PSException e){
                this.log.debug(netLog.error("lookup.perfsonarLookup", ErrSev.MAJOR, 
                        "Error contacting perfSONAR Lookup Service"));
                throw new LookupException("Error contacting perfSONAR Lookup Service");
            }
            this.log.debug(netLog.end("lookup.perfsonarLookup"));
            
            try{
                this.log.debug(netLog.start("lookup.storeResponse"));
                this.storeResponse(response, conn);
                this.log.debug(netLog.end("lookup.storeResponse"));
            }catch(Exception e){
                this.log.debug(netLog.end("lookup.storeResponse", ErrSev.MAJOR, 
                        "Error storing PS LS response in DB: " + e.getMessage()));
                //continue anyways because still found the service, it just wasn't cached
            }
        }

        //if still not found
        if(response == null){
            throw new LookupException("Unable to find " + lookupRequest.getType());
        }
       
        return response;
    }
    
    private LookupResponseContent databaseLookup(String query, List<String> sqlParams, Connection conn) throws SQLException {
        LookupResponseContent response = null;
        PreparedStatement lookupStmt = conn.prepareStatement(query);
        for(int i = 0; i < sqlParams.size(); i++){
            lookupStmt.setString(i+1, sqlParams.get(i));
        }
        ResultSet servResults = lookupStmt.executeQuery();
        //just get first service returned
        if(servResults.next()){
            response = new LookupResponseContent();
            response.setType(servResults.getString(2));
            int serviceId  = servResults.getInt(1);
            PreparedStatement protoStmt = conn.prepareStatement("SELECT " +
                    "protocol, url FROM protocols WHERE serviceId=?");
            protoStmt.setInt(1, serviceId);
            ResultSet protoResults = protoStmt.executeQuery();
            while(protoResults.next()){
                Protocol proto = new Protocol();
                proto.setType(protoResults.getString(1));
                proto.setLocation(protoResults.getString(2));
                response.getProtocol().add(proto);
            }
            protoStmt.close();
            
            PreparedStatement relStmt = conn.prepareStatement("SELECT " +
                    "type, relatedTo FROM relationships WHERE serviceId=?");
            relStmt.setInt(1, serviceId);
            ResultSet relResults = relStmt.executeQuery();
            while(relResults.next()){
                Relationship rel = new Relationship();
                rel.setType(relResults.getString(1));
                rel.setRelatedTo(relResults.getString(2));
                response.getRelationship().add(rel);
            }
            relStmt.close();
        }
        lookupStmt.close();
        
        return response;
    }

    private void storeResponse(LookupResponseContent response, Connection conn) throws SQLException, LookupException {
        if(response == null){
            return;
        }
        
        PreparedStatement servStmt = conn.prepareStatement("INSERT INTO services VALUES(DEFAULT, ?, ?)", 
                Statement.RETURN_GENERATED_KEYS);
        servStmt.setString(1, response.getType());
        servStmt.setLong(2, System.currentTimeMillis()/1000);
        servStmt.executeUpdate();
        ResultSet genKeys = servStmt.getGeneratedKeys();
        if(!genKeys.next()){
            throw new LookupException("No auto generated keys for service");
        }
        int serviceId = genKeys.getInt(1);
        servStmt.close();
        
        PreparedStatement relStmt = conn.prepareStatement("INSERT INTO relationships VALUES(DEFAULT, ?, ?, ?)");
        relStmt.setInt(1, serviceId);
        for(Relationship rel : response.getRelationship()){
            relStmt.setString(2, rel.getType());
            relStmt.setString(3, rel.getRelatedTo());
            relStmt.executeUpdate();
        }
        relStmt.close();
        
        PreparedStatement protoStmt = conn.prepareStatement("INSERT INTO protocols VALUES(DEFAULT, ?, ?, ?)");
        protoStmt.setInt(1, serviceId);
        for(Protocol proto : response.getProtocol()){
            protoStmt.setString(2, proto.getType());
            protoStmt.setString(3, proto.getLocation());
            protoStmt.executeUpdate();
        }
        protoStmt.close();
    }

    private LookupResponseContent perfsonarLookup(LookupRequestContent lookupRequest) throws PSException{
        LookupResponseContent response = new LookupResponseContent();
        Element datum = null;
       datum = this.globals.getPerfsonarClient().lookupService(lookupRequest.getType(), 
                    lookupRequest.getHasRelationship().getRelatedTo(), 
                    lookupRequest.getHasRelationship().getType(), "", "");
        
        if(datum == null){
            return null;
        }
        Element service = datum.getChild("service", this.psNs.TOPO);
        if(service == null){
            return null;
        }
        response.setType(service.getChildText("type", this.psNs.TOPO));
        if(response.getType() == null){
            return null;
        }
        List<Element> ports = service.getChildren("port", this.psNs.TOPO);
        for(Element port : ports){
            Protocol protocol = new Protocol();
            protocol.setLocation(port.getChildText("address", this.psNs.TOPO));
            if(protocol.getLocation() == null){
                return null;
            }
            Element psProtocol = port.getChild("protocol", this.psNs.TOPO);
            if(psProtocol == null){
                return null;
            }
            protocol.setType(psProtocol.getChildText("type", this.psNs.TOPO));
            if(protocol.getType() == null){
                return null;
            }
            response.getProtocol().add(protocol);
        }
        
        List<Element> relations = (List<Element>) service.getChildren("relation", this.psNs.TOPO);
        for(Element relation : relations){
            String relType = relation.getAttributeValue("type");
            if(relType == null){
                continue;
            }
            String relTo = relation.getChildText("idRef", this.psNs.TOPO);
            if(relTo == null){
                relTo = relation.getChildText("address", this.psNs.TOPO);
            }
            if(relTo == null){
                continue;
            }
            Relationship relationship = new Relationship();
            relationship.setType(relType);
            relationship.setRelatedTo(relTo);
            response.getRelationship().add(relationship);
        }
        return response;
    }
}