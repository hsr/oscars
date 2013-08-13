package net.es.oscars.topoBridge.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import edu.internet2.perfsonar.PSLookupClient;

import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.topoBridge.ps.PSTopoPuller;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.NMWGParserUtil;

public class TopologyCache {
    private final String SRC_FILE = "file";
    private final String SRC_TS   = "topoServer";
    private final String DOMAIN_WILDCARD   = "*";

    private static Logger log = Logger.getLogger(TopologyCache.class);

    private HashMap<String, Document> domains = new HashMap<String, Document>();
    private HashMap<String, Integer>  timestamps = new HashMap<String, Integer>();
    private HashMap<String, String>   domainSources = new HashMap<String, String>();
    private HashMap<String, String>   domainFiles   = new HashMap<String, String>();
    private HashMap<String, String[]> domainServers = new HashMap<String, String[]>();
    private String[] homeLookupServers = new String[0];
    private String[] globalLookupServers = new String[0];
    
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_TOPO);

    private static TopologyCache instance;
    private String nsAbbr = null;
    private String nsUri = null;
    private String domainPrefix = null;
    private Integer cacheSeconds = null;

    /**
     * Constructor - private because this is a Singleton
     */
    private TopologyCache()  throws ConfigException{

        cc.setServiceName(ServiceNames.SVC_TOPO);
        String configFile = cc.getFilePath(ConfigDefaults.CONFIG);
        Map config = ConfigHelper.getConfiguration(configFile);
        assert config != null : "No configuration";
        Map topoBridgeCfg = (Map) config.get("topoBridge");
        assert topoBridgeCfg != null : "No topoBridge stanza in configuration";

        nsAbbr = (String) topoBridgeCfg.get("nsAbbr");
        assert nsAbbr != null : "No nsAbbr in configuration";

        nsUri = (String) topoBridgeCfg.get("nsUri");
        assert nsUri != null : "No nsUri in configuration";

        domainPrefix = (String) topoBridgeCfg.get("domainPrefix");
        assert domainPrefix != null : "No domainPrefix in configuration";

        cacheSeconds = (Integer) topoBridgeCfg.get("cacheSeconds");
        assert cacheSeconds != null : "No cacheSeconds in configuration";

        Map domainsCfg = (Map) topoBridgeCfg.get("domains");
        assert domainsCfg != null : "No domainsCfg stanza in configuration";

        Iterator domainIdIt = domainsCfg.keySet().iterator();
        while (domainIdIt.hasNext()) {
            String domainId = NMWGParserUtil.normalizeURN((String) domainIdIt.next());
            Map domainCfg = (Map) domainsCfg.get(domainId);
            String source = (String) domainCfg.get("source");
            assert source != null : "No source in configuration for domain id: "+domainId;
            domainSources.put(domainId, source);
            if (source.equals(SRC_FILE)) {
                String file = (String) domainCfg.get("file");
                assert file != null : "No file in configuration for domain id: "+domainId;
                //if absolute path given use that...
                //else assume relative to service conf directory
                if(!file.startsWith(File.separator)){
                    file = ContextConfig.getInstance(ServiceNames.SVC_TOPO).getFilePath("defaultTopoDir") + File.separatorChar + file;
                }
                domainFiles.put(domainId,file);
            } else if (source.equals(SRC_TS)) {
                ArrayList<String> tsServers = (ArrayList) domainCfg.get("servers");
                if(tsServers == null){
                    this.initPSLookupServers();
                    if (this.homeLookupServers.length == 0 && this.globalLookupServers.length == 0) { 
                        throw new ConfigException("No topology servers or lookup servers configured for " + domainId);
                    }
                    domainServers.put(domainId, null);
                    continue;
                }
                
                String[] topoServers = new String[tsServers.size()];
                int i = 0;
                for (String server : tsServers) {
                    topoServers[i] = server;
                    i++;
                }
                domainServers.put(domainId, topoServers);

            } else {
                assert false : "Unknown source type for domain id: "+domainId;
            }
        }
    }
    
    private void initPSLookupServers() throws ConfigException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start("initPSLookupServers"));
        
        if(this.homeLookupServers.length > 0 || this.globalLookupServers.length > 0){
            return;
        }
        
        //get perfSONAR lookup settings from lookup module
        ArrayList<String> globalList = new ArrayList<String>();
        String configFilename = cc.getFilePath(ServiceNames.SVC_LOOKUP,cc.getContext(),
                ConfigDefaults.CONFIG);
        HashMap<String,Object> lookupMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
        Map perfsonar = (Map) lookupMap.get("perfsonar");
        if(perfsonar == null){
            //no host lookup, so server only accepts URNs
            this.log.debug(netLogger.end("initPSLookupServers"));
            return;
        }
        HashMap<String, String> logFieldMap = new HashMap<String, String>();
        String hintsFile = null;
        List<String> globalLookupServices = null;
        List<String> homeLookupServices = null;
        
        //hints file
        if(perfsonar.containsKey("globalHintsFile")){
            hintsFile = (String) perfsonar.get("globalHintsFile");
            logFieldMap.put("hints", hintsFile);
        }
        if(hintsFile != null){
            try {
                for(String url : PSLookupClient.getGlobalHints(hintsFile)){
                    globalList.add(url);
                }
            } catch (Exception e) {
                throw new ConfigException("Error downloading lookup service hints file: " + e.getMessage());
            }
        }
        
        //global lookup services
        if(perfsonar.containsKey("globalLookupServices")){
            globalLookupServices = (List<String>) perfsonar.get("globalLookupServices");
            logFieldMap.put("gLS", OSCARSNetLogger.serializeList(globalLookupServices));
        }
        if(globalLookupServices != null){
            globalList.addAll(globalLookupServices);
        }
        this.globalLookupServers = globalList.toArray(new String[globalList.size()]);
        
        //home lookup services
        if(perfsonar.containsKey("homeLookupServices")){
            homeLookupServices = (List<String>) perfsonar.get("homeLookupServices");
            logFieldMap.put("hLS", OSCARSNetLogger.serializeList(homeLookupServices));
        }
        if(homeLookupServices != null){
            this.homeLookupServers = homeLookupServices.toArray(
                    new String[homeLookupServices.size()]);
        }
        
        this.log.debug(netLogger.end("initPSLookupServers", null, null, logFieldMap));
    }
    
    /**
     * @return the TopologyCache singleton instance
     */
    public static TopologyCache getInstance() throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        log.info(netLogger.start("TopologyCache.init"));
        try {
            if (TopologyCache.instance == null) {
                TopologyCache.instance = new TopologyCache();
            }
        } catch (ConfigException e) {
            log.error(netLogger.error("TopologyCache.init", ErrSev.MAJOR, e.getMessage()));
            throw new OSCARSServiceException (e.getMessage());
        }
        
        log.info(netLogger.end("TopologyCache.init"));
        return TopologyCache.instance;
    }


    public Document getDomain(String domainId, OSCARSNetLogger netLogger) throws OSCARSFaultMessage {
        if (!isInitialized()) {
            throw new OSCARSFaultMessage("topology cache not initialized!");
        }

        Long now = System.currentTimeMillis() / 1000;

        Document domain = null;
        boolean fetchTS = false;
        boolean fetchFile = false;

        if (domainSources.get(domainId) == null) {
            if (domainSources.get(DOMAIN_WILDCARD) != null) {
                if (domainSources.get(DOMAIN_WILDCARD).equals(SRC_FILE)) {
                    fetchFile = true;
                } else if (domainSources.get(DOMAIN_WILDCARD).equals(SRC_TS)) {
                    fetchTS = true;
                }
            } else {
                throw new OSCARSFaultMessage("No valid source for domain id: "+domainId);
            }
        } else if (domainSources.get(domainId).equals(SRC_FILE)) {
            fetchFile = true;
        } else if (domainSources.get(domainId).equals(SRC_TS)) {
            if (domains.get(domainId) == null) {
                fetchTS = true;
            } else if (timestamps.get(domainId) < now - cacheSeconds) {
                fetchTS = true;
            } else {
                return domains.get(domainId);
            }
        }

        if (!fetchTS && !fetchFile) {
            throw new OSCARSFaultMessage("No valid source for domain id: "+domainId);
        } else if (fetchTS && fetchFile) {
            // should never happen but anyway:
            throw new OSCARSFaultMessage("Cannot determine valid source for domain id: "+domainId);
        }

        if (fetchTS) {
            domain = this.fetchDomain(domainId, netLogger);
            timestamps.put(domainId, now.intValue());
            domains.put(domainId, domain);
        }

        if (fetchFile) {
            domain = this.fetchDomainFromFile(domainId, netLogger);
        }

        return domain;
    }


    /**
     * This method fetches a Domain (CtrlPlaneDomain type) from a file with a Topology (CtrlPlaneTopology type). 
     * It does a linear search to find the given domain id in the Topology representation read from the input
     * file.
     * 
     * @param domainId The id of the CtrlPlaneDomain that will be returned (if found)  
     * @param netLogger Logger object
     * @return Document (A CtrlPlaneDomain type representation of the given domain id (if found)
     * @throws OSCARSFaultMessage
     */
    private Document fetchDomainFromFile(String domainId, OSCARSNetLogger netLogger) throws OSCARSFaultMessage {
        String filename = domainFiles.get(domainId);
        if(filename == null){
            filename = domainFiles.get(DOMAIN_WILDCARD);
        }
        
        //Create map to log domain ID with netLogger
        HashMap<String, String> netLogProps = new HashMap<String, String>();
        netLogProps.put("topo", domainId);
        
        log.debug(netLogger.start("fetchDomainFromFile", null, "file:/"+filename, netLogProps));
        SAXBuilder sb = new SAXBuilder();
        Document result = null;
        
        try {
            Document sourceDoc = sb.build(new File(filename));
            Element topology = sourceDoc.getRootElement();
            List<Element> domains = topology.getChildren("domain", Namespace.getNamespace(nsUri));
            for (Element domain : domains) {
                if (domain.getAttribute("id") != null && 
                        NMWGParserUtil.normalizeURN(domain.getAttribute("id").getValue()).equals(domainId)) {
                    domain.detach();
                    result = new Document(domain);
                    log.debug(netLogger.end("fetchDomainFromFile", null, 
                            "file:/"+filename, netLogProps));
                    return result;
                }
            }
        } catch (Exception e) {
            log.debug(netLogger.error("fetchDomainFromFile", ErrSev.MINOR, 
                    e.getMessage(), "file:/"+filename));
            throw new OSCARSFaultMessage(e.getMessage());
        }
        
        log.debug(netLogger.end("fetchDomainFromFile", null, 
                "file:/"+filename, netLogProps));
        return result;
    }

    private Document fetchDomain(String domainId, OSCARSNetLogger netLogger) throws OSCARSFaultMessage {
        PSTopoPuller pl = new PSTopoPuller();
        Document result = null;
        //Get topology servers
        String[] topoServers = domainServers.get(domainId);
        if(topoServers == null){
            topoServers = domainServers.get(DOMAIN_WILDCARD);
        }
        
        //Create map to log domain ID with netLogger
        HashMap<String, String> netLogProps = new HashMap<String, String>();
        netLogProps.put("topo", domainId);
        
        log.debug(netLogger.start("fetchDomain", null, 
                OSCARSNetLogger.serializeArray(topoServers), netLogProps));
        try {
            Element topo = pl.pullTopology(topoServers, this.homeLookupServers, 
                    this.globalLookupServers, domainPrefix+domainId, nsUri);
            Element domain = topo.getChild("domain", Namespace.getNamespace(nsUri));
            domain.detach();
            result = new Document(domain);
        } catch (Exception e) {
            log.debug(netLogger.error("fetchDomain", ErrSev.MINOR, 
                 e.getMessage(), OSCARSNetLogger.serializeArray(topoServers),
                 netLogProps));
            throw new OSCARSFaultMessage(e.getMessage());
        }
        log.debug(netLogger.end("fetchDomain", null, 
                OSCARSNetLogger.serializeArray(topoServers), netLogProps));
        return result;
    }

    private boolean isInitialized() {
        if (nsAbbr == null) {
            return false;
        }

        if (nsUri == null) {
            return false;
        }

        if (cacheSeconds == null) {
            return false;
        }

        return true;
    }


    public void setNsAbbr(String nsAbbr) {
        this.nsAbbr = nsAbbr;
    }

    public String getNsAbbr() {
        return this.nsAbbr;
    }

    public void setNsUri(String nsUri) {
        this.nsUri = nsUri;
    }

    public String getNsUri() {
        return nsUri;
    }


    public void setCacheSeconds(Integer cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public Integer getCacheSeconds() {
        return this.cacheSeconds;
    }

}
