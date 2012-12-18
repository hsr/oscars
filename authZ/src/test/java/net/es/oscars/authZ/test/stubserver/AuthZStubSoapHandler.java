package net.es.oscars.authZ.test.stubserver;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.hibernate.Session;

import net.es.oscars.utils.soap.OSCARSFaultUtils;
import net.es.oscars.authZ.beans.Attribute;
import net.es.oscars.authZ.common.AuthZCore;
import net.es.oscars.authZ.common.AuthZManager;
import net.es.oscars.authZ.http.AuthZSoapHandler;
import net.es.oscars.authZ.soap.gen.AuthZPortType;
import net.es.oscars.authZ.soap.gen.CheckAccessParams;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.authZ.soap.gen.CheckMultiAccessParams;
import net.es.oscars.authZ.soap.gen.MultiAccessPerm;
import net.es.oscars.authZ.soap.gen.MultiAccessPerms;
import net.es.oscars.authZ.soap.gen.PermType;
import net.es.oscars.authZ.soap.gen.ReqPermType;
import net.es.oscars.common.soap.gen.*;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.sharedConstants.AuthZConstants;

@javax.jws.WebService(
                      serviceName = ServiceNames.SVC_AUTHZ,
                      portName = "AuthZPort",
                      targetNamespace = "http://oscars.es.net/OSCARS/authZ",
                      endpointInterface = "net.es.oscars.authZ.soap.gen.AuthZPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")

public class AuthZStubSoapHandler implements AuthZPortType {

    private static final Logger LOG = Logger.getLogger(AuthZSoapHandler.class);

    /* (non-Javadoc)
     * @see net.es.oscars.authZ.soap.gen.AuthZPortType#checkAccess(net.es.oscars.authZ.soap.gen.CheckAccessParams  checkAccessReqMsg )*
     */
    public CheckAccessReply checkAccess(CheckAccessParams checkAccessReqMsg)
            throws OSCARSFaultMessage {

        LOG.debug("checkAccess.start");
        CheckAccessReply reply = new CheckAccessReply();
        reply.setPermission(AuthZConstants.ALL_USERS);
        return reply;
    }

    /* (non-Javadoc)
     * @see net.es.oscars.authZ.soap.gen.AuthZPortType#checkMultiAccess(net.es.oscars.authZ.soap.gen.CheckMultiAccessParams  checkMultiAccessReqMsg )*
     */
    public MultiAccessPerms
        checkMultiAccess(CheckMultiAccessParams checkMultiAccessReqMsg)
            throws OSCARSFaultMessage {

        LOG.debug("checkMultiAccess.start");
        MultiAccessPerms reply = new MultiAccessPerms();

        LOG.debug("checkMultiAccess.finish");
        return reply;
    }
}
