package net.es.oscars.wsnbroker.http;

import java.security.Principal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.PauseSubscription;
import org.oasis_open.docs.wsn.b_2.PauseSubscriptionResponse;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.ResumeSubscription;
import org.oasis_open.docs.wsn.b_2.ResumeSubscriptionResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.br_2.DestroyRegistration;
import org.oasis_open.docs.wsn.br_2.DestroyRegistrationResponse;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse;

import net.es.oscars.authN.soap.gen.DNType;
import net.es.oscars.authN.soap.gen.VerifyDNReqType;
import net.es.oscars.authN.soap.gen.VerifyReply;
import net.es.oscars.authZ.soap.gen.CheckAccessParams;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.wsnbroker.NotificationGlobals;
import net.es.oscars.wsnbroker.SubscriptionStatus;

import net.es.oscars.wsnbroker.soap.gen.InvalidFilterFault;
import net.es.oscars.wsnbroker.soap.gen.InvalidMessageContentExpressionFault;
import net.es.oscars.wsnbroker.soap.gen.InvalidProducerPropertiesExpressionFault;
import net.es.oscars.wsnbroker.soap.gen.InvalidTopicExpressionFault;
import net.es.oscars.wsnbroker.soap.gen.NotifyMessageNotSupportedFault;
import net.es.oscars.wsnbroker.soap.gen.PauseFailedFault;
import net.es.oscars.wsnbroker.soap.gen.PublisherRegistrationFailedFault;
import net.es.oscars.wsnbroker.soap.gen.PublisherRegistrationRejectedFault;
import net.es.oscars.wsnbroker.soap.gen.ResourceNotDestroyedFault;
import net.es.oscars.wsnbroker.soap.gen.ResourceUnknownFault;
import net.es.oscars.wsnbroker.soap.gen.ResumeFailedFault;
import net.es.oscars.wsnbroker.soap.gen.SubscribeCreationFailedFault;
import net.es.oscars.wsnbroker.soap.gen.TopicExpressionDialectUnknownFault;
import net.es.oscars.wsnbroker.soap.gen.TopicNotSupportedFault;
import net.es.oscars.wsnbroker.soap.gen.UnableToDestroySubscriptionFault;
import net.es.oscars.wsnbroker.soap.gen.UnacceptableInitialTerminationTimeFault;
import net.es.oscars.wsnbroker.soap.gen.UnacceptableTerminationTimeFault;
import net.es.oscars.wsnbroker.soap.gen.UnrecognizedPolicyRequestFault;
import net.es.oscars.wsnbroker.soap.gen.UnsupportedPolicyRequestFault;
import net.es.oscars.wsnbroker.soap.gen.WSNBrokerPortType;
import net.es.oscars.wsnbroker.utils.WSAddrParser;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.WSSecUtil;
import net.es.oscars.utils.svc.ServiceNames;

@javax.jws.WebService(
        serviceName = ServiceNames.SVC_WSNBROKER,
        portName = "WSNBrokerPort",
        targetNamespace = "http://oscars.es.net/OSCARS/wsnbroker",
        endpointInterface = "net.es.oscars.wsnbroker.soap.gen.WSNBrokerPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")

public class WSNBrokerSoapHandler implements WSNBrokerPortType {
    @Resource
    private WebServiceContext wsContext;
    private Logger log = Logger.getLogger(WSNBrokerSoapHandler.class);
    private NotificationGlobals globals;
    
    
    public WSNBrokerSoapHandler(){
        OSCARSNetLogger netLogger = this.initNetLogger();
        this.log.info(netLogger.start("init"));
        
        //init globals
        try {
            this.globals = NotificationGlobals.getInstance();
        } catch (Exception e) {
            this.log.info(netLogger.error("init", ErrSev.FATAL, e.getMessage()));
            System.exit(1);
        }
        
        this.log.info(netLogger.end("init"));
    }
    
