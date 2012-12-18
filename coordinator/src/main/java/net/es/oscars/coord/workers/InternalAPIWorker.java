package net.es.oscars.coord.workers;

import java.net.URL;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.utils.clients.InternalAPIClient;

import org.apache.log4j.Logger;

/**
 * InternalAPIworker
 * Used to send messages to a peer domain via the internal api
 * Used to forward requests and to sen InterDomainEvent messages
 */

public class InternalAPIWorker extends ModuleWorker {

    private static Logger        LOG             = Logger.getLogger(InternalAPIWorker.class.getName());
    private InternalAPIClient    internalClient  = null;
    private URL                  apiHost         = null;
    
    private static InternalAPIWorker instance;
    
    public static InternalAPIWorker getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new InternalAPIWorker();
        }
        return instance;
    }
    
    private InternalAPIWorker () throws OSCARSServiceException {
        LOG =  Logger.getLogger(InternalAPIWorker.class.getName());
        this.reconnect();
    }
    
    
    public InternalAPIClient getClient() {
        return this.internalClient;
    }

    public void reconnect() throws OSCARSServiceException {
        try {
            OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
            this.apiHost = Coordinator.getInstance().getInternalApiHost();
            URL wsdl = cc.getWSDLPath(ServiceNames.SVC_API_INTERNAL,null);
            LOG.debug (netLogger.getMsg("InternalAPIWorker.reconnect","API host= " + this.apiHost + 
                                    " WSDL= " + wsdl));
            this.internalClient = InternalAPIClient.getClient(this.apiHost,wsdl);
        } catch (Exception e) {
            throw new OSCARSServiceException (e);
        }
    }
    /**
     *  Sends an interDomainEvent
     * @param request the request that is sending the event
     * @param resDetails  details of the reservation that is being handled
     * @param requestType may be the type of the sending request or may be the event to send
     * @param destDomain  domain to send the message to
     * @throws OSCARSServiceException if invoke has failed to connect, OSCARSSoapService.invokes
     *          throws a OSE containing an ErrorReport
     */

    @SuppressWarnings("unchecked")
    public void sendEventContent (CoordRequest request,
                                  ResDetails resDetails,
                                  String requestType,
                                  String destDomain) throws OSCARSServiceException {
        
        InterDomainEventContent event = new InterDomainEventContent();
        
        if (requestType.equals(PCERequestTypes.PCE_CREATE)) {
            event.setType(NotifyRequestTypes.RESV_CREATE_CONFIRMED);
        } else if ( requestType.equals(PCERequestTypes.PCE_CREATE_COMMIT)) {
            event.setType(NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED);
        } else if ( requestType.equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
            event.setType(NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED);
        } else if ( requestType.equals(PCERequestTypes.PCE_MODIFY)) {
            event.setType(NotifyRequestTypes.RESV_MODIFY_CONFIRMED);
        } else {
            event.setType(requestType);
        }
        //event.setUserLogin((String) request.getAttribute(CoordRequest.LOGIN_ATTRIBUTE));
        event.setResDetails(resDetails);
        event.setErrorCode("ok");      
        event.setMessageProperties(request.getMessageProperties());

        Object[] req = new Object[] {event, destDomain};
        this.getClient().invoke("interDomainEvent", req);
        LOG.info (OSCARSNetLogger.getTlogger().end("InternalAPIWorker.sendEvent", "sending " +
                                                   event.getType() + " to domain " + destDomain));
    }

    /**
     *  Sends an interDomainEvent that contains an error notification
     * @param request the request that caused the error
     * @param resDetails  details of the reservation that is being handled
     * @param requestType may be the type of the request that caused the error  or
     *          may be the error to send.
     * @param errorMsg  string describing the error
     * @param errorSrc domain where the error originated
     * @param destDomain  domain to send the message to
     * @throws OSCARSServiceException if invoke has failed to connect, it will throw a OSE
     *          containing an ErrorReport
     */

    public void sendErrorEvent (CoordRequest request,
                                     ResDetails resDetails,
                                     String requestType,
                                     String errorMsg,
                                     String errorSrc,
                                     String destDomain) throws OSCARSServiceException {

       InterDomainEventContent event = new InterDomainEventContent();

       if (requestType.equals(PCERequestTypes.PCE_CREATE)) {
           event.setType(NotifyRequestTypes.RESV_CREATE_FAILED);
           event.setErrorCode(ErrorCodes.RESV_CREATE_FAILED);
       } else if ( requestType.equals(PCERequestTypes.PCE_CREATE_COMMIT)) {
           event.setType(NotifyRequestTypes.RESV_CREATE_FAILED);
           event.setErrorCode(ErrorCodes.RESV_CREATE_FAILED);
       } else if ( requestType.equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
           event.setType(NotifyRequestTypes.RESV_MODIFY_FAILED);
           event.setErrorCode(ErrorCodes.RESV_MODIFY_FAILED);
       } else if ( requestType.equals(PCERequestTypes.PCE_MODIFY)) {
           event.setType(NotifyRequestTypes.RESV_MODIFY_FAILED);
           event.setErrorCode(ErrorCodes.RESV_MODIFY_FAILED);
       } else if ( requestType.equals("createResvCompleted")) {
           event.setType(NotifyRequestTypes.RESV_CREATE_FAILED);
           event.setErrorCode(ErrorCodes.RESV_CREATE_FAILED);
       } else {
           event.setType(requestType);
           event.setErrorCode(requestType);
       }
       //event.setUserLogin((String) request.getAttribute(CoordRequest.LOGIN_ATTRIBUTE));
       event.setResDetails(resDetails);
       event.setErrorMessage(errorMsg);
       event.setErrorSource(errorSrc);
       event.setMessageProperties(request.getMessageProperties());

       Object[] req = new Object[] {event, destDomain};
       this.getClient().invoke("interDomainEvent", req);
       LOG.info (OSCARSNetLogger.getTlogger().end("InternalAPIWorker.sendErrorEvent ",  "sending error event " +
                                                   event.getType() + " to domain " + destDomain));
       }

    /**
     * sends an interDomain error event
     * @param errorReport filled in report of the error
     * @param destDomain domain to send the event to
     * @throws OSCARSServiceException if invoke has failed to connect, it will throw a OSE
     *          containing an ErrorReport
     */
    public void sendErrorEvent(CoordRequest request,
                               String notifyType,
                               ErrorReport errorReport,
                               String destDomain ) throws OSCARSServiceException {

        LOG.info (OSCARSNetLogger.getTlogger().start("InternalAPIWorker.sendErrorEvent",
                                                   notifyType + " to domain " + destDomain));
        InterDomainEventContent event = new InterDomainEventContent();
        ResDetails resDetails = new ResDetails();
        resDetails.setGlobalReservationId(errorReport.getGRI());
        event.setResDetails(resDetails);
        event.setErrorCode(errorReport.getErrorCode());
        event.setErrorSource(errorReport.getDomainId());
        event.setType(notifyType);
        event.setErrorMessage(errorReport.getErrorMsg());
        event.setMessageProperties(request.getMessageProperties());

        Object[] req = new Object[] {event, destDomain};
        this.getClient().invoke("interDomainEvent", req);
        LOG.info (OSCARSNetLogger.getTlogger().end("InternalAPIWorker.sendErrorEvent",  "sending " +
                                                   notifyType +" to domain " + destDomain));
    }

    @SuppressWarnings("unchecked")
    public void sendResCreateContent (CoordRequest request,
                                      ResDetails resDetails,
                                      String destDomain) throws OSCARSServiceException {

        LOG.info(OSCARSNetLogger.getTlogger().start("InternalAPIWorker.sendResCreateContent ","forwarding " +
                                                     request.getName()) + " to " + destDomain);
        ResCreateContent query = new ResCreateContent();
        String desc = (String) request.getAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE);
        query.setDescription (desc);
        query.setGlobalReservationId(request.getGRI());
     
        
        query.setUserRequestConstraint(resDetails.getUserRequestConstraint());
        query.setReservedConstraint(resDetails.getReservedConstraint());
        if (resDetails.getOptionalConstraint() != null) {
            // Optional constraints may not be defined.
            query.getOptionalConstraint().addAll(resDetails.getOptionalConstraint());
        }
        query.setMessageProperties(request.getMessageProperties());
        
        Object[] req = new Object[] {query, destDomain};
        this.getClient().invoke("createReservation", req);
        LOG.info(OSCARSNetLogger.getTlogger().end("InternalAPIWorker.sendResCreateContent", request.getName()));
    }

     public void sendResModifyContent (CoordRequest request,
                                       ResDetails resDetails,
                                       String destDomain) throws OSCARSServiceException {

         LOG.info(OSCARSNetLogger.getTlogger().start("InternalAPIWorker.sendResModifyContent ","forwarding " +
                                                     request.getName()) + " to " + destDomain);
        ModifyResContent query = new ModifyResContent();
        String desc = (String) request.getAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE);
        query.setDescription (desc);
        query.setGlobalReservationId(request.getGRI());


        query.setUserRequestConstraint(resDetails.getUserRequestConstraint());
        query.setReservedConstraint(resDetails.getReservedConstraint());
        if (resDetails.getOptionalConstraint() != null) {
            // Optional constraints may not be defined.
            query.getOptionalConstraint().addAll(resDetails.getOptionalConstraint());
        }
        query.setMessageProperties(request.getMessageProperties());

        Object[] req = new Object[] {query, destDomain};
        this.getClient().invoke("modifyReservation", req);
        LOG.info(OSCARSNetLogger.getTlogger().start("InternalAPIWorker.sendResModifyContent",request.getName()));
    }
    
    @SuppressWarnings("unchecked")
    public void sendCreatePath (CoordRequest request,
                                CreatePathContent query,
                                String destDomain) throws OSCARSServiceException {
        LOG.info(OSCARSNetLogger.getTlogger().start("InternalAPIWorker.sendCreatePath", "forwarding " +
                                                    request.getName() + " to " + destDomain));
        Object[] req = new Object[] {query, destDomain};
        this.getClient().invoke("createPath", req);
        LOG.info(OSCARSNetLogger.getTlogger().end("InternalAPIWorker.sendCreatePath", request.getName()));
    }
    
    
    @SuppressWarnings("unchecked")
    public void sendTeardownPath (CoordRequest request,
                                  TeardownPathContent query,
                                  String destDomain) throws OSCARSServiceException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG.info (OSCARSNetLogger.getTlogger().start("InternalAPIWorker.sendTeardownPath", "forwarding " +
                                                     request.getName() + " to " + destDomain));
        Object[] req = new Object[] {query, destDomain};
        this.getClient().invoke("teardownPath", req);
        LOG.info (OSCARSNetLogger.getTlogger().end("InternalAPIWorker.sendTeardownPath", request.getName()));
    }
    
    @SuppressWarnings("unchecked")
    public void sendCancelReservation (CoordRequest request,
                                CancelResContent query,
                                String destDomain) throws OSCARSServiceException {

        LOG.info (OSCARSNetLogger.getTlogger().start("InternalAPIWorker.sendCancelReservation", "forwarding " +
                                                     request.getName() + " to " + destDomain));
        Object[] req = new Object[] {query, destDomain};
        this.getClient().invoke("cancelReservation", req);
        LOG.info (OSCARSNetLogger.getTlogger().end("InternalAPIWorker.sendCancelReservation", request.getName()));
    }

}
