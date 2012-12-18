package net.es.oscars.pce.l3mpls;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
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

import net.es.oscars.api.soap.gen.v06.Layer3Info;
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

/*
TODO:
 X 1. IPV6 Support
 X 2. Test multiple prefixes and normalize whitesspace/commas
 3. WBUI: make sure list displays source and dest correctly
 4. WBUI: make sure query displays source, dest and other parameters correctly
 5. WBUI: make layer 3 clone work
 6. WBUI: Null source displays localhost
*/
public class L3MplsPCE {
    private static Logger LOG = Logger.getLogger(L3MplsPCE.class);
    private RMClient rmClient;
    private String rmUrl;
    private String rmWsdl;
    private String localDomain;

    final private String[] STATUSES = {
            StateEngineValues.ACTIVE, StateEngineValues.INPATHCALCULATION,
            StateEngineValues.INSETUP, StateEngineValues.INTEARDOWN, 
            StateEngineValues.INMODIFY, StateEngineValues.INCOMMIT, 
            StateEngineValues.COMMITTED, StateEngineValues.MODCOMMITTED,
            StateEngineValues.RESERVED
            };
    
    public L3MplsPCE(String rmUrl, String rmWsdl, String localDomain) throws OSCARSServiceException, MalformedURLException{
        this.rmUrl = rmUrl;
        this.rmWsdl = rmWsdl;
        if(this.rmWsdl == null){
            this.rmWsdl = rmUrl+"?wsdl";
        }
        this.rmClient = null;
        this.localDomain = localDomain;
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
        
        //skip if NOT a layer 3 reservation
        Layer3Info reqLayer3Info = this.isLayer3Reservation(pceData, netLogger);
        if(reqLayer3Info == null){
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
        
        //get ingress and egress
        List<CtrlPlaneHopContent> localPath = PathTools.getLocalHops(path, localDomain);
        String reqIngressLinkId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(localPath.get(0), NMWGParserUtil.LINK_TYPE));
        String reqEgressLinkId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(localPath.get(localPath.size() - 1), NMWGParserUtil.LINK_TYPE));
        String reqIngressNodeId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(reqIngressLinkId, NMWGParserUtil.NODE_TYPE));
        String reqEgressNodeId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(reqEgressLinkId, NMWGParserUtil.NODE_TYPE));
        
        //figure out if they are layer 3 links
        for(CtrlPlaneDomainContent domain : inputTopo.getDomain()){
            String currDomainId = NMWGParserUtil.normalizeURN(domain.getId());
            if(!currDomainId.equals(localDomain)){
                continue;
            }
            
            boolean ingFound = false;
            boolean egrFound = false;
            for(CtrlPlaneNodeContent node : domain.getNode()){
                for(CtrlPlanePortContent port : node.getPort()){
                    for(CtrlPlaneLinkContent link : port.getLink()){
                        String currLinkId = NMWGParserUtil.normalizeURN(link.getId());
                        
                        //check if ingress link
                        if(currLinkId.equals(reqIngressLinkId)){
                            ingFound = true;
                            if(this.isVlanLink(link)){
                                throw new OSCARSServiceException("Ingress link " + currLinkId + 
                                        " cannot be used for layer 3 reservation as it used for " +
                                        "provisioning layer 2 VPNS.");
                            }
                        }
                        
                        //check if egress link
                        if(currLinkId.equals(reqEgressLinkId)){
                            egrFound = true;
                            if(this.isVlanLink(link)){
                                throw new OSCARSServiceException("Egress link " + currLinkId + 
                                        " cannot be used for layer 3 reservation as it used for " +
                                        "provisioning layer 2 VPNS.");
                            }
                        }
                        
                        if(ingFound && egrFound){
                            break;
                        }
                    }
                    if(ingFound && egrFound){
                        break;
                    }
                }
                if(ingFound && egrFound){
                    break;
                }
            }
            if(ingFound && egrFound){
                break;
            }
        }
        
        //query rm so we can check for overlapping flowspecs
        ListRequest listReq = new ListRequest();
        listReq.setMessageProperties(query.getMessageProperties());
        listReq.setStartTime(pceData.getUserRequestConstraint().getStartTime());
        listReq.setEndTime(pceData.getUserRequestConstraint().getEndTime());
        for(String status : STATUSES){
            listReq.getResStatus().add(status);
        }
        ListReply listResponse = null;
        try {
            LOG.debug(netLogger.start("listResvs", null, this.rmUrl));
            AuthConditions authConds = new AuthConditions();
            AuthConditionType internalHopCond = new AuthConditionType();
            internalHopCond.setName("internalHopsAllowed");
            internalHopCond.getConditionValue().add("true");
            authConds.getAuthCondition().add(internalHopCond);
            Object request [] = {authConds,listReq};
            Object response[] = this.rmClient.invoke("listReservations",request);
            HashMap<String, String> netLogParams = new HashMap<String, String>();
            listResponse = (ListReply) response[0];
            netLogParams.put("resvCount", listResponse.getResDetails().size()+"");
            LOG.debug(netLogger.end("listResvs", null, this.rmUrl, netLogParams));
        } catch (Exception e) {
            LOG.debug(netLogger.error("listResvs", ErrSev.MAJOR, e.getMessage(), this.rmUrl));
            throw new OSCARSServiceException(e.getMessage());
        }
        
        
        //compare overlapping reservations
        for(ResDetails resv : listResponse.getResDetails()){
            //don't double-count current reservation
            if(resv.getGlobalReservationId().equals(query.getGri())){
                continue;
            }
            //skip reservations with no path or that are non-layer 3
            if(resv.getReservedConstraint() == null || 
                    resv.getReservedConstraint().getPathInfo() == null || 
                    resv.getReservedConstraint().getPathInfo().getPath() == null ||
                    resv.getReservedConstraint().getPathInfo().getPath().getHop() == null ||
                    resv.getReservedConstraint().getPathInfo().getLayer3Info() == null){
                continue;
            }
            //get ingress and egress
            CtrlPlaneLinkContent ingressLink = PathTools.getIngressLink(this.localDomain, resv.getReservedConstraint().getPathInfo().getPath());
            CtrlPlaneLinkContent egressLink = PathTools.getEgressLink(this.localDomain, resv.getReservedConstraint().getPathInfo().getPath());
            
            //if no ingress or egress then skip path
            if(ingressLink == null || egressLink == null){
                continue;
            }
            
            //Check if reservations share an edge port
            String ingressNodeId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(ingressLink.getId(), NMWGParserUtil.NODE_TYPE));
            String egressNodeId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(egressLink.getId(), NMWGParserUtil.NODE_TYPE));
            //System.out.println("ingressNodeId=" + ingressNodeId);
            //System.out.println("egressNodeId=" + egressNodeId);
            //System.out.println("reqIngressNodeId=" + reqIngressNodeId);
            //System.out.println("reqEgressNodeId=" + reqEgressNodeId);
            if(!ingressNodeId.equals(reqIngressNodeId) && 
                    !ingressNodeId.equals(reqEgressNodeId) && 
                    !egressNodeId.equals(reqIngressNodeId) && 
                    !egressNodeId.equals(reqEgressNodeId)){
                //System.out.println("Skipping because no overlapping ingress and egress");
                continue;
            }
            
            //if here, then there is an overlap so compare flowspec
            Layer3Info l3Info = resv.getReservedConstraint().getPathInfo().getLayer3Info();
            
            if(!this.checkIpOverlap(l3Info.getSrcHost(), reqLayer3Info.getSrcHost())){
                //System.out.println("Src IPs do not overlap");
                continue;
            }
            //System.out.println("Src IPs overlap");
            
            if(!this.checkIpOverlap(l3Info.getDestHost(), reqLayer3Info.getDestHost())){
                //System.out.println("Dest IPs do not overlap");
                continue;
            }
            //System.out.println("Dest IPs overlap");
            
            if(!this.checkPortOverlap(l3Info.getSrcIpPort(), reqLayer3Info.getSrcIpPort())){
                //System.out.println("src Ports do not overlap");
                continue;
            }
            //System.out.println("src Ports overlap");
            
            if(!this.checkPortOverlap(l3Info.getDestIpPort(), reqLayer3Info.getDestIpPort())){
                //System.out.println("dest Ports do not overlap");
                continue;
            }
            //System.out.println("dest Ports overlap");
            
            if(!this.checkStringOverlap(l3Info.getProtocol(), reqLayer3Info.getProtocol())){
                //System.out.println(" proto does not overlap");
                continue;
            }
            //System.out.println("proto overlap");
            
            if(!this.checkStringOverlap(l3Info.getDscp(), reqLayer3Info.getDscp())){
                //System.out.println("dscp does not overlap");
                continue;
            }
            //System.out.println("dscp overlap");
            
            throw new OSCARSServiceException("Layer 3 flowspec conflicts with reservation " + resv.getGlobalReservationId());
        }

        return pceData;
    }
    private boolean checkIpOverlap(String ips1, String ips2) throws OSCARSServiceException {
        //assume port being null means all ports
        if(ips1 == null || ips2 == null){
            return true;
        }
        
        //normailize
        ips1 = ips1.trim();
        ips1 = ips1.replaceAll("\\s+", ",");
        ips2 = ips2.trim();
        ips2 = ips2.replaceAll("\\s+", ",");
        //split-up list
        String[] ip1List = ips1.split(",");
        String[] ip2List = ips2.split(",");
        
        //convert to IP
        for(String ip1String : ip1List){
            //System.out.println("ip1String=" + ip1String);
            MaskedIP ip1 = new MaskedIP(ip1String);
            //System.out.println();
            
            for(String ip2String : ip2List){
                //System.out.println("ip2String=" + ip2String);
                MaskedIP ip2 = new MaskedIP(ip2String);
                if(ip1.prefixLength < ip2.prefixLength){
                    //ip1 is more general
                    //System.out.println("ip1 is more general");
                    //if((ip1.mask & ip2.address) == ip1.address){
                    if(ip1.compareMasked(ip2.address)){
                        return true;
                    }
                }else if(ip1.prefixLength > ip2.prefixLength){
                    //System.out.println("ip2 is more general");
                    //if((ip2.mask & ip1.address) == ip2.address){
                    if(ip2.compareMasked(ip1.address)){
                        return true;
                    }
                }else{
                    //they are equals
                    //System.out.println("lengths are equal");
                    //if(ip1.address == ip2.address){
                    if(ip1.equals(ip2.address)){
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private boolean checkStringOverlap(String str1, String str2) {
        //assume port being null means all ports
        if(str1 == null || str2 == null){
            return true;
        }
        
        //compare values
        if(str1.toLowerCase().equals(str2)){
            return true;
        }
        
        return false;
    }

    private boolean checkPortOverlap(Integer port1, Integer port2) {
        //assume port being null means all ports
        if(port1 == null || port2 == null){
            return true;
        }
        
        //compare values
        if(port1.equals(port2)){
            return true;
        }
        
        return false;
    }

    private Layer3Info isLayer3Reservation(PCEDataContent pceData,
            OSCARSNetLogger netLogger) {
        LOG.debug(netLogger.start("isLayer3Reservation"));
        HashMap<String, String> nlParams = new HashMap<String, String>();
        if(pceData.getReservedConstraint() != null && 
                pceData.getReservedConstraint().getPathInfo() != null && 
                pceData.getReservedConstraint().getPathInfo().getLayer3Info() != null){
            nlParams.put("result", "true");
            LOG.debug(netLogger.end("isLayer3Reservation", null, null, nlParams));
            return pceData.getReservedConstraint().getPathInfo().getLayer3Info();
        }else if(pceData.getUserRequestConstraint() != null && 
                pceData.getUserRequestConstraint().getPathInfo() != null && 
                pceData.getUserRequestConstraint().getPathInfo().getLayer3Info() != null){
            nlParams.put("result", "true");
            LOG.debug(netLogger.end("isLayer3Reservation", null, null, nlParams));
            return pceData.getUserRequestConstraint().getPathInfo().getLayer3Info();
        }
        
        nlParams.put("result", "false");
        LOG.debug(netLogger.end("isLayer3Reservation", null, null, nlParams));
        return null;
    }
    
    private boolean isVlanLink(CtrlPlaneLinkContent link) {
        if(link.getSwitchingCapabilityDescriptors() == null || 
                link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo() == null ||
                link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability() == null){
            return false;
        }
        return true;
    }
    
    private class MaskedIP {
        private byte[] address;
        private byte[] mask;
        private int prefixLength;
        
        public MaskedIP(String ipString) throws OSCARSServiceException{
            //split-up string
            ipString = ipString.trim();
            String[] ipParts = ipString.split("/");
            if(ipParts.length < 1 || ipParts.length > 2){
                throw new OSCARSServiceException(ipString + " must be an [IP] or [IP]/[PREFIX-LENGTH]>");
            }
            
            //verify IP portion
            String ipAddr = ipParts[0];
            InetAddress inet = null;
            try {
                inet = InetAddress.getByName(ipAddr);
            } catch (UnknownHostException e) {
                throw new OSCARSServiceException("Invalid IP " + ipAddr + ": " + e.getMessage());
            }
            byte[] byteIp = inet.getAddress();
            
            //verify prefix length
            int ipPrefix = byteIp.length * 8;
            if(ipParts.length == 2){
                try{
                    ipPrefix = Integer.parseInt(ipParts[1]);
                } catch (Exception e) {
                    throw new OSCARSServiceException("Prefix length " + ipParts[1] + 
                            " is not a number");
                }
            }
            this.prefixLength = ipPrefix;
            
            byte[] tmpMask = new byte[byteIp.length];
            for(int b = 0, l = ipPrefix; b < tmpMask.length && l > 0; b++, l-= 8){
                tmpMask[b] = (byte) ((0xFF << (8 - (l >= 8 ? 8 : l))));
            }
            this.mask = tmpMask;
            
            //combine
            this.address = new byte[byteIp.length];
            for(int i = 0; i < byteIp.length; i++){
                this.address[i] = (byte) ((byteIp[i] & 0xFF) & (this.mask[i] & 0xFF));
            }
            
            //System.out.println("ADDRESSBYTES=" + byteIp.length);
            //System.out.println("PREFIXLENGTH=" + this.prefixLength);
            //System.out.println("MASK=" + mask);
            //System.out.println("MASKBITS=");
            //for(int i = 0; i < this.mask.length; i++){
            //    System.out.println("    " + this.mask[i]);
            //}

            //System.out.println("ADDR=" + this.address);
            //System.out.println("ADDR BITS=");
            //for(int i = 0; i < this.address.length; i++){
            //   System.out.println("    " + this.address[i]);
            //}
        }
        
        public boolean compareMasked(byte[] addr2){
            if(this.mask.length != addr2.length){
                return false;
            }
            
            byte[] maskedAddr = new byte[addr2.length];
            for(int i = 0; i < this.mask.length; i++){
                maskedAddr[i] = (byte) ((addr2[i] & 0xFF) & (this.mask[i] & 0xFF));
            }
            
            return this.equals(maskedAddr);
        }
        
        public boolean equals(byte[] addr2){
            if(this.address.length != addr2.length){
                return false;
            }
            
            for(int i = 0; i < this.address.length; i++){
                if(this.address[i] != addr2[i]){
                    return false;
                }
            }
            
            return true;
        }
    }
}
