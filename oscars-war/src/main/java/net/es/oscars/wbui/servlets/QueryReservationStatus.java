package net.es.oscars.wbui.servlets;


import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.sf.json.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;
import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
/**
 * Query reservation servlet
 *
 * @author David Robertson, Mary Thompson
 *
 */
public class QueryReservationStatus extends HttpServlet {
    private Logger log = Logger.getLogger(QueryReservationStatus.class);
    private OSCARSNetLogger netLogger = null;

    /**
     * Handles QueryReservationStatus servlet request.
     * Called by StatusPoll.js, it will be called until the reservation
     * is in a stable state: active, reserved, finished, failed. or cancelled
     *
     * @param servletRequest Request HttpServletRequest contains the gri of the reservation
     * @param response HttpServletResponse contains: gri, status, user,
     *        description start, end and create times, bandwidth, vlan tag,
     *        and path information.
     */
    public void
        doGet(HttpServletRequest servletRequest, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "QueryReservationStatus";
        ServletCore core = ServletCore.getInstance();
        getServletContext().getAttribute(ServletCore.CORE);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        String transId = core.getTransId();
        this.netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_WBUI,transId);
        netLogger.setGRI(servletRequest.getParameter("gri"));
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.info(netLogger.start(methodName));
 
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
        QueryResContent queryReq = new QueryResContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setOriginator(subjectAttrs);
        msgProps.setGlobalTransactionId(transId);
        queryReq.setMessageProperties(msgProps);
        String gri = servletRequest.getParameter("gri");
        queryReq.setGlobalReservationId(gri);
        ResDetails resDetails= null;
        String authVal = null;
        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            // Send a queryReservation request 
            Object[] req = new Object[]{subjectAttrs,queryReq};
            Object[] res = coordClient.invoke("queryReservation",req);
            resDetails = ((QueryResReply) res[0]).getReservationDetails();
            List <OSCARSFaultReport> faultReports = ((QueryResReply) res[0]).getErrorReport();
            this.contentSection(resDetails, faultReports, outputMap);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        if (resDetails.getStatus() == null) {
            outputMap.put("status", "Reservation details for " + gri);
        } else {
            outputMap.put("status", resDetails.getStatus());
        }
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

    /**
     * Only fills in those fields that might have changed due to change
     * in reservation status.
     */
    public void
        contentSection(ResDetails resv,  List <OSCARSFaultReport> faultReports,
                       Map<String,Object> outputMap)
            throws OSCARSServiceException {

        UserRequestConstraintType uConstraint = resv.getUserRequestConstraint();
        PathInfo pathInfo = null;
        String pathType = null;
        ReservedConstraintType rConstraint = resv.getReservedConstraint();
        if (rConstraint !=  null) {
            pathInfo=rConstraint.getPathInfo();
            pathType = "reserved";
        } else {
            uConstraint = resv.getUserRequestConstraint();
            if (uConstraint == null) {
                throw new OSCARSServiceException("invalid reservation, no reserved or requested path");
            }
            pathInfo=uConstraint.getPathInfo();
            pathType="requested";
            log.debug(netLogger.getMsg("QueryReservationStatus","no path reserved, using requested path "));
        }
        if (!faultReports.isEmpty()) {
            QueryReservation.handleErrorReports(faultReports,outputMap);
        } else {
            outputMap.put("errorReportReplace","" );
        }

        CtrlPlanePathContent path = null;
        Layer2Info layer2Info = null;
        if (pathInfo != null) {
            path = pathInfo.getPath();
            layer2Info = pathInfo.getLayer2Info();
            // no layer2Info or later3Info create one from path -- hack to make rest of code work
            if (layer2Info == null && pathInfo.getLayer3Info() == null  && path != null) {
                layer2Info = QueryReservation.createLayer2(path);
            }
        }
        
        String status = resv.getStatus();
        outputMap.put("griReplace", resv.getGlobalReservationId());
        outputMap.put("statusReplace", status);
        if (layer2Info != null) {
            QueryReservation.handleVlans(path, status, layer2Info, outputMap);
        }
        QueryReservation.outputPaths(path, outputMap);
    }
}
