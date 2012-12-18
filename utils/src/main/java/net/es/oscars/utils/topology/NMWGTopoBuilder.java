package net.es.oscars.utils.topology;

import java.util.HashMap;

import net.es.oscars.utils.soap.OSCARSServiceException;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

public class NMWGTopoBuilder {
    private HashMap<String, CtrlPlaneDomainContent> domainMap;
    private HashMap<String, CtrlPlaneNodeContent> nodeMap;
    private CtrlPlaneTopologyContent topology;
    
    public NMWGTopoBuilder(){
        this.domainMap = new HashMap<String, CtrlPlaneDomainContent>();
        this.nodeMap = new HashMap<String, CtrlPlaneNodeContent>();
        this.topology = new CtrlPlaneTopologyContent();
    }
    
    public void addLink(CtrlPlaneLinkContent link) throws OSCARSServiceException {
        
        //create the domain or grab if exists
        String domainId = NMWGParserUtil.getURN(link.getId(), NMWGParserUtil.DOMAIN_TYPE);
        CtrlPlaneDomainContent domain = null;
        if(domainMap.containsKey(domainId)){
            domain = domainMap.get(domainId);
        }else{
            domain = new CtrlPlaneDomainContent();
            domain.setId(domainId);
            domainMap.put(domainId, domain);
            topology.getDomain().add(domain);
        }
        
        //create the node or grab if exists
        String nodeId = NMWGParserUtil.getURN(link.getId(), NMWGParserUtil.NODE_TYPE);
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
        port.setId(NMWGParserUtil.getURN(link.getId(), NMWGParserUtil.PORT_TYPE));
        node.getPort().add(port);
        
        port.getLink().add(link);
    }
    
    public CtrlPlaneTopologyContent getTopology(){
        return topology;
    }
}
