package net.es.oscars.pss.openflowj.io;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 * Class that accepts new OpenFlow connections from devices.
 *
 */
public class OpenFlowListener {
    protected int port;
    protected int idleTimeout;
    protected int readBufferSize;
    protected OpenFlowMessageHandler handler;

    public static int DEFAULT_PORT = 6633;
    public static int DEFAULT_IDLE_TIMEOUT = 10;//seconds
    public static int DEFAULT_READ_BUFFER_SIZE = 2048;

    public OpenFlowListener(int port, OpenFlowMessageHandler handler){
        this.port = port;
        this.idleTimeout = DEFAULT_IDLE_TIMEOUT;
        this.readBufferSize = DEFAULT_READ_BUFFER_SIZE;
        this.handler = handler;
    }

    public void start() throws IOException{
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast( "of_protocol", 
                new ProtocolCodecFilter(new OpenFlowCodecFactory()));

        acceptor.setHandler(this.handler);

        acceptor.getSessionConfig().setReadBufferSize( this.readBufferSize );
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, this.idleTimeout );

        acceptor.bind(new InetSocketAddress(this.port));
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

}
