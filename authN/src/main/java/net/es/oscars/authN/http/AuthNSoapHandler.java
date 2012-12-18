package net.es.oscars.authN.http;

import java.util.List;

import net.es.oscars.authN.common.AuthNCore;
import net.es.oscars.authN.common.AuthNException;
import net.es.oscars.authN.common.AuthNManager;
import net.es.oscars.authN.soap.gen.AuthNPortType;
import net.es.oscars.authN.soap.gen.VerifyDNReqType;
import net.es.oscars.authN.soap.gen.VerifyLoginReqType;
import net.es.oscars.authN.soap.gen.VerifyOrigReqType;
import net.es.oscars.authN.soap.gen.VerifyReply;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.soap.OSCARSFaultUtils;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.sharedConstants.ErrorCodes;

import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

@OSCARSNetLoggerize(moduleName=ModuleName.AUTHN)
@javax.jws.WebService(
    serviceName = ServiceNames.SVC_AUTHN,
    portName = "AuthNPort",
    targetNamespace = "http://oscars.es.net/OSCARS/authN",
    endpointInterface = "net.es.oscars.authN.soap.gen.AuthNPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class AuthNSoapHandler implements AuthNPortType {

    private static final Logger LOG = Logger.getLogger(AuthNSoapHandler.class);
    private AuthNCore core = AuthNCore.getInstance();

    /**
     * Checks the logidID and password of a user accessing the IDC via the WBUI interface
     * 
     * @param verifyLoginReq - contains a transactionId, the user login name and unencrypted password
     * @return VerfiyReply - contains the transactionId, and the Subject Attributes of the subject.
     * @throws OSCARSFaultMessage if the loginID is not recognized or the password is incorrect.
     *
     */
    public VerifyReply verifyLogin(VerifyLoginReqType verifyLoginReq)
            throws OSCARSFaultMessage    {

        String event = "verifyLogin";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = verifyLoginReq.getTransactionId();
        netLogger.init(ModuleName.AUTHN, transId);
        LOG.info(netLogger.start(event));
        AuthNManager mgr = core.getAuthNManager();
        VerifyReply reply = new VerifyReply();
        reply.setTransactionId(transId);
        Session session = core.getSession();
        try {
            session.beginTransaction();
            List<AttributeType> attributes =
                mgr.verifyLogin(verifyLoginReq.getLoginId().getLoginName(),
                                verifyLoginReq.getLoginId().getPassword());
            if (attributes != null ){
                SubjectAttributes subAttr = new SubjectAttributes();
                for (AttributeType attr: attributes) {
                    LOG.debug(netLogger.getMsg(event,"attr name: " + attr.getName() 
                               + "  attr value: "+ attr.getAttributeValue().get(0).toString()));
                    subAttr.getSubjectAttribute().add(attr);
                }
                reply.setSubjectAttributes(subAttr);
            }
        } catch (AuthNException ex) {
            ErrorReport errRep = new ErrorReport(ErrorCodes.AUTHORIZATION_FAILED, ex.getMessage(), ErrorReport.USER,
                                                 null, transId, System.currentTimeMillis()/1000L, ModuleName.AUTHN,
                                                 PathTools.getLocalDomainId());
            LOG.debug(netLogger.getMsg(event,errRep.toString()));
            OSCARSServiceException oEx = new OSCARSServiceException(errRep);
            OSCARSFaultUtils.handleError(oEx, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /**
     * handles request to verify the distinguished name and issues of the credential that
     * was used to sign a request message to the OSCARSIDC.
     * 
     * @param verifyDNReq - contains the transactionId, the credential's subject DN, and issuer DN
     * @return VerfiyReply - contains the transactionId, and the Subject Attributes of the subject.
     * @throws OSCARSFaultMessage if the SubectDN and Issuer DN are not recognized users in this domain.
     * 
     * @see net.es.oscars.authN.soap.gen.AuthNPortType
     */
    public VerifyReply verifyDN(VerifyDNReqType verifyDNReq) throws OSCARSFaultMessage {
        
        String event = "verifyDN";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = verifyDNReq.getTransactionId();
        netLogger.init(ModuleName.AUTHN, transId);
        LOG.info(netLogger.start(event,verifyDNReq.getDN().getSubjectDN()));
        AuthNManager mgr = core.getAuthNManager();
        VerifyReply reply = new VerifyReply();
        reply.setTransactionId(transId);
        Session session = core.getSession();
        try {
            session.beginTransaction();
            List<AttributeType> attributes =
                mgr.verifyDN(verifyDNReq.getDN());
            if (attributes != null ){
                SubjectAttributes subAttr = new SubjectAttributes();
                for (AttributeType attr: attributes) {
                    LOG.debug(netLogger.getMsg(event,"attr name: " + attr.getName() 
                            + "  attr value: "+ attr.getAttributeValue().get(0).toString()));
                    subAttr.getSubjectAttribute().add(attr);
                }
                reply.setSubjectAttributes(subAttr);
            }
        } catch (AuthNException ex) {
            ErrorReport errRep = new ErrorReport(ErrorCodes.AUTHORIZATION_FAILED, ex.getMessage(), ErrorReport.USER,
                                                 null, transId, System.currentTimeMillis()/1000L, ModuleName.AUTHN,
                                                 PathTools.getLocalDomainId());
            OSCARSServiceException oEx = new OSCARSServiceException(errRep);
            OSCARSFaultUtils.handleError(oEx, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /**
     *  Can be used to authenticate the attributes passed in MessageProperties
     *  Not implemented yet
     */
    public VerifyReply verifyOriginator(VerifyOrigReqType verifyOriginatorReq ) 
            throws OSCARSFaultMessage {
        
        String event = "verifyOriginator";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.AUTHN, verifyOriginatorReq.getTransactionId());
        LOG.debug(netLogger.start(event));
        LOG.error(netLogger.error(event,ErrSev.MINOR,"not implemented"));
        VerifyReply reply = new VerifyReply();
        reply.setTransactionId(verifyOriginatorReq.getTransactionId());
        reply.setSubjectAttributes(verifyOriginatorReq.getOriginator());
        return reply;
    }
}
