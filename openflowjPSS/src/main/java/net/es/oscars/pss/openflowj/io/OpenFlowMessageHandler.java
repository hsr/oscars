package net.es.oscars.pss.openflowj.io;


import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.openflow.protocol.OFEchoReply;
import org.openflow.protocol.OFEchoRequest;
import org.openflow.protocol.OFError;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFeaturesRequest;
import org.openflow.protocol.OFHello;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.factory.MessageParseException;

/**
 * Base OpenFlow message handler which processes the symmetric 
 * asynschronous messages and replies from controller-to-switch messages
 *of an openflow switch. These include:
 * 
 *  Symmetric Messages:
 *  	OFPT_HELLO
 *  	OFPT_ECHO_REQUEST
 *  	OFPT_ECHO_REPLY
 *  	OFPT_VENDOR
 *  
 *  Asynchronous Messages:
 *  	OFPT_PACKET_IN
 *  	OFPT_FLOW_REMOVED
 *  	OFPT_PORT_STATUS
 *  
 *  Switch-Controller Replies:
 *  	OFPT_FEATURES_REPLY
 *  	OFPT_GET_CONFIG_REPLY
 *  	OFPT_STATS_REPLY
 *  	OFPT_BARRIER_REPLY
 *  	OFPT_QUEUE_GET_CONFIG_REPLY
 *  
 * @author alake
 *
 */
abstract public class OpenFlowMessageHandler extends IoHandlerAdapter {

    public final static String OF_SESSION_ESTABLISHED = "OFSessionEstablished";

    public void messageReceived( IoSession session, Object message ) throws Exception {
        OFMessage ofMsg = (OFMessage) message;

        //route message to appropriate method
        if(ofMsg.getType().equals(OFType.HELLO)){
            this.hello(session, (OFHello)ofMsg);
        }else if(ofMsg.getType().equals(OFType.ECHO_REQUEST)){
            this.echoRequest(session, (OFEchoRequest)ofMsg);
        }else if(ofMsg.getType().equals(OFType.ECHO_REPLY)){
            this.echoReply(session, (OFEchoReply)ofMsg);
        }else if(ofMsg.getType().equals(OFType.FEATURES_REPLY)){
            this.featuresReply(session, (OFFeaturesReply)ofMsg);
        }else if(ofMsg.getType().equals(OFType.ERROR)){
            this.error(session, (OFError)ofMsg);
        }else{
            //System.err.println("Unsupported message type " + ofMsg.getType());
        }

    }

    abstract protected void hello(IoSession session, OFHello ofMsg);

    protected void echoRequest(IoSession session, OFEchoRequest ofMsg) {
        OFEchoReply ofEchoReply = new OFEchoReply();
        ofEchoReply.setPayload(ofMsg.getPayload());
        session.write(ofEchoReply);
    }

    protected void echoReply(IoSession session, OFEchoReply ofMsg) {
        return;
    }

    protected void featuresReply(IoSession session, OFFeaturesReply ofMsg) {
        session.setAttribute(OF_SESSION_ESTABLISHED, new Boolean(true));
        return;
    }

    protected void error(IoSession session, OFError ofMsg) {

        System.out.println("Error Code: " + ofMsg.getErrorCode());
        System.out.println("Error Type: " + ofMsg.getErrorType());
        try {
            System.out.println("Error Msg: " + ofMsg.getOffendingMsg());
        } catch (MessageParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }

}
