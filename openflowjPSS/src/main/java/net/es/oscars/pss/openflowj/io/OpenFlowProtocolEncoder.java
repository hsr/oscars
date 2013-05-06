package net.es.oscars.pss.openflowj.io;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.openflow.protocol.OFMessage;

/**
 * Converts an OpenFlowJ message object to bytes
 *
 */
public class OpenFlowProtocolEncoder extends ProtocolEncoderAdapter{

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out)
            throws Exception {
        ChannelBuffer channelBuf = ChannelBuffers.buffer(((OFMessage) message).getLength());
        ((OFMessage) message).writeTo(channelBuf);
        IoBuffer outputBuf = IoBuffer.allocate(channelBuf.array().length);
        outputBuf.put(channelBuf.array());
        outputBuf.flip();
        out.write(outputBuf);
    }

}
