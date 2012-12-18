package net.es.oscars.pce.bandwidth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
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

public class BandwidthPCE {
    private Logger log = Logger.getLogger(BandwidthPCE.class);
    private String rmWsdl;
    private String rmUrl;
    //private RMPortType rmClient;
    private RMClient rmClient;
    
    final private String[] STATUSES = {
            StateEngineValues.ACTIVE, StateEngineValues.INPATHCALCULATION,
            StateEngineValues.INSETUP, StateEngineValues.INTEARDOWN, 
            StateEngineValues.INMODIFY, StateEngineValues.INCOMMIT, 
            StateEngineValues.COMMITTED, StateEngineValues.MODCOMMITTED,
            StateEngineValues.RESERVED
            };
    final private double MBPS_DENOM = 1000000.0;
    
    
    public BandwidthPCE(String rmUrl, String rmWsdl) throws OSCARSServiceException{
        this.rmClient = null;
        this.rmUrl = rmUrl;
        if(rmWsdl == null){
            this.rmWsdl = rmUrl+"?wsdl";
        }else{
            this.rmWsdl = rmWsdl;
        }
    }
    
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException, MalformedURLException{
        return this.calculatePath(query, null);
    }
    
    public PCEDataContent calculatePath(PCEMessage query, OSCARSNetLogger netLogger) throws OSCARSServiceException, MalformedURLException{
        synchronized(this){
            if(this.rmClient == null){
                //this.rmClient = RMClient.getClient(new URL(this.rmUrl), new URL(this.rmWsdl)).getPortType();
                this.rmClient = RMClient.getClient(new URL(this.rmUrl), new URL(this.rmWsdl));
            }
        }
        PCEDataContent pceData = query.getPCEDataContent();
        CtrlPlaneTopologyContent inputTopo = pceData.getTopology();
        if(inputTopo == null){
            throw new OSCARSServiceException("No topology graph provided");
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
            Object request[] = {authConds,listReq};
            
            //response = this.rmClient.listReservations(authConds, listReq);
            Object response[] = this.rmClient.invoke("listReservations", request);
            listResponse = (ListReply) response[0];
            this.log.debug(netLogger.end("listResvs", null, this.rmUrl));
        } catch (Exception e) {
            this.log.debug(netLogger.error("listResvs", ErrSev.MAJOR, e.getMessage(), this.rmUrl));
            throw new OSCARSServiceException(e.getMessage());
        }
        
        //check if empty
        if(listResponse.getResDetails().isEmpty()){
            return pceData;
        }
        
        //gather all bandwidth
        HashMap<String, Double> portBandwidthMap = new HashMap<String, Double>();
        HashMap<String, Double> linkBandwidthMap = new HashMap<String, Double>();
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
            for(CtrlPlaneHopContent hop : resv.getReservedConstraint().getPathInfo().getPath().getHop()){
                try{
                    String portUrn = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.PORT_TYPE));
                    String linkUrn = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.LINK_TYPE));
                    int bandwidth = resv.getReservedConstraint().getBandwidth();
                    if(!portBandwidthMap.containsKey(portUrn)){
                        portBandwidthMap.put(portUrn, bandwidth + 0.0);
                    }else{
                        portBandwidthMap.put(portUrn, portBandwidthMap.get(portUrn) + bandwidth);
                    }
                    if(!linkBandwidthMap.containsKey(linkUrn)){
                        linkBandwidthMap.put(linkUrn, bandwidth + 0.0);
                    }else{
                        linkBandwidthMap.put(linkUrn, linkBandwidthMap.get(linkUrn) + bandwidth);
                    }
                }catch(Exception e){
                    //if any funky URNs then skip
                    continue;
                }
            }
        }
        
        //get elements in constraints
        HashMap<String, Boolean> reqElemMap = new HashMap<String, Boolean>();
        for(CtrlPlaneHopContent hop : path.getHop()){
            String hopElemId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop));
            reqElemMap.put(hopElemId, true);
            //if a link then also add the port
            if(NMWGParserUtil.getURNType(hopElemId) == NMWGParserUtil.LINK_TYPE){
                reqElemMap.put(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hopElemId, NMWGParserUtil.PORT_TYPE)), true);
            }
        }
        
        //remove oversubscribed elements from topology
        ArrayList<CtrlPlaneDomainContent> domainsToRemove = new ArrayList<CtrlPlaneDomainContent>();
        for(CtrlPlaneDomainContent domain : inputTopo.getDomain()){
            ArrayList<CtrlPlaneNodeContent> nodesToRemove = new ArrayList<CtrlPlaneNodeContent>();
            String domainId = NMWGParserUtil.normalizeURN(domain.getId());
            for(CtrlPlaneNodeContent node : domain.getNode()){
                String nodeId = NMWGParserUtil.normalizeURN(node.getId());
                ArrayList<CtrlPlanePortContent> portsToRemove = new ArrayList<CtrlPlanePortContent>();
                for(CtrlPlanePortContent port : node.getPort()){
                    String portId = NMWGParserUtil.normalizeURN(port.getId());
                    
                    //check minimum bandwidth
                    if(!this.validateMinimum(port.getMinimumReservableCapacity(), 
                            pceData.getUserRequestConstraint().getBandwidth())){
                        
                        portsToRemove.add(port);
                        if(reqElemMap.containsKey(portId)){
                            throw new OSCARSServiceException("Unable to find path because " + 
                                    portId + " has a minium bandwidth of " + 
                                    Double.parseDouble(port.getMinimumReservableCapacity())/MBPS_DENOM + 
                                    " Mbps and " + pceData.getUserRequestConstraint().getBandwidth() + 
                                    " Mbps was requested");
                        }
                        continue;
                    }
                    
                    //check granularity
                    if(!this.validateGranularity(port.getGranularity(),
                            pceData.getUserRequestConstraint().getBandwidth())){
                        portsToRemove.add(port);
                        if(reqElemMap.containsKey(portId)){
                            throw new OSCARSServiceException("Unable to find path because " + 
                                    portId + " must be reserved in increments of " + 
                                    Double.parseDouble(port.getGranularity())/MBPS_DENOM + 
                                    " Mbps and " + pceData.getUserRequestConstraint().getBandwidth() + 
                                    " Mbps was requested");
                        }
                        continue;
                    }
                    
                    //check if port oversubscribed
                    double usedPortBandwidth = pceData.getUserRequestConstraint().getBandwidth();
                    if(portBandwidthMap.containsKey(portId)){
                        usedPortBandwidth += portBandwidthMap.get(portId);
                    }
                    if(!this.validateMaximum(port.getMaximumReservableCapacity(), port.getCapacity(), usedPortBandwidth)){
                        portsToRemove.add(port);
                        if(reqElemMap.containsKey(portId)){
                            throw new OSCARSServiceException("Unable to find path because the maximum bandwidth of " + 
                                    portId + " has been exceeded. " +
                                    this.calculateRemainingBandwidth(port.getMaximumReservableCapacity(), 
                                    port.getCapacity(), (portBandwidthMap.containsKey(portId) ? portBandwidthMap.get(portId) : 0)) + 
                                    " is available and " + pceData.getUserRequestConstraint().getBandwidth() + 
                                    " Mbps was requested");
                        }
                        continue;
                    }
                    
                    //look at links
                    ArrayList<CtrlPlaneLinkContent> linksToRemove = new ArrayList<CtrlPlaneLinkContent>();
                    for(CtrlPlaneLinkContent link : port.getLink()){
                        String linkId = NMWGParserUtil.normalizeURN(link.getId());
                        
                        //check minimum bandwidth
                        if(!this.validateMinimum(link.getMinimumReservableCapacity(), 
                                pceData.getUserRequestConstraint().getBandwidth())){
                            linksToRemove.add(link);
                            if(reqElemMap.containsKey(linkId)){
                                throw new OSCARSServiceException("Unable to find path because " + 
                                        linkId + " has a minium bandwidth of " + 
                                        Double.parseDouble(port.getMinimumReservableCapacity())/MBPS_DENOM + 
                                        " Mbps and " + pceData.getUserRequestConstraint().getBandwidth() + 
                                        " Mbps was requested");
                            }
                            continue;
                        }
                        
                        //check granularity
                        if(!this.validateGranularity(link.getGranularity(),
                                pceData.getUserRequestConstraint().getBandwidth())){
                            linksToRemove.add(link);
                            if(reqElemMap.containsKey(linkId)){
                                throw new OSCARSServiceException("Unable to find path because " + 
                                        linkId + " must be reserved in increments of " + 
                                        Double.parseDouble(port.getGranularity())/MBPS_DENOM + 
                                        " Mbps and " + pceData.getUserRequestConstraint().getBandwidth() + 
                                        " Mbps was requested");
                            }
                            continue;
                        }
                        
                        //If link capcacity is not specified then assume same as port which was already checked
                        if(link.getMaximumReservableCapacity() == null && link.getCapacity() == null){
                            continue;
                        }
                        
                        //check if link oversubscribed
                        double usedLinkBandwidth = pceData.getUserRequestConstraint().getBandwidth();
                        if(linkBandwidthMap.containsKey(linkId)){
                            usedLinkBandwidth += linkBandwidthMap.get(linkId);
                        }
                        if(!this.validateMaximum(link.getMaximumReservableCapacity(), link.getCapacity(), usedLinkBandwidth)){
                            linksToRemove.add(link);
                            if(reqElemMap.containsKey(linkId)){
                                throw new OSCARSServiceException("Unable to find path because the maximum bandwidth of " + 
                                        linkId + " has been exceeded. " +
                                        this.calculateRemainingBandwidth(link.getMaximumReservableCapacity(), 
                                        link.getCapacity(), (linkBandwidthMap.containsKey(linkId) ? linkBandwidthMap.get(linkId) : 0)) + 
                                        " is available and " + pceData.getUserRequestConstraint().getBandwidth() + 
                                        " Mbps was requested");
                            }
                            continue;
                        }
                    }
                    //remove links
                    port.getLink().removeAll(linksToRemove);
                    
                    //remove port if no links left
                    if(port.getLink().isEmpty() && reqElemMap.containsKey(portId)){
                        throw new OSCARSServiceException("Unable to find path because port " + 
                                portId + " has no links available that meet the requested constraints.");
                    }else if(port.getLink().isEmpty()){
                        portsToRemove.add(port);
                    }
                }
                //remove ports
                node.getPort().removeAll(portsToRemove);
                //remove node if no ports left
                if(node.getPort().isEmpty() && reqElemMap.containsKey(nodeId)){
                    throw new OSCARSServiceException("Unable to find path because node " + 
                            nodeId + " has no ports available that meet the requested constraints.");
                }else if(node.getPort().isEmpty()){
                    nodesToRemove.add(node);
                }
            }
            //remove nodes
            domain.getNode().removeAll(nodesToRemove);
            //remove domain if no nodes left
            if(domain.getNode().isEmpty() && reqElemMap.containsKey(domainId)){
                throw new OSCARSServiceException("Unable to find path because domain " + 
                        domainId + " has no nodes available that meet the requested constraints.");
            }else if(domain.getNode().isEmpty()){
                domainsToRemove.add(domain);
            }
        }
        //remove nodes
        inputTopo.getDomain().removeAll(domainsToRemove);
        //remove domain if no nodes left
        if(inputTopo.getDomain().isEmpty()){
            //should not happen - or be extremely rare
            throw new OSCARSServiceException("All domains in path oversubscribed");
        }
        
        return pceData;
    }

    private boolean validateMinimum(String min, int reqBandwidth){
        //assume no minimum if null
        if(min == null){
            return true;
        }
        
        try{
            double minDoub = Double.parseDouble(min)/MBPS_DENOM;
            if(reqBandwidth < minDoub){
                return false;
            }
        }catch(Exception e){}

        return true;
    }
    
    private boolean validateGranularity(String granularity, int reqBandwidth){
        //assume any granularity if null
        if(granularity == null){
            return true;
        }
        
       //convert to bps since modulus operator does bad with small decimals
        double reqBwDouble = reqBandwidth * MBPS_DENOM;
        try{
            double granDoub = Double.parseDouble(granularity);
            if((reqBwDouble % granDoub) != 0){
                return false;
            }
        }catch(Exception e){}
        
        return true;
    }
    
    private boolean validateMaximum(String max1, String max2, double usedBandwidth){
        //if no maximum then remove it. less costly to undersubscribe than oversubscribe,
        if(max1 == null && max2 == null){
            return false;
        }
        
        //figure out which max to use
        double max = -1.0;
        if(max1 != null){
            try{
                max = Double.parseDouble(max1)/MBPS_DENOM;
            }catch(Exception e){
                return false;
            }
        }else if(max2 != null){
            try{
                max = Double.parseDouble(max2)/MBPS_DENOM;;
            }catch(Exception e){
                return false;
            }
        }
        
        //finally compare max
        if(usedBandwidth > max){
            return false;
        }

        return true;
    }
    
    private String calculateRemainingBandwidth(
            String maximumReservableCapacity, String capacity, double usedBandwidth) {
        if(maximumReservableCapacity == null && capacity == null){
            return "An undefined amount";
        }
        
        //figure out which max to use
        double max = -1;
        if(maximumReservableCapacity != null){
            try{
                max = Double.parseDouble(maximumReservableCapacity);
            }catch(Exception e){
                return "An undefined amount";
            }
        }else if(capacity != null){
            try{
                max = Double.parseDouble(capacity);
            }catch(Exception e){
                return "An undefined amount";
            }
        }

        return (max/MBPS_DENOM - usedBandwidth) + " Mbps";
    }
}
