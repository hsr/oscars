package net.es.oscars.pss.dragon.vlsr;


import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.ArrayList;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import net.es.oscars.logging.*;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSchException;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.beans.PSSCommand;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.dragon.util.DRAGONUtils;

import edu.internet2.hopi.dragon.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import org.ogf.schema.network.topology.ctrlplane.*;
/**
 * a connector for DRAGON VLSR using CLI
 * 
 * @author xi
 *
 */

public class VLSRConnector implements Connector {
    private Logger log = Logger.getLogger(VLSRConnector.class);
    private OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
    // private OSCARSNetLogger netLogger =  OSCARSNetLogger.getTlogger();

    private Session session = null;
    private DragonCSA csa = null;
    
    private long DELAY = 30;
    private int TIMEOUT = 30000;
    private int INSERVICE_CHECKS= 24;
    private int INSERVICE_CHECK_DELAY= 5000;

    // DRAGON connector configs
    private String cliUser = "dragon";
    private String cliPassword = "dragon";
    private String promptPattern = "vlsr";
    private boolean sshPortForward = false;
    private String sshUser = null;
    private String sshKey = null;
    private int sshPort = 22;
    private String localAddress  = "127.0.0.1";
    private int remotePort = 2611;
    private boolean hasNarb = false;
    private boolean setERO = false;
    private boolean tunnelMode = false; // TODO: config in yaml

    public VLSRConnector() {
        netLogger.init(ModuleName.PSS, "0000");
    }

    public DragonCSA getCsa() {
        return csa;
    }

    public void setCsa(DragonCSA csa) {
        this.csa = csa;
    }

    public void setConfig(GenericConfig connectorConfig) throws PSSException {
        if (connectorConfig == null) {
            throw new PSSException("no config set!");
        } else if (connectorConfig.getParams() == null) {
            throw new PSSException("login null ");
        }

        HashMap<String, Object> params = connectorConfig.getParams();
        if (params.get("cliUser") != null) {
            this.cliUser = (String)params.get("cliUser");
        }
        if (params.get("cliPassword") != null) {
            this.cliPassword = (String)params.get("cliPassword");
        }
        if (params.get("promptPattern") != null) {
            this.promptPattern = (String)params.get("promptPattern");
        }
        if (((Boolean)params.get("sshPortForward"))) {
            this.sshPortForward = true;
            this.sshUser = (String)params.get("sshUser");
            if (this.sshUser == null) {
                throw new PSSException("sshUser null");
            }
            this.sshKey = (String)params.get("sshKey");
            if (this.sshKey == null) {
                throw new PSSException("sshKey null");
            }
            if (params.get("sshPort") != null) {
                this.sshPort = (Integer)params.get("sshPort");
            }
            if (params.get("localAddress") != null) {
                this.localAddress = (String)params.get("localAddress");
            }
            if (params.get("remotePort") != null) {
                this.remotePort = (Integer)params.get("remotePort");
            }
        }
        if (params.get("hasNarb") != null && ((Boolean)params.get("hasNarb"))) {
            this.hasNarb = true;
        }
        if (params.get("setERO") != null && ((Boolean)params.get("setERO"))) {
            this.setERO = true;
        }
        if (params.get("tunnelMode") != null && ((Boolean)params.get("tunnelMode"))) {
            this.tunnelMode = true;
        }
    }

