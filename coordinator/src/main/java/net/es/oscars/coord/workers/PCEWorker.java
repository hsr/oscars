package net.es.oscars.coord.workers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.clients.PCEProxyClient;
import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.coord.runtimepce.ProxyAction;
import net.es.oscars.coord.runtimepce.AggProxyAction;
import net.es.oscars.coord.runtimepce.PCEData;
import net.es.oscars.coord.req.CoordRequest;

import net.es.oscars.common.soap.gen.MessagePropertiesType;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.soap.gen.v06.AggregatorCreateContent;
import net.es.oscars.pce.soap.gen.v06.AggregatorCreateCommitContent;
import net.es.oscars.pce.soap.gen.v06.AggregatorModifyCommitContent;
import net.es.oscars.pce.soap.gen.v06.AggregatorCancelContent;
import net.es.oscars.pce.soap.gen.v06.AggregatorModifyContent;
import net.es.oscars.pce.soap.gen.v06.PCECreateContent;
import net.es.oscars.pce.soap.gen.v06.PCECancelContent;
import net.es.oscars.pce.soap.gen.v06.PCECreateCommitContent;
import net.es.oscars.pce.soap.gen.v06.PCEModifyCommitContent;
import net.es.oscars.pce.soap.gen.v06.PCEModifyContent;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.pce.soap.gen.v06.TagDataContent;
import net.es.oscars.api.soap.gen.v06.InterDomainEventContent;

/**
 * A PCEWorker instance exists for each PCE service. An instance contains
 * the PCE name, endpoint address, the callback endpoint for PCE replies and a pceClient instance 
 * to access the PCE service. It is used by the PCE and Agg ProxyActions to send Create, Commit 
 * Cancel and Modify messages to the PCE.
 * 
 * @author lomax
 *
 */
public class PCEWorker extends ModuleWorker {

    private static Logger LOG = Logger.getLogger(PCEWorker.class.getName());
    // hashMap of a PCEworker instances for each PCE
    private static HashMap<String, PCEWorker> workers = new HashMap<String, PCEWorker>();

    private String         pceName       = null;
    private URL            proxyEndpoint = null;
    private URL            pceEndpoint   = null;
    private PCEProxyClient pceClient     = null;

    /**
     * Used by PCEAggProxyAction and PCEProxyAction to send queries and commits
     * 
     * @param name  name of the PCE
     * @param proxyEndpoint callback endpoint to which the PCE sends its reply
     * @param pceEndpoint  PCE service endpoint
     * @return  a PCEWorker instance for this PCE service
     * @throws MalformedURLException
     * @throws OSCARSServiceException
     */
    public static PCEWorker getPCEWorker(String name,
                                         String proxyEndpoint,
                                         String pceEndpoint)
                throws MalformedURLException, OSCARSServiceException {
        
        String index = name + "-" + pceEndpoint;
        PCEWorker worker = null;
        synchronized (PCEWorker.workers) {
            worker = workers.get(index);
            if (worker == null) {
                // Need to create a new instance.
                worker = new PCEWorker (name, proxyEndpoint, pceEndpoint);
                workers.put(index, worker);
            }
        }
        return worker;
    }
    /**
     *  Constructs a worker instance for the given PCE
     *  Called by PCEWorker.getPCEWorker so there is only one instance per PCE
     *  
     * @param name name of the PCE service
     * @param proxyEndpoint callback endpoint to which the PCE sends its reply
     * @param pceEndpoint  PCE service endpoint
     * @return new PCEWorker instance
     * @throws MalformedURLException
     * @throws OSCARSServiceException
     */
    private PCEWorker (String name,
                       String proxyEndpoint,
                       String pceEndpoint)
            throws MalformedURLException, OSCARSServiceException {
        
        this.pceEndpoint  = new URL(pceEndpoint);
        this.proxyEndpoint = new URL(proxyEndpoint);
        this.pceName = name;
        
        //  the following code can be disabled for debug purposes (so it does not actually try to connect to the PCE
        // System.out.println("PCEWorker: invoking the external PCE is disabled - debug mode.");
        this.reconnect();
    }
    
