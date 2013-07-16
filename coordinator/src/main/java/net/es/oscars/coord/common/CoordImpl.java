package net.es.oscars.coord.common;
import java.util.List;
import java.util.Date;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.authZ.soap.gen.CheckAccessParams;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusRespContent;
import net.es.oscars.resourceManager.soap.gen.UpdateFailureStatusReqContent;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.*;
import net.es.oscars.coord.req.*;
import net.es.oscars.coord.soap.gen.PSSReplyContent;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.topology.PathTools;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;

import net.es.oscars.coord.actions.AuthZCheckAccessAction;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.actions.CoordAction.State;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.soap.OSCARSFaultUtils;
import net.es.oscars.utils.soap.OSCARSServiceException;

import javax.net.ssl.SSLEngineResult;

import static net.es.oscars.utils.topology.PathTools.*;

@OSCARSNetLoggerize(moduleName = ModuleName.COORD)
@javax.jws.WebService(
                      serviceName = ServiceNames.SVC_COORD,
                      portName = "CoordPort",
                      targetNamespace = "http://oscars.es.net/OSCARS/coord",
                      endpointInterface = "net.es.oscars.coord.soap.gen.CoordPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class CoordImpl implements net.es.oscars.coord.soap.gen.CoordPortType {

    private static final Logger LOG = Logger.getLogger(CoordImpl.class.getName());
    private OSCARSNetLogger netLogger = null;
    private static final String moduleName = ModuleName.COORD;

    /**
     * queryResv a pass-through call to the ResourceManager to query a reservation
     * @param subjectAttributes - the subjectAttributes of the requester as returned by
     *     the AuthN service
     * @param queryResReq contains the GlobalReservationId of the reservation to be queried
     * @return ResDetails containing all information about the reservation. 
     * @throws OSCARSFaultMessage if access was denied, or no reservation was found.
     */
    public QueryResReply queryReservation(SubjectAttributes subjectAttributes,QueryResContent queryResReq) 
            throws OSCARSFaultMessage {
        
        String method = "queryReservation";
        String transId = (queryResReq != null ?
                queryResReq.getMessageProperties().getGlobalTransactionId(): "null");
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId); 
        if (queryResReq == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, "invalid argument: queryResContent is null"));
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException("queryResContent is null","user"), 
                    null, LOG, method);
        }
        String gri = queryResReq.getGlobalReservationId();
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method));
        QueryResReply reply = null;

        try {
             CheckAccessReply authDecision = checkAuthorization(method, transId, gri,subjectAttributes, AuthZConstants.QUERY);
             QueryReservationRequest request = new QueryReservationRequest(method + gri,
                                                                           authDecision.getConditions(),
                                                                           queryResReq);
             request.execute();
             if (request.getState() == CoordAction.State.FAILED) {
                 request.logError();
                 throw request.getException();
             }
             reply = request.getResultData();
             // LOG.debug("returning from execute");
        } catch (Exception ex) {
            OSCARSServiceException oEx;
            if (ex.getClass().getName().equals("net.es.oscars.utils.soap.OSCARSServiceException")){
                oEx = (OSCARSServiceException) ex;
                LOG.info(netLogger.error(method, ErrSev.MINOR, oEx.getErrorReport().getErrorMsg(),
                        oEx.getErrorReport().getErrorCode()));
            } else {
                LOG.info(netLogger.error(method, ErrSev.MINOR, ex.getMessage()+ " " + ex.toString()));
                ErrorReport errRep = new ErrorReport(ErrorCodes.RESV_QUERY_FAILED, ex.getMessage(),
                                                     ErrorReport.SYSTEM, gri, transId,
                                                     System.currentTimeMillis()/1000L, moduleName,
                                                     PathTools.getLocalDomainId());
                oEx = new OSCARSServiceException(errRep);
            }
            OSCARSFaultUtils.handleError ( oEx, null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return reply; 
    }

    /**
     * getErrorReport a pass-through call to the ResourceManager to find an errorReport associated with a transactionId
     * @param subjectAttributes - the subjectAttributes of the requester as returned by
     *     the AuthN service
     * @param getErrorReportReq contains the GlobalReservationId of the reservation to be queried
     * @return GetErrorReportResponseContaining containing any Fault reports for this transaction
     * @throws OSCARSFaultMessage if access was denied, or no reservation was found.
     */
    public GetErrorReportResponseContent getErrorReport(SubjectAttributes subjectAttributes,
                                                        GetErrorReportContent getErrorReportReq)
            throws OSCARSFaultMessage {

        String method = "getErrorReport";
        String transId = (getErrorReportReq != null ?
                getErrorReportReq.getMessageProperties().getGlobalTransactionId(): "null");
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId);
        if (getErrorReportReq == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, "invalid argument: GetErrorReportContent is null"));
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException("GetErrprReportContent is null","user"),
                    null, LOG, method);
        }
        String reqTransId = getErrorReportReq.getTransactionId();;
        LOG.info(netLogger.start(method));
        GetErrorReportResponseContent reply = null;

        try {
             CheckAccessReply authDecision = checkAuthorization(method, transId, null,subjectAttributes, AuthZConstants.QUERY);
             GetErrRepRequest request = new GetErrRepRequest(method + "=" + reqTransId, authDecision.getConditions(),
                                                             getErrorReportReq);

             request.execute();
             if (request.getState() == CoordAction.State.FAILED) {
                 request.logError();
                 throw request.getException();
             }
             reply = request.getResultData();
             // LOG.debug("returning from execute");
        } catch (Exception ex) {
            OSCARSServiceException oEx;
            if (ex.getClass().getName().equals("net.es.oscars.utils.soap.OSCARSServiceException")){
                oEx = (OSCARSServiceException) ex;
                LOG.info(netLogger.error(method, ErrSev.MINOR, oEx.getErrorReport().getErrorMsg(),
                                        oEx.getErrorReport().getErrorCode()));
            } else {
                LOG.info(netLogger.error(method, ErrSev.MINOR, ex.getMessage()+ " " + ex.toString()));
                ErrorReport errRep = new ErrorReport(ErrorCodes.RESV_QUERY_FAILED, ex.getMessage(),
                                                     ErrorReport.SYSTEM, "UNKNOWN_GRI", transId,
                                                     System.currentTimeMillis()/1000L, moduleName,
                                                     PathTools.getLocalDomainId());
                oEx = new OSCARSServiceException(errRep);
            }
            OSCARSFaultUtils.handleError ( oEx, null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return reply;
    }

    /**
     * listResv a pass-through call to the ResourceManager to list reservations
     * @param subjectAttributes - the subjectAttributes of the requester as returned by
     *     the AuthN service
     * @param listResvReq A list of constraints on what reservations to list.Includes start and end times,
     *     status, links included, etc.
     * @return ListReply containing a list of ResDetails for each reservation that matched the 
     *     input constraints.The List may be empty
     * @throws OSCARSFaultMessage if access was denied
     */
    public ListReply listReservations(SubjectAttributes subjectAttributes,ListRequest listResvReq) throws OSCARSFaultMessage    { 
        
        String method = "listReservations";
        String transId = (listResvReq != null ?
                listResvReq.getMessageProperties().getGlobalTransactionId(): "null");       
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId); 
        if (listResvReq == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, "invalid argument: listResvReq is null"));
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException("listResvReq is null","user"), 
                    null, LOG, method);
        }
        LOG.info(netLogger.start(method));
        ListReply listReply = null;
        try {
            CheckAccessReply authDecision = checkAuthorization(method, transId, null, subjectAttributes, AuthZConstants.LIST);
            ListReservationRequest request = new ListReservationRequest(method, authDecision.getConditions(), listResvReq);
            request.execute();
            // LOG.debug("returning from execute");
            if (request.getState() == CoordAction.State.FAILED) {
                request.logError();
                throw request.getException();
            }
            listReply = request.getResultData();
        } catch (Exception ex) {
            OSCARSServiceException oEx;
            if (ex.getClass().getName().equals("net.es.oscars.utils.soap.OSCARSServiceException")){
                oEx = (OSCARSServiceException) ex;
                LOG.info(netLogger.error(method, ErrSev.MINOR, oEx.getErrorReport().getErrorMsg(),
                                        oEx.getErrorReport().getErrorCode()));
            } else {
                LOG.info(netLogger.error(method, ErrSev.MINOR, ex.getMessage()+ " " + ex.toString()));
                ErrorReport errRep = new ErrorReport(ErrorCodes.RESV_LIST_FAILED, ex.getMessage(),
                                                     ErrorReport.SYSTEM, "UNKNOWN_GRI", transId,
                                                     System.currentTimeMillis()/1000L, moduleName,
                                                     PathTools.getLocalDomainId());
                oEx = new OSCARSServiceException(errRep);
            }
            OSCARSFaultUtils.handleError ( oEx, null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return listReply;
    }

    /**
     *  Called by the WBUI to allow a network engineer to clean up the data base
     *
     *  @param updateStatusReq contains the  new status and an error report explaining why it is being reset
     *  @return the new status
     */

    public UpdateStatusRespContent forceUpdateStatus(SubjectAttributes subjectAttributes,
                                                     UpdateFailureStatusReqContent updateStatusReq)
            throws OSCARSFaultMessage    {


        String method = "forceUpdateStatus";
        if (updateStatusReq == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, "invalid argument: updateStatusReq is null"));
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException("updateStatusReq is null","user"),
                    null, LOG, method);
        }
        String transId = updateStatusReq.getTransactionId();
        String gri = updateStatusReq.getGlobalReservationId();
        String status = updateStatusReq.getStatus();
        UpdateStatusRespContent reply = null;
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId);
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method, "Forcing status to " + status));
        if (subjectAttributes == null) {
            throw new OSCARSFaultMessage("invalid input to forceUpdateStatus: subjectAttributes is null");
        }
        if (gri == null) {
            throw new OSCARSFaultMessage("invalid input to forceUpdateStatus: gri is null");
        }
        try {
            CheckAccessReply authDecision = checkAuthorization(method, transId,  gri,
                                                               subjectAttributes, AuthZConstants.MODIFY);

            ForceUpdateStatusRequest request = new ForceUpdateStatusRequest(method + gri,
                                                                           authDecision.getConditions(),
                                                                           updateStatusReq);
            MessagePropertiesType msgProps = new MessagePropertiesType();
            msgProps.setGlobalTransactionId(transId);
            // not used, but fill in enough to conform to schema
            SubjectAttributes subj = new SubjectAttributes();
            AttributeType attr = new AttributeType();
            attr.setName("");
            subj.getSubjectAttribute().add(attr);
            msgProps.setOriginator(subj);

            request.setMessageProperties(msgProps);
            request.execute();
            if (request.getState() == CoordAction.State.FAILED) {
                throw request.getException();
            }
            reply = request.getResultData();
        } catch (OSCARSServiceException oEx) {
            LOG.info(netLogger.error(method, ErrSev.MINOR, oEx.getErrorReport().getErrorMsg(),
                        oEx.getErrorReport().getErrorCode()));
            OSCARSFaultUtils.handleError ( oEx, null, LOG, method);
        } catch (Exception ex) {
            LOG.info(netLogger.error(method, ErrSev.MINOR, ex.getMessage()+ " " + ex.toString()));
            ErrorReport errRep = new ErrorReport(ErrorCodes.RESV_MODIFY_FAILED , ex.getMessage(),
                                                 ErrorReport.SYSTEM, gri, transId,
                                                 System.currentTimeMillis()/1000L, moduleName,
                                                 PathTools.getLocalDomainId());

            OSCARSFaultUtils.handleError ( new OSCARSServiceException(errRep), null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return reply;
    }


    /**
     * Create Reservation
     * @param subjectAttributes - the users loginName, institution and any role or other attributes
     * @param createResvReq - contains the start and endtime, the bandwidth and path information
     * @returns createReply which has status set to "ok" and the GRI set if no faults were thrown
     *         otherwise status is set to "failed" and no GRI is returned.
     */
    public CreateReply createReservation(SubjectAttributes subjectAttributes,ResCreateContent createResvReq) 
       throws OSCARSFaultMessage    { 

        String method = "createReservation";
        String transId = (createResvReq != null ?
                createResvReq.getMessageProperties().getGlobalTransactionId(): "null");       
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId); 
        if (createResvReq == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, "invalid argument: createResvReq is null"));
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException("createResvReq is null","user"),
                    null, LOG, method);
        }
        String gri = createResvReq.getGlobalReservationId();
        // prevent having to make multiple tests later
        if (gri != null && gri.length() == 0 ) {
            gri = null;
            createResvReq.setGlobalReservationId(null);
        }; 
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method));
        if (subjectAttributes == null) {
            throw new OSCARSFaultMessage("invalid input to createReservation: subjectAttributes is null");
        }
        if (createResvReq == null) {
            throw new OSCARSFaultMessage("invalid input to createReservation: createResvReq is null");
        }
        String loginName = null;
        CreateReply createReply =  new CreateReply();
        List<AttributeType> reqAttrs = subjectAttributes.getSubjectAttribute();
        for (AttributeType at : reqAttrs) {
            if (at.getName().equals(AuthZConstants.LOGIN_ID)) {
                List<Object> values = at.getAttributeValue();
                // Login should be only one value.
                if (values.size() == 1) {
                    loginName= (String) values.get(0);
                }
            }
        } if (loginName == null) {
            throw new OSCARSFaultMessage("loginId attribute was missing");
        }

        CreateReservationRequest request = null;

        try {
            CheckAccessReply authDecision = checkAuthorization(method, transId,  gri, subjectAttributes, AuthZConstants.CREATE);
            /* check for bandwidth, duration, hops allowed  or permitted domains constraints */
            checkConditions( authDecision, createResvReq.getGlobalReservationId(),
                                   createResvReq.getUserRequestConstraint(),false );

            if (gri == null ) {
                request = new CreateReservationRequest(method, loginName, createResvReq);
            } else {
                request = new CreateReservationRequest(gri, method + "-" + gri, loginName, createResvReq);
            }
            request.setRequestData(createResvReq);
            request.setMessageProperties(createResvReq.getMessageProperties());
            request.execute();
            if (request.getState() == CoordAction.State.FAILED) {
                //something failed
                request.logError();
                throw request.getException();
            }
            createReply.setMessageProperties(createResvReq.getMessageProperties());
            createReply.setGlobalReservationId(request.getGRI().toString());
            createReply.setStatus("Ok");
        } catch (OSCARSServiceException ex) {
                // CreateReservationsRequest will handle any errors
                // session = null, no database connected
                OSCARSFaultUtils.handleError ( ex, null, LOG, method);
        } catch (Exception e) {
                 // user error: false, should have caught the ones that were expected already
                // session = null, no database connected
                OSCARSFaultUtils.handleError ( e, false, null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return createReply;
    }

    /**
     * cancelReservation  cancels a reservation
     *   if the reservation is currently active, a teardown will be initiated.
     *   All the PCEs will be notified that the reservation is cancelled, in case they need to
     *   release co-scheduled resources.
     * @param subjectAttributes - the subjectAttributes of the requester as returned by
     *     the AuthN service
     * @param cancelResRequest contains the GlobalReservationId of the reservation to be canceled
     *      and MessageProperties
     * @return CancelResReply contains the status of the reservation 
     * @throws OSCARSFaultMessage
     */
    public CancelResReply cancelReservation(SubjectAttributes subjectAttributes,CancelResContent cancelResRequest) 
    throws OSCARSFaultMessage    { 
        String method = "cancelReservation";
        String transId = (cancelResRequest != null ?
                cancelResRequest.getMessageProperties().getGlobalTransactionId(): "null");       
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId); 
        if (cancelResRequest == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, "invalid argument: cancelResRequest is null"));
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException(ErrorCodes.RESV_CANCEL_FAILED,
                                                                      "cancelResRequest is null",
                                                                       ErrorReport.USER),
                                           null, LOG, method);
        }
        String gri = cancelResRequest.getGlobalReservationId();
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method));
        CancelResReply reply =  new CancelResReply();

        if (subjectAttributes == null) {
            throw new OSCARSFaultMessage("invalid input to cancelReservation: subjectAttributes is null");
        }
        if (gri == null) {
            throw new OSCARSFaultMessage("invalid input to cancelReservation: gri is null");
        }
        try {
            CheckAccessReply authDecision = checkAuthorization(method, transId,  gri, subjectAttributes, AuthZConstants.MODIFY);
            CancelRequest request = new CancelRequest (method + "-" + gri,
                                                       authDecision.getConditions(),
                                                       cancelResRequest);
            
            request.execute();
            if (request.getState() == CoordAction.State.FAILED) {
                request.logError();
                throw request.getException();
            } 
            reply.setStatus("Ok");
        } catch (OSCARSServiceException ex) {
                // CancelRequest will handle any errors
                // session = null, no database connected
                OSCARSFaultUtils.handleError ( ex, null, LOG, method);
        } catch (Exception e) {
                 // user error: false, should have caught the ones that were expected already
                // session = null, no database connected
                OSCARSFaultUtils.handleError ( e, false, null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return reply;
    }
    /**
     * modifyReservation - can modify the start and end Times and bandwidth of a RESERVED reservation
     * and the end time of an ACTIVE reservation
     * @param subjectAttributes - the subjectAttributes of the requester as returned by
     *     the AuthN service
     * @param modifyResvReq contains the GRI, description, userConstraint,and optionally a reservedconstraint and
     *     optional constraints for the reservation
     * @return the status for the modified reservation
     */
    public ModifyResReply modifyReservation(SubjectAttributes subjectAttributes,ModifyResContent modifyResvReq) 
        throws OSCARSFaultMessage    { 
        String method = "modifyReservation";
        String transId = (modifyResvReq != null ?
                                    modifyResvReq.getMessageProperties().getGlobalTransactionId(): "null");
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId); 
        if (modifyResvReq == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR, "invalid argument: modifyResContent is null"));
         // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException("CreatePathContent is null","user"), 
                                            null, LOG, method);
        }
        ModifyResReply modifyReply = null;
        CheckAccessReply authDecision = null;
        String gri = modifyResvReq.getGlobalReservationId();
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method));
        String loginName =  null;
        try {
            authDecision = checkAuthorization(method, transId, gri, subjectAttributes, AuthZConstants.MODIFY);
            UserRequestConstraintType inputUC =   modifyResvReq.getUserRequestConstraint();

            // Get any missing values from the existing reservation
            QueryResContent queryResCon = new QueryResContent();
            queryResCon.setMessageProperties(modifyResvReq.getMessageProperties());
            queryResCon.setGlobalReservationId(modifyResvReq.getGlobalReservationId());
            QueryReservationRequest queryReq = new QueryReservationRequest(method,
                                                                          authDecision.getConditions(),
                                                                          queryResCon);
            queryReq.execute();
            if (queryReq.getState() == CoordAction.State.FAILED) {
                queryReq.logError();
                throw queryReq.getException();
            }
            ResDetails resDetails = queryReq.getResultData().getReservationDetails();
            UserRequestConstraintType modUC =  resDetails.getUserRequestConstraint();
            ReservedConstraintType modRC = resDetails.getReservedConstraint();
            // check that requested modifications are allowed
            String resState = resDetails.getStatus();
            Long curtime = System.currentTimeMillis()/1000L;
            if ( (inputUC.getBandwidth() != 0 )&&
                    (inputUC.getBandwidth() != modUC.getBandwidth())) {
                if ( !Coordinator.getInstance().isAllowActiveModify() && 
                        !resState.equals(StateEngineValues.RESERVED)) {
                    throw new OSCARSServiceException(ErrorCodes.INVALID_PARAM,
                                                    "Cannot change bandwidth of " +
                                                     resState + " reservation",
                                                     ErrorReport.USER);
                }
                modUC.setBandwidth(inputUC.getBandwidth());
                modRC.setBandwidth(inputUC.getBandwidth());
            }
            // the WBUI only keeps times in minutes and always inputs time values even if they were not changed
            if ((inputUC.getStartTime() != 0) &&
                    ((inputUC.getStartTime() > modUC.getStartTime() + 61) ||
                    (inputUC.getStartTime() < modUC.getStartTime() - 61)))  {
                if ( !resState.equals(StateEngineValues.RESERVED)) {
                    throw new OSCARSServiceException(ErrorCodes.INVALID_PARAM,
                                                    "Cannot change start time of " +
                                                     resState + " reservation",
                                                     ErrorReport.USER);
                } else {
                    if (inputUC.getStartTime() <= curtime) {
                        inputUC.setStartTime(curtime);
                    }
                }
                modUC.setStartTime(inputUC.getStartTime());
                modRC.setStartTime(inputUC.getStartTime());
            }
            // the WBUI only keeps times in minutes and always inputs time values even if they were not changed
            if ( (inputUC.getEndTime() != 0) &&
                    ((inputUC.getEndTime() > modUC.getEndTime() + 61) ||
                    (inputUC.getEndTime() < modUC.getEndTime() - 61)))  {
                if ( !resState.equals(StateEngineValues.RESERVED)  &&
                     !resState.equals(StateEngineValues.ACTIVE)   ) {
                    throw new OSCARSServiceException(ErrorCodes.INVALID_PARAM,
                                                    "Cannot change end time of " +
                                                       resState + " reservation",
                                                    ErrorReport.USER);
                } else {
                    if (inputUC.getEndTime() <= curtime) {
                        inputUC.setEndTime(curtime);
                    }
                }
                modUC.setEndTime(inputUC.getEndTime());
                modRC.setEndTime(inputUC.getEndTime());
            }

            if (modUC.getEndTime() <= modUC.getStartTime()) {
                throw new OSCARSServiceException(ErrorCodes.INVALID_PARAM,
                                            "requested end time earlier than start time",
                                             ErrorReport.USER);

            }

            modifyResvReq.setUserRequestConstraint(modUC);
            // include information about the current reservation for use in error handling
            modifyResvReq.setReservedConstraint(modRC);

            checkConditions( authDecision, null, modUC, true);
            loginName = getLoginName (subjectAttributes);
 
            /* Create a ModifyReservationRequest, set the description, subjectAttributes
             * and state Attributes.
             */
            ModifyReservationRequest  request = new ModifyReservationRequest(method + "-" + gri,
                                                                             gri,
                                                                             loginName,
                                                                             authDecision.getConditions(),
                                                                             modifyResvReq);
             // save original description if no new one is input
            if ( (request.getAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE) == null) ||
                    (request.getAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE).equals(""))) {
                request.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE, resDetails.getDescription());
            }
            // this is the current reservation state, not INMODIFY
            request.setAttribute(CoordRequest.STATE_ATTRIBUTE,resDetails.getStatus());
            // needed for ide errors
            request.setResDetails(resDetails);
            request.execute();
            if (request.getState() == CoordAction.State.FAILED) {
                request.logError();
                throw request.getException();
            }
            modifyReply = request.getResultData();
        } catch (OSCARSServiceException ex) {
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( ex, null, LOG, method);
        } catch (Exception e) {
            // user error: false, should have caught the ones that were expected already
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( e, false, null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return modifyReply;
    }
    /**
     * Instantiate a circuit for a reserved Reservation
     * @params  SubjectAttributes set if this is a user setup and the call has come from the OSCARSService 
     *          used to authorize the request. 
     *          Will be null if request comes from ResourceScheduler
     * @params CreatePathContent -set if this a user setup and the call has come from the OSCARSService
     *         contains a token or a GRI for the reservation. 
     *         Will be null if request comes from ResourceScheduler
     * @params ResDetails - set if this is a timer-automatic setup and the call has come from the 
     *         ResourceScheduler. Will be null if request comes from OSCARSService
     */
    public CreatePathResponseContent createPath(SubjectAttributes subjectAttributes,
                                                CreatePathContent createPathReq,
                                                ResDetails resDetails) throws OSCARSFaultMessage { 
 
        String method = "createPath";
        String transId = createPathReq != null? createPathReq.getMessageProperties().getGlobalTransactionId(): "null";
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId); 
        if (createPathReq == null) {
            LOG.warn("CreatePath: invalid argument: createPathContent is null");
         // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException("CreatePathContent is null","user"), 
                                            null, LOG, method);
        }
        String gri = createPathReq.getGlobalReservationId();
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method));

        CreatePathResponseContent response = new CreatePathResponseContent();
        CheckAccessReply authDecision = null;
        try {
           
            if (subjectAttributes != null  && resDetails == null){
                // Call came from OSCARSService, check authorization and query for details
                authDecision = checkAuthorization(method, transId, gri, subjectAttributes, AuthZConstants.SIGNAL);

                QueryResContent queryReq = new QueryResContent();
                queryReq.setMessageProperties(createPathReq.getMessageProperties());
                queryReq.setGlobalReservationId(gri);
                /* the authCondtions for signal are passed through the resourceManager query which will
                 * check for permitted login and permitted domains and
                 * only return the reservation details if any such conditions are met.
                 */
                QueryReservationRequest qRequest= new QueryReservationRequest("QueryReservation-" + gri,
                                                                               authDecision.getConditions(),
                                                                               queryReq);
                qRequest.execute();
                if (qRequest.getState() == CoordAction.State.FAILED){
                    qRequest.logError();
                    throw qRequest.getException();
                }
                
                // check if path setup is allowed at this time to this user
                QueryResReply qReply = qRequest.getResultData();
                resDetails = qReply.getReservationDetails();
                String resStatus = resDetails.getStatus();
                
                if (!resDetails.getUserRequestConstraint().getPathInfo().getPathSetupMode().equals("signal-xml")) {
                    throw new OSCARSServiceException(ErrorCodes.RESV_STATE_ERROR,
                                                    "Reservation is timer-automatic, does not allow manual setup",
                                                     ErrorReport.USER);
                }
                if (!resDetails.getStatus().equals(StateEngineValues.RESERVED )) {
                    throw new OSCARSServiceException(ErrorCodes.RESV_STATE_ERROR,
                                                     "Reservation is in state " + resDetails.getStatus() +
                                                     " setup path not allowed",
                                                     ErrorReport.USER);
                }
                if ( resDetails.getReservedConstraint().getStartTime() > System.currentTimeMillis()/1000) {
                    throw new OSCARSServiceException(ErrorCodes.RESV_STATE_ERROR,
                                                    "Reservation may not be setup until its start time of " +
                                                    new Date(resDetails.getReservedConstraint().getStartTime()*1000).toString(),
                                                    ErrorReport.USER  );
                }
            }

            PathRequest request =  PathRequest.getPathRequest(PathRequest.PSS_CREATE_PATH +  "-" + gri,
                                                              createPathReq.getMessageProperties(),
                                                              resDetails);
            request.execute();
            if ( request.getState().equals(State.FAILED)) {
                throw request.getException();
            } 
            response.setMessageProperties(request.getMessageProperties());
            response.setGlobalReservationId(request.getGRI().toString());
            response.setStatus("INSETUP");
        } catch (OSCARSServiceException ex) {
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( ex, null, LOG, method);
        } catch (Exception e) {
            LOG.debug(netLogger.end("caught exception " + e.toString()) );
            // isUser = false  
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( e, false, null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return response;
    }

    /**
     * teardownPath - tears down the circuit for the reservation. May be called from the
     *     OSCARSService interface for manual signaling or from the ResourceManager for automatic
     *     timed signaling
     * @param subjectAttributes - the subjectAttributes of the requester as returned by
     *     the AuthN service, must be set if called from OSARSService interface. May be null
     *     if called from ResourceManager
     * @param teardownPathReq set if called from OSCARSService interface. Contains GRI or token for reservation
     * @param resDetailsReq set if called from ResourceManager. Contains all the information needed for
     *     PSS path teardown
     */
    public TeardownPathResponseContent teardownPath(SubjectAttributes subjectAttributes,
                                                    TeardownPathContent teardownPathReq,
                                                    ResDetails resDetailsReq) 
                        throws OSCARSFaultMessage    { 
        String method ="teardownPath";
        String transId = (teardownPathReq != null ?
                teardownPathReq.getMessageProperties().getGlobalTransactionId(): "null");       
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId); 
        if (teardownPathReq == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR,"invalid argument: teardownPathContent is null"));
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( new OSCARSServiceException("TeardownPathContent is null","user"), 
                                            null, LOG, method);
        }
        String gri = teardownPathReq.getGlobalReservationId(); 
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method));
        
        ResDetails resDetails = resDetailsReq;
        boolean doPSSTeardown = true;
        String retStatus =StateEngineValues.INTEARDOWN;
        TeardownPathResponseContent response = new TeardownPathResponseContent();
        CheckAccessReply authDecision = null;

        try {
            if (subjectAttributes != null  && resDetails == null){
                // Call came from OSCARSService, check authorization and query for details 
                authDecision = checkAuthorization(method, transId, gri, subjectAttributes, AuthZConstants.SIGNAL);

                gri =teardownPathReq.getGlobalReservationId();
                QueryResContent queryReq = new QueryResContent();
                queryReq.setMessageProperties(teardownPathReq.getMessageProperties());
                queryReq.setGlobalReservationId(gri);

                /* ResourceManager queryReservation will check if the AuthConditions allow signaling of this reservation
                 * e.g if institution restriction, or own reservation only
                 */
                QueryReservationRequest qRequest= new QueryReservationRequest("QueryReservation-" + gri,
                                                                               authDecision.getConditions(),
                                                                               queryReq);
                qRequest.execute();
                if (qRequest.getState() == CoordAction.State.FAILED){
                    qRequest.logError();
                    throw qRequest.getException();
                }

                // check if path teardown is allowed at this time to this user
                QueryResReply qReply = qRequest.getResultData();
                resDetails = qReply.getReservationDetails();
                if (!resDetails.getUserRequestConstraint().getPathInfo().getPathSetupMode().equals("signal-xml")) {
                    throw new OSCARSServiceException("Reservation is timer-automatic, does not allow manual teardown",
                                    "user");
                }
                if (resDetails.getStatus().equals(StateEngineValues.FINISHED) ||
                       resDetails.getStatus().equals(StateEngineValues.RESERVED) ||
                       resDetails.getStatus().equals(StateEngineValues.INTEARDOWN)) {
                    doPSSTeardown = false;
                    retStatus = resDetails.getStatus();
                } else if (!resDetails.getStatus().equals(StateEngineValues.ACTIVE)) {
                    throw new OSCARSServiceException("Reservation is in state " + resDetails.getStatus() +
                            " teardown path not allowed","user");
                }
            }
            if (doPSSTeardown){
                gri = resDetails.getGlobalReservationId();

                PathRequest request =  PathRequest.getPathRequest(PathRequest.PSS_TEARDOWN_PATH +  "-" + gri,
                                                                  teardownPathReq.getMessageProperties(),
                                                                  resDetails);
                request.execute();
                retStatus = StateEngineValues.INTEARDOWN;
                if (request.getState().equals(State.FAILED)) {
                    throw request.getException();
                }
            }
            response.setMessageProperties(teardownPathReq.getMessageProperties());
            response.setGlobalReservationId(resDetails.getGlobalReservationId());
            response.setStatus(retStatus);
        } catch (OSCARSServiceException ex) {
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( ex, null, LOG, method);
        } catch (Exception e) {
            // isUser = false  
            // session = null, no database connected
            OSCARSFaultUtils.handleError ( e, false, null, LOG, method);
        }
        LOG.info(netLogger.end(method));
        return response;
        
    }
    /**
     * interDomainEvent - handles notifications from other IDCs about interdomain reservation status changes
     * It is a one way message, there is no reply.
     * @param subjectAttributes - the subjectAttributes of the requester as returned by
     *     the AuthN service
     * @param eventContent - include type of event, timestamp, loginId of reservation, resDetails,
     *      message details, etc.
     */
    public void interDomainEvent(SubjectAttributes subjectAttributes, InterDomainEventContent eventContent) { 
        String method = "InterDomainEvent";
        String transId = (eventContent != null ?
                eventContent.getMessageProperties().getGlobalTransactionId(): "null");
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId); 
        if (subjectAttributes == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR,"invalid argument: subjectAttributes is null"));
            return;
        }
        if (eventContent == null) {
            LOG.warn(netLogger.error(method,ErrSev.MAJOR,"invalid argument: eventContent is null"));
            return; 
        }

        String gri = eventContent.getResDetails().getGlobalReservationId();
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method, "received " + eventContent.getType()));
        
        try {
            CheckAccessReply authDecision = checkAuthorization(method, transId, gri, subjectAttributes, AuthZConstants.SENDEVENT);
        } catch (OSCARSServiceException e) {
            LOG.error(netLogger.error(method,ErrSev.MAJOR, "unauthorized interDomainEvent received"));
            return;
        }

        InterDomainEventRequest request = 
             new InterDomainEventRequest(method + "-" + eventContent.getType() + "-" + gri,
                                         eventContent.getMessageProperties().getGlobalTransactionId(),
                                         gri);
        request.setRequestData(eventContent);
        request.setMessageProperties(eventContent.getMessageProperties());

        request.execute();
        if (request.getState() == CoordAction.State.FAILED) {
            request.logError();
            Exception reqEx = request.getException();
            LOG.warn(netLogger.error(method,ErrSev.MINOR, eventContent.getType() + " failed with exception: "+ 
                    reqEx.getMessage()  + " for reservation " +  gri));
        }
        
        LOG.info(netLogger.end(method));
        return;
    }

    /***
     * Handles the pssReply one way message from the PSSService
     * @param pssReply
     */
    public void pssReply(PSSReplyContent pssReply) { 
        String method = "CoordImpl.pssReply";
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,pssReply.getTransactionId()); 
        String gri = pssReply.getGlobalReservationId();
        String replyType = pssReply.getReplyType();
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(method, replyType + " status is " + pssReply.getStatus()));
        try {
            PSSReplyRequest PSSReplyReq =  new PSSReplyRequest("PSSReplyRequest-"+replyType +"-" + gri, 
                                                                gri,
                                                                pssReply);
            PSSReplyReq.setRequestData(pssReply);
            PSSReplyReq.execute();
        } catch (Exception ex) {
            // internal error handling was done by PSSReplyReq
            // there is no return message
            LOG.error(netLogger.error(method,ErrSev.MINOR, ex.getMessage()));
        }
    }

    private String getLoginName(SubjectAttributes subjectAttributes ) throws OSCARSFaultMessage {
        String loginName = null;
        List<AttributeType> reqAttrs = subjectAttributes.getSubjectAttribute();
        for (AttributeType at : reqAttrs) {
            if (at.getName().equals(AuthZConstants.LOGIN_ID)) {
                List<Object> values = at.getAttributeValue();
                // Login should be only one value.
                if (values.size() == 1) {
                    loginName= (String) values.get(0);
                }
            }
        } if (loginName == null) {
            throw new OSCARSFaultMessage("loginId attribute was missing");
        }
        return loginName;
    }
    
    private CheckAccessReply checkAuthorization(String method, String transId, String gri, SubjectAttributes subjectAttributes,
            String permission) throws OSCARSServiceException {
        // Verify user's capabilities. (AuthZ)
        CheckAccessParams checkAccessParams = new CheckAccessParams();
        checkAccessParams.setTransactionId(transId);
        checkAccessParams.setSubjectAttrs(subjectAttributes);
        checkAccessParams.setResourceName(AuthZConstants.RESERVATIONS);
        checkAccessParams.setPermissionName(permission);
        AuthZCheckAccessAction checkAccess = new AuthZCheckAccessAction(method + "-AuthZCheckAction", null);

        // Add checkAccessParams into the authZCheckAccessAction
        checkAccess.setRequestData(checkAccessParams);
        checkAccess.execute();
        if (checkAccess.getState() == State.FAILED) {
            Exception ex = checkAccess.getException();
            OSCARSServiceException oEx;
            ErrorReport errRep = new ErrorReport(ErrorCodes.ACCESS_DENIED, "exception from authZ client",
                                                         ErrorReport.SYSTEM, gri, transId,
                                                         System.currentTimeMillis()/1000L, moduleName,
                                                         PathTools.getLocalDomainId());
            if (ex != null) {
                if (ex.getClass().getName().equals("net.es.oscars.utils.soap.OSCARSServiceException")){
                    oEx = (OSCARSServiceException) ex;
                    oEx.addErrorReport(errRep);
                } else {
                     oEx= new OSCARSServiceException(errRep);
                }
            }
            else{
                // shouldn't happen
                errRep.setErrorMsg("AuthZCheckAccess Failed for unknown reason");
                oEx = new OSCARSServiceException(errRep);
            }
            LOG.error(netLogger.error(method, ErrSev.MINOR, oEx.getMessage()));
            throw oEx;
        }
        CheckAccessReply authDecision = checkAccess.getResultData();
        if (authDecision.getPermission().equals(AuthZConstants.ACCESS_DENIED)) {
            String loginName = null;
            List<AttributeType> reqAttrs = subjectAttributes.getSubjectAttribute();
            for (AttributeType at : reqAttrs) {
                if (at.getName().equals(AuthZConstants.LOGIN_ID)) {
                    List<Object> values = at.getAttributeValue();
                    // Login should be only one value.
                    loginName= (String) values.get(0);
                }
            }
            String message = "authorization denied for user "  +  loginName;
            LOG.info(netLogger.getMsg(method,message));
            OSCARSServiceException oEx = new OSCARSServiceException(ErrorCodes.ACCESS_DENIED, message, ErrorReport.USER);
            throw oEx;
        }
        return authDecision;
    }
    
    /* check to see if any max-bandwidth or max-duration conditions are satisfied */
    private void checkConditions(CheckAccessReply authDecision, String gri, UserRequestConstraintType userConstraint, boolean modify )
                                                                throws OSCARSServiceException {
        AuthConditions authConds = authDecision.getConditions();
        boolean hopsAllowed = false;
        boolean griAllowed = false;
        for (AuthConditionType authCond: authConds.getAuthCondition() ) {
            if (authCond.getName().equals(AuthZConstants.MAX_BANDWIDTH)) {
                int reqBandwidth = userConstraint.getBandwidth();
                if (Integer.parseInt(authCond.getConditionValue().get(0)) <  reqBandwidth ) {
                    throw new OSCARSServiceException (ErrorCodes.ACCESS_DENIED, "requested bandwidth exceeds allowed limit",
                                                      ErrorReport.USER);
                }
            } else if (authCond.getName().equals(AuthZConstants.MAX_DURATION)){
                int reqDuration = (int) (userConstraint.getEndTime() - 
                                         userConstraint.getStartTime());
                if ( Integer.parseInt(authCond.getConditionValue().get(0)) < reqDuration) {
                    throw new OSCARSServiceException (ErrorCodes.ACCESS_DENIED, "requested duration exceeds allowed limit",
                                                      ErrorReport.USER);
                }
            } else if (authCond.getName().equals(AuthZConstants.SPEC_PATH_ELEMS)) {
                hopsAllowed = true;
            } else if (authCond.getName().equals(AuthZConstants.SPECIFY_GRI)) {
                griAllowed = true;
            } else if (authCond.getName().equals(AuthZConstants.PERMITTED_DOMAINS)){
                List <String> domains = authCond.getConditionValue();
                if ( !checkDomains(userConstraint, domains)) {
                    throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED, "requested path not in permitted domain",
                                                     ErrorReport.USER);
                }
            }
        }
        if (modify) {
            // path and gri are ok
            return;
        }
        if (userConstraint.getPathInfo().getPath() != null &&
                !userConstraint.getPathInfo().getPath().getHop().isEmpty() ) {
            if (!hopsAllowed ) {
                throw new OSCARSServiceException (ErrorCodes.ACCESS_DENIED, "not allowed to input path elements",
                                                  ErrorReport.USER);
            }
        }
        if (gri != null && gri.length() != 0  &&
                griAllowed == false) {
            throw new OSCARSServiceException (ErrorCodes.ACCESS_DENIED, "not allowed to input gri",
                                              ErrorReport.USER);
        }
    }
    
    /**
     *  Check to see if either the src or destination of the reservation is in an allowed domain
     * @param uc the UserRequestConstraint for the reservation
     * @param domains String array containing all the domains allowed for this request
     * @return true, if this reservation is allowed for this request, false otherwise
     */
    public boolean checkDomains(UserRequestConstraintType uc, List<String> domains)
                throws OSCARSServiceException {
        String src, dest;
        if (uc.getPathInfo().getLayer2Info() !=  null) {
            src = uc.getPathInfo().getLayer2Info().getSrcEndpoint();
            dest = uc.getPathInfo().getLayer2Info().getDestEndpoint();
        } else {
            throw new OSCARSServiceException(ErrorCodes.NOT_IMPLEMENTED, "layer3 not implemented yet",
                                             ErrorReport.SYSTEM);
        }
        
        for (String dom : domains) {
            if (dom.equals(src) ||
                    dom.equals(dest)) {
                return true;
            }
        }
        return false;
    }
}
