package net.es.oscars.api.http;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import org.apache.log4j.Logger;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

import java.util.*;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.security.Principal;
import java.security.cert.X509Certificate;

import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.authN.soap.gen.DNType;
import net.es.oscars.authN.soap.gen.VerifyDNReqType;
import net.es.oscars.authN.soap.gen.VerifyReply;
import net.es.oscars.api.common.OSCARSIDC;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSFaultUtils;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.validator.DataValidator;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

/**
 * Implementation class for the OSCARService V.06
 * Receives all incoming messages. If MessageProperties in the request message
 * is null, it is assumed that the message is from a user and not a peerIDC. In this
 * case, this class will assign a globalTransactionId, and will set the originator
 * to the loginId of the user that signed the request. This information will be added
 * to the request and then passed to other services and any subsequent IDCs.
 * 
 * @author Eric Pouyoul, Mary Thompson
 *
 */
@OSCARSNetLoggerize(moduleName = ModuleName.API)
@javax.jws.WebService(
        serviceName = ServiceNames.SVC_API,
        portName = "OSCARS",
        targetNamespace = "http://oscars.es.net/OSCARS/06",
        endpointInterface = "net.es.oscars.api.soap.gen.v06.OSCARS")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class OSCARSSoapHandler06 implements OSCARS {

    @javax.annotation.Resource
    private WebServiceContext myContext;
    // Implements requests
    private static final Logger LOG = Logger.getLogger(OSCARSSoapHandler06.class.getName());
    // private OSCARSNetLogger netLogger;
    private static final String moduleName = ModuleName.API;

    /**
     * 
     * @param createReservation content of the reservation to create
     * @return CreateReply contains the GRI that has been assigned to the reservation, probably
     *          a status of "RECEIVED" or "ACCEPTED" 
     * @throws OSCARSFaultMessage
     */
    public CreateReply createReservation(ResCreateContent createReservation) throws OSCARSFaultMessage {
        return OSCARSSoapHandler06.createReservation(createReservation, this.myContext);
    }

    public static CreateReply createReservation(ResCreateContent createReservation, WebServiceContext context) throws OSCARSFaultMessage {
        String event = "createReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = createReservation.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);
        
        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        netLogger.setGRI(createReservation.getGlobalReservationId());
        LOG.info(netLogger.start(event));
        CreateReply response = null;
        try {
            DataValidator.validate(createReservation, false);
            
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties(msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            createReservation.setMessageProperties(msgProps);
            
            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            // Build the query
            Object[] req = new Object[]{subjectAttributes, createReservation};
            Object[] res = coordClient.invoke("createReservation",req);
            if ((res == null) || (res.length == 0)) {
                OSCARSFaultUtils.handleError (new OSCARSServiceException("No response to createReservation"), true, null, LOG, event);
            }
            response = (CreateReply) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event,response.getGlobalReservationId()));
        return response;
    }

    /**
     * 
     * @param modifyReservation  contains GRI, description and all the reservation Constraints
     * @return the ResDetails for the modified reservation
     * @throws OSCARSFaultMessage
     */
    public ModifyResReply modifyReservation(ModifyResContent modifyReservation) throws OSCARSFaultMessage    {
        return OSCARSSoapHandler06.modifyReservation(modifyReservation,this.myContext);
    }

    public static ModifyResReply modifyReservation(ModifyResContent modifyReservation, WebServiceContext context) throws OSCARSFaultMessage    {
        
        String event = "OSCARSSoapHandler06.modifyReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = modifyReservation.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);

        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        netLogger.setGRI(modifyReservation.getGlobalReservationId());
        LOG.info(netLogger.start(event));
        ModifyResReply response = null;
        try {
            // don't do data validation at this point, since the UserConstraint is allowed to
            // have no path information. It will be filled in by the Coordinator..
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties(msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            modifyReservation.setMessageProperties(msgProps);
               
            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            // Build the query
            Object[] req = new Object[]{subjectAttributes, modifyReservation};
            Object[] res = coordClient.invoke("modifyReservation",req);
            response = (ModifyResReply) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
            
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return response;
    }

    /**
     * 
     * @param queryReservation contains GRI of reservation to query
     * @return QueryResReply- details of the reservation
     * @throws OSCARSFaultMessage
     */
    public QueryResReply queryReservation(QueryResContent queryReservation) throws OSCARSFaultMessage    {
        return OSCARSSoapHandler06.queryReservation(queryReservation,this.myContext);
    }
    public static QueryResReply queryReservation(QueryResContent queryReservation, WebServiceContext context) throws OSCARSFaultMessage    {
        String event = "OSCARSSoapHandler06.queryReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = queryReservation.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);

        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        netLogger.setGRI(queryReservation.getGlobalReservationId());
        LOG.info(netLogger.start(event));
        QueryResReply response = null;

        try {
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            List<AttributeType> attrs = subjectAttributes.getSubjectAttribute();
            //LOG.debug("QueryRes: number of attributes returned: " + attrs.size());
            msgProps = OSCARSSoapHandler06.updateMessageProperties(msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            queryReservation.setMessageProperties(msgProps);
               
            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            // Build the query
            Object[] req = new Object[]{subjectAttributes, queryReservation};
            Object[] res = coordClient.invoke("queryReservation",req);
            response = (QueryResReply) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return response;
    }

    /**
     *  Gets any error reports for a specified transaction
     *
     * @param getErrorReportReq  contains the transactionId for which errors are wanted.
     * @return   contains any error reports for the transaction that was input
     * @throws OSCARSFaultMessage
     */
    public GetErrorReportResponseContent getErrorReport(GetErrorReportContent getErrorReportReq) throws OSCARSFaultMessage    {
            return OSCARSSoapHandler06.getErrorReport(getErrorReportReq,this.myContext);
        }
        public static GetErrorReportResponseContent getErrorReport(GetErrorReportContent getErrorReportReq,
                                                                  WebServiceContext context) throws OSCARSFaultMessage    {
            String event = "OSCARSSoapHandler06.getErrprReport";
            OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
            MessagePropertiesType msgProps = getErrorReportReq.getMessageProperties();
            msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);

            netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
            LOG.info(netLogger.start(event, "for the transaction: " + getErrorReportReq.getTransactionId()));
            GetErrorReportResponseContent response = null;

            try {
                SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
                List<AttributeType> attrs = subjectAttributes.getSubjectAttribute();
                //LOG.debug("GetErrorReport: number of attributes returned: " + attrs.size());
                msgProps = OSCARSSoapHandler06.updateMessageProperties(msgProps, event, subjectAttributes, netLogger);
                // update messageProperties in case it has been modified
                getErrorReportReq.setMessageProperties(msgProps);

                CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
                // Build the query
                Object[] req = new Object[]{subjectAttributes, getErrorReportReq};
                Object[] res = coordClient.invoke("getErrorReport",req);
                response = (GetErrorReportResponseContent) res[0];
                // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
            } catch (OSCARSServiceException ex) {
                OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
            } catch (Exception ex) {
                OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
            }
            LOG.info(netLogger.end(event));
            return response;
        }

    /**
     * 
     * @param cancelReservation contains GRI of the reservation to cancel
     * @return CancelResReply contains status of the reservation
     * @throws OSCARSFaultMessage
     */
    public CancelResReply cancelReservation(CancelResContent cancelReservation) throws OSCARSFaultMessage    {
        return OSCARSSoapHandler06.cancelReservation(cancelReservation, this.myContext);
    }
    public static CancelResReply cancelReservation(CancelResContent cancelReservation, WebServiceContext context) throws OSCARSFaultMessage    {
        String event = "OSCARSSoapHandler06.cancelReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = cancelReservation.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);

        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        netLogger.setGRI(cancelReservation.getGlobalReservationId());
        LOG.info(netLogger.start(event, "reservation:" + cancelReservation.getGlobalReservationId()));
        CancelResReply response = null;
        try {
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties(msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            cancelReservation.setMessageProperties(msgProps);

            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            // Build the query
            Object[] req = new Object[]{subjectAttributes, cancelReservation};
            Object[] res = coordClient.invoke("cancelReservation",req);
            response = (CancelResReply) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return response;
    }

    /**
     * 
     * @param listReservations contains parameters of reservations to list
     *      may include, startTime, endTime, status, linkId, vlan, description
     *      number of reservations to be returned, and the offset to start at.
     * @return list of Reservations  there will be a ResDetails structure for each reservation
     * @throws OSCARSFaultMessage
     */
    public ListReply listReservations(ListRequest listReservations) throws OSCARSFaultMessage {
        return OSCARSSoapHandler06.listReservations(listReservations, this.myContext);
    }
    public static ListReply listReservations(ListRequest listReservations, WebServiceContext context) throws OSCARSFaultMessage {
        String event = "OSCARSSoapHandler06.listReservations";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = listReservations.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);
        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        LOG.info(netLogger.start(event));
        ListReply response = null;
        try {
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties(msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            listReservations.setMessageProperties(msgProps);
            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();

            // Build the query
            Object[] req = new Object[]{subjectAttributes, listReservations};
            Object[] res = coordClient.invoke("listReservations",req);
            response = (ListReply) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return response;
    }
    
    /**
     * 
     * @param createPath contains GRI and optionally the token of the path to be setup
     * @return CreatePathResponseContent  contain status and GRI
     * @throws OSCARSFaultMessage
     */
    public CreatePathResponseContent createPath(CreatePathContent createPath) throws OSCARSFaultMessage {
       return OSCARSSoapHandler06.createPath(createPath, this.myContext);
    }
    public static CreatePathResponseContent createPath(CreatePathContent createPath, WebServiceContext context) throws OSCARSFaultMessage {
        String event = "OSCARSSoapHandler06.createPath";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = createPath.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);
        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        netLogger.setGRI(createPath.getGlobalReservationId());
        LOG.info(netLogger.start(event));
        CreatePathResponseContent response = null;
        
        try {
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties(msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            createPath.setMessageProperties(msgProps);

            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            // Build the query 
            Object[] req = new Object[]{subjectAttributes, createPath, null};
            Object[] res = coordClient.invoke("createPath",req);
            if (res[0] == null) {
                LOG.error("coordinator returned an empty message");
                throw new OSCARSServiceException("unexpected empty message from coordinator");
            }
            response = (CreatePathResponseContent) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return response;
    }

    /**
     * 
     * @param refreshPath GRI and optionally token for the reservation 
     * @return RefreshPathResponseContent containing the GRI and status of the reservation
     * @throws OSCARSFaultMessage
     */
    public RefreshPathResponseContent refreshPath(RefreshPathContent refreshPath) throws OSCARSFaultMessage {
        return OSCARSSoapHandler06.refreshPath(refreshPath, this.myContext);
    }
    public static RefreshPathResponseContent refreshPath(RefreshPathContent refreshPath, WebServiceContext context) throws OSCARSFaultMessage {
        String event = "OSCARSSoapHandler06.refreshPath";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = refreshPath.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);
        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        netLogger.setGRI(refreshPath.getGlobalReservationId());
        LOG.info(netLogger.start(event));
        RefreshPathResponseContent response = null;
        try {
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            refreshPath.setMessageProperties(msgProps);

            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            // Build the query
            Object[] req = new Object[]{subjectAttributes, refreshPath};
            Object[] res = coordClient.invoke("refreshPath",req);
            response = (RefreshPathResponseContent) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return response;
    }

    /**
     * 
     * @param teardownPath TearDownPathContent contains GRI and optionally token for the reservation to be cancelled
     * @return the TearDownPahtContent
     * @throws OSCARSFaultMessage
     */
    public TeardownPathResponseContent teardownPath(TeardownPathContent teardownPath) throws OSCARSFaultMessage {
        return OSCARSSoapHandler06.teardownPath(teardownPath, this.myContext);
    }
    public static TeardownPathResponseContent teardownPath(TeardownPathContent teardownPath, WebServiceContext context) throws OSCARSFaultMessage {
        String event = "OSCARSSoapHandler06.teardownPath";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = teardownPath.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null,netLogger);
        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        netLogger.setGRI(teardownPath.getGlobalReservationId());
        LOG.info(netLogger.start(event));
        TeardownPathResponseContent response = null;
        try {
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            teardownPath.setMessageProperties(msgProps);

            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            // Build the query
            Object[] req = new Object[]{subjectAttributes, teardownPath, null};
            Object[] res = coordClient.invoke("teardownPath",req);
            response = (TeardownPathResponseContent) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return response;
    }

    /**
     * Accept notification of an event from a peer IDC
     * 
     * @param eventContent
     * @return void
     */
    public void interDomainEvent(InterDomainEventContent eventContent) {
        OSCARSSoapHandler06.interDomainEvent(eventContent, this.myContext);
    }
    public static void interDomainEvent(InterDomainEventContent eventContent, WebServiceContext context) {
        String event = "OSCARSSoapHandler06.interDomainEvent";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = eventContent.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);
        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        String gri = null;
        if (eventContent.getResDetails() !=  null) {
            gri = eventContent.getResDetails().getGlobalReservationId();
            netLogger.setGRI(gri);
        }
        HashMap<String,Principal> principals = getSecurityPrincipals(context,netLogger);
        // IDCs should use a DN that contains  their domain/host name.
        String reqDN = principals.get("subject").getName();
        LOG.info(netLogger.start(event, "received " + eventContent.getType() + " for reservation " +
                                 gri + " from " + reqDN));
        try {
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            eventContent.setMessageProperties(msgProps);
            
            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            Object[] req = new Object[]{subjectAttributes,eventContent};
            coordClient.invoke("interDomainEvent",req);
        } catch (OSCARSServiceException ex) {
            LOG.error(netLogger.error(event, ErrSev.MAJOR,
                                           "interDomainEvent coordinator failed " + ex.getMessage()));
        } catch (Exception ex) {
            LOG.error(netLogger.error(event, ErrSev.MAJOR,
                                           "interDomainEvent coordinator caused exception " + ex.toString()));
            ex.printStackTrace();
        }
        LOG.info(netLogger.end(event, eventContent.getType()));

    }

  
   /**
    * 
    * @param getNetworkTopology
    * @return
    * @throws OSCARSFaultMessage
    */
    public GetTopologyResponseContent getNetworkTopology(GetTopologyContent getNetworkTopology)
            throws OSCARSFaultMessage    {
        return OSCARSSoapHandler06.getNetworkTopology(getNetworkTopology, this.myContext);
    }

    public static GetTopologyResponseContent getNetworkTopology(GetTopologyContent getNetworkTopology, WebServiceContext context)
            throws OSCARSFaultMessage    {

        String event = "OSCARSSoapHandler06.getNetworkTopology";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = getNetworkTopology.getMessageProperties();
        msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, null, netLogger);
        netLogger.init(OSCARSSoapHandler06.moduleName, msgProps.getGlobalTransactionId());
        LOG.info(netLogger.start(event));
        GetTopologyResponseContent response = null;
        try {
            SubjectAttributes subjectAttributes = OSCARSSoapHandler06.AuthNRequester(msgProps, context, netLogger);
            msgProps = OSCARSSoapHandler06.updateMessageProperties (msgProps, event, subjectAttributes, netLogger);
            // update messageProperties in case it has been modified
            getNetworkTopology.setMessageProperties(msgProps);

            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            // Build the query
            Object[] req = new Object[]{subjectAttributes, getNetworkTopology};
            Object[] res = coordClient.invoke("getNetTopology",req);
            response = (GetTopologyResponseContent) res[0];
            // be sure that reply from api includes the transactionId.
            if (response.getMessageProperties() == null ){
                response.setMessageProperties(msgProps);
            }
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError( ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return response;
    }

    private static HashMap<String, Principal> getSecurityPrincipals(WebServiceContext context, OSCARSNetLogger netLogger) {
        String event = "getSecurityPrincipals";
        //LOG.debug(netLogger.start(event));
        HashMap<String, Principal> result = new HashMap<String, Principal>();

        try {
            MessageContext inContext = (MessageContext) context.getMessageContext();
            if (inContext == null) {
                LOG.error(netLogger.error(event,ErrSev.MAJOR, "message context is NULL"));
                return null;
            }
            Vector results = (Vector) inContext.get(WSHandlerConstants.RECV_RESULTS);
            for (int i = 0; results != null && i < results.size(); i++) {
                WSHandlerResult hResult = (WSHandlerResult) results.get(i);
                Vector hResults = hResult.getResults();
                //LOG.debug(netLogger.getMsg(event,"handler results size is " + hResults.size()));
                for (int j = 0; j < hResults.size(); j++) {
                    WSSecurityEngineResult eResult = (WSSecurityEngineResult) hResults.get(j);
                    // A timestamp action does not have an
                    // associated principal. Only Signature and UsernameToken
                    // actions return a principal.
                    //LOG.debug("TAG_ACTION is " + ((java.lang.Integer) eResult.get(
                    //       WSSecurityEngineResult.TAG_ACTION)).intValue());
                    if ((((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.SIGN)) {
                        Principal subjectDN = ((X509Certificate) eResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getSubjectDN();
                        Principal issuerDN = ((X509Certificate) eResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getIssuerDN();
                        result.put("subject", subjectDN);
                        result.put("issuer", issuerDN);
                    }
                    else if ((((java.lang.Integer) eResult.get(
                                WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.UT)) {
                        Principal subjectName = (Principal) eResult.get(
                                WSSecurityEngineResult.TAG_PRINCIPAL);
                        LOG.debug(netLogger.getMsg(event,"getSecurityPrincipals.getSecurityInfo, " +
                                "Principal's name from UserToken: " + subjectName));
                        result.put("userTokenName", subjectName);
                    } /*else if (((java.lang.Integer) eResult.get(
                                WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.TS) {
                        Timestamp TS =(Timestamp)eResult.get(WSSecurityEngineResult.TAG_TIMESTAMP);
                        LOG.debug("Timestamp created: " + TS.getCreated());
                        LOG.debug("Timestamp expires: " + TS.getExpires());
                     } */
                }
            }
        } catch (Exception e) {
            LOG.error(netLogger.error(event,ErrSev.MAJOR,
                                    "caught.exception: " + e.toString()));
            e.printStackTrace();
            return null;
        }
        //LOG.debug(netLogger.end(event));
        return result;
    }
    /**
     * AuthNRequester gets the DN from the message and calls the AuthN server to get 
     * the user's attributes if any.
     * 
     * @return a list of the users attributes of the entity that signed the request.
     *    if the user is registered, login_id and institution attributes will be returned.
     * @throws OSCARSServiceException if there are no attributes
     */

      private static SubjectAttributes AuthNRequester (MessagePropertiesType msgProps,
                                                       WebServiceContext context,
                                                       OSCARSNetLogger netLogger)
                    throws OSCARSServiceException {

          String event = "AuthNRequester";
          String userDN = null;
          String issuerDN= null;
          try {
              HashMap<String, Principal> principals = OSCARSSoapHandler06.getSecurityPrincipals(context, netLogger);
              userDN = principals.get("subject").getName();
              issuerDN = principals.get("issuer").getName();
              LOG.info(netLogger.getMsg(event,"subject: "+ userDN));
              LOG.debug(netLogger.getMsg(event,"issuer: " +  issuerDN));
          } catch (Exception ex ){
              LOG.error(netLogger.error(event,ErrSev.MAJOR, "caught Exception: " + ex.toString()));
              throw new OSCARSServiceException(ex);
          }
          AuthNClient authNClient = OSCARSIDC.getInstance().getAuthNClient();
          VerifyDNReqType verifyDNReq = new VerifyDNReqType();
          DNType DN = new DNType();
          DN.setSubjectDN(userDN);
          DN.setIssuerDN(issuerDN);
          verifyDNReq.setDN(DN);
          verifyDNReq.setTransactionId(msgProps.getGlobalTransactionId());
          Object[] req = new Object[]{verifyDNReq};
          Object[] res = authNClient.invoke("verifyDN",req);
          VerifyReply reply = (VerifyReply)res[0];
          SubjectAttributes subjectAttrs = reply.getSubjectAttributes();
          if (subjectAttrs == null || subjectAttrs.getSubjectAttribute().isEmpty()){
              ErrorReport errRep = new ErrorReport (ErrorCodes.ACCESS_DENIED,
                                                  "no atributes for user " + userDN,
                                                  ErrorReport.USER);
              throw new OSCARSServiceException(errRep);
          }
          return subjectAttrs;
      }
      
      /**
       * updateMessageProperties - if messageProperties was not included in the incomming request,
       *       create one populated by a new uniqueGlobalTransaction and an originator with the LOGIN_ID
       *       attribute of the user who signed the message. Incoming message from end users will not have
       *       messageProperties include. Those from peer IDCs should.
       */
      private static MessagePropertiesType updateMessageProperties (MessagePropertiesType msgProps,
                                                                    String event,SubjectAttributes subjectAttributes,
                                                                    OSCARSNetLogger netLogger) {
          SubjectAttributes originator;
          if (msgProps == null) {
              msgProps = new MessagePropertiesType();
          }
          String transId = msgProps.getGlobalTransactionId();
          if (transId == null || transId.equals("")) {
                 transId = PathTools.getLocalDomainId() + "-API-" + UUID.randomUUID().toString();
                 msgProps.setGlobalTransactionId(transId);
          }        
          originator = msgProps.getOriginator();
          if ((originator == null) && (subjectAttributes != null)) {
              for (AttributeType att: subjectAttributes.getSubjectAttribute()) {
                  if (att.getName().equals(AuthZConstants.LOGIN_ID)) {
                      originator = new SubjectAttributes();
                      originator.getSubjectAttribute().add(att);
                      LOG.debug(netLogger.getMsg(event, "setting message Originator to " +
                                                 att.getAttributeValue()));
                  }
              }
              msgProps.setOriginator(originator);
          }
          return msgProps;
      }
}
