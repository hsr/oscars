package net.es.oscars.pce.vlan;

import java.net.MalformedURLException;
import java.net.URL;
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
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.topology.VlanRange;

public class VlanPCE {
    private Logger log = Logger.getLogger(VlanPCE.class);
    private RMClient rmClient;
    private String rmUrl;
    private String rmWsdl;
    private String localDomain;
    private String vlanScope;
    private boolean mpls;
    
    final static public String SCOPE_DOMAIN = "domain";
    final static public String SCOPE_NODE = "node";
    final static public String SCOPE_PORT = "port";
    final static public String SCOPE_LINK = "link";
    final private String L2_SWCAP_TYPE = "l2sc";

    final private String[] STATUSES = {
            StateEngineValues.ACTIVE, StateEngineValues.INPATHCALCULATION,
            StateEngineValues.INSETUP, StateEngineValues.INTEARDOWN, 
            StateEngineValues.INMODIFY, StateEngineValues.INCOMMIT, 
            StateEngineValues.COMMITTED, StateEngineValues.MODCOMMITTED,
            StateEngineValues.RESERVED
            };
    
    public VlanPCE(String rmUrl, String rmWsdl, String localDomain, String vlanScope) throws OSCARSServiceException, MalformedURLException{
        this.rmUrl = rmUrl;
        this.rmWsdl = rmWsdl;
        if(this.rmWsdl == null){
            this.rmWsdl = rmUrl+"?wsdl";
        }
        this.rmClient = null;
        this.localDomain = localDomain;
        this.vlanScope = vlanScope;
        if(this.vlanScope == null){
            this.vlanScope = SCOPE_PORT;
        }
        this.mpls = false;
    }
    
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException, MalformedURLException{
        return this.calculatePath(query, null);
    }
    
    public PCEDataContent calculatePath(PCEMessage query, OSCARSNetLogger netLogger) throws OSCARSServiceException, MalformedURLException{
        synchronized(this){
            if(this.rmClient == null){
                this.rmClient = RMClient.getClient(new URL(this.rmUrl), new URL(this.rmWsdl));
            }
        }
        
        PCEDataContent pceData = query.getPCEDataContent();
        CtrlPlaneTopologyContent inputTopo = pceData.getTopology();
        if(inputTopo == null){
            throw new OSCARSServiceException("No topology graph provided");
        }
        
        //skip if layer 3 reservation
        //assumes can't mix layer2 and layer 3 reservation
        if(this.isLayer3Reservation(pceData, netLogger)){
            return pceData;
        }
        
        //get path
        CtrlPlanePathContent path = null;
        if(pceData.getReservedConstraint() != null && 
                pceData.getReservedConstraint().getPathInfo() != null){
            path = pceData.getReservedConstraint().getPathInfo().getPath();
        }else if(pceData.getUserRequestConstraint() != null && 
                pceData.getUserRequestConstraint().getPathInfo() != null){
            path = pceData.getUserRequestConstraint().getPathInfo().getPath();
        }else{
            throw new OSCARSServiceException("Received a request with no " +
                    "reservedConstraint or userRequestConstraint containing " +
                    "a PathInfo element");
        }
        
        //make sure path is not null
        if(path == null){
            throw new OSCARSServiceException("Received a null path in request");
        }
        
        //query rm
        ListRequest listReq = new ListRequest();
        listReq.setMessageProperties(query.getMessageProperties());
        listReq.setStartTime(pceData.getUserRequestConstraint().getStartTime());
        listReq.setEndTime(pceData.getUserRequestConstraint().getEndTime());
        for(String status : STATUSES){
            listReq.getResStatus().add(status);
        }
        ListReply listResponse = null;
        try {
            this.log.debug(netLogger.start("listResvs", null, this.rmUrl));
            AuthConditions authConds = new AuthConditions();
            AuthConditionType internalHopCond = new AuthConditionType();
            //TODO: Make these constants
            internalHopCond.setName("internalHopsAllowed");
            internalHopCond.getConditionValue().add("true");
            authConds.getAuthCondition().add(internalHopCond);
            Object request [] = {authConds,listReq};
            Object response[] = this.rmClient.invoke("listReservations",request);
            HashMap<String, String> netLogParams = new HashMap<String, String>();
            listResponse = (ListReply) response[0];
            netLogParams.put("resvCount", listResponse.getResDetails().size()+"");
            this.log.debug(netLogger.end("listResvs", null, this.rmUrl, netLogParams));
        } catch (Exception e) {
            this.log.debug(netLogger.error("listResvs", ErrSev.MAJOR, e.getMessage(), this.rmUrl));
            throw new OSCARSServiceException(e.getMessage());
        }
        
        HashMap<String, CtrlPlaneHopContent> reqElemMap = new HashMap<String, CtrlPlaneHopContent>();
        HashMap<String, VlanRange> vlanMap = new HashMap<String, VlanRange>();
        //build map with constraints
        for(CtrlPlaneHopContent hop : path.getHop()){
            if(!this.isVlanHop(hop)){
                continue;
            }
            String hopElemId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop));
            reqElemMap.put(hopElemId, hop);
            String vlanRange = hop.getLink().getSwitchingCapabilityDescriptors()
                                .getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
            
