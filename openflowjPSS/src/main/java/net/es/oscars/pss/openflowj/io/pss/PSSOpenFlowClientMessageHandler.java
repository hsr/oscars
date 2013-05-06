package net.es.oscars.pss.openflowj.io.pss;

import net.es.oscars.pss.openflowj.common.OpenFlowJPSSCore;
import net.es.oscars.pss.openflowj.io.OpenFlowClientMessageHandler;

import org.apache.mina.core.session.IoSession;

/**
 * Does PSS specific tasks related to tracking connections initiated by controller
 *
 */
public class PSSOpenFlowClientMessageHandler extends OpenFlowClientMessageHandler{

    protected String deviceAddress = null;

    public PSSOpenFlowClientMessageHandler(String deviceAddress){
        this.deviceAddress = deviceAddress;
    }

    public void sessionOpened( IoSession session ) throws Exception {
        OpenFlowJPSSCore.getInstance().addOFConnection(session, this.deviceAddress);
        super.sessionOpened(session);
    }

    public void sessionClosed( IoSession session ) throws Exception {
        OpenFlowJPSSCore.getInstance().removeOFConnection(session, this.deviceAddress);
    }
}
