package net.es.oscars.authN.http.policy;

import java.util.List;

import net.es.oscars.utils.soap.OSCARSServiceException;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import net.es.oscars.authCommonPolicy.soap.gen.AttrDetails;
import net.es.oscars.authCommonPolicy.soap.gen.ListAttrsReply;
import net.es.oscars.authCommonPolicy.soap.gen.ModifyAttrDetails;
import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.authN.beans.Institution;
import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.common.AuthNCore;
import net.es.oscars.authN.common.PolicyManager;
import net.es.oscars.authN.common.AuthNException;
import net.es.oscars.authN.soap.gen.policy.AttrReply;
import net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType;
import net.es.oscars.authN.soap.gen.policy.FullUserParams;
import net.es.oscars.authN.soap.gen.policy.ListAttrsRequest;
import net.es.oscars.authN.soap.gen.policy.ListInstsReply;
import net.es.oscars.authN.soap.gen.policy.ListUsersParams;
import net.es.oscars.authN.soap.gen.policy.ListUsersReply;
import net.es.oscars.authN.soap.gen.policy.ModifyInstParams;
import net.es.oscars.authN.soap.gen.policy.QueryUserReply;
import net.es.oscars.authN.soap.gen.policy.SessionOpParams;
import net.es.oscars.authN.soap.gen.policy.UserDetails;
import net.es.oscars.common.soap.gen.EmptyArg;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.soap.OSCARSFaultUtils;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.sharedConstants.ErrorCodes;

@OSCARSNetLoggerize(moduleName=ModuleName.AUTHNP)
@javax.jws.WebService(
    serviceName = ServiceNames.SVC_AUTHN_POLICY,
    portName = "AuthNPolicyPort",
    targetNamespace = "http://oscars.es.net/OSCARS/authNPolicy",
    endpointInterface = "net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")

public class AuthNPolicySoapHandler implements AuthNPolicyPortType {

