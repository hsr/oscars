package net.es.oscars.pce.nullpce;

import java.util.List;
import org.apache.log4j.Logger;

import net.es.oscars.pce.SimplePCEJob;
import net.es.oscars.pce.SimplePCEServer;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.PCEProtocolHandler;
import net.es.oscars.pce.soap.gen.v06.PCECancelContent;
import net.es.oscars.pce.soap.gen.v06.PCECreateCommitContent;
import net.es.oscars.pce.soap.gen.v06.PCEModifyCommitContent;
import net.es.oscars.pce.soap.gen.v06.PCECreateContent;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.pce.soap.gen.v06.PCEModifyContent;
import net.es.oscars.pce.soap.gen.v06.TagDataContent;

@javax.xml.ws.BindingType(value ="http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class NullPCEProtocolHandler extends PCEProtocolHandler {

    private static final Logger LOG = Logger.getLogger(PCEProtocolHandler.class.getName());
    private static final String moduleName = "NullPCEProtocolHandler";

    /**
     * handles the pceCreate message. Copies the input parameters into
     * a pceMessage, creates and queues a pceJob.
     */
    public void pceCreate(PCECreateContent pceCreate)   { 
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String globalReservationId = pceCreate.getGlobalReservationId();
        MessagePropertiesType msgProps = pceCreate.getMessageProperties();
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        netLogger.setGRI(globalReservationId);
        String pceName = pceCreate.getPceName();
        String callBackEndpoint = pceCreate.getCallBackEndpoint();
        PCEDataContent pceData = pceCreate.getPceData();
        LOG.info(netLogger.start(PCERequestTypes.PCE_CREATE));
        try {
            // Create a query object
            PCEMessage pceQuery = new PCEMessage (msgProps,
                                                  globalReservationId,
                                                  pceCreate.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_CREATE,
                                                  pceData);
            // Add the query to the list
            NullPCE.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    /**
     * handles pceCreateCommit message
     */
    public void pceCreateCommit(PCECreateCommitContent pceCreateCommit)  {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = pceCreateCommit.getMessageProperties();
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String globalReservationId = pceCreateCommit.getGlobalReservationId();
        netLogger.setGRI(globalReservationId);
        String pceName = pceCreateCommit.getPceName();
        String callBackEndpoint = pceCreateCommit.getCallBackEndpoint();
        PCEDataContent pceData = pceCreateCommit.getPceData();
        LOG.info(netLogger.start(PCERequestTypes.PCE_CREATE_COMMIT));
        try {
            // Create a query object
            PCEMessage pceQuery = new PCEMessage (msgProps,
                                                  globalReservationId,
                                                  pceCreateCommit.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_CREATE_COMMIT,
                                                  pceData);
            // Add the query to the list
            NullPCE.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    /**
     * handles pceModifyCommit message
     */
    public void pceModifyCommit(PCEModifyCommitContent pceModifyCommit)  {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = pceModifyCommit.getMessageProperties();
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String globalReservationId = pceModifyCommit.getGlobalReservationId();
        netLogger.setGRI(globalReservationId);
        String pceName = pceModifyCommit.getPceName();
        String callBackEndpoint = pceModifyCommit.getCallBackEndpoint();
        PCEDataContent pceData = pceModifyCommit.getPceData();
        LOG.info(netLogger.start(PCERequestTypes.PCE_MODIFY_COMMIT));
        try {
            // Create a query object
            PCEMessage pceQuery = new PCEMessage (msgProps,
                                                  globalReservationId,
                                                  pceModifyCommit.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_MODIFY_COMMIT,
                                                  pceData);
            // Add the query to the list
            NullPCE.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    public void pceCancel(PCECancelContent pceCancel) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = pceCancel.getMessageProperties();
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String globalReservationId = pceCancel.getGlobalReservationId();
        netLogger.setGRI(globalReservationId);
        String pceName = pceCancel.getPceName();
        String callBackEndpoint = pceCancel.getCallBackEndpoint();
        PCEDataContent pceData = pceCancel.getPceData();
        LOG.info(netLogger.start(PCERequestTypes.PCE_CANCEL));
        try {
            PCEMessage pceCancelMessage = new PCEMessage (msgProps,
                                                          globalReservationId,
                                                          pceCancel.getId(),
                                                          pceName,
                                                          callBackEndpoint,
                                                          PCERequestTypes.PCE_CANCEL,
                                                          pceData);
            
            // Add the query to the list
            NullPCE.getInstance().addPceQuery(pceCancelMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void pceModify(PCEModifyContent pceModify) { 
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = pceModify.getMessageProperties();       
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String globalReservationId = pceModify.getGlobalReservationId();
        netLogger.setGRI(globalReservationId);
        String pceName = pceModify.getPceName();
        String callBackEndpoint = pceModify.getCallBackEndpoint();
        PCEDataContent pceData = pceModify.getPceData();
        LOG.info(netLogger.start("pcModify"));
        try {
            PCEMessage pceModifyMessage = new PCEMessage (msgProps,
                                                          globalReservationId,
                                                          pceModify.getId(),
                                                          pceName,
                                                          callBackEndpoint,
                                                          PCERequestTypes.PCE_MODIFY,
                                                          pceData);
            
            // Add the query to the list
            NullPCE.getInstance().addPceQuery(pceModifyMessage);            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}