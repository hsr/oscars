package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import net.sf.json.*;
import org.apache.log4j.Logger;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.common.soap.gen.EmptyArg;


public class UserRemove extends HttpServlet {
    private Logger log = Logger.getLogger(UserRemove.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserRemove";
        log.info(methodName + ".start");
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        ServletCore core = (ServletCore)
            getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
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
        String profileName = request.getParameter("profileName");

        // cannot remove oneself
        if (profileName.equals(userName)) {
            log.warn("User "+userName+" not allowed to remove himself");
            ServletUtils.handleFailure(out, "You may not remove your own account.", methodName);
            return;
        }

        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                               sessionReply.getAttributes(), "Users", "modify");
            if (!authVal.equals("ALLUSERS")) {
                log.warn(userName +" has no permission to modify users");
                ServletUtils.handleFailure(out,"You do not have the permissions to modify users", methodName);
                return;
            }

            Object[] soapReq = new Object[]{profileName};
            Object[] resp = authNPolicyClient.invoke("removeUser", soapReq);
            authVal = ServletUtils.checkPermission(authZClient,
                               sessionReply.getAttributes(), "Users", "list");
            // shouldn't be able to get to this point, but just in case
            if (!authVal.equals("ALLUSERS")) {
                log.error(userName + "has no permission to list users, should not have gotten this far");
                ServletUtils.handleFailure(out, "You do not have the permissions to list users", methodName);
                return;
            }
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "User " + profileName + " successfully removed");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);

        log.info(methodName + ":end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