    private static final Logger LOG = Logger.getLogger(AuthNPolicySoapHandler.class);
    private AuthNCore core = AuthNCore.getInstance(); // shares core configuration with AuthN

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#setSession(net.es.oscars.authN.soap.gen.policy.SessionOpParams  setSessionReqMsg )*
     */
    public EmptyArg setSession(SessionOpParams setSessionReqMsg)
            throws OSCARSFaultMessage {

        
        String event = "PolicyManager:setSession";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        EmptyArg reply = new EmptyArg();
        PolicyManager mgr = core.getPolicyManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            String userName = setSessionReqMsg.getUserName();
            String sessionName = setSessionReqMsg.getSessionName();
            mgr.setSession(userName, sessionName);
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event );
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#validSession(net.es.oscars.authN.soap.gen.policy.SessionOpParams  validSessionReqMsg )*
     */
    public AttrReply validSession(SessionOpParams validSessionReqMsg)
            throws OSCARSFaultMessage    {

        String event = "PolicyManager:validSession";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        AttrReply reply = new AttrReply();
        PolicyManager mgr = core.getPolicyManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            String userName = validSessionReqMsg.getUserName();
            String sessionName = validSessionReqMsg.getSessionName();
            List<AttributeType> attributes =
                mgr.validSession(userName, sessionName);
            if (attributes == null ){
                throw new OSCARSFaultMessage ("user " + userName +
                                                  " has no attributes");
            }
            for (AttributeType attr: attributes) {
                reply.getSubjectAttributes().add(attr);
            }
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#addUser(net.es.oscars.authN.soap.gen.policy.FullUserParams  addUserReqMsg )*
     */
    public EmptyArg addUser(FullUserParams addUserReqMsg)
            throws OSCARSFaultMessage    {

        String event = "PolicyManager:addUser";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        EmptyArg reply = new EmptyArg();
        PolicyManager mgr = core.getPolicyManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            UserDetails userDetails = addUserReqMsg.getUserDetails();
            User user = this.userReqToUser(userDetails);
            mgr.createUser(user, userDetails.getInstitution(),
                           addUserReqMsg.getNewAttributes());
        } catch (AuthNException ex) {
            ErrorReport errRep = new ErrorReport(ErrorCodes.INVALID_PARAM,ex.getMessage(),ErrorReport.USER);
            OSCARSServiceException oex = new OSCARSServiceException(errRep);
            OSCARSFaultUtils.handleError ( oex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#queryUser(java.lang.String  queryUserReqMsg )*
     */
    public QueryUserReply queryUser(String userName)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:queryUser";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.debug(netLogger.start(event));
        Session session = core.getSession();
        QueryUserReply reply = new QueryUserReply();
        PolicyManager mgr = core.getPolicyManager();
        try {
            session.beginTransaction();
            User user = mgr.queryUser(userName);
            UserDetails userReply = this.userToUserReply(user);
            List<AttributeType> attributes = mgr.queryUserAttrs(userName);
            AttrReply attrReply = new AttrReply();
            List<AttributeType> replyAttrs = attrReply.getSubjectAttributes();
            for (AttributeType attr: attributes) {
                replyAttrs.add(attr);
            }
            reply.setUserDetails(userReply);
            reply.setUserAttributes(attrReply);
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.debug(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#listUsers(net.es.oscars.authN.soap.gen.policy.ListUsersParams  listUsersReqMsg )*
     */
    public ListUsersReply listUsers(ListUsersParams listUsersReqMsg)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:listUsers";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.debug(netLogger.start(event));
        List<User> users = null;
        ListUsersReply reply = new ListUsersReply();
        PolicyManager mgr = core.getPolicyManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            String attributeValue = listUsersReqMsg.getAttribute();
            String institutionName = listUsersReqMsg.getInstitution();
            users = mgr.listUsers(attributeValue, institutionName);
            for (User user: users) {
                UserDetails userDetails = this.userToUserReply(user);
                reply.getUserDetails().add(userDetails);
            }
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.debug(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#modifyUser(net.es.oscars.authN.soap.gen.policy.FullUserParams  modifyUserReqMsg )*
     */
    public EmptyArg modifyUser(FullUserParams modifyUserReqMsg)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:modifyUser";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        PolicyManager mgr = core.getPolicyManager();
        Session session = core.getSession();
        EmptyArg reply = new EmptyArg();
        try {
            session.beginTransaction();
            UserDetails userDetails = modifyUserReqMsg.getUserDetails();
            List<String> curRoles = modifyUserReqMsg.getCurAttributes();
            List<String> newRoles = modifyUserReqMsg.getNewAttributes();
            User user = this.userReqToUser(userDetails);
            mgr.modifyUser(user, curRoles, newRoles,
                           userDetails.getInstitution(),
                           modifyUserReqMsg.isPasswordChanged());
        } catch (AuthNException ex) {
            ErrorReport errRep = new ErrorReport(ErrorCodes.INVALID_PARAM,ex.getMessage(),ErrorReport.USER);
            OSCARSServiceException oex = new OSCARSServiceException(errRep);
            OSCARSFaultUtils.handleError ( oex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#removeUser(java.lang.String  removeUserReqMsg )*
     */
    public EmptyArg removeUser(String userName)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:removeUser";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        EmptyArg reply = new EmptyArg();
        PolicyManager mgr = core.getPolicyManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            mgr.removeUser(userName);
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#addInst(java.lang.String  addInstReqMsg )*
     */
    public EmptyArg addInst(String institutionName)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:addInst";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        EmptyArg reply = new EmptyArg();
        PolicyManager mgr = core.getPolicyManager();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            mgr.createInstitution(institutionName);
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#listInsts(net.es.oscars.authN.soap.gen.policy.EmptyArg  listInstsReqMsg )*
     */
    public ListInstsReply listInsts(EmptyArg listInstsReqMsg)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:listInst";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.debug(netLogger.start(event));
        Session session = core.getSession();
        PolicyManager mgr = core.getPolicyManager();
        ListInstsReply reply = new ListInstsReply();
        try {
            session.beginTransaction();
            List<Institution> insts = mgr.listInstitutions();
            for (Institution inst: insts) {
                reply.getName().add(inst.getName());
            }
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.debug(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#modifyInst(net.es.oscars.authN.soap.gen.policy.ModifyInstParams  modifyInstReqMsg )*
     */
    public EmptyArg modifyInst(ModifyInstParams modifyInstReqMsg)
            throws OSCARSFaultMessage {
        
        String event = "PolicyManager:modifyInst";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        EmptyArg reply = new EmptyArg();
        Session session = core.getSession();
        PolicyManager mgr = core.getPolicyManager();
        try {
            session.beginTransaction();
            mgr.modifyInstitution(modifyInstReqMsg.getOldName(),
                                  modifyInstReqMsg.getNewName());
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session,LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }
    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#removeInst(java.lang.String  removeInstReqMsg )*
     */
    public EmptyArg removeInst(String institutionName)
            throws OSCARSFaultMessage {
        
        String event = "PolicyManager:removeInst";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        EmptyArg reply = new EmptyArg();
        Session session = core.getSession();
        PolicyManager mgr = core.getPolicyManager();
        try {
            session.beginTransaction();
            mgr.removeInstitution(institutionName);
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#addAttr(net.es.oscars.authN.soap.gen.policy.AttrDetails  addAttrReqMsg )*
     */
    public EmptyArg addAttr(AttrDetails addAttrReqMsg)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:addAttr";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        EmptyArg reply = new EmptyArg();
        Session session = core.getSession();
        Attribute attribute = new Attribute();
        attribute.setAttrId(addAttrReqMsg.getAttrId());
        attribute.setValue(addAttrReqMsg.getValue());
        attribute.setDescription(addAttrReqMsg.getDescription());
        PolicyManager mgr = core.getPolicyManager();
        try {
            session.beginTransaction();
            mgr.createAttribute(attribute);
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#listAttrs(net.es.oscars.authN.soap.gen.policy.ListAttrsRequest  listAttrsReqMsg )*
     */
    public ListAttrsReply listAttrs(ListAttrsRequest listAttrsReqMsg)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:listAttrs";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.debug(netLogger.start(event));
        PolicyManager mgr = core.getPolicyManager();
        List<Attribute> attributes = null;
        ListAttrsReply reply = new ListAttrsReply();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            String param = listAttrsReqMsg.getParam();
            String value = listAttrsReqMsg.getValue();
            attributes = mgr.listAttributes(param, value);
            for (Attribute attribute: attributes) {
                AttrDetails attrDetails = new AttrDetails();
                attrDetails.setAttrId(attribute.getAttrId());
                attrDetails.setValue(attribute.getValue());
                attrDetails.setDescription(attribute.getDescription());
                reply.getAttribute().add(attrDetails);
            }
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.debug(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#modifyAttr(net.es.oscars.authN.soap.gen.policy.ModifyAttrDetails  modifyAttrReqMsg )*
     */
    public EmptyArg modifyAttr(ModifyAttrDetails modifyAttrReqMsg)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:modifyAttr";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        PolicyManager mgr = core.getPolicyManager();
        EmptyArg reply = new EmptyArg();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            AttrDetails oldReqAttr = modifyAttrReqMsg.getOldAttrInfo();
            AttrDetails modReqAttr = modifyAttrReqMsg.getModAttrInfo();
            Attribute oldAttribute = new Attribute();
            oldAttribute.setAttrId(oldReqAttr.getAttrId());
            oldAttribute.setValue(oldReqAttr.getValue());
            oldAttribute.setDescription(oldReqAttr.getDescription());
            Attribute modAttribute = new Attribute();
            modAttribute.setAttrId(modReqAttr.getAttrId());
            modAttribute.setValue(modReqAttr.getValue());
            modAttribute.setDescription(modReqAttr.getDescription());
            mgr.modifyAttribute(oldAttribute, modAttribute);
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType#removeAttr(java.lang.String  removeAttrReqMsg )*
     */
    public EmptyArg removeAttr(String attributeValue)
            throws OSCARSFaultMessage {

        String event = "PolicyManager:removeAttr";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String transId = "TBD";
        netLogger.init(ModuleName.AUTHNP, transId);
        LOG.info(netLogger.start(event));
        PolicyManager mgr = core.getPolicyManager();
        EmptyArg reply = new EmptyArg();
        Session session = core.getSession();
        try {
            session.beginTransaction();
            mgr.removeAttribute(attributeValue);
        } catch (AuthNException ex) {
            OSCARSFaultUtils.handleError ( ex, true, session, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError ( ex, false, session, LOG, event);
        }
        session.getTransaction().commit();
        LOG.info(netLogger.end(event));
        return reply;
    }

    /**
     * Builds Axis2 UserDetails class, given Hibernate User bean.
     *
     * @param user A Hibernate User instance
     * @return UserDetails instance
     */
    private UserDetails userToUserReply(User user) {
        UserDetails reply = new UserDetails();
        reply.setLogin(user.getLogin());
        reply.setPassword(user.getPassword());
        reply.setCertIssuer(user.getCertIssuer());
        reply.setCertSubject(user.getCertSubject());
        reply.setLastName(user.getLastName());
        reply.setFirstName(user.getFirstName());
        reply.setEmailPrimary(user.getEmailPrimary());
        reply.setPhonePrimary(user.getPhonePrimary());
        reply.setDescription(user.getDescription());
        reply.setEmailSecondary(user.getEmailSecondary());
        reply.setPhoneSecondary(user.getPhoneSecondary());
        reply.setInstitution(user.getInstitution().getName());
        return reply;
    }

    /**
     * Builds Hibernate bean, given Axis2 UserDetails class.
     *
     * @param userDetails An Axis 2 UserDetails instance
     * @return a Hibernate User instance
     */
    private User userReqToUser(UserDetails userDetails) {
        User user = new User();
        user.setLogin(userDetails.getLogin());
        user.setPassword(userDetails.getPassword());
        user.setCertIssuer(userDetails.getCertIssuer());
        user.setCertSubject(userDetails.getCertSubject());
        user.setLastName(userDetails.getLastName());
        user.setFirstName(userDetails.getFirstName());
        user.setEmailPrimary(userDetails.getEmailPrimary());
        user.setPhonePrimary(userDetails.getPhonePrimary());
        user.setPassword(userDetails.getPassword());
        user.setDescription(userDetails.getDescription());
        user.setEmailSecondary(userDetails.getEmailSecondary());
        user.setPhoneSecondary(userDetails.getPhoneSecondary());
        return user;
    }

}
