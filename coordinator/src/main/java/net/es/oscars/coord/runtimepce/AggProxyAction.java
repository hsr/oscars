package net.es.oscars.coord.runtimepce;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.soap.gen.v06.PCEError;
import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.workers.PCEWorker;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;

/**
 * * AggProxyAction manages the pathTags and the sets of PCEData that need to be aggregated.
 * It also processes pceReplies
 * 
 * @author lomax
 *
 */
public class AggProxyAction extends ProxyAction  {

    private static final long serialVersionUID = 1439115737928915954L;
    private static final Logger LOG = Logger.getLogger(AggProxyAction.class.getName());
    private OSCARSNetLogger netLogger = null;

    private HashMap<String, PCEData> aggPceDataSet = new HashMap<String, PCEData>();
    private HashMap<String, Boolean> mandatoryTags = new HashMap<String, Boolean>();
    private int neededTags = 0;
    
    /**
     * Constructor - calls ProxyAction to register this action onto the PCERuntimeAction
     * 
     * @param parentPce ProxyAction for any parentPCE
     * @param coordRequest 
     * @param name String name of the Aggregator
     * @param pathTag
     * @param mandatoryTags
     * @param proxyEndpoint callback address to which PCE service sends PCEReply messages
     * @param pceEndpoint  address of PCE service to be called
     * @param transactionId - global transaction id for this IDC request

     * @param requestType  pceCreate, pceCreateCommit, pceModifyCommit, pceModify, pceCancel, aggregatorCreate
     */
    @SuppressWarnings("unchecked")
    public AggProxyAction (ProxyAction parentPce,
                           CoordRequest coordRequest,
                           PCERuntimeAction pceRuntime,
                           String name,
                           String pathTag,
                           List<String> mandatoryTags,
                           String proxyEndpoint,
                           String pceEndpoint,
                           String transactionId,
                           String requestType) {
        
        // Note that in the input data is not set when creating the object but is dynamically
        // stored at execution time.
        super (parentPce,
               coordRequest,
               pceRuntime,
               name,
               pathTag,
               ProxyAction.Role.AGGREGATOR,
               proxyEndpoint,
               pceEndpoint,
               transactionId,
               requestType);
        netLogger = new OSCARSNetLogger(ModuleName.PCERUNTIME,
                ((coordRequest != null) ? coordRequest.getTransactionId(): null));
        netLogger.setGRI((coordRequest != null) ? coordRequest.getGRI(): null); 
        for (String tag : mandatoryTags) {
            this.mandatoryTags.put(tag, new Boolean (false));
        }
        this.neededTags = this.mandatoryTags.size();
    }
    
 
    // An aggregator sends the request data to all its children PCE
    public void execute() {
        for (CoordAction<PCEData,PCEData> pce : this) {
            pce.setRequestData(this.getRequestData());
            // Aggregators trigger the execution of its children PCE's before they are
            // executed themselves.
            pce.process();
        }
    }  
    
    public void executed() {
        super.executed();
    }
    
    /**
     * if there is an aggregator (this.getAggregator) call agg.AddAggData 
     * otherwise call pceRuntimeAction.setResultData.
     */
    public void processReply (PCEData pceData) {
        String method = "AggProxyAction.processReply";
        LOG.debug(netLogger.start(method, this.getRequestType()));
        OSCARSNetLogger.setTlogger(netLogger);
        if ((this.getRequestType().equals(PCERequestTypes.PCE_CREATE)) ||
            (this.getRequestType().equals(PCERequestTypes.PCE_MODIFY)) ||
            (this.getRequestType().equals(PCERequestTypes.PCE_CANCEL))) {
            // The result goes to its closest aggregator or PCERuntime
            AggProxyAction agg = this.getAggregator();
            if (agg == null) {
                // There is no aggregator for this PCE. Send the result to the PCERuntimeAction.
                this.getPCERuntimeAction().setResultData (pceData, this);
            } else {
                agg.addAggData (pceData, this);
            }
     
            // This PCE is now executed
            this.executed();
            LOG.debug(netLogger.end(method));
        } else if (this.getRequestType().equals(PCERequestTypes.PCE_CREATE_COMMIT) ||
                   this.getRequestType().equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
            // The result goes to its closest aggregator or PCERuntime
            AggProxyAction agg = this.getAggregator();
            if (agg == null) {
                // There is no aggregator for this PCE. Send the result to the PCERuntimeAction.
                this.getPCERuntimeAction().setResultData (pceData, this);
            } else {
                agg.addAggData (pceData, this);
            }
            this.executed();
            LOG.debug(netLogger.end(method));
        }
    }

