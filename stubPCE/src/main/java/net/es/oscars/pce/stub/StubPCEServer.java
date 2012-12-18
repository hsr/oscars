package net.es.oscars.pce.stub;

import java.util.HashMap;
import java.util.List;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

import net.es.oscars.api.soap.gen.v06.OptionalConstraintType;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.SimplePCEServer;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;

@OSCARSService (
        serviceName = "StubPCE",
        config = "config.yaml",
        implementor = "net.es.oscars.pce.SimplePCEProtocolHandler"
)
public class StubPCEServer extends SimplePCEServer{
    
    final private int DOMAIN_TYPE = 1;
    final private int NODE_TYPE = 2;
    final private int PORT_TYPE = 3;
    final private int LINK_TYPE = 4;
    final private String DEFAULT_TE_METRIC = "10";
    final private String DEFAULT_ENC_TYPE = "ethernet";
    final private String DEFAULT_SW_TYPE = "l2sc";
    final private String DEFAULT_SUGG_VLAN = "3000";
    final private String DEFAULT_AVAIL_VLAN = "2000-4000";
    
    static private SharedConfig config = null;
    static {
        StubPCEServer.config = new SharedConfig("StubPCE");
        StubPCEServer.config.setLog4j();
    }
    
    public StubPCEServer() throws OSCARSServiceException {
        super();
    }
    
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException {
        PCEDataContent pceData = new PCEDataContent();
        HashMap<String, CtrlPlaneDomainContent> domainMap = new HashMap<String, CtrlPlaneDomainContent>();
        HashMap<String, CtrlPlaneNodeContent> nodeMap = new HashMap<String, CtrlPlaneNodeContent>();
        
        //Pass along constraints given
        pceData.setReservedConstraint(query.getPCEDataContent().getReservedConstraint());
        pceData.setUserRequestConstraint(query.getPCEDataContent().getUserRequestConstraint());
        for(OptionalConstraintType optConstraint : query.getPCEDataContent().getOptionalConstraint()){
            pceData.getOptionalConstraint().add(optConstraint);
        }
        
        //Get the source and destination hops
        List<CtrlPlaneHopContent> hops = query.getPCEDataContent().getReservedConstraint().getPathInfo().getPath().getHop();
        CtrlPlaneHopContent srcHop = hops.get(0);
        CtrlPlaneHopContent destHop = hops.get(hops.size() - 1);
        
        //create some fake intermediate hops
        CtrlPlaneHopContent srcTrunkHop = new CtrlPlaneHopContent();
        srcTrunkHop.setLinkIdRef(NMWGParserUtil.getURN(srcHop, NODE_TYPE) + ":port=trunk:link=*");
        CtrlPlaneHopContent destTrunkHop = new CtrlPlaneHopContent();
        destTrunkHop.setLinkIdRef(NMWGParserUtil.getURN(destHop, NODE_TYPE) + ":port=trunk:link=*");
        
        //now build the topology
        CtrlPlaneTopologyContent topo = new CtrlPlaneTopologyContent();
        this.addTopoElem(srcHop, "urn:ogf:network:domain=*:node=*:port=*:link=*", domainMap, nodeMap, topo);
        this.addTopoElem(srcTrunkHop, destTrunkHop.getLinkIdRef(), domainMap, nodeMap, topo);
        this.addTopoElem(destTrunkHop, srcTrunkHop.getLinkIdRef(), domainMap, nodeMap, topo);
        this.addTopoElem(destHop, "urn:ogf:network:domain=*:node=*:port=*:link=*", domainMap,nodeMap, topo);
        pceData.setTopology(topo);
        
        return pceData;
    }
    
    public PCEDataContent commitPath(PCEMessage msg) throws OSCARSServiceException{
        PCEDataContent pceData = new PCEDataContent();
        
        //Pass along constraints given
        pceData.setReservedConstraint(msg.getPCEDataContent().getReservedConstraint());
        pceData.setUserRequestConstraint(msg.getPCEDataContent().getUserRequestConstraint());
        for(OptionalConstraintType optConstraint : msg.getPCEDataContent().getOptionalConstraint()){
            pceData.getOptionalConstraint().add(optConstraint);
        }
        
        //choose vlans
        CtrlPlaneTopologyContent topo = msg.getPCEDataContent().getTopology();
        for(CtrlPlaneDomainContent domain : topo.getDomain()){
            for(CtrlPlaneNodeContent node : domain.getNode()){
                for(CtrlPlanePortContent port : node.getPort()){
                    for(CtrlPlaneLinkContent link : port.getLink()){
                        CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo =
                            link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo();
                        swcapInfo.setSuggestedVLANRange(null);
                        swcapInfo.setVlanRangeAvailability(DEFAULT_SUGG_VLAN);
                    }
                }
            }
        }
        pceData.setTopology(topo);
        
        return pceData;
    }
    
    private void addTopoElem(CtrlPlaneHopContent hop, String remoteLinkId,
            HashMap<String, CtrlPlaneDomainContent> domainMap,
            HashMap<String, CtrlPlaneNodeContent> nodeMap,
            CtrlPlaneTopologyContent topo) throws OSCARSServiceException {
        
        //create the domain or grab if exists
        String domainId = NMWGParserUtil.getURN(hop, DOMAIN_TYPE);
        CtrlPlaneDomainContent domain = null;
        if(domainMap.containsKey(domainId)){
            domain = domainMap.get(domainId);
        }else{
            domain = new CtrlPlaneDomainContent();
            domain.setId(domainId);
            domainMap.put(domainId, domain);
            topo.getDomain().add(domain);
        }
        
        //create the node or grab if exists
        String nodeId = NMWGParserUtil.getURN(hop, NODE_TYPE);
        CtrlPlaneNodeContent node = null;
        if(nodeMap.containsKey(nodeId)){
            node = nodeMap.get(nodeId);
        }else{
            node = new CtrlPlaneNodeContent();
            node.setId(nodeId);
            nodeMap.put(nodeId, node);
            domain.getNode().add(node);
        }
        
        
        //create the port
        CtrlPlanePortContent port = new CtrlPlanePortContent();
        port.setId(NMWGParserUtil.getURN(hop, PORT_TYPE));
        node.getPort().add(port);
        
        //create link. just ignore the link params if there are any
        CtrlPlaneLinkContent link = new CtrlPlaneLinkContent();
        link.setId(NMWGParserUtil.getURN(hop, LINK_TYPE));
        //parameter required by schema that doesn't make sense in this context
        link.setTrafficEngineeringMetric(DEFAULT_TE_METRIC);
        CtrlPlaneSwcapContent swcap = new CtrlPlaneSwcapContent();
        swcap.setEncodingType(DEFAULT_ENC_TYPE);
        swcap.setSwitchingcapType(DEFAULT_SW_TYPE);
        CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
        swcapInfo.setSuggestedVLANRange(DEFAULT_SUGG_VLAN);
        swcapInfo.setVlanRangeAvailability(DEFAULT_AVAIL_VLAN);
        swcap.setSwitchingCapabilitySpecificInfo(swcapInfo);
        link.setSwitchingCapabilityDescriptors(swcap);
        link.setRemoteLinkId(remoteLinkId);
        port.getLink().add(link);
    }
    
    public static void main(String[] args){
        try {
            // Set SSL keystores
            //OSCARSSoapService.setSSLBusConfiguration((
            //      new URL(("file:" + config.getFilePath("server-cxf.xml")))));
            StubPCEServer pceServer = new StubPCEServer();
            pceServer.startServer(false);
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        } //catch (MalformedURLException e) {
          //  e.printStackTrace();
          //}
        
    }
}
