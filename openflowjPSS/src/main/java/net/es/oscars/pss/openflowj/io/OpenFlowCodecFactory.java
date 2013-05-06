package net.es.oscars.pss.openflowj.io;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Codec that sets OpenFlow decoder and encoder
 */
public class OpenFlowCodecFactory implements ProtocolCodecFactory {
    private ProtocolDecoder decoder;
    private ProtocolEncoder encoder;

    public OpenFlowCodecFactory(){
        this.decoder = new OpenFlowProtocolDecoder();
        this.encoder = new OpenFlowProtocolEncoder();
    }

    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return this.decoder;
    }

    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return this.encoder;
    }

}
