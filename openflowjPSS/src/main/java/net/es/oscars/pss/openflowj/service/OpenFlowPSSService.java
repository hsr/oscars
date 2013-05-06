package net.es.oscars.pss.openflowj.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.openflow.protocol.OFMessage;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.api.CircuitService;
import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.openflowj.common.OpenFlowJPSSCore;
import net.es.oscars.pss.openflowj.config.OFConfigGen;
import net.es.oscars.pss.openflowj.connect.OpenFlowPSSCommand;
import net.es.oscars.pss.openflowj.io.OpenFlowListener;
import net.es.oscars.pss.openflowj.util.OFPathTools;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.pss.util.ConnectorUtils;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;
import net.es.oscars.utils.topology.PathTools;

public class OpenFlowPSSService implements CircuitService {
    private String circuitServiceId;

    final private static String PARAM_CTRL_PORT = "controllerPort";
    final private static String PARAM_ECHO_INT = "openFlowEchoInterval";
    public void setConfig(CircuitServiceConfig config) throws PSSException {

        //determine service ID
        this.circuitServiceId = config.getId();

        //start listener
        int ofListenerPort = OpenFlowListener.DEFAULT_PORT;
        if(config.getParams() != null && 
                config.getParams().containsKey(PARAM_CTRL_PORT) && 
                config.getParams().get(PARAM_CTRL_PORT) != null){
            ofListenerPort = Integer.parseInt(config.getParams().get(PARAM_CTRL_PORT));
        }
        OpenFlowJPSSCore.getInstance().startListener(ofListenerPort);

        //start echo manager
        if(config.getParams() != null && 
                config.getParams().containsKey(PARAM_ECHO_INT) && 
                config.getParams().get(PARAM_ECHO_INT) != null){
            OpenFlowJPSSCore.getInstance().setOfEchoInterval(
                    Integer.parseInt(config.getParams().get(PARAM_ECHO_INT)) * 1000L);
        }
        OpenFlowJPSSCore.getInstance().startEchoThread();
    }

    public List<PSSAction> setup(List<PSSAction> actions) throws PSSException {
        return this.processActions(actions);
    }

    public List<PSSAction> teardown(List<PSSAction> actions)
            throws PSSException {
        return this.processActions(actions);
    }

    public List<PSSAction> status(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            //finalize PSS action
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }

        return results;
    }

    public List<PSSAction> modify(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            //finalize PSS action
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }

        return results;
    }

    private List<PSSAction> processActions(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        String localDomainId = PathTools.getLocalDomainId();
        if(localDomainId == null){
            throw new PSSException("Unable to determine local domain ID. Please verify it is configured.");
        }

        for (PSSAction action : actions) {
            ResDetails res = null;
            if(action.getActionType().equals(ActionType.SETUP)){
                res = action.getRequest().getSetupReq().getReservation();
            }else if(action.getActionType().equals(ActionType.TEARDOWN)){
                res = action.getRequest().getTeardownReq().getReservation();
            }

            if(res == null || res.getReservedConstraint() == null || 
                    res.getReservedConstraint().getPathInfo() == null || 
                    res.getReservedConstraint().getPathInfo().getPath() == null){
                throw new PSSException("Reservation does not contain a path in the reservedConstraint");
            }

            //determine list of nodes that need to be contacted
            List<String> nodeList = null;
            try {
                nodeList = this.buildNodeList(res, localDomainId);
            } catch (OSCARSServiceException e) {
                throw new PSSException("Unable to build node list");
            }	

            //Iterate through nodes to be contacted
            for(String nodeId : nodeList){
                //Get address, connector and config generator
                String nodeAddr = ConnectorUtils.getDeviceAddress(nodeId);
                Connector conn = ClassFactory.getInstance().getDeviceConnectorMap().getDeviceConnector(nodeId);
                OFConfigGen configGen = null;
                try{ 
                    configGen = (OFConfigGen)ConnectorUtils.getDeviceConfigGenerator(nodeId, this.circuitServiceId);
                }catch(Exception e){
                    e.printStackTrace();
                    throw new PSSException("ConfigGenerator must be of type OFConfigGen");
                }

                //send all the messages
                List<OFMessage> ofMsgs = configGen.getOFConfig(action, nodeId);
                for(OFMessage ofMsg : ofMsgs){
                    //send message
                    OpenFlowPSSCommand cmd = new OpenFlowPSSCommand();
                    cmd.setTransactionId(ofMsg.getXid()+"");
                    cmd.setDeviceAddress(nodeAddr);
                    cmd.setOpenFlowMessage(ofMsg);
                    conn.sendCommand(cmd);
                }
            }

            //finalize PSS action
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }

        return results;
    }

    private List<String> buildNodeList(ResDetails res, String localDomainId) throws PSSException, OSCARSServiceException{
        CtrlPlanePathContent path = res.getReservedConstraint().getPathInfo().getPath();
        List<String> nodeList = new ArrayList<String>();
        List<CtrlPlaneHopContent> localHops = PathTools.getLocalHops(path, localDomainId);
        if(localHops == null || localHops.size() == 0){
            throw new PSSException("Unable to find local path");
        }
        HashMap<String,Boolean> nodeTracker = new HashMap<String,Boolean>();
        for(CtrlPlaneHopContent hop : localHops){
            String nodeId = OFPathTools.extractNodeId(NMWGParserUtil.getURN(hop, NMWGParserUtil.NODE_TYPE));
            if(nodeTracker.containsKey(nodeId)){
                continue;
            }
            nodeList.add(nodeId);
            nodeTracker.put(nodeId, true);
        }

        return nodeList;
    }

}
