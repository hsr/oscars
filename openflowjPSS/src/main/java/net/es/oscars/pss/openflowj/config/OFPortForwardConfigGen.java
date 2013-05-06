package net.es.oscars.pss.openflowj.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.NMWGParserUtil;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.U16;

/**
 * Basic implementation that creates and removes a simple OpenFlow port-forward 
 * rule-set to a device. In otehr words tells traffic tha comes in one port, to 
 * go out another. 
 *
 */
public class OFPortForwardConfigGen extends ExplicitOFConfigGen{
    static protected HashMap<String, Integer> portIdMap = null;

    private static final String CONFIG_FILE_PORTS = "config-openflow-ports.yaml";
    
    /**
     * Initializes class by building a map of URNs from the topology to port
     * numbers recognizable by OpenFlow 
     */
    synchronized private static void init() {
        if(portIdMap != null){
            return;
        }

        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        Map config = null;
        try {
            String configFile = cc.getFilePath(CONFIG_FILE_PORTS);
            config = ConfigHelper.getConfiguration(configFile);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        portIdMap = new HashMap<String, Integer>();
        for(Object linkIdObj : config.keySet()){
            String linkId = NMWGParserUtil.normalizeURN((String) linkIdObj);
            int port = Integer.parseInt((String) config.get(linkIdObj));
            portIdMap.put(linkId, port);
        }
    }
    
    /**
     * Constructor
     */
    public OFPortForwardConfigGen(){
        init();
    }
    
    /**
     * Generates messages to add new port-forward flow to tables
     */
    public List<OFMessage> addFlow(CtrlPlaneHopContent inHop, CtrlPlaneHopContent outHop,
            ResDetails reservation) throws PSSException{
        return this.addRemoveFlow(inHop, outHop, reservation, false);
    }
    
    /**
     * Generates messages to remove a port-forward flow from the tables
     */
    public List<OFMessage> removeFlow(CtrlPlaneHopContent inHop, CtrlPlaneHopContent outHop,
            ResDetails reservation) throws PSSException {
        return this.addRemoveFlow(inHop, outHop, reservation, true);
    }

    private List<OFMessage> addRemoveFlow(CtrlPlaneHopContent inHop, CtrlPlaneHopContent outHop,
            ResDetails reservation, boolean remove) throws PSSException {
        //init
        short type = (remove ? OFFlowMod.OFPFC_DELETE_STRICT : OFFlowMod.OFPFC_ADD);
        List<OFMessage> results = new ArrayList<OFMessage>();
        int inPort = this.lookupPortId(inHop);
        int outPort = this.lookupPortId(outHop);

        //build forward
        results.add(this.buildFlowMod(inPort, outPort, type));
        //build reverse
        results.add(this.buildFlowMod(outPort, inPort, type));

        return results;
    }
    
    /**
     * Private method to build the message
     * 
     * @param inPort the ingress port
     * @param outPort the egress port
     * @param type indicates whether this is an 'add' or 'delete'
     * @return
     */
    private OFMessage buildFlowMod(int inPort, int outPort, short type){
        OFFlowMod flowModMsg = new OFFlowMod();
        flowModMsg.setCommand(type);
        flowModMsg.setCookie((new Random()).nextLong());

        //define match on in port
        OFMatch ofMatch = new OFMatch();
        ofMatch.setWildcards(OFMatch.OFPFW_ALL - OFMatch.OFPFW_IN_PORT);
        ofMatch.setInputPort((short) inPort);
        flowModMsg.setMatch(ofMatch);

        //define fixed fields
        flowModMsg.setIdleTimeout((short) 0);
        flowModMsg.setHardTimeout((short) 0);
        flowModMsg.setBufferId(-1);
        flowModMsg.setOutPort(OFPort.OFPP_NONE);

        //define output action
        flowModMsg.setActions(new ArrayList<OFAction>());
        OFActionOutput outAction = new OFActionOutput();
        outAction.setPort((short) outPort);
        flowModMsg.getActions().add(outAction);

        //explicitly set message length
        flowModMsg.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH + OFActionOutput.MINIMUM_LENGTH));

        return flowModMsg;
    }
    
    /**
     * Extract a port ID from the map given a hop object
     * @param hop the hop containing the id of teh port to find
     * @return the port number. throws exception if not found.
     * @throws PSSException
     */
    private int lookupPortId(CtrlPlaneHopContent hop) throws PSSException{
        String portId = null;
        try {
            portId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.PORT_TYPE));
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
            throw new PSSException(e.getMessage());
        }
        if(!portIdMap.containsKey(portId) || portIdMap.get(portId) == null){
            throw new PSSException("Unable to find openflow port ID for link " + portId);
        }

        return portIdMap.get(portId);
    }

}
