package net.es.oscars.pss.openflowj.io;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.jboss.netty.buffer.ChannelBuffers;
import org.openflow.protocol.factory.BasicFactory;

/**
 * Converts bytes to an OpenFlowJ message object
 *
 */
public class OpenFlowProtocolDecoder extends CumulativeProtocolDecoder{

    protected boolean doDecode(IoSession sesssion, IoBuffer in,
            ProtocolDecoderOutput out) throws Exception {

        if(in.remaining() < 4){
            return false;
        }

        in.mark();
        int version = in.getUnsigned();
        int type = in.getUnsigned();
        int length = in.getShort();
        in.reset();

        if(in.remaining() < length){
            return false;
        }

        byte[] msgBytes = new byte[length];
        in.get(msgBytes);

        BasicFactory ofMsgFactory = new BasicFactory();
        out.write(ofMsgFactory.parseMessageOne(ChannelBuffers.copiedBuffer(msgBytes)));

        return true;
    }

}