    public void notify(Notify request)  {
        OSCARSNetLogger netLogger = this.initNetLogger();
        this.log.info(netLogger.start("NotificationSoapHandler.notify"));
        Connection conn = null;
        try{
            conn = this.globals.getConnection();
            SubjectAttributes subjAttrs = this.getAuthNAttributes();
            //throws exception if permission is denied
            this.authzOp(subjAttrs, AuthZConstants.SUBSCRIPTIONS, AuthZConstants.PUBLISHTO);
            for(NotificationMessageHolderType notifcation : request.getNotificationMessage()){
                this.globals.getNotifyMgr().processNotify(conn, notifcation);
            }
        }catch(Exception e){
            this.log.error(netLogger.error("NotificationSoapHandler.notify", ErrSev.MAJOR, e.getMessage()));
            this.globals.releaseDbConnection(conn);
            e.printStackTrace();
            return;
        }
        this.globals.releaseDbConnection(conn);
        this.log.info(netLogger.end("NotificationSoapHandler.notify"));
        
    }
    
    public SubscribeResponse subscribe(Subscribe request) throws TopicExpressionDialectUnknownFault,
            SubscribeCreationFailedFault,
            UnacceptableInitialTerminationTimeFault, TopicNotSupportedFault,
            InvalidMessageContentExpressionFault,
            UnrecognizedPolicyRequestFault,
            InvalidProducerPropertiesExpressionFault,
            UnsupportedPolicyRequestFault, InvalidTopicExpressionFault,
            ResourceUnknownFault, NotifyMessageNotSupportedFault,
            InvalidFilterFault {
        
        OSCARSNetLogger netLogger = this.initNetLogger();
        this.log.info(netLogger.start("NotificationSoapHandler.subscribe"));
        SubscribeResponse response = new SubscribeResponse();
        Connection conn = null;
        try{
            conn = this.globals.getConnection();
            SubjectAttributes subjAttrs = this.getAuthNAttributes();
            //throws exception if permission is denied
            this.authzOp(subjAttrs, AuthZConstants.SUBSCRIPTIONS, AuthZConstants.CREATE);
            response = this.globals.getSubscribeMgr().create(conn, request, 
                    subjAttrs);
        }catch(Exception e){
            this.log.error(netLogger.error("NotificationSoapHandler.subscribe", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            this.globals.releaseDbConnection(conn);
            throw new SubscribeCreationFailedFault(e.getMessage());
        }
        this.globals.releaseDbConnection(conn);
        this.log.info(netLogger.end("NotificationSoapHandler.subscribe"));
        return response;
    }

    public UnsubscribeResponse unsubscribe(Unsubscribe request)
            throws UnableToDestroySubscriptionFault, ResourceUnknownFault {
        
        OSCARSNetLogger netLogger = this.initNetLogger();
        this.log.info(netLogger.start("NotificationSoapHandler.unsubscribe"));
        UnsubscribeResponse response = new UnsubscribeResponse();
        Connection conn = null;
        try{
            conn = this.globals.getConnection();
            SubjectAttributes subjAttrs = this.getAuthNAttributes();
            AuthConditions authConds =
                this.authzOp(subjAttrs, AuthZConstants.SUBSCRIPTIONS, AuthZConstants.MODIFY);
            response.setSubscriptionReference(request.getSubscriptionReference());   
            this.globals.getSubscribeMgr().changeStatus(conn, request.getSubscriptionReference(), 
                    SubscriptionStatus.INACTIVE_STATUS, subjAttrs, authConds);
        }catch(Exception e){
            this.log.error(netLogger.error("NotificationSoapHandler.unsubscribe", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            this.globals.releaseDbConnection(conn);
            throw new UnableToDestroySubscriptionFault(e.getMessage());
        }
        this.globals.releaseDbConnection(conn);
        this.log.info(netLogger.end("NotificationSoapHandler.unsubscribe"));
        
        return response;
    }
    
    public RenewResponse renew(Renew request) throws UnacceptableTerminationTimeFault, ResourceUnknownFault {
     
        OSCARSNetLogger netLogger = this.initNetLogger();
        this.log.info(netLogger.start("NotificationSoapHandler.renew"));
        RenewResponse response = new RenewResponse();
        Connection conn = null;
        try{
            conn = this.globals.getConnection();
            SubjectAttributes subjAttrs = this.getAuthNAttributes();
            AuthConditions authConds =
                this.authzOp(subjAttrs, AuthZConstants.SUBSCRIPTIONS, AuthZConstants.MODIFY);
            response = this.globals.getSubscribeMgr().renew(conn,
                    WSAddrParser.getAddress(request.getSubscriptionReference()), 
                    request.getTerminationTime(), authConds);

        }catch(Exception e){
            this.log.error(netLogger.error("NotificationSoapHandler.renew", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            this.globals.releaseDbConnection(conn);
            throw new ResourceUnknownFault(e.getMessage());
        }
        this.globals.releaseDbConnection(conn);
        this.log.info(netLogger.end("NotificationSoapHandler.renew"));
     
        return response;
    }
    
    public PauseSubscriptionResponse pauseSubscription(PauseSubscription request)
            throws ResourceUnknownFault, PauseFailedFault {
        OSCARSNetLogger netLogger = this.initNetLogger();
        this.log.info(netLogger.start("NotificationSoapHandler.pauseSubscription"));
        
        PauseSubscriptionResponse response = new PauseSubscriptionResponse();
        Connection conn = null;
        try{
            conn = this.globals.getConnection();
            SubjectAttributes subjAttrs = this.getAuthNAttributes();
            AuthConditions authConds =
                this.authzOp(subjAttrs, AuthZConstants.SUBSCRIPTIONS, AuthZConstants.MODIFY);
            response.setSubscriptionReference(request.getSubscriptionReference());   
            this.globals.getSubscribeMgr().changeStatus(conn, request.getSubscriptionReference(), 
                    SubscriptionStatus.PAUSED_STATUS, subjAttrs, authConds);
        }catch(Exception e){
            this.log.error(netLogger.error("NotificationSoapHandler.pauseSubscription", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            this.globals.releaseDbConnection(conn);
            throw new PauseFailedFault(e.getMessage());
        }
        this.globals.releaseDbConnection(conn);
        this.log.info(netLogger.end("NotificationSoapHandler.pauseSubscription"));
        return response;
    }

    public ResumeSubscriptionResponse resumeSubscription(ResumeSubscription request)
            throws ResumeFailedFault, ResourceUnknownFault {
        OSCARSNetLogger netLogger = this.initNetLogger();
        this.log.info(netLogger.start("NotificationSoapHandler.resumeSubscription"));
        
        ResumeSubscriptionResponse response = new ResumeSubscriptionResponse();
        Connection conn = null;
        try{
            conn = this.globals.getConnection();
            SubjectAttributes subjAttrs = this.getAuthNAttributes();
            AuthConditions authConds =
                this.authzOp(subjAttrs, AuthZConstants.SUBSCRIPTIONS, AuthZConstants.MODIFY);
            response.setSubscriptionReference(request.getSubscriptionReference());
            this.globals.getSubscribeMgr().changeStatus(conn, request.getSubscriptionReference(), 
                    SubscriptionStatus.ACTIVE_STATUS, subjAttrs, authConds);
        }catch(Exception e){
            this.log.error(netLogger.error("NotificationSoapHandler.resumeSubscription", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            this.globals.releaseDbConnection(conn);
            throw new ResumeFailedFault(e.getMessage());
        }
        this.globals.releaseDbConnection(conn);
        this.log.info(netLogger.end("NotificationSoapHandler.resumeSubscription"));
        
        return response;
    }
    
/* public OscarsListSubscriptionsResponse listSubscriptions(
            OscarsListSubscriptions request) throws OSCARSFaultMessage {
        
        OSCARSNetLogger netLogger = this.initNetLogger(request.getMessageProperties());
        OscarsListSubscriptionsResponse response = null;
        this.log.info(netLogger.start("NotificationSoapHandler.listSubscriptions"));
        try{
            response = this.globals.getSubscribeMgr().list(request);
        }catch(Exception e){
            this.log.error(netLogger.error("NotificationSoapHandler.listSubscriptions", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            throw new OSCARSFaultMessage(e.getMessage());
        }
        this.log.info(netLogger.end("NotificationSoapHandler.listSubscriptions"));
        
        return response;
    }
*/
    
    public RegisterPublisherResponse registerPublisher(RegisterPublisher arg0)
            throws UnacceptableInitialTerminationTimeFault,
        TopicNotSupportedFault, PublisherRegistrationRejectedFault,
        InvalidTopicExpressionFault, ResourceUnknownFault,
        PublisherRegistrationFailedFault {
        // TODO Auto-generated method stub
        return null;
    }

    public DestroyRegistrationResponse destroyRegistration(
            DestroyRegistration arg0) throws ResourceUnknownFault,
            ResourceNotDestroyedFault {
        // TODO Auto-generated method stub
        return null;
    }
    
    private OSCARSNetLogger initNetLogger() {
        OSCARSNetLogger netLogger = new OSCARSNetLogger(ModuleName.WSNBROKER, UUID.randomUUID().toString());
        OSCARSNetLogger.setTlogger(netLogger);
        
        return netLogger;
    }
    
    private SubjectAttributes getAuthNAttributes() throws OSCARSServiceException{
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        HashMap<String,String> netLogProps = new HashMap<String, String>();
        
        this.log.info(netLogger.start("getAuthNAttributes"));
        String userDN = null;
        String issuerDN= null;
        try {
            HashMap<String, Principal> principals = WSSecUtil.getSecurityPrincipals(wsContext);
            userDN = principals.get("subject").getName();
            issuerDN = principals.get("issuer").getName();
            netLogProps.put("userDN", userDN);
            netLogProps.put("issuerDN", issuerDN);
        } catch (Exception e ){
            this.log.error(netLogger.error("getAuthNAttributes",ErrSev.MAJOR, e.getMessage()));
            throw new OSCARSServiceException(e);
        }
        AuthNClient authNClient = this.globals.getAuthNClient();
        if(authNClient == null){
            this.log.info(netLogger.end("getAuthNAttributes", 
                    "Did not contact authN service because not configured."));
            return null;
        }
        VerifyDNReqType verifyDNReq = new VerifyDNReqType();
        DNType DN = new DNType();
        DN.setSubjectDN(userDN);
        DN.setIssuerDN(issuerDN);
        verifyDNReq.setDN(DN);
        verifyDNReq.setTransactionId(netLogger.getGUID());
        Object[] req = new Object[]{verifyDNReq};
        Object[] res = null;
        try{
            res = authNClient.invoke("verifyDN",req);
        }catch(Exception e){
            this.log.error(netLogger.error("getAuthNAttributes",ErrSev.MAJOR, e.getMessage(), this.globals.getAuthNUrl(), netLogProps));
            throw new OSCARSServiceException(e);
        }
        VerifyReply reply = (VerifyReply)res[0];
        SubjectAttributes subjectAttrs = reply.getSubjectAttributes();
        
        this.log.info(netLogger.end("getAuthNAttributes", null, this.globals.getAuthNUrl(), netLogProps));
        return subjectAttrs;
    }
    
    private AuthConditions authzOp(SubjectAttributes attrs, 
            String resource, String permission) throws OSCARSServiceException{
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        HashMap<String,String> netLogProps = new HashMap<String, String>();
        this.log.info(netLogger.start("authzOp"));
        //if no attributes just return
        if(attrs == null){
            this.log.info(netLogger.end("authzOp"));
            return null;
        }

        AuthZClient authZClient = NotificationGlobals.getInstance().getAuthZClient();
        if(authZClient == null){
            this.log.info(netLogger.end("authzOp", 
                    "Did not contact authZ service because not configured."));
            return null;
        }
        CheckAccessParams checkAccessReq = new CheckAccessParams();
        checkAccessReq.setTransactionId(netLogger.getGUID());
        checkAccessReq.setSubjectAttrs(attrs);
        checkAccessReq.setResourceName(resource);
        checkAccessReq.setPermissionName(permission);
        CheckAccessReply checkAccessReply = null;
        try{
            Object[] req = new Object[]{checkAccessReq};
            Object[] res = authZClient.invoke("checkAccess", req);
            checkAccessReply = (CheckAccessReply)res[0];
        }catch(Exception e){
            this.log.error(netLogger.error("authzOp",ErrSev.MAJOR, e.getMessage(), 
                    NotificationGlobals.getInstance().getAuthZUrl(), netLogProps));
            throw new OSCARSServiceException(e);
        }
        
        if(AuthZConstants.ACCESS_DENIED.equals(checkAccessReply.getPermission())){
            String errMsg = "Permision denied for for operation " + permission;
            this.log.error(netLogger.error("authzOp",ErrSev.MAJOR, errMsg, 
                    NotificationGlobals.getInstance().getAuthZUrl(), netLogProps));
            throw new OSCARSServiceException(errMsg);
        }
        
        this.log.info(netLogger.end("authzOp", null, 
                NotificationGlobals.getInstance().getAuthZUrl(), netLogProps));
     
        return checkAccessReply.getConditions();
    }
}
