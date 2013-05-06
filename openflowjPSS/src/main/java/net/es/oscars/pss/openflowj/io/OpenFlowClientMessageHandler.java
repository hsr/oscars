package net.es.oscars.pss.openflowj.io;

import org.apache.mina.core.session.IoSession;
import org.openflow.protocol.OFFeaturesRequest;
import org.openflow.protocol.OFHello;

/**
 * Class that handles message passing when OSCARS side initiates connection
 *
 */
public class OpenFlowClientMessageHandler extends OpenFlowMessageHandler{

    public void sessionOpened( IoSession session ) throws Exception {
        session.write(new OFHello());
    }

    @Override
    protected void hello(IoSession session, OFHello ofMsg) {
        //send feature request
        session.write(new OFFeaturesRequest());
    }

}