    /**
     * sends a PCECreate message
     * 
     * @param pce ProxyAction containing the parameters for the query message
     * @throws OSCARSServiceException
     */
    public void sendPceCreate (ProxyAction pce) throws OSCARSServiceException {
 
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "sendPCECreate";
        LOG.info (netLogger.start(event, pce.getName()));
        
        PCEData pceData = pce.getRequestData();
        
        if (pceData == null) {
            // No PCE Data. This should not happen.
            throw new OSCARSServiceException ("There is no PCEData associated with the request.");
        }
        // Build the PCECreate request
        PCEDataContent pceDataContent = new PCEDataContent();
        pceDataContent.setUserRequestConstraint (pceData.getUserRequestConstraint());
        pceDataContent.setReservedConstraint (pceData.getReservedConstraint());
        pceDataContent.setTopology(pceData.getTopology());
        if (pceData.getOptionalConstraint() != null) {
            // Optional constraints may not be defined.
            pceDataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
        }
        PCECreateContent queryContent = new PCECreateContent();
        ResCreateContent requestContent = (ResCreateContent) pce.getCoordRequest().getRequestData();
        MessagePropertiesType msgProps = requestContent.getMessageProperties();
        queryContent.setMessageProperties(msgProps);
        queryContent.setGlobalReservationId(pce.getCoordRequest().getGRI());
        queryContent.setPceName(pce.getName());
        queryContent.setCallBackEndpoint(pce.getProxyEndpoint());
        LOG.debug(netLogger.getMsg(event, "setting id to " + pce.getTransactionId()));
        queryContent.setId(pce.getTransactionId());
        queryContent.setPceData(pceDataContent);
        Object[] req = new Object[] {queryContent};
        this.getPCEProxyClient().invoke("PCECreate", req);
        LOG.info (netLogger.end(event, pce.getName()));
    }
 
    /**
     * sends a PCECancel message
     * 
     * @param pce ProxyAction containing the parameters for the query message
     * @throws OSCARSServiceException
     */
    public void sendPceCancel (ProxyAction pce) throws OSCARSServiceException {
 
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "sendPCECancel";
        LOG.info (netLogger.start(event,pce.getName()));
        
        PCEData pceData = pce.getRequestData();
        if (pceData == null) {
            // No PCE Data. This should not happen.
            throw new OSCARSServiceException ("no PCE data");
        }
        // Build the PCECreate request
        PCEDataContent pceDataContent = new PCEDataContent();
        pceDataContent.setUserRequestConstraint (pceData.getUserRequestConstraint());
        pceDataContent.setReservedConstraint (pceData.getReservedConstraint());
        pceDataContent.setTopology(pceData.getTopology());
        if (pceData.getOptionalConstraint() != null) {
            // Optional constraints may not be defined.
            pceDataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
        }
        PCECancelContent queryContent = new PCECancelContent();
        CancelResContent requestContent = (CancelResContent) pce.getCoordRequest().getRequestData();
        MessagePropertiesType msgProps = requestContent.getMessageProperties();
        queryContent.setMessageProperties(msgProps);
        queryContent.setGlobalReservationId(pce.getCoordRequest().getGRI());
        queryContent.setPceName(pce.getName());
        queryContent.setCallBackEndpoint(pce.getProxyEndpoint());
        queryContent.setId(pce.getTransactionId());
        queryContent.setPceData(pceDataContent);
        Object[] req = new Object[] {queryContent};
        this.getPCEProxyClient().invoke("PCECancel", req);
        LOG.info (netLogger.end(event, pce.getName()));
    }
    
