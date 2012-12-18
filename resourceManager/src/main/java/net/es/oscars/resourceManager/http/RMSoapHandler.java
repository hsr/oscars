package net.es.oscars.resourceManager.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.resourceManager.soap.gen.*;
import net.es.oscars.common.soap.gen.*;
import net.es.oscars.resourceManager.common.RMCore;
import net.es.oscars.resourceManager.common.ResourceManager;
import net.es.oscars.utils.soap.OSCARSFaultUtils;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;

@OSCARSNetLoggerize(moduleName = ModuleName.RM)
@javax.jws.WebService(
                      serviceName = ServiceNames.SVC_RM,
                      portName = "RMPort",
                      targetNamespace = "http://oscars.es.net/OSCARS/resourceManager",
                      endpointInterface = "net.es.oscars.resourceManager.soap.gen.RMPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class RMSoapHandler implements RMPortType {

    private static final Logger LOG =
        Logger.getLogger(RMSoapHandler.class.getName());
    private String moduleName = 
        this.getClass().getAnnotation(OSCARSNetLoggerize.class).moduleName();

    private RMCore core = RMCore.getInstance();

    public AssignGriRespContent assignGri(AssignGriReqContent assignGriReq)
            throws OSCARSFaultMessage { 

        String event = "assignGri";
        String transId = assignGriReq.getTransactionId();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,transId);
        LOG.debug(netLogger.start(event));
        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        String gri = null;
        AssignGriRespContent reply = new AssignGriRespContent();
        
        LOG.debug(netLogger.getMsg(event,"transactionId is:" + transId));
        reply.setTransactionId(assignGriReq.getTransactionId());

        try {
           session.beginTransaction();
           gri = mgr.generateGRI();
           reply.setGlobalReservationId(gri);

        } catch (OSCARSServiceException ex) {
            completeErrorReport(ex,gri,transId);
            OSCARSFaultUtils.handleError( ex, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        netLogger.setGRI(gri);
        LOG.debug(netLogger.end(event));
        return reply;
    }
    /**
     * updates the status of a reservation
     * @param updateStatusReq contains gri and  new status
     * @throws OSCARSFaultMessage if reservation is not found or state transition is not allowed
     * @seeAlso StateEngine for allowed state transitions
     */
    public UpdateStatusRespContent
        updateStatus(UpdateStatusReqContent updateStatusReq)
           throws OSCARSFaultMessage { 
        String event = "updateStatus";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId =  updateStatusReq.getTransactionId();
        netLogger.init(moduleName, transId );
        netLogger.setGRI(updateStatusReq.getGlobalReservationId());
        LOG.debug(netLogger.start(event,
                                 " status is " + updateStatusReq.getStatus()));
        UpdateStatusRespContent response = new UpdateStatusRespContent();
        response.setTransactionId(updateStatusReq.getTransactionId());
        String retStatus = null;
        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            retStatus = mgr.updateStatus(updateStatusReq.getGlobalReservationId(),
                    updateStatusReq.getStatus(), false, null);
        } catch (OSCARSServiceException ex) {
            completeErrorReport(ex, updateStatusReq.getGlobalReservationId(), transId);
            OSCARSFaultUtils.handleError( ex, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.debug(netLogger.end(event));
        response.setGlobalReservationId(updateStatusReq.getGlobalReservationId());
        response.setStatus(retStatus);
        return response;
    }

    /**
        * updates the status of a reservation which has failed in the coordinator
        * @param updateStatusReq contains gri and  new status
        * @throws OSCARSFaultMessage if reservation is not found or state transition is not allowed
        * @seeAlso StateEngine for allowed state transitions
        */
       public UpdateStatusRespContent
           updateFailureStatus(UpdateFailureStatusReqContent updateStatusReq)
              throws OSCARSFaultMessage {
           String event = "updateFailureStatus";
           OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
           String transId  = updateStatusReq.getTransactionId();
           netLogger.init(moduleName, transId);
           netLogger.setGRI(updateStatusReq.getGlobalReservationId());
           LOG.debug(netLogger.start(event,
                                    " status is " + updateStatusReq.getStatus() +
                                    " failure reason is " + updateStatusReq.getErrorReport().getErrorMsg()));
           UpdateStatusRespContent response = new UpdateStatusRespContent();
           response.setTransactionId(updateStatusReq.getTransactionId());
           String retStatus = null;
           ResourceManager mgr = core.getResourceManager();
           Session session = core.getSession();
           try {
               session.beginTransaction();
               retStatus = mgr.updateStatus(updateStatusReq.getGlobalReservationId(),
                                            updateStatusReq.getStatus(),true,
                                            ErrorReport.fault2report(updateStatusReq.getErrorReport()));
           } catch (OSCARSServiceException ex) {
               completeErrorReport(ex,updateStatusReq.getGlobalReservationId(),transId);
               OSCARSFaultUtils.handleError( ex, session, LOG, event);
           } catch (Exception ex) {
               OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
           }
           session.getTransaction().commit();
           LOG.debug(netLogger.end(event));
           response.setGlobalReservationId(updateStatusReq.getGlobalReservationId());
           response.setStatus(retStatus);
           return response;
       }

    /**
     * returns the status of a reservation
     * @param getStatusReq contains gri and  new status
     * @throws OSCARSFaultMessage if reservation is not found or state transition is not allowed
     * @seeAlso StateEngine for allowed state transitions
     */
    public GetStatusRespContent
        getStatus(GetStatusReqContent getStatusReq)
           throws OSCARSFaultMessage { 
        String event = "getStatus";
        String transId = getStatusReq.getTransactionId();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, transId );
        netLogger.setGRI(getStatusReq.getGlobalReservationId());
        LOG.debug(netLogger.start(event));
        GetStatusRespContent response = new GetStatusRespContent();
        response.setTransactionId(getStatusReq.getTransactionId());
        String retStatus = null;
        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            retStatus = mgr.getStatus(getStatusReq.getGlobalReservationId());
        } catch (OSCARSServiceException ex) {
            completeErrorReport(ex,getStatusReq.getGlobalReservationId(),transId);
            OSCARSFaultUtils.handleError( ex, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.debug(netLogger.end(event));
        response.setGlobalReservationId(getStatusReq.getGlobalReservationId());
        response.setStatus(retStatus);
        return response;
    }

    /**
      * returns any errorReport for a transaction
      * @param authConditions contains any limits on what the user can query, e.g only
     *     reservations that he owns, or that start and stop at his domain
      * @param getErrorReportReq contains the transaction id of the transaction to be queried
      * @throws OSCARSFaultMessage if errorReport is not found
      */
     public GetErrorReportResponseContent getErrorReport(AuthConditions authConditions,GetErrorReportContent getErrorReportReq)
            throws OSCARSFaultMessage {
         String event = "getErrorReport";
         String transId = getErrorReportReq.getTransactionId();
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         netLogger.init(moduleName, transId );
         LOG.debug(netLogger.start(event));
         GetErrorReportResponseContent response = new GetErrorReportResponseContent();
         response.setMessageProperties(getErrorReportReq.getMessageProperties());
         ResourceManager mgr = core.getResourceManager();
         Session session = core.getSession();
         try {
             session.beginTransaction();
             ErrorReport ret = mgr.getErrReportByTransId(authConditions, getErrorReportReq.getTransactionId());
             for (ErrorReport nextRep : ret){
                 response.getErrorReport().add(ErrorReport.report2fault(nextRep));
             }
         } catch (OSCARSServiceException ex) {
             completeErrorReport(ex,"UNKNOWN_GRI",transId);
             OSCARSFaultUtils.handleError( ex, session, LOG, event);
         } catch (Exception ex) {
             OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
         }
         session.getTransaction().commit();
         LOG.debug(netLogger.end(event));
         return response;
     }

    /**
     * Store a reservation into the database
     * @param storeReq contains the resDetails for the reservation
     * @return StoreRespContent no content, just the transaction Id
     * @throws OSCARSFaultMessage
     */
    public StoreRespContent store(StoreReqContent storeReq)
           throws OSCARSFaultMessage    { 
        
        String event = "storeReservation";
        String transId = storeReq.getTransactionId();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, transId);
        netLogger.setGRI(storeReq.getReservation().getGlobalReservationId());
        LOG.info(netLogger.start(event,
                                " status is " + storeReq.getReservation().getStatus()));

        StoreRespContent response = new StoreRespContent();
        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            mgr.store(storeReq.getReservation());
        
        } catch (OSCARSServiceException ex) {
            completeErrorReport(ex,storeReq.getReservation().getGlobalReservationId(),transId);
            OSCARSFaultUtils.handleError( ex, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.debug(netLogger.end(event));
        response.setTransactionId(storeReq.getTransactionId ());
        return response;
    }
    /**
     * queryReservation returns all the information about a requested reservation
     * @param authConditions - contains any limits on what the user can query, e.g only
     *     reservations that he owns, or that start and stop at his domain
     * @param queryReservationRequest contains the GlobalReservationId of the reservation to query
     * @return QueryResPeply contains the ResDetails for the reservation
     * @throws OSCARSFaultMessage including "access denied" and "No reservation matches requested gri"
     */
    public QueryResReply
        queryReservation(AuthConditions authConditions, QueryResContent queryReservationRequest)
            throws OSCARSFaultMessage { 

        String event ="queryReservation";
        String transId = queryReservationRequest.getMessageProperties().getGlobalTransactionId();
        String gri = queryReservationRequest.getGlobalReservationId();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        
        netLogger.init(moduleName, transId);
        netLogger.setGRI(gri);
        LOG.info(netLogger.start(event));
 
        QueryResReply queryRep = new QueryResReply();
        queryRep.setMessageProperties(queryReservationRequest.getMessageProperties());
        ResDetails resDetails = null;

        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            resDetails = mgr.query(authConditions,gri);
            queryRep.setReservationDetails(resDetails);
            // TODO maybe only need to call getErrorReport if Status = FAILED or UNKNOWN
            ErrorReport errRep = mgr.getErrReportByGRI(authConditions,gri);
            if (!errRep.isEmpty()){
                for (ErrorReport er: errRep) {
                    OSCARSFaultReport of = ErrorReport.report2fault(er);
                    queryRep.getErrorReport().add(of);
                }
            }
        } catch (OSCARSServiceException ex) {
            ErrorReport er = ex.getErrorReport();
            completeErrorReport(ex,gri,transId);
            OSCARSFaultUtils.handleError( ex, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return queryRep;
    }

    /**
     * cancelReservation checks to see if a cancel is allowed for this reservation; removes any
     *     pendingAction that might be in the Scheduler queue, and returns the resDetails of the
     *     requested reservation to the coordinator, so it can inform the PCEs
     * @param authConditions - contains any limits on what the user can cancel, e.g only
     *     reservations that he owns, or that start and stop at his domain
     * @param cancelReservationRequest the GlobalReservationId of the reservation to cancel
     * @return ResDetails - the information about the reservation
     * @throws OSCARSFaultMessage including "access denied" and "No reservation matches requested gri"
     */
    public RMCancelRespContent cancelReservation(AuthConditions authConditions, CancelResContent cancelReservationRequest)
            throws OSCARSFaultMessage { 

        String event ="cancelReservation";
        String transId = cancelReservationRequest.getMessageProperties().getGlobalTransactionId();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, transId);
        netLogger.setGRI(cancelReservationRequest.getGlobalReservationId());
        LOG.info(netLogger.start(event ));
        ResDetails resDetails = new ResDetails();
        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            resDetails = mgr.cancel(authConditions,cancelReservationRequest.getGlobalReservationId());
        
        } catch (OSCARSServiceException ex) {
            completeErrorReport(ex,cancelReservationRequest.getGlobalReservationId(),transId);
            OSCARSFaultUtils.handleError( ex, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        RMCancelRespContent response = new RMCancelRespContent();
        response.setTransactionId(cancelReservationRequest.getMessageProperties().getGlobalTransactionId());
        response.setReservation(resDetails);
        LOG.info(netLogger.end(event ));
        return response;
    }


    /**
     * modifyReservation checks to see if a modify is allowed for this reservation; removes any
     *     pendingAction that might be in the Scheduler queue, and returns the modified resDetails of the
     *     requested reservation to the coordinator, so it can inform the PCEs
     * @param authConditions - contains any limits on what the user can modify, e.g only
     *     reservations that he owns, or that start and stop at his domain
     * @param modifyReservationRequest the GlobalReservationId of the reservation to modify
     * @return ResDetails - the information about the reservation
     * @throws OSCARSFaultMessage including "access denied" and "No reservation matches requested gri"
     */
    public 
        ModifyResReply modifyReservation(AuthConditions authConditions, ModifyResContent modifyReservationRequest)
            throws OSCARSFaultMessage { 

        String event ="modifyReservation";
        String transId =  modifyReservationRequest.getMessageProperties().getGlobalTransactionId();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, transId);
        netLogger.setGRI(modifyReservationRequest.getGlobalReservationId());
        LOG.info(netLogger.start(event));
        String status = null;
        ModifyResReply response = new ModifyResReply();
        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            status = mgr.modify(authConditions,modifyReservationRequest.getGlobalReservationId());
        
        } catch (OSCARSServiceException ex) {
            completeErrorReport(ex,modifyReservationRequest.getGlobalReservationId(),transId);
            OSCARSFaultUtils.handleError( ex, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        response.setGlobalReservationId(modifyReservationRequest.getGlobalReservationId());
        response.setStatus(status);
        response.setMessageProperties(modifyReservationRequest.getMessageProperties());
        LOG.info(netLogger.end(event));
        return response;
    }

    /**
     * List all the reservations on this IDC that meet the input constraints.
     *
     * @param authConditions Contains user's login name and institution
     * @param request the listRequest. Includes an
     *  array of reservation statuses. a list of topology identifiers, a list
     *  of VLAN tags or ranges, start and end times, number of reservations
     *  requested, and offset of first reservation to return.  
     *
     * @return reply ListReply encapsulating server reply.
     * @throws OSCARSFaultMessage
     */
    public ListReply listReservations(AuthConditions authConditions, ListRequest request)
            throws OSCARSFaultMessage { 

        String event = "listReservations";
        String transId = request.getMessageProperties().getGlobalTransactionId();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, transId);
        LOG.info(netLogger.start(event));
        ResourceManager mgr = core.getResourceManager();
        Session session = core.getSession();
        ListReply response = new ListReply();
        List<ResDetails> reservations = new ArrayList<ResDetails>();

        Long startTime = request.getStartTime();;
        Long endTime = request.getEndTime();
        String userName = request.getUser();
        List<String> linkIds = request.getLinkId();
        List<VlanTag> inVlanTags = request.getVlanTag();
        List<String> statuses = request.getResStatus();
        List<String> vlanTags = new ArrayList<String>();
        if (linkIds != null && linkIds.size() != 0) {
            for (String linkId: linkIds) {
                linkId.trim();
            }
        }
        if (inVlanTags != null && inVlanTags.size() != 0) {
            for (VlanTag v: inVlanTags) {
                if (v != null) {
                    String s = v.getValue();
                    if (s != null && !s.trim().equals("")) {
                        vlanTags.add(s.trim());
                    }
                }
            }
        }
        if (statuses != null && statuses.size() != 0) {
            for (String s: statuses) {
                s.trim();
            }
        }
        int numRequested = (request.getResRequested() != null ? request.getResRequested() : 100) ;
        int resOffset = (request.getResOffset() !=null ? request.getResOffset() : 0 );
        String description = request.getDescription();

        try {
            session.beginTransaction();
            reservations = mgr.list(authConditions, numRequested, resOffset,
                    statuses,  description,  userName, linkIds, vlanTags, startTime, endTime);
            response.setTotalResults(reservations.size());
            for ( ResDetails res : reservations ) {
                response.getResDetails().add(res);
            }
        } catch (OSCARSServiceException ex) {
            completeErrorReport(ex,"UNKNOWN_GRI",transId);;
            OSCARSFaultUtils.handleError( ex, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        response.setMessageProperties(request.getMessageProperties());
        LOG.info(netLogger.end(event));
        return response;
    }

    public GetAuditDataRespContent
        getAuditData(GetAuditDataReqContent getAuditDataReq)
            throws OSCARSFaultMessage { 

        String event = "getAuditData";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, getAuditDataReq.getTransactionId());
        LOG.info(netLogger.start(event));
        try {
            GetAuditDataRespContent _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new OSCARSFaultMessage("OSCARSFaultMessage...");
    }

    private void completeErrorReport(OSCARSServiceException ex, String gri, String transId ) {
        ErrorReport errRep = ex.getErrorReport();
        errRep.setGRI(gri);
        errRep.setModuleName(moduleName);
        errRep.setTransId(transId);
        errRep.setTimestamp(System.currentTimeMillis()/1000);
        errRep.setDomainId(PathTools.getLocalDomainId());
    }
}
