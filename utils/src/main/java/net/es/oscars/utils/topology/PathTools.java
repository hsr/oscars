package net.es.oscars.utils.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

public class PathTools {
    private static Logger LOG = Logger.getLogger(PathTools.class);
    private static String localDomainID = null;
 
    /*
     * getDomains
     * 
     * Returns an array of strings containing the ordered list of the domains within the provided path.
     */
    public static ArrayList<String> getDomains (CtrlPlanePathContent path) throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        if (path == null) {
            LOG.error(netLogger.error("getDomains",ErrSev.MINOR,"path is null"));
            throw new OSCARSServiceException ("path is null");
        }
        //extract domains
        ArrayList<String> domainStack = new ArrayList<String>();
        
        String lastDomain = null;
        
        for(CtrlPlaneHopContent hop : path.getHop()){
            String domainId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.DOMAIN_TYPE));
            if(!domainId.equals(lastDomain)){
                domainStack.add(domainId);
            }
            lastDomain = domainId;
        }
        
        return domainStack;
    }

    /*
     * getLastDomain
     * 
     * Returns a string containing the identifier of the last domain of currentDomain in the provided path.
     */
    public static String getLastDomain(CtrlPlanePathContent path) throws OSCARSServiceException {
        ArrayList<String> domains = PathTools.getDomains(path);
        int nbOfDomains = domains.size();
        if (nbOfDomains == 0) {
            throw new OSCARSServiceException ("invalid argument");
        }
        // Get the last domain in the stack
        String lastDomain = domains.get(nbOfDomains - 1);
        return lastDomain;
    }
 
    /*
     * getFirstDomain
     * 
     * Returns a string containing the identifier of the first domain of currentDomain in the provided path.
     */
    public static String getFirstDomain(CtrlPlanePathContent path) throws OSCARSServiceException {
        ArrayList<String> domains = PathTools.getDomains(path);
        int nbOfDomains = domains.size();
        if (nbOfDomains == 0) {
            throw new OSCARSServiceException ("invalid argument");
        }
        // Get the first domain in the stack
        String lastDomain = domains.get(0);
        return lastDomain;
    }
    
    /*
     * getNextDomain
     * 
     * Returns a string containing the identifier of the next domain of currentDomain in the provided path.
     * Returns null if either currentDomain is not in path, of if currentDomain is the last domain in the path 
     * 
     */
    public static String getNextDomain(CtrlPlanePathContent path, String currentDomain) throws OSCARSServiceException {
        ArrayList<String> domains = PathTools.getDomains(path);
        int nbOfDomains = domains.size();
        if ((nbOfDomains == 0) || (currentDomain == null)) {
            throw new OSCARSServiceException ("invalid arguments");
        }
        // Get the next domain in the stack
        int index = 0;
        for (String domain : domains) {
            if (domain.equals(currentDomain)) {
                if ((index + 1) <= (nbOfDomains - 1)) {
                    return domains.get(index + 1);
                }
            }
            ++index;
        }
        return null;
    }
    
    /*
     * getPreviousDomain
     * 
     * Returns a string containing the identifier of the previous domain of currentDomain in the provided path.
     * Returns null if either currentDomain is not in path, of if currentDomain is the first domain in the path 
     * 
     */
    public static String getPreviousDomain(CtrlPlanePathContent path, String currentDomain) throws OSCARSServiceException {
        ArrayList<String> domains = PathTools.getDomains(path);
        int nbOfDomains = domains.size();
        if ((nbOfDomains == 0) || (currentDomain == null)) {
            throw new OSCARSServiceException ("invalid arguments");
        }
        // Get the previous domain in the stack
        int index = 0;
        for (String domain : domains) {
            if (domain.equals(currentDomain)) {
                if ((index - 1) >= 0) {
                    return domains.get(index - 1);
                }
            }
            ++index;
        }
        return null;
    }
 
    public static boolean isPathLocalOnly (CtrlPlanePathContent path) {
        try {
            ArrayList<String> domains = PathTools.getDomains(path);
            if ((domains == null) || (domains.size() == 1) || (domains.size() == 0)) {
                return true;
            } else {
                return false;
            }
        } catch (OSCARSServiceException e) {
            // An invalid path is considered as local only
            return true;
        }
    }
    
    /**
     * Sets the identifier of the local domain (use for unit testing etc)
     * 
     */
    public static void setLocalDomainId(String domainId) {
        PathTools.localDomainID = domainId;
    }

    /**
     * Returns the identifier of the local domain.
     * 
     * @return the identifier of the local domain
     * @throws ConfigException 
     */
    public static String getLocalDomainId() {
        if (PathTools.localDomainID != null) {
            return NMWGParserUtil.normalizeURN(PathTools.localDomainID);
        }
        HashMap<String,Object> localDomainMap = PathTools.getLocalDomainSettings();
        if(localDomainMap == null || !localDomainMap.containsKey("id")){
            return null;
        }
        PathTools.localDomainID = localDomainMap.get("id") + "";
        return NMWGParserUtil.normalizeURN(PathTools.localDomainID);
    }
    
    /**
     * Returns the settings of the local domain as a map
     * 
     * @return the local domain settings
     * @throws ConfigException 
     */
    public static HashMap<String, Object> getLocalDomainSettings() {
        ContextConfig cc = ContextConfig.getInstance();
        String configFilename = null;
        try {
            configFilename = cc.getFilePath(ServiceNames.SVC_UTILS,
                                            cc.getContext(),
                                            ConfigDefaults.CONFIG);
        } catch (ConfigException e) {
            return null;
        }
        HashMap<String,Object> utilConfig = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
        HashMap<String,Object> localDomainMap = null;
        if(utilConfig.containsKey("localDomain")){
            localDomainMap = (HashMap<String,Object>) utilConfig.get("localDomain");
        }
        
        return localDomainMap;
    }
    
    /**
     * Returns the ingress link for a domain given a path
     * 
     * @param domainId the domain with the ingress you are trying to find
     * @param path the path to search
     * @return the egress link
     * @throws OSCARSServiceException
     */
    public static CtrlPlaneLinkContent getIngressLink(String domainId, CtrlPlanePathContent path) throws OSCARSServiceException{
        domainId = NMWGParserUtil.normalizeURN(domainId);
        for(CtrlPlaneHopContent hop : path.getHop()){
            if(domainId.equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.DOMAIN_TYPE)))){
                if( hop.getLink() == null){
                    return null;
                }
                return hop.getLink();
            }
        }
        
        return null;
    }
    
    /**
     * Returns the egress link for a domain given a path
     * 
     * @param domainId the domain with the egress you are trying to find
     * @param path the path to search
     * @return the egress link
     * @throws OSCARSServiceException
     */
    public static CtrlPlaneLinkContent getEgressLink(String domainId, CtrlPlanePathContent path) throws OSCARSServiceException{
        domainId = NMWGParserUtil.normalizeURN(domainId);
        CtrlPlaneLinkContent egressLink = null;
        for(CtrlPlaneHopContent hop : path.getHop()){
            if(domainId.equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.DOMAIN_TYPE)))){
                egressLink = hop.getLink();
            }else if(egressLink != null){
                return egressLink;
            }
        }
        
        return egressLink;
    }
    
    /**
     * returns just the part of the hops in a path that match the domainId passed
     * 
     * @param path
     * @param domainId
     * @return the local part of the hops
     * @throws OSCARSServiceException
     */
    
    public static List<CtrlPlaneHopContent> getLocalHops(CtrlPlanePathContent path, String domainId) throws OSCARSServiceException {
        List<CtrlPlaneHopContent> localHops = new ArrayList<CtrlPlaneHopContent>();
        List<CtrlPlaneHopContent> hops = path.getHop();
        for (int i = 0; i < hops.size(); i++) {
            if (!domainId.equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hops.get(i), NMWGParserUtil.DOMAIN_TYPE)))) {
                continue;
            }
            localHops.add(hops.get(i));
        }
        return localHops;

        
    }
}