    protected void sendAggregatorCreate() throws OSCARSServiceException, MalformedURLException {

        PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                     this.getProxyEndpoint(),
                                                     this.getPceEndpoint());
        pceWorker.sendAggregatorCreate (this);

    }

    protected void sendCreateCommitQuery() throws OSCARSServiceException, MalformedURLException {

        PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                     this.getProxyEndpoint(),
                                                     this.getPceEndpoint());
        pceWorker.sendAggregatorCreateCommit(this);
    }


    protected void sendModifyCommitQuery() throws OSCARSServiceException, MalformedURLException {

        PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                     this.getProxyEndpoint(),
                                                     this.getPceEndpoint());
        pceWorker.sendAggregatorModifyCommit(this);
    }


    protected void sendCancelQuery() throws OSCARSServiceException, MalformedURLException {

        PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                     this.getProxyEndpoint(),
                                                     this.getPceEndpoint());
        pceWorker.sendAggregatorCancel(this);
    }

    protected void sendModifyQuery() throws OSCARSServiceException, MalformedURLException {

        PCEWorker pceWorker = PCEWorker.getPCEWorker(this.getName(),
                                                     this.getProxyEndpoint(),
                                                     this.getPceEndpoint());
    //    pceWorker.sendPceModify(this);
        pceWorker.sendAggregatorModify(this);
    }


    /**
     *   If all the required data has been received, send the pceQuery for this request to the Aggregator
     *   via private send* methods and PCEWorker.send messages.
     * @param data
     * @param srcPce
     */
    public void addAggData (PCEData data, ProxyAction srcPce) {
        OSCARSNetLogger.setTlogger(netLogger);
        String method = "AggProxyAction.addAggData";
        // Check if the source PCE is tagged
        String tag = srcPce.getPathTag();
        if (tag == null) {
            // No configured tag. Use source PCE name as tag
            tag = srcPce.getName();
        }

        synchronized (this.aggPceDataSet) {
            this.aggPceDataSet.put(tag, data);
        }
        
        synchronized (this.mandatoryTags) {
            // Check if this aggregator has already received data for this tag
            Boolean tagValue = this.mandatoryTags.get(tag);
            if ((tagValue != null) && (! tagValue.booleanValue())) {
                this.mandatoryTags.put(tag, new Boolean(true));
                // decrement the number of data this aggregator still needs to receive
                // Do not call sendAggregatorQuery from this block since it is synchronized
                --this.neededTags;
            }
        }
        try{
            if (this.neededTags == 0) {
                // All required data has been received.
                if (this.getRequestType().equals(PCERequestTypes.PCE_CREATE)) {
                    this.sendAggregatorCreate();
                } else if (this.getRequestType().equals(PCERequestTypes.PCE_CREATE_COMMIT)) {
                    this.sendCreateCommitQuery();
                } else if (this.getRequestType().equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
                    this.sendModifyCommitQuery();
                } else if (this.getRequestType().equals(PCERequestTypes.PCE_CANCEL)) {
                    this.sendCancelQuery();
                } else if (this.getRequestType().equals(PCERequestTypes.PCE_MODIFY)) {
                    this.sendModifyQuery();
                }
            }
        } catch (Exception ex){
          // wasn't able to send message
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, this.getName() + " request type= " +
                                    this.getRequestType() + " failed with " + ex.getClass().getName() + " " +
                                    ex.getMessage()));
            OSCARSFault of = new OSCARSFault();
            OSCARSFaultReport faultReport = new OSCARSFaultReport();
            faultReport.setDomainId(PathTools.getLocalDomainId());
            faultReport.setErrorType(ErrorReport.SYSTEM);
            faultReport.setErrorMsg(ex.getMessage());
            faultReport.setGri(this.getCoordRequest().getGRI());
            of.setErrorReport(faultReport);
            of.setMsg(this.getRequestType() + " failed with exception " + ex.getMessage());
            processErrorReply(of);
        }
    }
    
    public HashMap<String, PCEData> getAggData() {
        return this.aggPceDataSet;
    }
 
}

