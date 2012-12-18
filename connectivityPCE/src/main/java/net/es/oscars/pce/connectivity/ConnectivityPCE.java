package net.es.oscars.pce.connectivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.clients.TopoBridgeClient;
import net.es.oscars.topoBridge.soap.gen.GetTopologyRequestType;
import net.es.oscars.topoBridge.soap.gen.GetTopologyResponseType;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;
import net.es.oscars.utils.topology.NMWGTopoBuilder;
import net.es.oscars.utils.topology.PathTools;

/**
 * Implementation of a connectivity PCE that looks at the given path and 
 * builds a connected graph by pulling topologies from the topology service.
 * This PCE is intended to be the first PCE called by the PCE Runtime as it 
 * creates an initial graph that can be pruned by other PCEs. 
 * 
 * It determines which topologies to pull by doing a breadth first search from 
 * the destination domain to the source domain. The breadth first search stops when
 * the source domain is reached, it has exhausted the list of connected domains, or 
 * the breadth-first search as exceeded the maximum search depth as specified by the 
 * maxSearchDepth property. The default maximum search depth is 10.

 * @author Andy Lake <andy@es.net>
 *
 */
public class ConnectivityPCE {
    private Logger log = Logger.getLogger(ConnectivityPCE.class);
    private TopoBridgeClient topoBridgeClient;
    private String topoBridgeUrl;
    private String topoBridgeWsdl;
    private String moduleName;
    private int maxSearchDepth;
    
    private final int DEFAULT_MAX_DEPTH = 10;
    
    
    /**
     * @param moduleName a String indicating the name of the module as it 
     *  should be shown in the logs for NetLogger events.
     * @param topoBridgeUrl the URL of the TopoBridge service
     * @param topoBridgeWSDL a URL to the location of the TopoBridge service WSDL
     * @throws OSCARSServiceException
     */
    public ConnectivityPCE(String moduleName, String topoBridgeUrl, String topoBridgeWsdl) throws OSCARSServiceException{
        this.moduleName = moduleName;
        OSCARSNetLogger netLogger = new OSCARSNetLogger(moduleName);
        this.log.info(netLogger.start("init"));
        this.topoBridgeClient = null;
        this.maxSearchDepth = DEFAULT_MAX_DEPTH;
        this.topoBridgeUrl = topoBridgeUrl;
        this.topoBridgeWsdl = topoBridgeWsdl;
        this.log.info(netLogger.end("init"));
    }
    
    /**
     * Core method that looks at a PCEQuery to determine the domains in the 
     * path and then pulls the topologies.
     * 
     * @param query the PCEMessage to process
     * @return a PCEDataContent object containing the original constraints and
     *  a topology element containing the graph built
     * @throws OSCARSServiceException
     */
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException{
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start("calculatePath"));
        PCEDataContent pceData = query.getPCEDataContent();
        if(pceData.getTopology() != null){
            this.log.debug(netLogger.error("calculatePath", ErrSev.MINOR, "Connectivity PCE " +
                    "received a request with a non-null topology."));
            throw new OSCARSServiceException("Topology must be empty for connectivityPCE");
        }
        
        CtrlPlaneTopologyContent topoGraph = new CtrlPlaneTopologyContent();
        
        //get path constraint
        CtrlPlanePathContent path = null;
        if(pceData.getReservedConstraint() != null && 
                pceData.getReservedConstraint().getPathInfo() != null){
            path = pceData.getReservedConstraint().getPathInfo().getPath();
        }else if(pceData.getUserRequestConstraint() != null && 
                pceData.getUserRequestConstraint().getPathInfo() != null){
            path = pceData.getUserRequestConstraint().getPathInfo().getPath();
        }else{
            String msg = "Connectivity PCE received a request with no " +
                    "reservedConstraint or userRequestConstraint containing " +
                    "a PathInfo element";
            this.log.debug(netLogger.error("calculatePath", ErrSev.MINOR, msg));
            throw new OSCARSServiceException(msg);
        }
        
        //make sure path is not null
        if(path == null){
            this.log.debug(netLogger.error("calculatePath", ErrSev.MINOR, "Received a null path in request"));
            throw new OSCARSServiceException("Received a null path in request");
        }
        
        //extract domains
        ArrayList<String> domainStack = PathTools.getDomains(path);
        
