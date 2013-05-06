package net.es.oscars.pss.openflowj.io;

import org.apache.mina.core.session.IoSession;
import org.openflow.protocol.OFFeaturesRequest;
import org.openflow.protocol.OFHello;

/**
 * Handles message when OSCARS is the server
 */
public class OpenFlowServerMessageHandler extends OpenFlowMessageHandler{

    protected void hello(IoSession session, OFHello ofMsg){
        //TODO: check version

        //send hello
        session.write(new OFHello());

        //send feature request
        session.write(new OFFeaturesRequest());
    }
}
