package net.es.oscars.pss.openflowj.io.pss;

import org.apache.mina.core.session.IoSession;

import net.es.oscars.pss.openflowj.common.OpenFlowJPSSCore;
import net.es.oscars.pss.openflowj.io.OpenFlowServerMessageHandler;

/**
 * Does PSS specific tasks related to tracking connections initiated by switch
 *
 */
public class PSSOpenFlowServerMessageHandler extends OpenFlowServerMessageHandler{

    public void sessionOpened( IoSession session ) throws Exception {
        OpenFlowJPSSCore.getInstance().addOFConnection(session, null);
    }

    public void sessionClosed( IoSession session ) throws Exception {
        OpenFlowJPSSCore.getInstance().removeOFConnection(session, null);
    }

}
