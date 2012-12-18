package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZPolicyClient;
import net.es.oscars.authZ.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.common.soap.gen.EmptyArg;


public class AuthorizationRemove extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationRemove.class);
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "AuthorizationRemove";
        log.info(methodName + ".start");
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        ServletCore core = (ServletCore)
            getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZPolicyClient authZPolicyClient = core.getAuthZPolicyClient();
        AuthZClient authZClient = core.getAuthZClient();

        UserSession userSession = new UserSession(core);
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, request,
                                     methodName);
        if (sessionReply == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }
        String userName = sessionReply.getUserName();
        List<AttributeType> userAttributes = sessionReply.getAttributes();
        String attributeName = request.getParameter("authAttributeName");
        String permissionName = request.getParameter("permissionName");
        String resourceName = request.getParameter("resourceName");
        String constraintName = request.getParameter("constraintName");

        log.debug("Removing attribute: " + attributeName +" resource: " + resourceName + " permission: "
                + permissionName + " constraintName: " + constraintName );

        AuthDetails authDetails = new AuthDetails();
        authDetails.setAttributeValue(attributeName);
        authDetails.setResourceName(resourceName);
        authDetails.setPermissionName(permissionName);
        authDetails.setConstraintName(constraintName);
        authDetails.setConstraintType("ignored");
        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                               userAttributes, "AAA", "modify");
            if (authVal.equals("DENIED"))  {
                String errorMsg = "User "+userName+" not allowed to remove authorizations";
                log.warn(errorMsg);
                ServletUtils.handleFailure(out, errorMsg, methodName);
                return;
            }
            Object[] soapReq = new Object[]{authDetails};
            Object[] resp = authZPolicyClient.invoke("removeAuth", soapReq);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "authorization deleted");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        log.info(methodName + ":end");
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
