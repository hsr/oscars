package net.es.oscars.coord.runtimepce;

import java.net.MalformedURLException;
import java.util.HashMap;

import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.soap.gen.v06.PCEError;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.workers.PCEWorker;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.sharedConstants.ErrorCodes;

/**
 * PCEProxyAction is an Action that calls pceWorker to send PCECreate,PCECreateCommit,
 * PCEModify, pceModifyCommit and PCECancel messages.
 * It processes pceReply and pceErrorReply messages
 * 
 * @author lomax
 *
 */
public class PCEProxyAction extends ProxyAction {

    private static final Logger LOG = Logger.getLogger(PCEProxyAction.class.getName());
    private OSCARSNetLogger netLogger = null;

    private static final long serialVersionUID = 1439115737928915954L;

    private HashMap<String, PCEData> aggPceDataSet = new HashMap<String, PCEData>();

    /**
     * Constructor for PCEProxyAction
     * 
     * @param parentPce ProxyAction for any parentPCE
     * @param coordRequest 
     * @param name String name of PCE
     * @param pathTag
     * @param proxyEndpoint callback address to which PCE service sends PCEReply messages
     * @param pceEndpoint  address of PCE service to be called
     * @param transactionId global transaction id for this IDC request
     * @param requestType pceCreate, pceCreateCommit, pceModifyCommit, pceModify, pceCancel
     */
    public PCEProxyAction (ProxyAction parentPce,
                           CoordRequest coordRequest,
                           PCERuntimeAction pceRuntime,
                           String name,
                           String pathTag,
                           String proxyEndpoint,
                           String pceEndpoint,
                           String transactionId,
                           String requestType) {
        
        // Note that in the input data is not set when creating the object but is dynamically
        // stored at execution time.
        super (parentPce, coordRequest, pceRuntime, name,pathTag,ProxyAction.Role.PCE,proxyEndpoint,pceEndpoint,
               transactionId,requestType);
        netLogger = new OSCARSNetLogger(ModuleName.PCERUNTIME,
                                        ((coordRequest != null) ? coordRequest.getTransactionId(): null));
        netLogger.setGRI((coordRequest != null) ? coordRequest.getGRI(): null);
    }
   
