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


public class AuthorizationModify extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationModify.class);

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "AuthorizationModify";
        this.log = Logger.getLogger(this.getClass());
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
        String permissionName  = request.getParameter("permissionName");
        String resourceName  = request.getParameter("resourceName");
        String constraintName = request.getParameter("constraintName");
        String constraintValue = request.getParameter("constraintValue");
        String oldAttributeName = request.getParameter("oldAuthAttributeName");
        String oldPermissionName = request.getParameter("oldPermissionName");
        String oldResourceName  = request.getParameter("oldResourceName");
        String oldConstraintName  = request.getParameter("oldConstraintName");
        log.debug("modifying attribute: " + oldAttributeName + " to "+ attributeName +
                " resource: " + oldResourceName  + " to " + resourceName  +
                " permission: " + oldPermissionName  + " to " + permissionName  +
                " constraintName: " + oldConstraintName + " to " + constraintName );

        ModifyAuthDetails req = new ModifyAuthDetails();
        AuthDetails oldAuth = new AuthDetails();
        AuthDetails newAuth = new AuthDetails();
        oldAuth.setAttributeValue(oldAttributeName);
        oldAuth.setResourceName(oldResourceName);
        oldAuth.setPermissionName(oldPermissionName);
        oldAuth.setConstraintName(oldConstraintName);
        oldAuth.setConstraintType("ignored");
        newAuth.setAttributeValue(attributeName);
        newAuth.setResourceName(resourceName);
        newAuth.setPermissionName(permissionName);
        newAuth.setConstraintName(constraintName);
        newAuth.setConstraintValue(constraintValue);
        newAuth.setConstraintType("ignored");
        req.setOldAuthInfo(oldAuth);
        req.setModAuthInfo(newAuth);
        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                               userAttributes, "AAA", "modify");
            if (authVal.equals("DENIED"))  {
                log.warn("Not allowed to modify an authorization");
                ServletUtils.handleFailure(out, "not allowed to modify an authorization", methodName);
                return;
            }
            Object[] soapReq = new Object[]{req};
            Object[] resp = authZPolicyClient.invoke("modifyAuth", soapReq);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "Authorization modified");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        log.info(methodName + ".finish");
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
