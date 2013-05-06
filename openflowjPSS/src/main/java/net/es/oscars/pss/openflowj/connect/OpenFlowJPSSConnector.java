package net.es.oscars.pss.openflowj.connect;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.beans.PSSCommand;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.openflowj.common.OpenFlowJPSSCore;
import net.es.oscars.pss.openflowj.io.OpenFlowClient;
import net.es.oscars.pss.openflowj.io.pss.PSSOpenFlowClientMessageHandler;
import net.es.oscars.pss.openflowj.util.OFClientTCPPortUtil;

/**
 * Establishes and manages connections to OpenFlow devices.
 *
 */
public class OpenFlowJPSSConnector implements Connector {
    private Logger log = Logger.getLogger(OpenFlowJPSSConnector.class);

    final static public int MAX_HELLO_WAIT = 10;

    public void setConfig(GenericConfig config) throws PSSException {
        //no-op
        return;
    }
    
    public String sendCommand(PSSCommand cmd) throws PSSException {
        //cast command to OpenFlowPSSCommand
        OpenFlowPSSCommand ofCmd = null;
        try{
            ofCmd = (OpenFlowPSSCommand) cmd;
        }catch(Exception e){
            throw new PSSException("The OpenFlowJPSSConnector can only accepts OpenFlowPSSCommand objects. " +
                    "Please check your service and connector confiuration files");
        }

        //find session
        IoSession ofConnection = OpenFlowJPSSCore.getInstance().getOFConnection(cmd.getDeviceAddress());
        if(ofConnection == null){
            try{
                ofConnection = OpenFlowJPSSConnector.connect(cmd.getDeviceAddress());
            }catch(PSSException e){
                throw e;
            }catch(Exception e){
                e.printStackTrace();
                throw new PSSException(e.getMessage());
            }
        }

        //write message
        ofConnection.write(ofCmd.getOpenFlowMessage());

        return "";
    }

    synchronized static public IoSession connect(String deviceAddress) throws PSSException{
        OpenFlowJPSSCore globals = OpenFlowJPSSCore.getInstance();
        //handle concurrency in case session established after call to this function
        if(globals.getOFConnection(deviceAddress) != null){
            return globals.getOFConnection(deviceAddress);
        }

        //establish connection
        OpenFlowClient client = new OpenFlowClient(deviceAddress, 
                OFClientTCPPortUtil.lookupMgmtPort(deviceAddress), 
                new PSSOpenFlowClientMessageHandler(deviceAddress));
        try{
            client.connect();
        }catch(Exception e){
            throw new PSSException(e.getMessage());
        }
        int checkCount = 0;
        while(!client.isOpenFlowSessionEstablished()){
            if(checkCount >= 10){
                throw new PSSException("Timeout waiting for HELLO message from " + deviceAddress);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            checkCount++;
        }

        return client.getSession();
    }
}