    /**
     *  @throws IOException
     *  @throws PSSException
     */
    private void connect(String address) throws IOException, PSSException {
        String event = "VLSRConnector.connect";
        this.log.debug(netLogger.start(event));

        csa = new DragonCSA();
        csa.setLogger(this.getClass().getName());
        csa.setTimeout(this.TIMEOUT);
        csa.setPromptPattern(".*" + this.promptPattern + ".*[>#]");

        JSch jsch = new JSch();

        int port = this.findTelnetPort(sshPortForward);

        if (sshPortForward) {
            /* Initialize ssh client */
            try {
                jsch.addIdentity(sshKey);
            } catch (JSchException e) {
                this.log.error(netLogger.error(event, ErrSev.MAJOR, "SSH Error: " + e.getMessage()));
                throw new PSSException(e.getMessage());
            }

            /* Create  ssh forward tunnel */
            try {
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                this.session = jsch.getSession(this.sshUser, address, sshPort);
                this.session.setConfig(config);
                this.session.connect();
            } catch (JSchException e) {
                this.log.error(netLogger.error(event, ErrSev.MAJOR, "Unable to create SSH tunnel: " + e.getMessage()));
                throw new PSSException(e.getMessage());
            }

            /* Create SSH tunnel and keep trying if fail */
            int bindFailCount = 0;
            for (int i = 0; i < 10; i++) {
                try {
                    this.session.setPortForwardingL(port, this.localAddress,
                            this.remotePort);
                    this.log.info(netLogger.getMsg(event, "SSH bound to local port "
                            + port + " after " + i + " failures."));
                    break;
                } catch (JSchException e) {
                    this.log.debug(netLogger.getMsg(event,
                            "Failed to build SSH tunnel: " + e.getMessage()));
                    bindFailCount++;
                }
                port = this.findTelnetPort(sshPortForward);
            }

            /* Throw exception if couldn't create SSH tunnel after 10 tries */
            if (bindFailCount == 10) {
                throw new PSSException("Failed to create SSH tunnel after 10 attempts");
            }
        }

        /* Log into  VLSR */
        this.log .info(netLogger.getMsg(event,"logging into VLSR " + address + " "
                + Integer.toString(this.remotePort)));

        if (!csa.login(this.localAddress, port, this.cliPassword)) {
            this.log.error(netLogger.error(event, ErrSev.MAJOR,
                "Unable to login to VLSR " + address + ": " + csa.getError()));
            if (this.session != null) {
                this.session.disconnect();
                this.session = null;
            }
            throw new PSSException("Unable to log into VLSR " + csa.getError());
        }

        this.log.debug(netLogger.end(event));
    }

    /**
     * Shut down gracefully.
     */
    private void disconnect() {
        String event = "VLSRConnector.disconnect";
        this.log.debug(netLogger.start(event));
        if (this.csa != null) {
            this.csa.disconnect();
        }
        if (this.session != null) {
            this.session.disconnect();
            this.session = null;
        }
        this.log.debug(netLogger.end(event));
    }

