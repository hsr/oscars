package net.es.oscars.pss.openflowj.config;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.openflowj.util.OFPathTools;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;
import net.es.oscars.utils.topology.PathTools;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.openflow.protocol.OFMessage;

/**
 * Abstract class for generating OpenFlow messages to devices that support 'implicit'
 * mode'. Implicit mode means sending only one request to the source node. The source
 * and destination will be the ingress and egress of the domain with no internal path details.
 * It is up to the device to assign these. This is a special case and in general most people will
 * want to use ExplicitOFConfigGen for general OpenFlow configuration.
 *
 */
abstract public class ImplicitOFConfigGen extends OFConfigGen{
    
    /**
     * Method that parses path, determines ingress and egress, and then
     * generates OpenFlow messages
     * 
     * @param action the setup or teardown action to be performed
     * @param nodeId the node to send the openflow messages
     */
    @Override
    public List<OFMessage> getOFConfig(PSSAction action, String nodeId)
            throws PSSException {

        //determine if previous node is same model
        List<OFMessage> results = new ArrayList<OFMessage>();
        ResDetails res = null;
        if(action.getActionType().equals(ActionType.SETUP)){
            res = action.getRequest().getSetupReq().getReservation();
        }else if(action.getActionType().equals(ActionType.TEARDOWN)){
            res = action.getRequest().getTeardownReq().getReservation();
        }
        List<CtrlPlaneHopContent> localHops;
        try {
            localHops = PathTools.getLocalHops(
                    res.getReservedConstraint().getPathInfo().getPath(), 
                    PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
            throw new PSSException(e.getMessage());
        }

        //iterate through local hops. try to handle case if node appears twice in path.
        String nodeModel = ClassFactory.getInstance().getDeviceModelMap().getDeviceModel(nodeId);
        CtrlPlaneHopContent inHop = null;
        String prevNodeModel = null;
        CtrlPlaneHopContent prevHop = null;
        for(CtrlPlaneHopContent hop : localHops){
            //get node id of hop
            String hopNodeId = null;
            try {
                hopNodeId = OFPathTools.extractNodeId(NMWGParserUtil.getURN(hop, NMWGParserUtil.NODE_TYPE));
            } catch (OSCARSServiceException e) {
                e.printStackTrace();
                throw new PSSException(e.getMessage());
            }


            if(inHop == null){
                if(!hopNodeId.equals(nodeId)){
                    prevNodeModel = ClassFactory.getInstance().getDeviceModelMap().getDeviceModel(hopNodeId);
                    prevHop = hop;
                    continue;
                }else if(prevNodeModel == null || !prevNodeModel.equals(nodeModel)){
                    inHop = hop;
                    prevNodeModel = nodeModel;
                    prevHop = hop;
                    continue;
                }else{
                    //previous hop is same model, since implicit we have nothing to do
                    return results;
                }
            }else{
                String currNodeModel = ClassFactory.getInstance().getDeviceModelMap().getDeviceModel(hopNodeId);
                if(!currNodeModel.equals(nodeModel)){
                    if(action.getActionType().equals(ActionType.SETUP)){
                        results.addAll(this.addFlow(inHop, prevHop, res));
                    }else if(action.getActionType().equals(ActionType.TEARDOWN)){
                        results.addAll(this.removeFlow(inHop, prevHop, res));
                    }
                    inHop = null;
                    prevNodeModel = currNodeModel;
                }			
            }
            prevHop = hop;
        }

        //If implicit setup ends at last hop, do it here
        if(inHop != null){
            if(action.getActionType().equals(ActionType.SETUP)){
                results.addAll(this.addFlow(inHop, prevHop, res));
            }else if(action.getActionType().equals(ActionType.TEARDOWN)){
                results.addAll(this.removeFlow(inHop, prevHop, res));
            }
            inHop = null;
        }

        return results;
    }
    
    /**
     * Stub method for generating a message to add a flow to an openflow device.
     * 
     * @param inHop the ingress link on a node
     * @param outHop the egress link on a node
     * @param reservation the reservation details
     * @return a list of OpenFlow messages to be sent to the device
     * @throws PSSException
     */
    abstract public List<OFMessage> addFlow(CtrlPlaneHopContent inHop, CtrlPlaneHopContent outHop, ResDetails reservation) throws PSSException;
    
    /**
     * Stub method for generating a message to remove a flow from an openflow device.
     * 
     * @param inHop the ingress link on a node
     * @param outHop the egress link on a node
     * @param reservation the reservation details
     * @return a list of OpenFlow messages to be sent to the device
     * @throws PSSException
     */
    abstract public List<OFMessage> removeFlow(CtrlPlaneHopContent inHop, CtrlPlaneHopContent outHop, ResDetails reservation) throws PSSException;


}
