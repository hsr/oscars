package net.es.oscars.pce;

import org.apache.log4j.Logger;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.pce.soap.gen.v06.PCECancelContent;
import net.es.oscars.pce.soap.gen.v06.PCECreateCommitContent;
import net.es.oscars.pce.soap.gen.v06.PCEModifyCommitContent;
import net.es.oscars.pce.soap.gen.v06.PCECreateContent;
import net.es.oscars.pce.soap.gen.v06.PCEModifyContent;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;


/**
 * A PCEProtocol handler that can be used or extended by PCE servers. Handles
 * all the  messages. Note: not used by the PCERuntime.
 * 
 * @author lomax
 *
 */
@javax.xml.ws.BindingType(value ="http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class SimplePCEProtocolHandler extends PCEProtocolHandler {

    private Logger log = Logger.getLogger(this.getClass());
    private String netLogModName = null;
    private String serviceName = null;
    
    public SimplePCEProtocolHandler(){
        if(this.getClass().isAnnotationPresent(OSCARSNetLoggerize.class)){
            OSCARSNetLoggerize anno = this.getClass().getAnnotation(OSCARSNetLoggerize.class);
            this.netLogModName = anno.moduleName();
            this.serviceName = anno.serviceName();
        }
    }
    
    /**
     * handles the pceCreate message. Copies the input parameters into
     * a pceMessage, creates and queues a pceJob.
     */
    public void pceCreate(PCECreateContent pceCreate)   { 
        OSCARSNetLogger netLogger = null;
        String event = PCERequestTypes.PCE_CREATE;
        MessagePropertiesType msgProps = pceCreate.getMessageProperties();
        String globalReservationId = pceCreate.getGlobalReservationId();
        String pceName = pceCreate.getPceName();
        String callBackEndpoint = pceCreate.getCallBackEndpoint();
        PCEDataContent pceData = pceCreate.getPceData();
        if (this.netLogModName != null){
            netLogger = OSCARSNetLogger.getTlogger();
            netLogger.init(netLogModName,msgProps.getGlobalTransactionId());
            netLogger.setGRI(globalReservationId);
            this.log.info(netLogger.start(event));
        }
        try {
            // Create a query object
            PCEMessage pceCreateMessage = new PCEMessage (msgProps,
                                                   globalReservationId,
                                                   pceCreate.getId(),
                                                   pceName,
                                                   callBackEndpoint,
                                                   PCERequestTypes.PCE_CREATE,
                                                   pceData);
            // Add the query to the list
            SimplePCEServer.getInstance(this.getServiceName()).addPceJob(pceCreateMessage, SimplePCEJob.CREATE_TYPE);
            if(netLogModName != null){
                this.log.info(netLogger.end(event));
            }
         } catch (Exception ex) {
            if(netLogModName != null){
                this.log.error(netLogger.error(event, ErrSev.MAJOR, ex.getMessage()));
            }
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * handles pceCreateCommit message
     */
    public void pceCreateCommit(PCECreateCommitContent pceCreateCommit)  {
        OSCARSNetLogger netLogger = null;
        String event = PCERequestTypes.PCE_CREATE_COMMIT;
        MessagePropertiesType msgProps = pceCreateCommit.getMessageProperties();
        String globalReservationId = pceCreateCommit.getGlobalReservationId();
        String pceName = pceCreateCommit.getPceName();
        String callBackEndpoint = pceCreateCommit.getCallBackEndpoint();
        PCEDataContent pceData = pceCreateCommit.getPceData();
        if(this.netLogModName != null){
            netLogger = OSCARSNetLogger.getTlogger();
            netLogger.init(netLogModName,msgProps.getGlobalTransactionId());
            netLogger.setGRI(globalReservationId);
            this.log.info(netLogger.start(event));
        }
        
        try {
            PCEMessage pceCreateCommitMessage = new PCEMessage (msgProps,
                                                                globalReservationId,
                                                                pceCreateCommit.getId(),
                                                                pceName,
                                                                callBackEndpoint,
                                                                PCERequestTypes.PCE_CREATE_COMMIT,
                                                                pceData);
            
            // Add the query to the list
            SimplePCEServer.getInstance(this.getServiceName()).addPceJob(pceCreateCommitMessage, SimplePCEJob.CREATE_COMMIT_TYPE);
            
            if(netLogModName != null){
                this.log.info(netLogger.end(event));
            }
        } catch (Exception ex) {
            if(netLogModName != null){
                this.log.error(netLogger.error(event, ErrSev.MAJOR, ex.getMessage()));
            }
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

        /**
     * handles pceModifyCommit message
     */
    public void pceModifyCommit(PCEModifyCommitContent pceModifyCommit)  {
        OSCARSNetLogger netLogger = null;
        String event = PCERequestTypes.PCE_MODIFY_COMMIT;
        MessagePropertiesType msgProps = pceModifyCommit.getMessageProperties();
        String globalReservationId = pceModifyCommit.getGlobalReservationId();
        String pceName = pceModifyCommit.getPceName();
        String callBackEndpoint = pceModifyCommit.getCallBackEndpoint();
        PCEDataContent pceData = pceModifyCommit.getPceData();
        if(this.netLogModName != null){
            netLogger = OSCARSNetLogger.getTlogger();
            netLogger.init(netLogModName,msgProps.getGlobalTransactionId());
            netLogger.setGRI(globalReservationId);
            this.log.info(netLogger.start(event));
        }

        try {
            PCEMessage pceModifyCommitMessage = new PCEMessage (msgProps,
                                                                globalReservationId,
                                                                pceModifyCommit.getId(),
                                                                pceName,
                                                                callBackEndpoint,
                                                                PCERequestTypes.PCE_MODIFY_COMMIT,
                                                                pceData);

            // Add the query to the list
            SimplePCEServer.getInstance(this.getServiceName()).addPceJob(pceModifyCommitMessage, SimplePCEJob.MODIFY_COMMIT_TYPE);

            if(netLogModName != null){
                this.log.info(netLogger.end(event));
            }
        } catch (Exception ex) {
            if(netLogModName != null){
                this.log.error(netLogger.error(event, ErrSev.MAJOR, ex.getMessage()));
            }
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    public void pceModify(PCEModifyContent pceModify) { 
        OSCARSNetLogger netLogger = null;
        String event = PCERequestTypes.PCE_MODIFY;
        MessagePropertiesType msgProps = pceModify.getMessageProperties();
        String globalReservationId = pceModify.getGlobalReservationId();
        String pceName = pceModify.getPceName();
        String callBackEndpoint = pceModify.getCallBackEndpoint();
        PCEDataContent pceData = pceModify.getPceData();
        if(this.netLogModName != null){
            netLogger = OSCARSNetLogger.getTlogger();
            netLogger.init(netLogModName,msgProps.getGlobalTransactionId());
            netLogger.setGRI(globalReservationId);
            this.log.info(netLogger.start(event));
        }
        try {
            PCEMessage pceModifyMessage = new PCEMessage (msgProps,
                                                          globalReservationId,
                                                          pceModify.getId(),
                                                          pceName,
                                                          callBackEndpoint,
                                                          PCERequestTypes.PCE_MODIFY,
                                                          pceData);
            
            // Add the query to the list
            SimplePCEServer.getInstance(this.getServiceName()).addPceJob(pceModifyMessage, SimplePCEJob.MODIFY_TYPE);
            
            if(netLogModName != null){
                this.log.info(netLogger.end(event));
            }
        } catch (Exception ex) {
            if(netLogModName != null){
                this.log.error(netLogger.error(event, ErrSev.MAJOR, ex.getMessage()));
            }
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    public void pceCancel(PCECancelContent pceCancel) {
        OSCARSNetLogger netLogger = null;
        String event = PCERequestTypes.PCE_CANCEL;
        MessagePropertiesType msgProps = pceCancel.getMessageProperties();
        String globalReservationId = pceCancel.getGlobalReservationId();
        String pceName = pceCancel.getPceName();
        String callBackEndpoint = pceCancel.getCallBackEndpoint();
        PCEDataContent pceData = pceCancel.getPceData();
        if(this.netLogModName != null){
            netLogger = OSCARSNetLogger.getTlogger();
            netLogger.init(netLogModName,msgProps.getGlobalTransactionId());
            netLogger.setGRI(globalReservationId);
            this.log.info(netLogger.start(event));
        }
        try {
            PCEMessage pceModifyMessage = new PCEMessage (msgProps,
                                                          globalReservationId,
                                                          pceCancel.getId(),
                                                          pceName,
                                                          callBackEndpoint,
                                                          PCERequestTypes.PCE_CANCEL,
                                                          pceData);
            
            // Add the query to the list
            SimplePCEServer.getInstance(this.getServiceName()).addPceJob(pceModifyMessage, SimplePCEJob.CANCEL_TYPE);
            if(netLogModName != null){
                this.log.info(netLogger.end(event));
            }
        } catch (Exception ex) {
            if(netLogModName != null){
                this.log.error(netLogger.error(event, ErrSev.MAJOR, ex.getMessage()));
            }
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * This methods returns the name of the PCE. This must be implemented by all the classes that
     * extend SimplePCEServer
     * @return
     */
    public String getServiceName () {
       return this.serviceName;
    }
}