    /**
     * sendAggregatorCreate -sends a query message to an aggregator
     * 
     * @param agg AggProxyAction containing the parameters for the query message
     * @throws OSCARSServiceException
     */
    public void sendAggregatorCreate (AggProxyAction agg) throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "sendAggregatorCreate";
        LOG.info (netLogger.start(event, agg.getName()));
        // Retrieve all the PCEData this aggregator needs to process
        HashMap<String, PCEData> aggData = agg.getAggData();
               
        // Build the AggregatorCreate request
        TagDataContent pceDataContent = new TagDataContent();
        Set<Map.Entry<String,PCEData>> dataSet = aggData.entrySet();
        
        ArrayList<TagDataContent> tagData = new ArrayList<TagDataContent>();
        
        for (Map.Entry<String, PCEData> dataEntry : dataSet) {
            TagDataContent data = new TagDataContent();
            data.setTag (dataEntry.getKey());
            PCEData pceData = dataEntry.getValue();
            
            PCEDataContent dataContent = new PCEDataContent();
            dataContent.setUserRequestConstraint(pceData.getUserRequestConstraint());
            dataContent.setReservedConstraint(pceData.getReservedConstraint());
            dataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
            dataContent.setTopology(pceData.getTopology());

            data.setConstraints(dataContent);
            
            tagData.add(data);
        }
        AggregatorCreateContent queryContent = new AggregatorCreateContent();
        ResCreateContent requestContent = (ResCreateContent) agg.getCoordRequest().getRequestData();
        MessagePropertiesType msgProps = requestContent.getMessageProperties();
        queryContent.setMessageProperties(msgProps);
        queryContent.setGlobalReservationId(agg.getCoordRequest().getGRI());
        queryContent.setPceName(agg.getName());
        queryContent.setCallBackEndpoint(agg.getProxyEndpoint());
        queryContent.setId(agg.getTransactionId());
        List<TagDataContent> tagDataContent = queryContent.getPceData();
        tagDataContent.addAll(tagData);
 
