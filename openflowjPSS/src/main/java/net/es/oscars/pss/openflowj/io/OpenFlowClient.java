package net.es.oscars.pss.openflowj.io;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * Class to establish connection to OpenFlow device. Used when OSCARS
 * initiates connection.
 *
 */
public class OpenFlowClient {
    protected String hostname;
    protected int port;
    protected OpenFlowMessageHandler handler;
    protected IoSession session; 
    protected long timeout;

    public static int DEFAULT_PORT = 6633;
    public static int DEFAULT_TIMEOUT = 10*1000;//10 seconds

    public OpenFlowClient(String hostname, int port, OpenFlowMessageHandler handler){
        this.hostname = hostname;
        this.port = port;
        this.handler = handler;
        this.session = null;
        this.timeout = DEFAULT_TIMEOUT;
    }

    public void connect() {
        NioSocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("of_protocol", 
                new ProtocolCodecFilter(new OpenFlowCodecFactory()));
        connector.setHandler(this.handler);
        connector.setConnectTimeoutMillis(this.timeout);
        ConnectFuture future = connector.connect(new InetSocketAddress(this.hostname, this.port));
        future.awaitUninterruptibly();
        try{
            this.session = future.getSession();
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error opening connection to " + 
                    this.hostname + ":" + this.port + " - " + e.getMessage());
        }
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public OpenFlowMessageHandler getHandler() {
        return handler;
    }

    public void setHandler(OpenFlowMessageHandler handler) {
        this.handler = handler;
    }

    public IoSession getSession() {
        return session;
    }


    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isOpenFlowSessionEstablished() {
        if(this.session == null){
            return false;
        }
        if(!this.session.containsAttribute(OpenFlowMessageHandler.OF_SESSION_ESTABLISHED)){
            return false;
        }
        if(this.session.getAttribute(OpenFlowMessageHandler.OF_SESSION_ESTABLISHED) == null){
            return false;
        }

        return (Boolean)this.session.getAttribute(OpenFlowMessageHandler.OF_SESSION_ESTABLISHED);
    }
}
