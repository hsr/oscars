package edu.internet2.ion.ionui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;
import net.sf.json.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;

import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.es.oscars.wbui.servlets.CheckSessionReply;
/**
 * Cancel Reservation servlet
 *
 */

public class IONCancelReservation extends HttpServlet {
    private Logger log = Logger.getLogger(IONCancelReservation.class);

    /**
     * Handles ION CancelReservation request.
     *
     * @param request HttpServletRequest - contains gri of reservation to cancel
     * @param response HttpServletResponse -contains gri of reservation, success or error status
     */
    public void
        doGet(HttpServletRequest servletRequest, HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        String methodName = "IONCancelReservation";
        String transId = PathTools.getLocalDomainId() + "-IONUI-" + UUID.randomUUID().toString();
        OSCARSNetLogger netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_IONUI,transId);
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.info(netLogger.start(methodName));
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
 
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        String status = "unknown";
        ServletCore core = ServletCore.getInstance();
        getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        CoordClient coordClient = core.getCoordClient();
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZClient authZClient = core.getAuthZClient();
        UserSession userSession = new UserSession(core);
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, servletRequest,
                    methodName);
        if (sessionReply == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid"));
            return;
        }
        String userName = sessionReply.getUserName();
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid"));
            return;
        }

        List<AttributeType> userAttributes = sessionReply.getAttributes(); 
        SubjectAttributes subjectAttrs = new SubjectAttributes();
        List<AttributeType> reqAttrs = subjectAttrs.getSubjectAttribute();
        for (AttributeType attr: userAttributes) {
            reqAttrs.add(attr);
        }
        CancelResContent cancelReq =  new CancelResContent();
        String gri = servletRequest.getParameter("gri");
        cancelReq.setGlobalReservationId(gri);
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setOriginator(subjectAttrs);
        msgProps.setGlobalTransactionId(transId);
        cancelReq.setMessageProperties(msgProps);
        String authVal = null;
        try {
            // Send a cancelReservation request 
            Object[] req = new Object[]{subjectAttrs,cancelReq};
            Object[] res = coordClient.invoke("cancelReservation",req);
            status = ((CancelResReply)res[0]).getStatus();
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
	outputMap.put("gri", gri);
        outputMap.put("status", "Reservation " + gri + " cancellation: " +
                                status); 
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(netLogger.end(methodName));
        return;
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
