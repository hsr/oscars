package net.es.oscars.pss.connect;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.beans.PSSCommand;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.config.ConfigHolder;

/**
 * a connector for Juniper routers using JunoScript
 * 
 * You will need to create a new instance of this class for each router.
 * 
 * @author haniotak, dwrobertson
 *
 */

public class JunoscriptConnector implements Connector {
    /*
    logger wrapper for JSch
     */
    private class JschLogger implements com.jcraft.jsch.Logger {
        private Logger log = Logger.getLogger(JschLogger.class);

        public boolean isEnabled(int arg0) {
            return true;
        }

        public void log(int arg0, String arg1) {
            log.debug(String.format("[SFTP/SSH -&gt; %s]", arg1));
        }
    }

    private Logger log = Logger.getLogger(JunoscriptConnector.class);
    // private OSCARSNetLogger netLogger =  OSCARSNetLogger.getTlogger();

    private BufferedReader fromServer = null;
    private OutputStream toServer = null;
    private Session session = null;
    private Channel channel = null;
    
    
    private String privkeyfile  = null;
    private String passphrase   = null;
    private String login        = null;
    private boolean commitsync   = false;


    public JunoscriptConnector() {
    }
    
    public void setConfig(GenericConfig connectorConfig) throws PSSException {
        if (connectorConfig == null) {
            throw new PSSException("no config set!");
        } else if (connectorConfig.getParams() == null) {
            throw new PSSException("login null ");
        }
        HashMap<String, Object> params = connectorConfig.getParams();
        
        if (params.get("privkeyfile") == null) {
            throw new PSSException("privkeyfile null");
        } else if (params.get("login") == null) {
            throw new PSSException("login null ");
        } else if (params.get("passphrase") == null) {
            throw new PSSException("passphrase null ");
        }

        this.privkeyfile    = (String) params.get("privkeyfile");
        this.login          = (String) params.get("login");
        this.passphrase     = (String) params.get("passphrase");
        if (params.get("commitsync") != null) {
            commitsync = (Boolean)params.get("commitsync");
        }
    }
    
    

    /**
     *  @throws IOException
     *  @throws PSSException
     */
    private void connect(String address) throws IOException, PSSException {

        this.log.debug("connect.start");


        JSch jsch = new JSch();
        JSch.setLogger(new JschLogger());
        try {
            jsch.addIdentity(login,
                    IOUtils.toByteArray(new FileInputStream(privkeyfile)),
                    null,
                    passphrase.getBytes());
            this.session = jsch.getSession(login, address, 22);
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            this.session.setConfig(config);
            this.session.connect();

            this.channel = this.session.openChannel("exec");
            
            this.toServer = channel.getOutputStream();
            ((ChannelExec) this.channel).setCommand("junoscript");
            
            this.fromServer = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            
            this.channel.connect();
        } catch (JSchException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.debug("connect.finish");
    }
    
    /**
     * Shut down gracefully.
     */
    private void shutdown() throws IOException {
        this.log.debug("shutdown.start");
        if (this.channel != null) {
            this.channel.disconnect();
        }
        if (this.session != null) {
            this.session.disconnect();
        }
        if (this.fromServer != null) {
            this.fromServer.close();
        } 
        if (this.toServer != null) {
            this.toServer.close();
        }
        this.log.debug("shutdown.finish");
    }

    /**
     * Sends the XML command to the server.
     * @param command XML document with Junoscript commands
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    public String sendCommand(PSSCommand command) throws PSSException {

        log.debug("sendCommand start");
        // String event = "pssSendCommand";
        // netLogger.init(ModuleName.PSS, command.getTransactionId());
        // log.debug(netLogger.start(event));
        
        // prepare XML handlers
        SAXBuilder sb = new SAXBuilder();
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        format.setEncoding("US-ASCII");
        outputter.setFormat(format);
        XMLOutputter prettyOut = new XMLOutputter(Format.getPrettyFormat());
        

        
        CircuitServiceConfig circuitServiceConfig = ConfigHolder.getInstance().getBaseConfig().getCircuitService();
        
        
        String deviceCommand = command.getDeviceCommand();
        if (commitsync) {
            deviceCommand = deviceCommand.replaceAll("<commit-configuration />", "<commit-configuration> <synchronize/> </commit-configuration>");
        }
        String address = command.getDeviceAddress();
        if (deviceCommand == null) {
            throw new PSSException("null device command");
        } else if (address == null) {
            throw new PSSException("null device address");
        }
        log.debug("sendCommand deviceCommand "+deviceCommand);
        log.debug("sendCommand address "+address);
        
        String responseString = "";
        try {
            Document commandDoc = sb.build(new StringReader(deviceCommand));
            // built XML document
            
            
            // log if necessary
            if (circuitServiceConfig.isLogRequest()) {
                String logOutput = outputter.outputString(commandDoc);
                this.log.info("\nCOMMAND\n\n" + logOutput);
            }
            
            if (circuitServiceConfig.isStub()) {
                log.debug("set to stub mode; exiting");
                return "";
            }

            
            // connect to router over SSH
            this.log.debug("connecting to "+address);
            this.connect(address);
            this.log.debug("sending command...");
            // send command
            outputter.output(commandDoc, this.toServer);
            
            // grab response
            Document responseDoc = null;
            if (this.fromServer == null) {
                throw new PSSException("Cannot get output stream from device");
            }
            responseDoc = sb.build(this.fromServer);
            if (responseDoc == null) {
                throw new PSSException("Device "+address+" did not return a response");
            }
            
            // convert response to string
            ByteArrayOutputStream buff  = new ByteArrayOutputStream();
            prettyOut.output(responseDoc, buff);
            responseString = buff.toString();
            
            boolean errorInResponse = this.checkForErrors(responseDoc);
            if (errorInResponse) {
                this.log.error("\nRESPONSE ERROR:\n"+responseString);
                throw new PSSException("Device "+address+" returned a configuration error: "+responseString);
            } else if (circuitServiceConfig.isLogResponse()) {
                this.log.info("\nRESPONSE:\n\n"+responseString);
            }
            

            this.log.info("response received");
            this.log.info("sendCommand.end for "+address);

        } catch (JDOMException e) {
            log.error(e);
            throw new PSSException(e);
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e);
        } finally {
            // close SSH connection
            try {
                this.shutdown();
            } catch (IOException e) {
                log.error(e);
                throw new PSSException(e);
            }
            
        }

        return responseString;
    }

    @SuppressWarnings("unchecked")
    private boolean checkForErrors(Document responseDoc) {
        boolean hasErrors = false;
        Namespace xnmNs = Namespace.getNamespace("xnm", "http://xml.juniper.net/xnm/1.1/xnm");
        List<Element> rpcReplies = responseDoc.getRootElement().getChildren("rpc-reply");
        for (Element rpcReply : rpcReplies) {
            Element configRes = rpcReply.getChild("load-configuration-results");
            if (configRes != null) {
                List<Element> configErrors = configRes.getChildren("error", xnmNs);
                if (configErrors != null && !configErrors.isEmpty()) {
                    hasErrors = true;
                }
            }
            
            Element commitRes = rpcReply.getChild("commit-results");
            if (commitRes != null) {
                List<Element> commitErrors = commitRes.getChildren("error", xnmNs);
                if (commitErrors != null && !commitErrors.isEmpty()) {
                    hasErrors = true;
                }
            }
        }
        return hasErrors;
        
    }

}