            if(vlanRange != null){
                String urnKey = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, this.urnType()));
                vlanMap.put(urnKey, new VlanRange(vlanRange));
            }
        }
        
        //build map of VLANs based on scope
        for(ResDetails resv : listResponse.getResDetails()){
            //don't double-count current reservation
            if(resv.getGlobalReservationId().equals(query.getGri())){
                continue;
            }
            //skip reservations with no path
            if(resv.getReservedConstraint() == null || 
                    resv.getReservedConstraint().getPathInfo() == null || 
                    resv.getReservedConstraint().getPathInfo().getPath() == null ||
                    resv.getReservedConstraint().getPathInfo().getPath().getHop() == null){
                continue;
            }
            //get ingress and egress
            CtrlPlaneLinkContent ingressLink = PathTools.getIngressLink(this.localDomain, resv.getReservedConstraint().getPathInfo().getPath());
            CtrlPlaneLinkContent egressLink = PathTools.getEgressLink(this.localDomain, resv.getReservedConstraint().getPathInfo().getPath());
            
            //if no ingress or egress then skip path
            if(ingressLink == null || egressLink == null){
                continue;
            }
            
            for(CtrlPlaneHopContent hop : resv.getReservedConstraint().getPathInfo().getPath().getHop()){
                //skip if not a link or not an ethernet link
                if(!this.isVlanHop(hop)){
                    continue;
                }
                CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = hop.getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo();
                try{
                    //Check if hop in local domain
                    String domainId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.DOMAIN_TYPE));
                    if(!this.localDomain.equals(domainId)){
                        continue;
                    }

                    ////if mpls then only check if ingress or egress
                    String hopLinkId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop));
                    if(this.mpls && 
                            !hopLinkId.equals(NMWGParserUtil.normalizeURN(ingressLink.getId())) && 
                            !hopLinkId.equals(NMWGParserUtil.normalizeURN(egressLink.getId()))){
                        continue;
                    }
                    
                    //Find the VLAN used by reservation
                    VlanRange usedRange = new VlanRange(swcapInfo.getVlanRangeAvailability());
                    if(StateEngineValues.INPATHCALCULATION.equals(resv.getStatus()) && swcapInfo.getSuggestedVLANRange() != null){
                        usedRange = new VlanRange(swcapInfo.getSuggestedVLANRange());
                    }else if(StateEngineValues.INPATHCALCULATION.equals(resv.getStatus())){
                        //skip INPATHCALCULATION resv if no suggested VLAN being held
                        continue;
                    }
                    
                    //add VLAN range to map
                    String urnKey = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, this.urnType()));
                    if(!vlanMap.containsKey(urnKey)){
                        vlanMap.put(urnKey, new VlanRange(VlanRange.ANY_RANGE));
                    }
                    vlanMap.put(urnKey, VlanRange.subtract(vlanMap.get(urnKey), usedRange));
                }catch(Exception e){
                    //if any funky URNs then skip
                    continue;
                }
            }
        }
        
        //prune VLANs
        String src = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(path.getHop().get(0)));
        String dest = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(path.getHop().get(path.getHop().size() - 1)));
        for(CtrlPlaneDomainContent domain : inputTopo.getDomain()){
            String currDomainId = NMWGParserUtil.normalizeURN(domain.getId());
            ArrayList<CtrlPlaneNodeContent> nodesToRemove = new ArrayList<CtrlPlaneNodeContent>();
            for(CtrlPlaneNodeContent node : domain.getNode()){
                ArrayList<CtrlPlanePortContent> portsToRemove = new ArrayList<CtrlPlanePortContent>();
                for(CtrlPlanePortContent port : node.getPort()){
                    ArrayList<CtrlPlaneLinkContent> linksToRemove = new ArrayList<CtrlPlaneLinkContent>();
                    for(CtrlPlaneLinkContent link : port.getLink()){
                        CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
                        
                        String fullLinkId = NMWGParserUtil.normalizeURN(link.getId());
                        if(!this.localDomain.equals(currDomainId) &&
                                reqElemMap.containsKey(fullLinkId)){
                            this.updateReqInterdomainLink(link, reqElemMap.get(fullLinkId));
                            continue;
                        }else if(!this.localDomain.equals(currDomainId)){
                            //ignore non-local links that are not explicitly constrained
                            continue;
                        }
                        
                        if(!this.isEdge(link, src, dest) && (!this.isVlanLink(link) || this.mpls)){
                            //clear out vlans
                            if(swcap != null && swcap.getSwitchingCapabilitySpecificInfo() != null){
                                swcap.getSwitchingCapabilitySpecificInfo().setVlanRangeAvailability(null);
                                swcap.getSwitchingCapabilitySpecificInfo().setSuggestedVLANRange(null);
                                swcap.getSwitchingCapabilitySpecificInfo().setVlanTranslation(null);
                            }
                            continue;
                        }
                        String urnKey = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(link.getId(), this.urnType()));
                        if(vlanMap.containsKey(urnKey)){
                            VlanRange linkRange = new VlanRange(swcap.getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability());
                            VlanRange newRange = VlanRange.and(linkRange, vlanMap.get(urnKey));
                            String linkId = NMWGParserUtil.normalizeURN(link.getId());
                            if(newRange.isEmpty() && reqElemMap.containsKey(linkId)){
                                throw new OSCARSServiceException("There are no VLANs available on link " + linkId);
                            }else if(newRange.isEmpty()){
                                linksToRemove.add(link);
                            }else{
                                swcap.getSwitchingCapabilitySpecificInfo().setVlanRangeAvailability(newRange.toString());
                            }
                        }
                    }
                    //remove links
                    this.pruneLinks(port.getLink(), linksToRemove, netLogger);
                    String portId = NMWGParserUtil.normalizeURN(port.getId());
                    if(port.getLink().isEmpty() && reqElemMap.containsKey(portId)){
                        throw new OSCARSServiceException("There are no VLANs available on port " + portId);
                    }else if(port.getLink().isEmpty()){
                        portsToRemove.add(port);
                    }
                }
                //remove ports
                this.prunePorts(node.getPort(), portsToRemove, netLogger);
                String nodeId = NMWGParserUtil.normalizeURN(node.getId());
                if(node.getPort().isEmpty() && reqElemMap.containsKey(nodeId)){
                    throw new OSCARSServiceException("There are no VLANs available on node " + nodeId);
                }else if(node.getPort().isEmpty()){
                    nodesToRemove.add(node);
                }
            }
            //remove nodes
            this.pruneNodes(domain.getNode(), nodesToRemove, netLogger);
            if(domain.getNode().isEmpty()){
                throw new OSCARSServiceException("No VLANs in the domain " + 
                        this.localDomain + " are available at the requested time");
            }
        }

        return pceData;
    }
    
    private void updateReqInterdomainLink(CtrlPlaneLinkContent link,
            CtrlPlaneHopContent hop) throws OSCARSServiceException {
        //check if hop has swcap info
        if(hop.getLink() == null || 
                hop.getLink().getSwitchingCapabilityDescriptors() == null ||
                hop.getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo() == null){
            return;
        }
        
        CtrlPlaneSwitchingCapabilitySpecificInfo hopSwcapInfo = hop.getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo();
        
        //make sure link fields are not null - if they are then this is not applicable to leave
        if(link.getSwitchingCapabilityDescriptors() == null || 
               link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo() == null){
            throw new OSCARSServiceException("Switching capability constraints provided for link that doesn't have any capabilities.");
        }
        
        CtrlPlaneSwitchingCapabilitySpecificInfo linkSwcapInfo = link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo();
        
        /* NOTE: If either of the below fields are null we still want 
         * to set them. This is so we don't reset MPLS considerations 
         * of other domains. */
        //update suggested range
        linkSwcapInfo.setSuggestedVLANRange(hopSwcapInfo.getSuggestedVLANRange());
        
        //update constrained range
        linkSwcapInfo.setVlanRangeAvailability(hopSwcapInfo.getVlanRangeAvailability());
        
    }

    private boolean isVlanLink(CtrlPlaneLinkContent link) {
        if(link.getSwitchingCapabilityDescriptors() == null || 
                link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo() == null ||
                link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability() == null){
            return false;
        }
        return true;
    }

    private boolean isLayer3Reservation(PCEDataContent pceData,
            OSCARSNetLogger netLogger) {
        this.log.debug(netLogger.start("isLayer3Reservation"));
        HashMap<String, String> nlParams = new HashMap<String, String>();
        if(pceData.getReservedConstraint() != null && 
                pceData.getReservedConstraint().getPathInfo() != null && 
                pceData.getReservedConstraint().getPathInfo().getLayer3Info() != null){
            nlParams.put("result", "true");
            this.log.debug(netLogger.end("isLayer3Reservation", null, null, nlParams));
            return true;
        }else if(pceData.getUserRequestConstraint() != null && 
                pceData.getUserRequestConstraint().getPathInfo() != null && 
                pceData.getUserRequestConstraint().getPathInfo().getLayer3Info() != null){
            nlParams.put("result", "true");
            this.log.debug(netLogger.end("isLayer3Reservation", null, null, nlParams));
            return true;
        }
        
        nlParams.put("result", "false");
        this.log.debug(netLogger.end("isLayer3Reservation", null, null, nlParams));
        return false;
    }

    private boolean isEdge(CtrlPlaneLinkContent link, String src, String dest) throws OSCARSServiceException {
        String localDomainId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(link.getId(), NMWGParserUtil.DOMAIN_TYPE));
        String remoteDomainId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(link.getRemoteLinkId(), NMWGParserUtil.DOMAIN_TYPE));
        if(!localDomainId.equals(remoteDomainId)){
            return true;
        }
        String linkId = NMWGParserUtil.normalizeURN(link.getId());
        if(linkId.equals(src) || linkId.equals(dest)){
            return true;
        }
        return false;
    }

    private int urnType(){
        int urnType = -1;
        if(SCOPE_DOMAIN.equals(this.vlanScope)){
            urnType = NMWGParserUtil.DOMAIN_TYPE;
        }else if(SCOPE_NODE.equals(this.vlanScope)){
            urnType = NMWGParserUtil.NODE_TYPE;
        }else if(SCOPE_PORT.equals(this.vlanScope)){
            urnType = NMWGParserUtil.PORT_TYPE;
        }else if(SCOPE_LINK.equals(this.vlanScope)){
            urnType = NMWGParserUtil.LINK_TYPE;
        }else{
            throw new RuntimeException("Unrecognized VLAN scope " + this.vlanScope);
        }
        
        return urnType;
    }
    
    private boolean isVlanHop(CtrlPlaneHopContent hop){
        if(hop.getLink() == null || !isVlanLink((hop.getLink()))){
                return false;
        }
        return true;
    }
    
    private void pruneLinks(List<CtrlPlaneLinkContent> list1, List<CtrlPlaneLinkContent> list2, OSCARSNetLogger netLogger){
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        ArrayList<String> ids = new ArrayList<String>();
        for(CtrlPlaneLinkContent elem : list2){
            ids.add(NMWGParserUtil.normalizeURN(elem.getId()));
        }
        netLogParams.put("links", OSCARSNetLogger.serializeList(ids));
        this.prune(list1, list2, netLogger, netLogParams);
    }
    
    private void prunePorts(List<CtrlPlanePortContent> list1, List<CtrlPlanePortContent> list2, OSCARSNetLogger netLogger){
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        ArrayList<String> ids = new ArrayList<String>();
        for(CtrlPlanePortContent elem : list2){
            ids.add(NMWGParserUtil.normalizeURN(elem.getId()));
        }
        netLogParams.put("ports", OSCARSNetLogger.serializeList(ids));
        this.prune(list1, list2, netLogger, netLogParams);
    }
    
    private void pruneNodes(List<CtrlPlaneNodeContent> list1, List<CtrlPlaneNodeContent> list2, OSCARSNetLogger netLogger){
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        ArrayList<String> ids = new ArrayList<String>();
        for(CtrlPlaneNodeContent elem : list2){
            ids.add(NMWGParserUtil.normalizeURN(elem.getId()));
        }
        netLogParams.put("nodes", OSCARSNetLogger.serializeList(ids));
        this.prune(list1, list2, netLogger, netLogParams);
    }
    
    private void prune(List<?> list1, List<?> list2, OSCARSNetLogger netLogger, HashMap<String,String> netLogParams){
        if(list2.isEmpty()){
            return;
        }
        this.log.debug(netLogger.start("prune", null, null, netLogParams));
        list1.removeAll(list2);
        this.log.debug(netLogger.end("prune"));
    }
    
    public void setMpls(boolean mpls){
        this.mpls = mpls;
    }
    
    public boolean getMpls(){
        return this.mpls;
    }
}
