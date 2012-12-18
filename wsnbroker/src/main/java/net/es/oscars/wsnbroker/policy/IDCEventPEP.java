package net.es.oscars.wsnbroker.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.MessageType;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.w3c.dom.Element;


import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.topology.NMWGParserUtil;

public class IDCEventPEP implements NotifyPEP{
    Logger log = Logger.getLogger(IDCEventPEP.class);
    
    public String getPermissionName() {
        return AuthZConstants.QUERY;
    }

    public String getResourceName() {
        return AuthZConstants.RESERVATIONS;
    }

    public boolean topicMatches(List<String> topics) {
        for(String topic : topics){
            if(topic.startsWith("idc:")){
                return true;
            }
        }
        return false;
    }

    public boolean enforcePolicy(MessageType notifyMsg,
            ArrayList<String> authZLogins, ArrayList<String> authZDomains) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start("IDCEventPEP.enforcePolicy"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        
        if(notifyMsg.getAny() == null || notifyMsg.getAny().isEmpty()){
            netLogProps.put("allowed", "true");
            this.log.debug(netLogger.end("IDCEventPEP.enforcePolicy", null, null, netLogProps));
            return true;
        }
        
        for(Object eventObj : notifyMsg.getAny()){
            //Convert element to string...
            Element eventElem = (Element) eventObj;
            
            //convert string to IDC Event
            EventContent event = null;
            try {
                JAXBContext context = JAXBContext.newInstance(EventContent.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                event = (EventContent) ((JAXBElement)unmarshaller.unmarshal(eventElem)).getValue();
            } catch (Exception e) {
                continue;
            }
            
            //check logins
            boolean loginPassed = false;
            if(authZLogins == null || authZLogins.isEmpty()){
                //if no logins assume all allowed
                loginPassed = true;
            }
            for(String login : authZLogins){
                //check originator - pretty ugly because we have to dig way 
                //into the message but it gets the job done
                if(event.getMessageProperties() == null ||
                        event.getMessageProperties().getOriginator() == null ||
                        event.getMessageProperties().getOriginator().getSubjectAttribute() == null){
                    //if no login then assume its ok
                    // may need to revisit this if it causes a problem
                    loginPassed = true;
                    break;
                }
                for(AttributeType attr : event.getMessageProperties().getOriginator().getSubjectAttribute()){
                    if(AuthZConstants.LOGIN_ID.equals(attr.getName()) && 
                            attr.getAttributeValue() != null &&
                            !attr.getAttributeValue().isEmpty()){                        
                        if(login.toLowerCase().equals(attr.getAttributeValue().get(0).toString().toLowerCase())){
                            loginPassed = true;
                            break;
                        }
                    }
                }
                //we can stop checking if they are allowed to see the originators resvs
                if(loginPassed || event.getResDetails() == null ||
                        event.getResDetails().getLogin() == null){
                    break;
                }
                
                //check reservation owner
                if(login.toLowerCase().equals(event.getResDetails().getLogin().toLowerCase())){
                    loginPassed = true;
                    break;
                }
                
            }
            if(!loginPassed){
                netLogProps.put("allowed", "false");
                netLogProps.put("loginPassed", "false");
                this.log.debug(netLogger.end("IDCEventPEP.enforcePolicy", null, null, netLogProps));
                return false;
            }
            
            //check domains
            if(authZDomains == null || authZDomains.isEmpty()){
                continue;
            }
            boolean domainsPassed = false;
            CtrlPlanePathContent path = this.extractPath(event.getResDetails());
            if(path == null){
                //if event does not have a path then nothing to check
                continue;
            }
            
            for(CtrlPlaneHopContent hop : path.getHop()){
                for(String authZDomain : authZDomains){
                    authZDomain = NMWGParserUtil.normalizeURN(authZDomain);
                    try {
                        if(authZDomain.equals(NMWGParserUtil.getURN(hop, 
                                NMWGParserUtil.DOMAIN_TYPE))){
                            domainsPassed = true;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            if(!domainsPassed){
                netLogProps.put("allowed", "false");
                netLogProps.put("domainsPassed", "false");
                this.log.debug(netLogger.end("IDCEventPEP.enforcePolicy", null, null, netLogProps));
                return false;
            }
        }
        netLogProps.put("allowed", "true");
        this.log.debug(netLogger.end("IDCEventPEP.enforcePolicy", null, null, netLogProps));
        return true;
    }

    private CtrlPlanePathContent extractPath(ResDetails resDetails) {
        if(resDetails == null){
            return null;
        }
        
        CtrlPlanePathContent path = null;
        if(resDetails.getReservedConstraint() != null &&
                resDetails.getReservedConstraint().getPathInfo() != null &&
                resDetails.getReservedConstraint().getPathInfo().getPath() != null){
            path =  resDetails.getReservedConstraint().getPathInfo().getPath();
        }else if(resDetails.getUserRequestConstraint() != null &&
                resDetails.getUserRequestConstraint().getPathInfo() != null &&
                resDetails.getUserRequestConstraint().getPathInfo().getPath() != null){
            path = resDetails.getUserRequestConstraint().getPathInfo().getPath();
        }
        
        return path;
    }

}