        //Contact topology service
        ArrayList<String> queryQueue = new ArrayList<String>();
        HashMap<String, Boolean> visitedDomains = new HashMap<String, Boolean>();
        while(!domainStack.isEmpty()){
            String currentDomain = domainStack.remove(domainStack.size() - 1);
            queryQueue.add(currentDomain);
            // Leave the targetDomain null when we're querying the source domain (which we do last)
            // otherwise set the targetDomain to the next domain in the stack
            String targetDomain = null;
            if(!domainStack.isEmpty()){
                targetDomain = domainStack.get(domainStack.size() - 1);
            }
            try{
                int depthCount = 0;
                while(!queryQueue.isEmpty()){
                    if(depthCount > this.maxSearchDepth){
                        throw new OSCARSServiceException("Unable to find " +
                                "connectivity between " + currentDomain + 
                                " and " + targetDomain + " because exceeded " +
                                "the maximum search depth of " + this.maxSearchDepth);
                    }
                    List<CtrlPlaneDomainContent> domains = 
                        this.processDomains(targetDomain, currentDomain,
                                queryQueue, visitedDomains, netLogger);
                    topoGraph.getDomain().addAll(domains);
                    depthCount++;
                }
            }catch(OSCARSServiceException e){
                this.log.error(netLogger.error("calculatePath", ErrSev.MAJOR, e.getMessage()));
                throw e;
            }
        }

