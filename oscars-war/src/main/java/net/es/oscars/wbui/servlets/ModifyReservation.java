package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import net.es.oscars.api.soap.gen.v06.ModifyResReply;
import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.soap.OSCARSServiceException;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.log4j.*;
import net.sf.json.*;

import net.es.oscars.wbui.servlets.UserSession;


/**
 * Servlet to handle a modifyReservation request
 * Parses the servletRequest, check the session validity, gets the
 * user's attributes and calls the Coordinator client to modify the reservation.
 * @author davidr,mrt
 *
 */

public class ModifyReservation extends HttpServlet {
    private Logger log = Logger.getLogger(ModifyReservation.class);

     /**
     * doGet
     *
     * @param request HttpServlerRequest - contains start and end times, bandwidth,
     * description and pathInfo (not used)
     * @param response HttpServler Response - contain gri and success or error status
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String methodName = "ModifyReservation";
         String transId  = PathTools.getLocalDomainId() + "-WBUI-" +
                           UUID.randomUUID().toString();
        OSCARSNetLogger netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_WBUI, transId);
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.info(netLogger.start(methodName));

        PrintWriter out = response.getWriter();
        ServletCore core = (ServletCore)
              getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        /* put transId in core so that the queryReservationStatus servlets that are run as a consequence
           of modifyReservation will use same transId.*/
        core.setTransId(transId);
        UserSession userSession = new UserSession(core);
        CoordClient coordClient = core.getCoordClient();
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();

        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, request,
                                     methodName);
        if (sessionReply == null) {
            this.log.warn(netLogger.error(methodName, ErrSev.MINOR,"No user session: cookies invalid"));
            return;
        }

        response.setContentType("application/json");

        List<AttributeType> userAttributes = sessionReply.getAttributes();
        SubjectAttributes subjectAttrs = new SubjectAttributes();
        List<AttributeType> reqAttrs = subjectAttrs.getSubjectAttribute();
        for (AttributeType attr: userAttributes) {
            reqAttrs.add(attr);
        }
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setOriginator(subjectAttrs);
        msgProps.setGlobalTransactionId(transId);
        ModifyResContent modifyReq = null;
        String gri = null;
        try {
            modifyReq = this.toReservation(request);
            modifyReq.setMessageProperties(msgProps);
            // Send modifyReservation request
            Object[] req = new Object[]{subjectAttrs,modifyReq};
            Object[] res = coordClient.invoke("modifyReservation",req);
            ModifyResReply coordResponse = (ModifyResReply) res[0];
            gri = coordResponse.getGlobalReservationId();
        } catch (OSCARSServiceException ex) {
            // errors in the server will show up as an OSCARServerException
            this.log.error(netLogger.error(methodName, ErrSev.MAJOR,"caught exception  invoking modifyReservation " +
                                            ex.toString()));
            ServletUtils.handleFailure(out, log, ex, methodName);
            return;
        } catch (Exception ex) {
            this.log.error(netLogger.error(methodName, ErrSev.MAJOR,"caught exception  invoking modifyReservation " +
                                            ex.toString()));
            ex.printStackTrace();
            ServletUtils.handleFailure(out, log, ex, methodName);
            return;
        }
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "modified reservation with GRI " +  gri);
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

    public ModifyResContent toReservation(HttpServletRequest request) {

        String strParam = null;
        int bandwidth = 0;
        Long seconds = 0L;
        String description = "";
        ModifyResContent resv = new ModifyResContent();
        UserRequestConstraintType userCon = new UserRequestConstraintType();
        this.log.debug("servlet Request is " + request.toString());
        strParam = request.getParameter("gri");
        resv.setGlobalReservationId(strParam);
        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("modifyStartSeconds");
        //this.log.debug("modifyStartSeconds is " + strParam);
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        userCon.setStartTime(seconds);
        seconds = 0L;
        strParam = request.getParameter("modifyEndSeconds");
        //this.log.debug("modifyEndSeconds is " + strParam);
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        userCon.setEndTime(seconds);

        strParam = request.getParameter("modifyBandwidth");
        //this.log.debug("modifyBandwidth is " + strParam);
        if (strParam != null && !strParam.trim().equals("")) {
            bandwidth = Integer.valueOf(strParam.trim());
        }
        userCon.setBandwidth(bandwidth);
        strParam = request.getParameter("modifyDescription");
        //this.log.debug("modifyDescription is " + strParam);
        if ((strParam != null) && (!strParam.equals(""))){
            description = strParam;
        }
        resv.setDescription(description);
        userCon.setPathInfo(new PathInfo());
        resv.setUserRequestConstraint(userCon);
        return resv;
    }
}
