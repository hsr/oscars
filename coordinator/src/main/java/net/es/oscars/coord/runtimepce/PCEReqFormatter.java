package net.es.oscars.coord.runtimepce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.VlanTag;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

public class PCEReqFormatter {
    public static PCEReqFormatter instance = null;
    final private String L2_ENC_TYPE = "ethernet";
    final private String L2_SWCAP_TYPE = "l2sc";
    final private String UNTAGGED = "0";
    
    public static PCEReqFormatter getInstance(){
        if(instance == null){
            instance = new PCEReqFormatter();
        }
        return instance;
    }
    
    public void normalize(PCEData pceData){
        if(pceData.getReservedConstraint() != null){
            return;
        }
        pceData.setReservedConstraint(this.resvFromUser(pceData.getUserRequestConstraint()));
    }
    
    /**
     * Converts topology to a path. It will only return the correct path if the topology represents a 
     * point-to-point line. It will likely return an error if the path contains any loops.
     * 
     * @param topo the topology to convert
     * @param pathConstraint the path from a constraint. used to identify the endpoints.
     * @return the topology converted to a path
     */
    public CtrlPlanePathContent topoToPath(CtrlPlaneTopologyContent topo, CtrlPlanePathContent pathConstraint){
        CtrlPlanePathContent path = new CtrlPlanePathContent();
 
        if ((pathConstraint == null) ||
            (pathConstraint.getHop() == null) ||
            (pathConstraint.getHop().size() == 0)) {
            // Provided pathConstraint is empty. Return it unchanged.
            return pathConstraint;
        }
        //build maps
        HashMap<String, List<CtrlPlaneLinkContent>> domainMap = new HashMap<String, List<CtrlPlaneLinkContent>>();
        HashMap<String, CtrlPlaneNodeContent> nodeMap = new HashMap<String, CtrlPlaneNodeContent>();
        HashMap<String, CtrlPlaneLinkContent> linkMap = new HashMap<String, CtrlPlaneLinkContent>();
        for(CtrlPlaneDomainContent domain : topo.getDomain()){
            List<CtrlPlaneLinkContent> domainLinks = new ArrayList<CtrlPlaneLinkContent>();
            domainMap.put(NMWGParserUtil.normalizeURN(domain.getId()), domainLinks);
            for(CtrlPlaneNodeContent node : domain.getNode()){
                nodeMap.put(NMWGParserUtil.normalizeURN(node.getId()), node);
                for(CtrlPlanePortContent port : node.getPort()){
                    for(CtrlPlaneLinkContent link : port.getLink()){
                        linkMap.put(NMWGParserUtil.normalizeURN(link.getId()), link);
                        domainLinks.add(link);
                    }
                }
            }
        }
        
        //walk through topology
        String currLinkId = NMWGParserUtil.normalizeURN(
                NMWGParserUtil.getURN(pathConstraint.getHop().get(0)));
        String destLinkId = NMWGParserUtil.normalizeURN(
                NMWGParserUtil.getURN(pathConstraint.getHop().get(
                        pathConstraint.getHop().size() -1)));
        while(currLinkId != null){
            CtrlPlaneLinkContent currLink = linkMap.get(currLinkId);
            linkMap.remove(currLinkId);
            path.getHop().add(this.createHop(currLink));
            String remoteLinkId = NMWGParserUtil.normalizeURN(currLink.getRemoteLinkId());
            String nodeId = null;
            String domainId = null;
            try {
                nodeId = NMWGParserUtil.normalizeURN(
                        NMWGParserUtil.getURN(currLinkId, NMWGParserUtil.NODE_TYPE));
                domainId = NMWGParserUtil.normalizeURN(
                        NMWGParserUtil.getURN(currLinkId, NMWGParserUtil.DOMAIN_TYPE));
            } catch (OSCARSServiceException e) {
                throw new RuntimeException(e);
            }
            
            if(linkMap.containsKey(remoteLinkId)){
                currLinkId = remoteLinkId;
            }else if(nodeMap.containsKey(nodeId)){
                currLinkId = null;
                for(CtrlPlanePortContent port : nodeMap.get(nodeId).getPort()){
                    String tmpLinkId = NMWGParserUtil.normalizeURN(port.getLink().get(0).getId());
                    if(linkMap.containsKey(tmpLinkId)){
                        currLinkId = tmpLinkId;
                        break;
                    }
                }
            }
            
            //treat the domain like a node if domain only has two hops and no other options
            if(currLinkId == null && domainMap.containsKey(domainId) && domainMap.get(domainId).size() == 2){
                for(CtrlPlaneLinkContent link : domainMap.get(domainId)){
                    String tmpLinkId = NMWGParserUtil.normalizeURN(link.getId());
                    if(linkMap.containsKey(tmpLinkId)){
                        currLinkId = tmpLinkId;
                        break;
                    }
                }
            }
        }
        
        //make sure destination is last hop
        if(!NMWGParserUtil.normalizeURN(path.getHop().get(path.getHop().size() - 1).
                getLink().getId()).equals(destLinkId)){
            throw new RuntimeException("Error converting topology to path.");
        }
        
        return path;
    }
    
