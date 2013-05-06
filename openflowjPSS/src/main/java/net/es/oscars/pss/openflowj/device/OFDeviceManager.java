package net.es.oscars.pss.openflowj.device;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.beans.PSSException;

import org.apache.mina.core.session.IoSession;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

/**
 * Class used to represent an OpenFlow device.
 *
 */
abstract public class OFDeviceManager {
    protected String nodeId;
    protected String nodeAddress;
    protected boolean implicitSetup;
    protected IoSession session;

    public OFDeviceManager(String nodeId, boolean implicitSetup){

    }

    abstract protected void addFlow(CtrlPlaneHopContent inHop, CtrlPlaneHopContent outHop,
            ResDetails reservation) throws PSSException;

    abstract protected void removeFlow(CtrlPlaneHopContent inHop, CtrlPlaneHopContent outHop, 
            ResDetails reservation) throws PSSException;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public boolean isImplicitSetup() {
        return implicitSetup;
    }

    public void setImplicitSetup(boolean implicitSetup) {
        this.implicitSetup = implicitSetup;
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

}