        pceData.setTopology(topoGraph);
        this.log.debug(netLogger.end("calculatePath"));
        return pceData;
    }
    
    /**
     * Calls the topology service and determines the direct neighboring 
     * domains of each domain polled. It adds these neighbors to the 
     * queryQueue if they have not yet been visited. It will return when the 
     * target domain has been found or it has visited all the domains at the
     * current level of the breadth-first search.
     * 
     * @param targetDomain the next domain in the path that once found indicates we should return
     * @param currentDomain the current domain. Not needed for calculation but passed so useful error messages may be produced/
     * @param queryQueue a queue of domains that need to be pulled from the TopoBridge
     * @param visitedDomains a Map indicating the domains that have already been visited
     * @param netLogger an OSCARSNetLogger class passed so guid and other session variables remain consistent in logs
     * @return a list of domains to add to the topology element
     * @throws OSCARSServiceException
     */
    private List<CtrlPlaneDomainContent> processDomains(String targetDomain, 
            String currentDomain, ArrayList<String> queryQueue, 
            HashMap<String, Boolean> visitedDomains, 
            OSCARSNetLogger netLogger) throws OSCARSServiceException{
        ArrayList<CtrlPlaneDomainContent> domains = new ArrayList<CtrlPlaneDomainContent>();
        GetTopologyRequestType topoRequest = new GetTopologyRequestType();
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setGlobalTransactionId("TBD");
        topoRequest.setMessageProperties(msgProps);
        topoRequest.getDomainId().addAll(queryQueue);
        queryQueue.clear();
        GetTopologyResponseType topoResponse = null;
        HashMap<String, String> netLogProps = new HashMap<String, String>();
        netLogProps.put("topos", OSCARSNetLogger.serializeList(topoRequest.getDomainId()));
        try {
            
            this.log.debug(netLogger.start("getTopology", null, this.topoBridgeUrl, netLogProps));
            synchronized(this){
                if(this.topoBridgeClient == null){
                    this.topoBridgeClient = TopoBridgeClient.getClient(topoBridgeUrl, this.topoBridgeWsdl);
                }
            }
            Object[] request = {topoRequest};
            Object [] response = this.topoBridgeClient.invoke("getTopology", request);
            //response = this.topoBridgeClient.getTopology(topoRequest);
            topoResponse = (GetTopologyResponseType)response[0];
            this.log.debug(netLogger.end("getTopology", null, this.topoBridgeUrl, netLogProps));
        } catch (Exception e) {
            this.log.debug(netLogger.error("getTopology",ErrSev.MAJOR, e.getMessage(), topoBridgeUrl, netLogProps));
            throw new OSCARSServiceException("Error from topoBridge: " + e.getMessage());
        }
        
        HashMap<String, Boolean> edgeDomainMap = new HashMap<String, Boolean>();
        for(CtrlPlaneTopologyContent topo : topoResponse.getTopology()){
            for(CtrlPlaneDomainContent domain : topo.getDomain()){
                String domainId = NMWGParserUtil.normalizeURN(domain.getId());
                visitedDomains.put(domainId, true);
                domains.add(domain);
                
                //if this is the last domain then we don't need to look any further
                if(targetDomain == null){
                    return domains;
                }
                
                //Get edge domains
                for(CtrlPlaneNodeContent node : domain.getNode()){
                    for(CtrlPlanePortContent port : node.getPort()){
                        for(CtrlPlaneLinkContent link : port.getLink()){
                            String remoteDomain = NMWGParserUtil.getURN(link.getRemoteLinkId(), NMWGParserUtil.DOMAIN_TYPE);
                            remoteDomain = NMWGParserUtil.normalizeURN(remoteDomain);
                            if(!NMWGParserUtil.TOPO_ID_WILDCARD.equals(remoteDomain) &&
                                    !domainId.equals(remoteDomain) && 
                                    !visitedDomains.containsKey(remoteDomain)){
                                edgeDomainMap.put(remoteDomain, true);
                            }
                        }
                    }
                }
                
                //if found targetDomain then return and it will be retrieved 
                //on the next call to this method
                if(targetDomain != null && edgeDomainMap.containsKey(targetDomain)){
                    return domains;
                }
            }
        }
        
        //if we're here then we didn't find the target domain so if there's
        //no new domains to search then we have no connectivity
        if(edgeDomainMap.isEmpty()){
            throw new OSCARSServiceException(currentDomain + " is not connected to " + 
                    targetDomain + " given the current topologies registered for each domain");
        }
        
        //add to edge domains to queue
        queryQueue.addAll(edgeDomainMap.keySet());
        
        return domains;
    }
    
    /**
     * Returns the maximum number of levels for which the breadth-first 
     * search will traverse between two domains in the path until it fails.
     * 
     * @return the value of the maxSearchDepth property
     */
    public int getMaxSearchDepth() {
        return this.maxSearchDepth;
    }

    /**
     * Sets the maximum number of levels for which the breadth-first 
     * search will traverse between two domains in the path until it fails.
     * 
     * @param maxSearchDepth the value of the maxSearchDepth property to set
     */
    public void setMaxSearchDepth(int maxSearchDepth) {
        this.maxSearchDepth = maxSearchDepth;
    }
    
    /**
     * Convert the path constraint to a topology for other PCEs during the commit phase
     * 
     * 
     */
    public PCEDataContent commitPath(PCEMessage query) throws OSCARSServiceException{
        PCEDataContent pceData = query.getPCEDataContent();
        
        //get path - during commit MUST have reservedConstraint
        CtrlPlanePathContent path = null;
        if(pceData.getReservedConstraint() != null && 
                pceData.getReservedConstraint().getPathInfo() != null){
            path = pceData.getReservedConstraint().getPathInfo().getPath();
        }else{
            throw new OSCARSServiceException("Received a request with no " +
                    "reservedConstraint containing a PathInfo element");
        }
        
        //make sure path is not null
        if(path == null || path.getHop() == null || path.getHop().isEmpty()){
            throw new OSCARSServiceException("Received an empty path in request");
        }
        
        //create topology
        String prevNodeId = null;
        CtrlPlaneLinkContent prevLink = null;
        NMWGTopoBuilder topoBuilder = new NMWGTopoBuilder();
        for(CtrlPlaneHopContent hop : path.getHop()){
            //path during commit must have links so no need for extra checks
            if(hop.getLink() == null){
                throw new OSCARSServiceException("Received path during commit phase with no link object in hop");
            }
            //make sure the remoteLinkId is set since no implied order in topology element
            String currNodeId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.NODE_TYPE));
            if(prevLink != null && prevLink.getRemoteLinkId() == null && 
                    !currNodeId.equals(prevNodeId)){
                prevLink.setRemoteLinkId(NMWGParserUtil.getURN(hop, NMWGParserUtil.LINK_TYPE));
                hop.getLink().setRemoteLinkId(NMWGParserUtil.getURN(prevLink.getId(), NMWGParserUtil.LINK_TYPE));
            }else if(prevLink != null && prevLink.getRemoteLinkId() == null){
                prevLink.setRemoteLinkId("urn:ogf:network:*:*:*:*");
            }else if(prevLink != null && prevLink.getRemoteLinkId() != null && 
                    hop.getLink().getRemoteLinkId() == null && 
                    NMWGParserUtil.normalizeURN(prevLink.getRemoteLinkId()).equals(NMWGParserUtil.normalizeURN(hop.getLink().getId()))){
                hop.getLink().setRemoteLinkId(NMWGParserUtil.getURN(prevLink.getId(), NMWGParserUtil.LINK_TYPE));
            }
            topoBuilder.addLink(hop.getLink());
            prevNodeId = currNodeId;
            prevLink = hop.getLink();
        }
        //if last link remoteLinkId is not set then set to wildcard
        if(prevLink != null && prevLink.getRemoteLinkId() == null){
            prevLink.setRemoteLinkId("urn:ogf:network:*:*:*:*");
        }
        pceData.setTopology(topoBuilder.getTopology());
        
        return pceData;
    }
}