    /**
     *  Uses a PCEWorker to send a pceCreate or pceCreatecommit,  to the external PCE
     */
    public void execute()  {
        String method = "PCEProxyAction.execute";
        LOG.debug(netLogger.start(method, this.getRequestType()));
        try {
            if (this.getRequestType().equals(PCERequestTypes.PCE_CREATE)) {
                this.sendPCECreate();
            } else if (this.getRequestType().equals(PCERequestTypes.PCE_CREATE_COMMIT)) {
                this.sendPCECreateCommit();
            } else if (this.getRequestType().equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
                this.sendPCEModifyCommit();
            } else if (this.getRequestType().equals(PCERequestTypes.PCE_CANCEL)) {
                this.sendPCECancel();
            } else if (this.getRequestType().equals(PCERequestTypes.PCE_MODIFY)) {
                this.sendPCEModify();
            }
        } catch (Exception ex){
            // wasn't able to send message
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, this.getName() + " request type= " +
                                    this.getRequestType() + " failed with  " +  ex.getClass().getName() + " " +
                                    ex.getMessage()));
            OSCARSFault of = new OSCARSFault();
            OSCARSFaultReport faultReport = new OSCARSFaultReport();
            faultReport.setDomainId(PathTools.getLocalDomainId());
            faultReport.setErrorType(ErrorReport.SYSTEM);
            faultReport.setErrorMsg(ex.getMessage());
            faultReport.setErrorCode(ErrorCodes.COULD_NOT_CONNECT);
            faultReport.setGri(this.getCoordRequest().getGRI());
            of.setErrorReport(faultReport);
            of.setMsg(this.getRequestType() + " failed with exception " + ex.getMessage());
            processErrorReply(of);
        }
    }  
    
    public void executed() {
        super.executed();
    }
    
    /**
     * processes a PCEReply message from a PCE
     * Saves the new pceData. If there are chained PCE, update their PCEData and force processing
     * of each one. 
     * Otherwise if this action has an aggregator, update the pceData there.
     * If there is neither, just update the data in the pceRuntimeAction.
     * Finally mark this pceAction as PROCESSED
     * @param pceData - the pceData structure received from PCE
     */
    public void processReply (PCEData pceData) {
        String method = "PCEProxyAction.processReply";
        LOG.debug (netLogger.start(method , this.getRequestType()));
        OSCARSNetLogger.setTlogger(netLogger);
        if (this.getRequestType().equals(PCERequestTypes.PCE_CREATE) ||
            this.getRequestType().equals(PCERequestTypes.PCE_CREATE_COMMIT) ||
            this.getRequestType().equals(PCERequestTypes.PCE_MODIFY_COMMIT) ||
            this.getRequestType().equals(PCERequestTypes.PCE_MODIFY) ||
            this.getRequestType().equals(PCERequestTypes.PCE_CANCEL)) {
            
            // Set the data to be this PCE result data
            super.setResultData(pceData);
            
            // Move the result data to the next element.
            if (this.size() == 0) {
                LOG.debug(netLogger.getMsg(method, "at last element, request Type is " + this.getRequestType()));
                // This is the last PCE of this branch. The result is sent as the input of the closest AGGRETATOR
                AggProxyAction agg = this.getAggregator();
                if (agg == null) {
                    LOG.debug(netLogger.getMsg(method,"no agg"));
                    // There is no aggregator for this PCE. Send the result to the PCERuntimeAction.
                    this.getPCERuntimeAction().setResultData (pceData, (ProxyAction)this);
                } else {
                    agg.addAggData (pceData, this);
                }
            } else {
                // In general, a PCE (not an aggregator) has only one child, but it possible for PCE to have 
                // several children PCE, using a upper level aggregator.
                for (CoordAction<PCEData,PCEData> pce : this) {
                    pce.setRequestData(pceData);
                    // force processing
                    pce.process();
                }
            }
            // This PCE is now executed
            this.executed();

            LOG.debug(netLogger.end(method ));
            return;
        }
        LOG.debug(netLogger.error(method,ErrSev.MINOR, " unknown request type " + this.getRequestType()));
        return;
    }
    
    public void setResultData (PCEData data, ProxyAction srcPce) {
        super.setResultData(data);
        // Result data has been received: this PCE is now executed
        this.executed();
    }
    
    protected void sendPCECreate() throws OSCARSServiceException, MalformedURLException {

            OSCARSNetLogger.setTlogger(netLogger);
            PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                         this.getProxyEndpoint(),
                                                         this.getPceEndpoint());
            pceWorker.sendPceCreate (this);
    }

    protected void sendPCECreateCommit() throws OSCARSServiceException, MalformedURLException  {

            PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                         this.getProxyEndpoint(),
                                                         this.getPceEndpoint());
            pceWorker.sendPceCreateCommit (this);
    }

    protected void sendPCEModifyCommit() throws OSCARSServiceException, MalformedURLException  {

            PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                         this.getProxyEndpoint(),
                                                         this.getPceEndpoint());
            pceWorker.sendPceModifyCommit (this);
    }
 
    protected void sendPCECancel() throws OSCARSServiceException, MalformedURLException  {

        PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                     this.getProxyEndpoint(),
                                                     this.getPceEndpoint());
        pceWorker.sendPceCancel(this);
    }    
    
    protected void sendPCEModify() throws OSCARSServiceException, MalformedURLException  {

        PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                     this.getProxyEndpoint(),
                                                     this.getPceEndpoint());
        pceWorker.sendPceModify (this);
    }

    protected void sendPCEErrorResponse() {
        
    }
}

