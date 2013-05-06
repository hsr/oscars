package net.es.oscars.pss.openflowj.common;


import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.openflowj.io.OpenFlowListener;
import net.es.oscars.pss.openflowj.io.OpenFlowMessageHandler;
import net.es.oscars.pss.openflowj.io.pss.PSSOpenFlowServerMessageHandler;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.openflow.protocol.OFEchoRequest;
/**
 * Singleton class to store global configuration options. Also starts
 * OpenFlow protocol listener to accept new connections.
 *
 */
public class OpenFlowJPSSCore {
    private static Logger log = Logger.getLogger(OpenFlowJPSSCore.class);
    private static OpenFlowJPSSCore instance = null;

    private OpenFlowListener ofListener;
    private Map<String, IoSession> ofConnections;
    private long ofEchoInterval;

    final static public long DEFAULT_ECHO_INT = 5000; 

    private OpenFlowJPSSCore(){
        //Initialize connection map
        this.ofConnections = new HashMap<String, IoSession>();
        this.ofEchoInterval = DEFAULT_ECHO_INT;
    }

    synchronized static public OpenFlowJPSSCore getInstance(){
        if(instance == null){
            instance = new OpenFlowJPSSCore();
        }
        return instance;
    }

    /**
     * Starts thread to listen for OpenFlow protocol connections
     * from OpenFlow switches.
     * 
     * @param ofListenerPort
     */
    synchronized public void startListener(int ofListenerPort){
        if(this.ofListener != null){
            return;
        }

        //start listener
        this.ofListener = new OpenFlowListener(ofListenerPort, new PSSOpenFlowServerMessageHandler());
        OFListenerThread ofListenerThread = new OFListenerThread();
        ofListenerThread.start();
    }

    /**
     * Starts thread that periodcially sends echo requests over connections
     */
    synchronized public void startEchoThread(){
        OFEchoThread echoThread = new OFEchoThread();
        echoThread.start();
    }

    /**
     * Adds a new OpenFlow session to the session map that will be keyed by the 
     * provided device address
     * 
     * @param session the OpenFlow session to add
     * @param deviceAddress the address of the device controlled by this session
     */
    public void addOFConnection(IoSession session, String deviceAddress) {
        this.updateOFConnections(session, deviceAddress, false);
    }

    /**
     * Removes the OpenFlow session from the session map belonging to deviceAddress
     * 
     * @param session the OpenFlow session to remove
     * @param deviceAddress the device address represented by the session
     */
    public void removeOFConnection(IoSession session, String deviceAddress) {
        this.updateOFConnections(session, deviceAddress, true);
    }

    /**
     * Returns a session based on the given device address
     * 
     * @param ipAddr the address of the session to lookup
     * @return the session or null if not found
     */
    public IoSession getOFConnection(String ipAddr) {
        if(!this.ofConnections.containsKey(ipAddr)){
            return null;
        }

        return this.ofConnections.get(ipAddr);
    }

    /**
     * Internal method for maintaining the session map
     * 
     * @param session the session to update
     * @param deviceAddress the address of the device controlled by the session
     * @param remove a boolean if true means the sessions should be removed, if false then add it
     */
    synchronized private void updateOFConnections(IoSession session, String deviceAddress, boolean remove) {
        if(session == null){
            return;
        }

        String remoteIP = ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress();

        if(remove){
            if(!this.ofConnections.containsKey(remoteIP) || 
                    this.ofConnections.get(remoteIP) == null){
                throw new RuntimeException("Connection from " + remoteIP + " not found.");
            }
            this.ofConnections.remove(remoteIP);
            if(deviceAddress != null &&
                    this.ofConnections.containsKey(deviceAddress) &&
                    this.ofConnections.get(deviceAddress) != null){
                this.ofConnections.remove(deviceAddress);
            }
        }else{
            if(this.ofConnections.containsKey(remoteIP) && 
                    this.ofConnections.get(remoteIP) != null){
                throw new RuntimeException("A connection is already open from " + remoteIP);
            }
            if(deviceAddress != null &&
                    this.ofConnections.containsKey(deviceAddress) &&
                    this.ofConnections.get(deviceAddress) != null){
                throw new RuntimeException("A connection is already open for " + deviceAddress);
            }else if(deviceAddress != null){
                this.ofConnections.put(deviceAddress, session);
            }
            this.ofConnections.put(remoteIP, session);
        }
    }

    /**
     * Returns the time in between echo requests
     * @return the echo interval
     */
    public long getOfEchoInterval() {
        return ofEchoInterval;
    }

    /**
     * Sets the time in between echo requests
     * @return the echo interval
     */
    public void setOfEchoInterval(long ofEchoInterval) {
        this.ofEchoInterval = ofEchoInterval;
    }

    /**
     * Thread containing the listener that accepts new OpenFlow connections
     *
     */
    private class OFListenerThread extends Thread {
        public void run() {
            try {
                ofListener.start();
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Thread that sends echo request for each openflow session
     *
     */
    private class OFEchoThread extends Thread {
        public void run() {
            try {
                while(true){
                    for(String device : ofConnections.keySet()){
                        //catch so one connection error does not mess up others
                        try{
                            if(ofConnections.get(device).containsAttribute(OpenFlowMessageHandler.OF_SESSION_ESTABLISHED) &&
                                    ofConnections.get(device).getAttribute(OpenFlowMessageHandler.OF_SESSION_ESTABLISHED) != null &&
                                    (Boolean)ofConnections.get(device).getAttribute(OpenFlowMessageHandler.OF_SESSION_ESTABLISHED)){
                                ofConnections.get(device).write(new OFEchoRequest());
                            }
                        }catch(Exception e){
                            log.debug("Error sending OpenFlow Echo to " + device + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    Thread.sleep(ofEchoInterval);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