        Object[] req = new Object[] {queryContent};
        this.getPCEProxyClient().invoke("AggregatorCreate", req);
        LOG.info (netLogger.end(event, agg.getName()));
    }
    
    
    /**
     * sendAggregatorCreateCommit -sends a query message to an aggregator
     * 
     * @param agg AggProxyAction containing the parameters for the query message
     * @throws OSCARSServiceException
     */
    public void sendAggregatorCreateCommit (AggProxyAction agg) throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "sendPAggregatorCreateCommit";
        LOG.info (netLogger.start(event, agg.getName()));
        // Retrieve all the PCEData this aggregator needs to process
        HashMap<String, PCEData> aggData = agg.getAggData();
               
        // Build the AggregatorCreate request
        TagDataContent pceDataContent = new TagDataContent();
        Set<Map.Entry<String,PCEData>> dataSet = aggData.entrySet();
        
        ArrayList<TagDataContent> tagData = new ArrayList<TagDataContent>();
        
        for (Map.Entry<String, PCEData> dataEntry : dataSet) {
            TagDataContent data = new TagDataContent();
            data.setTag (dataEntry.getKey());
            PCEData pceData = dataEntry.getValue();
            
            PCEDataContent dataContent = new PCEDataContent();
            dataContent.setUserRequestConstraint(pceData.getUserRequestConstraint());
            dataContent.setReservedConstraint(pceData.getReservedConstraint());
            dataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
            dataContent.setTopology(pceData.getTopology());

            data.setConstraints(dataContent);
            
            tagData.add(data);
        }
        AggregatorCreateCommitContent queryContent = new AggregatorCreateCommitContent();
        
        MessagePropertiesType msgProps = null;
        Object requestData = (Object) agg.getCoordRequest().getRequestData();
        if (requestData instanceof ResCreateContent) {
            ResCreateContent requestContent = (ResCreateContent) requestData;
            msgProps = requestContent.getMessageProperties();
        } else if (requestData instanceof InterDomainEventContent) {
            InterDomainEventContent requestContent = (InterDomainEventContent) requestData;
            msgProps = requestContent.getMessageProperties();
        }       
        queryContent.setMessageProperties (msgProps);
        queryContent.setGlobalReservationId(agg.getCoordRequest().getGRI());
        queryContent.setPceName(agg.getName());
        queryContent.setCallBackEndpoint(agg.getProxyEndpoint());
        queryContent.setId(agg.getTransactionId());
        List<TagDataContent> tagDataContent = queryContent.getPceData();
        tagDataContent.addAll(tagData);
 
        Object[] req = new Object[] {queryContent};
        this.getPCEProxyClient().invoke("AggregatorCreateCommit", req);
        LOG.info (netLogger.end(event,agg.getName()));
    }

    /**
      * sendAggregatorCreate -sends a query message to an aggregator
      *
      * @param agg AggProxyAction containing the parameters for the query message
      * @throws OSCARSServiceException
      */
     public void sendAggregatorModify (AggProxyAction agg) throws OSCARSServiceException {
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         String event = "sendAggregatorModify";
         LOG.info (netLogger.start(event, agg.getName()));
         // Retrieve all the PCEData this aggregator needs to process
         HashMap<String, PCEData> aggData = agg.getAggData();

         // Build the AggregatorCreate request
         TagDataContent pceDataContent = new TagDataContent();
         Set<Map.Entry<String,PCEData>> dataSet = aggData.entrySet();

         ArrayList<TagDataContent> tagData = new ArrayList<TagDataContent>();

         for (Map.Entry<String, PCEData> dataEntry : dataSet) {
             TagDataContent data = new TagDataContent();
             data.setTag (dataEntry.getKey());
             PCEData pceData = dataEntry.getValue();

             PCEDataContent dataContent = new PCEDataContent();
             dataContent.setUserRequestConstraint(pceData.getUserRequestConstraint());
             dataContent.setReservedConstraint(pceData.getReservedConstraint());
             dataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
             dataContent.setTopology(pceData.getTopology());

             data.setConstraints(dataContent);

             tagData.add(data);
         }
         AggregatorModifyContent queryContent = new AggregatorModifyContent();
         ModifyResContent requestContent = (ModifyResContent) agg.getCoordRequest().getRequestData();
         MessagePropertiesType msgProps = requestContent.getMessageProperties();
         queryContent.setMessageProperties(msgProps);
         queryContent.setGlobalReservationId(agg.getCoordRequest().getGRI());
         queryContent.setPceName(agg.getName());
         queryContent.setCallBackEndpoint(agg.getProxyEndpoint());
         queryContent.setId(agg.getTransactionId());
         List<TagDataContent> tagDataContent = queryContent.getPceData();
         tagDataContent.addAll(tagData);

         Object[] req = new Object[] {queryContent};
         this.getPCEProxyClient().invoke("AggregatorModify", req);
         LOG.info (netLogger.end(event, agg.getName()));
     }

    /**
      * sendAggregatorModifyCommit -sends a query message to an aggregator
      *
      * @param agg AggProxyAction containing the parameters for the query message
      * @throws OSCARSServiceException
      */
     public void sendAggregatorModifyCommit (AggProxyAction agg) throws OSCARSServiceException {
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         String event = "sendPAggregatorModifyCommit";
         LOG.info (netLogger.start(event, agg.getName()));
         // Retrieve all the PCEData this aggregator needs to process
         HashMap<String, PCEData> aggData = agg.getAggData();

         // Build the AggregatorCreate request
         TagDataContent pceDataContent = new TagDataContent();
         Set<Map.Entry<String,PCEData>> dataSet = aggData.entrySet();

         ArrayList<TagDataContent> tagData = new ArrayList<TagDataContent>();

         for (Map.Entry<String, PCEData> dataEntry : dataSet) {
             TagDataContent data = new TagDataContent();
             data.setTag (dataEntry.getKey());
             PCEData pceData = dataEntry.getValue();

             PCEDataContent dataContent = new PCEDataContent();
             dataContent.setUserRequestConstraint(pceData.getUserRequestConstraint());
             dataContent.setReservedConstraint(pceData.getReservedConstraint());
             dataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
             dataContent.setTopology(pceData.getTopology());

             data.setConstraints(dataContent);

             tagData.add(data);
         }
         AggregatorModifyCommitContent queryContent = new AggregatorModifyCommitContent();

         MessagePropertiesType msgProps = null;
         Object requestData = (Object) agg.getCoordRequest().getRequestData();
         if (requestData instanceof ModifyResContent) {
             ModifyResContent requestContent = (ModifyResContent) requestData;
             msgProps = requestContent.getMessageProperties();
         } else if (requestData instanceof InterDomainEventContent) {
             InterDomainEventContent requestContent = (InterDomainEventContent) requestData;
             msgProps = requestContent.getMessageProperties();
         }
         queryContent.setMessageProperties (msgProps);
         queryContent.setGlobalReservationId(agg.getCoordRequest().getGRI());
         queryContent.setPceName(agg.getName());
         queryContent.setCallBackEndpoint(agg.getProxyEndpoint());
         queryContent.setId(agg.getTransactionId());
         List<TagDataContent> tagDataContent = queryContent.getPceData();
         tagDataContent.addAll(tagData);

         Object[] req = new Object[] {queryContent};
         this.getPCEProxyClient().invoke("AggregatorModifyCommit", req);
         LOG.info (netLogger.end(event,agg.getName()));
     }

    /**
     * sendAggregatorCancel -sends a cancel message to an aggregator
     * 
     * @param agg AggProxyAction containing the parameters for the query message
     * @throws OSCARSServiceException
     */
    public void sendAggregatorCancel (AggProxyAction agg) throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "sendPAggregatorCancel";
        LOG.info (netLogger.start(event, agg.getName()));
        // Retrieve all the PCEData this aggregator needs to process
        HashMap<String, PCEData> aggData = agg.getAggData();
               
        // Build the AggregatorCreate request
        TagDataContent pceDataContent = new TagDataContent();
        Set<Map.Entry<String,PCEData>> dataSet = aggData.entrySet();
        
        ArrayList<TagDataContent> tagData = new ArrayList<TagDataContent>();
        
        for (Map.Entry<String, PCEData> dataEntry : dataSet) {
            TagDataContent data = new TagDataContent();
            data.setTag (dataEntry.getKey());
            PCEData pceData = dataEntry.getValue();
            
            PCEDataContent dataContent = new PCEDataContent();
            dataContent.setUserRequestConstraint(pceData.getUserRequestConstraint());
            dataContent.setReservedConstraint(pceData.getReservedConstraint());
            dataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
            dataContent.setTopology(pceData.getTopology());

            data.setConstraints(dataContent);
            
            tagData.add(data);
        }
        AggregatorCancelContent queryContent = new AggregatorCancelContent();
        CancelResContent requestContent = (CancelResContent) agg.getCoordRequest().getRequestData();
        MessagePropertiesType msgProps = requestContent.getMessageProperties();
        queryContent.setMessageProperties(msgProps);
        queryContent.setGlobalReservationId(agg.getCoordRequest().getGRI());
        queryContent.setPceName(agg.getName());
        queryContent.setCallBackEndpoint(agg.getProxyEndpoint());
        queryContent.setId(agg.getTransactionId());
        List<TagDataContent> tagDataContent = queryContent.getPceData();
        tagDataContent.addAll(tagData);
 
        Object[] req = new Object[] {queryContent};
        this.getPCEProxyClient().invoke("AggregatorCancel", req);
        LOG.info (netLogger.end(event, agg.getName()));
    }
    
    /**
     * sendPceCreateCommit - sends a message to a PCE that it should commit the resources
     * 
     * @param pce ProxyAction containing the parameters for the commit message
     * @throws OSCARSServiceException
     */
    public void sendPceCreateCommit (ProxyAction pce) throws OSCARSServiceException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "sendPCECreateCommit";
        LOG.info (netLogger.start(event, pce.getName()));
        PCEData pceData = pce.getRequestData();
        if (pceData == null) {
            // No PCE Data. This should not happen.
            LOG.warn(netLogger.error(event,ErrSev.MINOR, "no PCE data in pceCreateCommit to " + pce.getName()));
            throw new OSCARSServiceException ("no PCE data");
        }
        CoordRequest coordRequest = pce.getCoordRequest();
        // Build the PCECreateCommit request
        PCEDataContent pceDataContent = new PCEDataContent();
        pceDataContent.setUserRequestConstraint (pceData.getUserRequestConstraint());
        pceDataContent.setReservedConstraint (pceData.getReservedConstraint());
        pceDataContent.setTopology(pceData.getTopology());
        if (pceData.getOptionalConstraint() != null) {
            // Optional constraints may not be defined.
            pceDataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
        }

        Object requestData = (Object) coordRequest.getRequestData();
        MessagePropertiesType msgProps = null;
        List<TagDataContent> tagDataContent = null;

        PCECreateCommitContent queryContent = new PCECreateCommitContent();
        queryContent.setGlobalReservationId(pce.getCoordRequest().getGRI());
        queryContent.setPceName(pce.getName());
        queryContent.setCallBackEndpoint(pce.getProxyEndpoint());
        queryContent.setId(pce.getTransactionId());
        queryContent.setPceData(pceDataContent);
        if (requestData instanceof ResCreateContent) {
                ResCreateContent requestContent = (ResCreateContent) requestData;
                msgProps = requestContent.getMessageProperties();
        } else if (requestData instanceof InterDomainEventContent) {
                InterDomainEventContent requestContent = (InterDomainEventContent) requestData;
                msgProps = requestContent.getMessageProperties();
        }
        queryContent.setMessageProperties(msgProps);
        Object[] req = new Object[] {queryContent};
        this.getPCEProxyClient().invoke("PCECreateCommit", req);
        LOG.info (netLogger.end(event, pce.getName()));
    }

       /**
     * sendPceModifyCommit - sends a message to a PCE that it should commit the resources
     *
     * @param pce ProxyAction containing the parameters for the commit message
     * @throws OSCARSServiceException
     */
    public void sendPceModifyCommit (ProxyAction pce) throws OSCARSServiceException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "sendPCEModifyCommit";
        LOG.info (netLogger.start(event, pce.getName()));
        PCEData pceData = pce.getRequestData();
        if (pceData == null) {
            // No PCE Data. This should not happen.
            LOG.warn(netLogger.error(event,ErrSev.MINOR, "no PCE data in pceModifyCommit to " + pce.getName()));
            throw new OSCARSServiceException ("no PCE data");
        }

        // Build the PCEModifyCommit request
        PCEDataContent pceDataContent = new PCEDataContent();
        pceDataContent.setUserRequestConstraint (pceData.getUserRequestConstraint());
        pceDataContent.setReservedConstraint (pceData.getReservedConstraint());
        pceDataContent.setTopology(pceData.getTopology());
        if (pceData.getOptionalConstraint() != null) {
            // Optional constraints may not be defined.
            pceDataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
        }

        MessagePropertiesType msgProps = null;
        Object requestData = (Object) pce.getCoordRequest().getRequestData();
        if (requestData instanceof ModifyResContent) {
             ModifyResContent requestContent = (ModifyResContent) requestData;
             msgProps = requestContent.getMessageProperties();
        } else if (requestData instanceof InterDomainEventContent) {
             InterDomainEventContent requestContent = (InterDomainEventContent) requestData;
             msgProps = requestContent.getMessageProperties();
        }
        List<TagDataContent> tagDataContent = null;

        PCEModifyCommitContent queryContent = new PCEModifyCommitContent();
        queryContent.setGlobalReservationId(pce.getCoordRequest().getGRI());
        queryContent.setPceName(pce.getName());
        queryContent.setCallBackEndpoint(pce.getProxyEndpoint());
        queryContent.setId(pce.getTransactionId());
        queryContent.setPceData(pceDataContent);
        if (requestData instanceof ResCreateContent) {
                ResCreateContent requestContent = (ResCreateContent) requestData;
                msgProps = requestContent.getMessageProperties();
        } else if (requestData instanceof InterDomainEventContent) {
                InterDomainEventContent requestContent = (InterDomainEventContent) requestData;
                msgProps = requestContent.getMessageProperties();
        }
        queryContent.setMessageProperties(msgProps);
        Object[] req = new Object[] {queryContent};
        this.getPCEProxyClient().invoke("PCEModifyCommit", req);
        LOG.info (netLogger.end(event, pce.getName()));
    }
    /**
     * sendPceModify - sends a message to a PCE that it should modify the resources
     * 
     * @param pce ProxyAction containing the parameters for the modify message
     * @throws OSCARSServiceException
     */
    public void sendPceModify (ProxyAction pce) throws OSCARSServiceException {
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "sendPCEModify";
        LOG.info (netLogger.start(event, pce.getName()));
        PCEData pceData = pce.getRequestData();
        if (pceData == null) {
            // No PCE Data. This should not happen.
            LOG.warn(netLogger.error(event,ErrSev.MINOR,"no PCE data in pceModify to " + pce.getName()));
            throw new OSCARSServiceException ("no PCE data");
        }
        // Build the PCEModify request
        PCEDataContent pceDataContent = new PCEDataContent();
        pceDataContent.setUserRequestConstraint (pceData.getUserRequestConstraint());
        pceDataContent.setReservedConstraint (pceData.getReservedConstraint());
        pceDataContent.setTopology(pceData.getTopology());
        if (pceData.getOptionalConstraint() != null) {
            // Optional constraints may not be defined.
            pceDataContent.getOptionalConstraint().addAll(pceData.getOptionalConstraint());
        }
        PCEModifyContent queryContent = new PCEModifyContent();
        ModifyResContent requestContent = (ModifyResContent) pce.getCoordRequest().getRequestData();
        MessagePropertiesType msgProps = requestContent.getMessageProperties();
        queryContent.setMessageProperties(msgProps);
        queryContent.setGlobalReservationId(pce.getCoordRequest().getGRI());
        queryContent.setPceName(pce.getName());
        queryContent.setCallBackEndpoint(pce.getProxyEndpoint());
        queryContent.setId(pce.getTransactionId());
        queryContent.setPceData(pceDataContent);
        Object[] req = new Object[] {queryContent};
        this.getPCEProxyClient().invoke("PCEModify", req);
        LOG.info (netLogger.end(event, pce.getName()));
    }
 
    /**
     * getPCEProxyClient
     * @return PCEProxyClient for this worker instance
     */
    public PCEProxyClient getPCEProxyClient() {
        return this.pceClient;
    }

    /**
     * reconnect  Instantiates a PCE client to the pceEndpoint in this worker
     *  saves the client as this.pceClient
     */
    public void reconnect() throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "PCEWorker.reconnect";
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
            URL pceWsdl = cc.getWSDLPath(ServiceNames.SVC_PCE,null);
            LOG.debug (netLogger.start(event,"PCE Module host= " + this.pceEndpoint + " WSDL= " + pceWsdl.toString()));
            this.pceClient = PCEProxyClient.getClient(this.pceEndpoint,pceWsdl);
        } catch (Exception e) {
            LOG.warn(netLogger.error(event, ErrSev.MAJOR, this.pceEndpoint + ": " + e.toString()));
            throw new OSCARSServiceException (e);
        }
        LOG.debug(netLogger.end(event));
    }
}
