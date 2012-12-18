package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.soap.gen.UpdateFailureStatusReqContent;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusRespContent;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.log4j.*;
import net.sf.json.*;


 /**
 * Cancel Reservation servlet
 *
 * @author David Robertson, Mary Thompson
 *
 */

public class OverrideStatusReservation extends HttpServlet {
    private Logger log = Logger.getLogger(OverrideStatusReservation.class);

     /**
      * Handles OverrideStatusReservation servlet request.
      * Allows a OSCARS-engineer to reset the status of a broken reservation.
      *
      * @param servletRequest HttpServletRequest - contains gri and new status of reservation to fix
      * @param response HttpServletResponse -contains gri of reservation, success or error status
      */

    public void doGet(HttpServletRequest servletRequest, HttpServletResponse response)
        throws IOException, ServletException {

        String methodName = "OverrideStatusReservation";
        String transId = PathTools.getLocalDomainId() + "-WBUI-" + UUID.randomUUID().toString();
        OSCARSNetLogger netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_WBUI,transId);
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
        UserSession userSession = new UserSession(core);
        CheckSessionReply sessionReply =
           userSession.checkSession(out, authNPolicyClient, servletRequest,
                   methodName);
        if (sessionReply == null) {
           this.log.warn(netLogger.error(methodName, ErrSev.MINOR,"No user session: cookies invalid"));
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

        UpdateFailureStatusReqContent updateReq = new UpdateFailureStatusReqContent();
        String gri = servletRequest.getParameter("gri");
        String oldStatus = servletRequest.getParameter("status");
        status = servletRequest.getParameter("forcedStatus");
        String statusMessage = null;
        OSCARSFaultReport faultReport = ErrorReport.report2fault(new ErrorReport(ErrorCodes.UNKNOWN,
                                                  "Status changed from " + oldStatus + " to " + status + " by " + userName,
                                                  ErrorReport.SYSTEM,
                                                  gri,
                                                  transId,
                                                  System.currentTimeMillis()/1000L,
                                                  "WBUI",
                                                  PathTools.getLocalDomainId()));

        updateReq.setGlobalReservationId(gri);
        updateReq.setStatus(status);
        updateReq.setTransactionId(transId);
        updateReq.setErrorReport(faultReport);
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setOriginator(subjectAttrs);
        msgProps.setGlobalTransactionId(transId);
        try {
            // Send a updateStatus request
            Object[] req = new Object[]{subjectAttrs,updateReq};
            Object[] res = coordClient.invoke("forceUpdateStatus",req);
            statusMessage = ((UpdateStatusRespContent)res[0]).getStatus();
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }

        outputMap.put("gri", gri);
        outputMap.put("status", statusMessage);
        outputMap.put("statusReplace", statusMessage);
        StringBuilder sb = new StringBuilder();
        sb.append("<tbody>");
        sb.append("<tr><td> Error Code </td><td>" + faultReport.getErrorCode() + "</td></tr>");
        sb.append("<tr><td> Error Message </td><td>" + faultReport.getErrorMsg() + "</td></tr>");
        sb.append("<tr><td> Error Type </td><td>" + faultReport.getErrorType() + "</td></tr>");
        sb.append("</tbody>");
        outputMap.put("errorReportReplace", sb.toString());
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ":end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }
}