    /**
     * Sends the CLI commands to VLSR -- Unused in DRAGON VLSRConnector
     * @param PSSCommand
\    * @throws PSSException
     */
    public String sendCommand(PSSCommand command) throws PSSException {
        String event = "VLSRConnector.sendCommand";
        this.log.debug(netLogger.start(event));
        
        String deviceCommand = command.getDeviceCommand();
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
            
            // connect to router over SSH
            this.log.debug("connecting to "+address);
            this.connect(address);
            this.log.debug("sending command...");
            responseString = this.csa.command(deviceCommand);
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e);
        } finally {
            // close SSH connection
            this.disconnect();
        }
        this.log.debug(netLogger.end(event));
        return responseString;
    }

   /**
     * setup a the LSP for the given reservation.
     *
     * @param resv the reservation whose path will be set up
     * @throws PSSException
     */
    public void setupPath(ResDetails resv) throws PSSException{
        String event = "VLSRConnector.setupPath";
        this.log.info(netLogger.start(event));

        ReservedConstraintType rc = resv.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();
        CtrlPlanePathContent path = pi.getPath();
        String srcEp = DRAGONUtils.getEndPoint(resv, false);
        String dstEp = DRAGONUtils.getEndPoint(resv, true);

        String srcNodeId = DRAGONUtils.getDeviceId(resv, false);
        String telnetAddress = DRAGONUtils.getDeviceAddress(srcNodeId);
        if (telnetAddress == null) {
            throw new PSSException("Unable to resolve address for source node: "
                    + srcNodeId);
        }
        String dstNodeId = DRAGONUtils.getDeviceId(resv, true);
        String telnetAddressDest = DRAGONUtils.getDeviceAddress(dstNodeId);
        if (telnetAddress == null) {
            throw new PSSException("Unable to resolve address for destination node: "
                    + dstNodeId);
        }
        ArrayList<String> ero = null;
        ArrayList<String> subnetEro = null;
        CtrlPlaneLinkContent ingressLink;
        CtrlPlaneLinkContent egressLink;
        try {
            ingressLink = PathTools.getIngressLink(PathTools.getLocalDomainId(), path);
        } catch (OSCARSServiceException e) {
            throw (new PSSException("PathTools.getIngressLink caught: " + e.getMessage()));
        }
        try {
            egressLink = PathTools.getEgressLink(PathTools.getLocalDomainId(), path);
        } catch (OSCARSServiceException e) {
            throw (new PSSException("PathTools.getEgressLink caught: " + e.getMessage()));
        }
        int ingressLinkDescr = this.getLinkDescr(ingressLink);
        int egressLinkDescr = this.getLinkDescr(egressLink);

        /* Get source and destination node address */
        InetAddress ingress = DRAGONUtils.getVlsrRouterId(srcEp);
        InetAddress egress = DRAGONUtils.getVlsrRouterId(dstEp);

        /* Get source and destination local ID */
        DragonLocalID ingLocalId = DRAGONUtils.getLocalId(srcEp,
                                          ingressLinkDescr, hasNarb);
        DragonLocalID egrLocalId = DRAGONUtils.getLocalId(dstEp,
                                          egressLinkDescr, hasNarb);
        int ingLocalIdIface = DRAGONUtils.getLocalIdNum(srcEp,
                (!ingLocalId.getType().equals(DragonLocalID.TAGGED_PORT_GROUP)));
        int egrLocalIdIface = DRAGONUtils.getLocalIdNum(dstEp,
                (!ingLocalId.getType().equals(DragonLocalID.TAGGED_PORT_GROUP)));

        /* Initialize LSP */
        DragonLSP lsp = new DragonLSP(ingress, ingLocalId, egress,
                                            egrLocalId, null, 0);
        String gri = resv.getGlobalReservationId();
        lsp.setLSPName(gri);
        netLogger.setGRI(gri);
        if(setERO){
            ero = DRAGONUtils.getPathEro(path, false);
            subnetEro = DRAGONUtils.getPathEro(path, true);
            lsp.setEro(ero);
            lsp.setSubnetEro(subnetEro);
        }

        /* Set layer specific params */
        String bandwidth = DRAGONUtils.prepareBandwidth(rc.getBandwidth(), path);
        lsp.setBandwidth(bandwidth);

        if (ingressLinkDescr <= 0
                && ingLocalId.getType().equals(DragonLocalID.SUBNET_INTERFACE)
                && this.tunnelMode) {
            lsp.setSrcVtag(-1);
        } else if (ingressLinkDescr <= 0) {
            lsp.setSrcVtag(0);
            lsp.setE2EVtag(Math.abs(ingressLinkDescr));
        } else {
            lsp.setSrcVtag(ingressLinkDescr);
        }

        if (egressLinkDescr <= 0
                && egrLocalId.getType().equals(DragonLocalID.SUBNET_INTERFACE)
                && tunnelMode) {
            lsp.setDstVtag(-1);
        } else if (egressLinkDescr <= 0) {
            lsp.setDstVtag(0);
            lsp.setE2EVtag(Math.abs(egressLinkDescr));
        } else {
            lsp.setDstVtag(egressLinkDescr);
        }

        lsp.setSrcLocalID(ingLocalId);
        lsp.setDstLocalID(egrLocalId);
        lsp.setSWCAP(DragonLSP.SWCAP_L2SC);
        lsp.setEncoding(DragonLSP.ENCODING_ETHERNET);
        lsp.setGPID(DragonLSP.GPID_ETHERNET);
        
        if (ConfigHolder.getInstance().getBaseConfig().getCircuitService().isStub()) {
            this.log.info("(Stub-Mode) Setting up LSP: " + gri);
            this.log.info("(Stub-Mode) Ingress VLSR: " + ingress.getHostAddress());
            this.log.info("(Stub-Mode) ingress localId " + ingLocalId.getType()
                    + "=" + Integer.toString(ingLocalId.getNumber()));
            this.log.info("(Stub-Mode) ingress vtag: "
                    + Integer.toString(lsp.getSrcVtag()));
            this.log.info("(Stub-Mode) Egress VLSR: " + egress.getHostAddress());
            this.log.info("(Stub-Mode) egress localId: " + egrLocalId.getType()
                    + "=" + Integer.toString(egrLocalId.getNumber()));
            this.log.info("(Stub-Mode) engress vtag: "
                    + Integer.toString(lsp.getDstVtag()));
            this.log.info(netLogger.end(event));
            return;
        }

        /* Create egress local-id unless escaped*/
        if(hasNarb && !DRAGONUtils.escapeLocalIdCreation(dstEp)){
            try {
                this.connect(telnetAddressDest);
            } catch (IOException e) {
                log.error(e);
                throw new PSSException(e);
            }

            /* Create egress local id */
            csa.deleteLocalId(egrLocalId);
            if (csa.createLocalId(egrLocalId, egrLocalIdIface)) {
                this.log.info(netLogger.getMsg(event,
                        "Created local-id " + egrLocalId.getType()
                        + " " + egrLocalId.getNumber()));
            } else {
                this.log.error(netLogger.error(event, ErrSev.MAJOR,
                        "unable to create dest local-id "
                        + lsp.getLSPName() + ": " + csa.getError()));
                this.disconnect();
                throw new PSSException("Unable to create LSP: Dest local-id "
                        + csa.getError());
            }

            /* Logout */
            this.disconnect();
        }

        /* Log into ingress VLSR */
        try {
            this.connect(telnetAddress);
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e);
        }

        /* Create ingress local-id unless escaped */
        if(hasNarb && !DRAGONUtils.escapeLocalIdCreation(srcEp)){

            /* Delete local-id if ingress and egress not the same */
            if(!(ingress.getHostAddress().equals(egress.getHostAddress()) &&
                ingLocalId.getNumber() == egrLocalId.getNumber())){
                csa.deleteLocalId(ingLocalId);
            }

            if(csa.createLocalId(ingLocalId, ingLocalIdIface)){
                this.log.info(netLogger.getMsg(event,
                        "Created local-id " + ingLocalId.getType() + " " +
                    ingLocalId.getNumber()));
            }else{
                this.log.error("unable to create src local-id " +
                    lsp.getLSPName() + ": " + csa.getError());
                this.disconnect();
                throw new PSSException("Unable to create LSP: src local-id " +
                    csa.getError());
            }
        }

        /* Set up LSP */
        if(csa.setupLSP(lsp)){
            this.log.info(netLogger.getMsg(event,"set up lsp " + lsp.getLSPName()));
        }else{
            this.log.error(netLogger.error(event, ErrSev.MAJOR,
                    "Unable to setup LSP " + lsp.getLSPName())
                + ": " + csa.getError());
            this.disconnect();
            throw new PSSException("Unable to setup LSP: " +
                csa.getError());
        }

        /* Check lsp every few seconds */
        for(int i = 1; i <= INSERVICE_CHECKS; i++){
            String status = null;
            lsp = csa.getLSPByName(gri);

            /* Verify LSP still exists */
            if(lsp == null){
                this.log.error(netLogger.error(event, ErrSev.MAJOR,
                        "LSP " + gri + " failed. Status: LSP could not be found"));
                this.disconnect();
                throw new PSSException("Path failure occured.");
            }

            /* Check if LSP status */
            status = lsp.getStatus();
            if(status.equals(DragonLSP.STATUS_INSERVICE)){
                this.log.info(netLogger.getMsg(event, "LSP is IN SERVICE"));
                break;
            }else if(!(status.equals(DragonLSP.STATUS_COMMIT) ||
                       status.equals(DragonLSP.STATUS_LISTENING)) || i == INSERVICE_CHECKS){
                this.log.error(netLogger.error(event, ErrSev.MAJOR,
                        "Path setup failed. Status=" + lsp.getStatus()));
                if(csa.teardownLSP(gri)){
                    this.log.info(netLogger.getMsg(event, "Deleted LSP after error"));
                }else{
                    this.log.error(netLogger.error(event, ErrSev.MAJOR,
                        "Unable to delete LSP after error: " + csa.getError()));
                }
                this.disconnect();
                throw new PSSException("LSP creation failed. There may be " +
                    "an error in the underlying network.");
            }

            /* Sleep for 5 seconds */
            try{
                Thread.sleep(INSERVICE_CHECK_DELAY);
            }catch(Exception e){
                throw new PSSException("Could not sleep to wait for circuit");
            }
        }

        /* Logout */
        this.disconnect();

        this.log.info(netLogger.end(event));
    }
     /**
     * Verifies LSP is still active by running a VLSR "show lsp" command
     *
     * @param resv the reservation whose path will be refreshed
     * @throws PSSException
     */
    public String pathStatus(ResDetails resv, boolean atIngress) throws PSSException{
        String event = "VLSRConnector.pathStatus";
        this.log.info(netLogger.start(event));

        ReservedConstraintType rc = resv.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();
        CtrlPlanePathContent path = pi.getPath();
        String srcEp = DRAGONUtils.getEndPoint(resv, false);
        String dstEp = DRAGONUtils.getEndPoint(resv, true);

        String srcNodeId = DRAGONUtils.getDeviceId(resv, false);
        String telnetAddress = DRAGONUtils.getDeviceAddress(srcNodeId);
        if (telnetAddress == null) {
            throw new PSSException("Unable to resolve address for source node: "
                    + srcNodeId);
        }
        String dstNodeId = DRAGONUtils.getDeviceId(resv, true);
        String telnetAddressDest = DRAGONUtils.getDeviceAddress(dstNodeId);
        if (telnetAddress == null) {
            throw new PSSException("Unable to resolve address for destination node: "
                    + dstNodeId);
        }

        String gri = resv.getGlobalReservationId();
        netLogger.setGRI(gri);

        if (ConfigHolder.getInstance().getBaseConfig().getCircuitService().isStub()) {
            this.log.info("(Stub-Mode) Getting status for LSP: " + gri);
            this.log.info(netLogger.end(event));
            return "UNKNOWN-Stub";
        }

        DragonLSP lsp = null;

        /* Log into ingress VLSR */
        try {
            if (atIngress)
                this.connect(telnetAddress);
            else
                this.connect(telnetAddressDest);
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e);
        }

        lsp = csa.getLSPByName(gri);
        if(lsp == null) {
            this.log.error(netLogger.error(event, ErrSev.MAJOR,
                "Unable to get LSP by name " + gri + ": " + csa.getError()));
            this.disconnect();
            throw new PSSException("Unable to find LSP: " + gri);
        }

        /* Logout */
        this.disconnect();

        this.log.info(netLogger.end(event));

        return lsp.getStatus();
    }

    /**
     * Removes LSP by running a VLSR "delete lsp" command
     *
     * @param resv the reservation whose path will be removed
     * @throws PSSException
     */
    public void teardownPath(ResDetails resv) throws PSSException{
        String event = "VLSRConnector.teardownPath";
        this.log.info(netLogger.start(event));

        ReservedConstraintType rc = resv.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();
        CtrlPlanePathContent path = pi.getPath();
        String srcEp = DRAGONUtils.getEndPoint(resv, false);
        String dstEp = DRAGONUtils.getEndPoint(resv, true);

        String srcNodeId = DRAGONUtils.getDeviceId(resv, false);
        String telnetAddress = DRAGONUtils.getDeviceAddress(srcNodeId);
        if (telnetAddress == null) {
            throw new PSSException("Unable to resolve address for source node: "
                    + srcNodeId);
        }
        String dstNodeId = DRAGONUtils.getDeviceId(resv, true);
        String telnetAddressDest = DRAGONUtils.getDeviceAddress(dstNodeId);
        if (telnetAddress == null) {
            throw new PSSException("Unable to resolve address for destination node: "
                    + dstNodeId);
        }

        String gri = resv.getGlobalReservationId();
        netLogger.setGRI(gri);

        if (ConfigHolder.getInstance().getBaseConfig().getCircuitService().isStub()) {
            this.log.info("(Stub-Mode) Tearing down LSP: " + gri);
            this.log.info(netLogger.end(event));
            return;
        }

        /* Log into ingress VLSR */
        try {
            this.connect(telnetAddress);
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e);
        }

        if(csa.teardownLSP(gri)){
            this.log.info(netLogger.getMsg(event, "Tore down lsp " + gri));
        }else{
            this.log.error(netLogger.error(event, ErrSev.MAJOR,
                    "Unable to teardown LSP " + gri + ": " + csa.getError()));
            this.disconnect();
            /* Do not throw exception only if the LSP is gone.*/
            if (csa.getError().indexOf("No matching LSP") == -1) {
                throw new PSSException("Unable to teardown LSP: " +
                    csa.getError());
            } 
        }

        /* Logout */
        this.disconnect();

        this.log.info(netLogger.end(event));
    }


    /**
     * Retrieves the port used to login to the VLSR CLI.
     *
     * @param sshPortForward
     * @return port number used to access VLSR CLI
     * @throws PSSException
     */
    private int findTelnetPort(boolean sshPortForward) throws PSSException{
        int port = 0;
        if(sshPortForward){
            Random rand = new Random(System.currentTimeMillis());
            port = 49152 + rand.nextInt(16383);
        }else{
            port = this.remotePort;
        }
        return port;
    }

    /**
     * Private utility method for retrieving the first or last hop linkDescr
     * in a stored path.
     *
     * @param path the path from which the ingress or egress will be retrieved
     * @param isIngress true if ingress should be selected, false if egress
     * @return the ingress or egress link description as an int
     *
     */
    private int getLinkDescr(CtrlPlaneLinkContent link) {
        String linkDescr = link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
        if (linkDescr == null) {
            return -1;
        } else {
            return Integer.parseInt(linkDescr);
        }
    }
}