    private ReservedConstraintType resvFromUser(UserRequestConstraintType userConstraint){
        ReservedConstraintType resConstraint = new ReservedConstraintType();
        
        //sanity check request
        if(userConstraint.getPathInfo() == null){
            //should not happen
            throw new RuntimeException("User constraint does not contain a PathInfo element");
        }else if(userConstraint.getPathInfo().getLayer2Info() != null && 
                userConstraint.getPathInfo().getLayer3Info() != null){
            //not supported
            throw new RuntimeException("Specifying both Layer2Info " +
                    "and Layer3Info is not currently supported");
        }else if(userConstraint.getPathInfo().getLayer2Info() != null && 
                userConstraint.getPathInfo().getMplsInfo() != null){
            //not supported
            throw new RuntimeException("Specifying both Layer2Info " +
                    "and MplsInfo is not currently supported");
        }else if(userConstraint.getPathInfo().getLayer2Info() == null &&
                userConstraint.getPathInfo().getLayer3Info() == null &&
                userConstraint.getPathInfo().getPath() == null){
            throw new RuntimeException("Must specify layer2Info, layer3Info or a path");
        }
        
        //copy over user constraints
        resConstraint.setBandwidth(userConstraint.getBandwidth());
        resConstraint.setStartTime(userConstraint.getStartTime());
        resConstraint.setEndTime(userConstraint.getEndTime());
        resConstraint.setPathInfo(new PathInfo());
        
        //build new PathInfo
        resConstraint.getPathInfo().setLayer2Info(userConstraint.getPathInfo().getLayer2Info());
        resConstraint.getPathInfo().setLayer3Info(userConstraint.getPathInfo().getLayer3Info());
        resConstraint.getPathInfo().setMplsInfo(userConstraint.getPathInfo().getMplsInfo());
        resConstraint.getPathInfo().setPathSetupMode(userConstraint.getPathInfo().getPathSetupMode());
        resConstraint.getPathInfo().setPathType(userConstraint.getPathInfo().getPathType());
        if(userConstraint.getPathInfo().getPath() != null && userConstraint.getPathInfo().getPath().getHop().size() >= 2){
            //TODO: Check path for non-URNs
            resConstraint.getPathInfo().setPath(userConstraint.getPathInfo().getPath());
        }else if(userConstraint.getPathInfo().getLayer2Info() != null){
            CtrlPlanePathContent path = new CtrlPlanePathContent();
            path.setId("path-1");
            path.getHop().add(this.createHop(
                    userConstraint.getPathInfo().getLayer2Info().getSrcEndpoint(), 
                    userConstraint.getPathInfo().getLayer2Info().getSrcVtag()));
            path.getHop().add(this.createHop(
                    userConstraint.getPathInfo().getLayer2Info().getDestEndpoint(), 
                    userConstraint.getPathInfo().getLayer2Info().getDestVtag()));
            resConstraint.getPathInfo().setPath(path);
        }else if(userConstraint.getPathInfo().getLayer3Info() != null){
            //TODO: Map IPs/hostname to ingress/egress URNs
            throw new RuntimeException("Can't currently normalize layer3Info.");
        }

        return resConstraint;
    }
    
    private CtrlPlaneHopContent createHop(String id, VlanTag vlanTag){
        CtrlPlaneLinkContent link = new CtrlPlaneLinkContent();
        CtrlPlaneSwcapContent swcap = new CtrlPlaneSwcapContent();
        CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = 
            new CtrlPlaneSwitchingCapabilitySpecificInfo();
        
        //TODO: lookup non-URNs in lookup service
        
        //build hop
        link.setId(id);
        swcap.setSwitchingcapType(L2_SWCAP_TYPE);
        swcap.setEncodingType(L2_ENC_TYPE);
        if(vlanTag != null){
            if(vlanTag.isTagged()){
                swcapInfo.setVlanRangeAvailability(vlanTag.getValue());
            }else{
                swcapInfo.setVlanRangeAvailability(UNTAGGED);
            }
        }
        swcap.setSwitchingCapabilitySpecificInfo(swcapInfo);
        link.setSwitchingCapabilityDescriptors(swcap);
        
        return this.createHop(link);
    }
    
    private CtrlPlaneHopContent createHop(CtrlPlaneLinkContent link){
        CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
        
        //build hop
        hop.setId(UUID.randomUUID().toString());
        hop.setLink(link);
        
        return hop;
    }
}
