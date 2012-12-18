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
import net.es.oscars.authCommonPolicy.soap.gen.*;

public class AuthorizationList extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationList.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "AuthorizationList";
        this.log.info(methodName + ":start");
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
        String profileName = request.getParameter("profileName");
        List<AttributeType> userAttributes = sessionReply.getAttributes();
        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                               userAttributes, "AAA", "list");
            if (authVal.equals("DENIED")) {
                String errorMsg = "User "+userName+" has no permission to list authorizations";
                this.log.warn(errorMsg);
                ServletUtils.handleFailure(out, errorMsg, methodName);
                return;
            }
            this.outputAuthorizations(outputMap, request, authZPolicyClient);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("status", "Authorization list");
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

    public void outputAttributeMenu(Map<String, Object> outputMap,
                                    AuthZPolicyClient client)
            throws OSCARSServiceException {

        List<AttrDetails> attributes =
            ServletUtils.getAllAttributes(null, client);
        List<String> attributeList = new ArrayList<String>();
        attributeList.add("Any");
        attributeList.add("true");
        for (AttrDetails attr: attributes) {
            attributeList.add(attr.getValue());
            attributeList.add("false");
        }
        outputMap.put("attributeSelectMenu", attributeList);
    }

    /**
     * Sets the list of authorizations to display in a grid.
     *
     * @param outputMap Map containing JSON data
     * @param request HttpServletRequest form parameters
     * @throws AAAException
     */
    public void outputAuthorizations(Map<String, Object> outputMap,
                        HttpServletRequest request, AuthZPolicyClient client)
            throws OSCARSServiceException {

        String attributeName = request.getParameter("attributeName");
        if (attributeName != null) {
            attributeName = attributeName.trim();
        } else {
            attributeName = "";
        }
        String attrsUpdated = request.getParameter("authListAttrsUpdated");
        if (attrsUpdated != null) {
            attrsUpdated = attrsUpdated.trim();
        } else {
            attrsUpdated = "";
        }
        String listType = "";
        ListAuthsParams req = new ListAuthsParams();
        if (!attributeName.equals("") && !attributeName.equals("Any")) {
            req.setAttribute(attributeName);
        }
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("listAuths", soapReq);
        ListAuthsReply listAuthsReply = (ListAuthsReply) resp[0];
        List<AuthDetails> auths = listAuthsReply.getAuthDetails();
        if (attributeName.equals("") ||
            ((attrsUpdated != null) && !attrsUpdated.equals(""))) {
            this.outputAttributeMenu(outputMap, client);
        }
        ArrayList<HashMap<String,String>> authList =
            new ArrayList<HashMap<String,String>>();
        int ctr = 0;
        for (AuthDetails auth: auths) {
            HashMap<String,String> authMap = new HashMap<String,String>();
            authMap.put("id", Integer.toString(ctr));
            authMap.put("attribute", auth.getAttributeValue());
            authMap.put("resource", auth.getResourceName());
            authMap.put("permission", auth.getPermissionName());
            String constraintName = auth.getConstraintName();
            authMap.put("constraint", constraintName);
            String constraintType = auth.getConstraintType();
            String constraintValue = auth.getConstraintValue();
            if (constraintValue == null) {
                if (constraintType.equals("boolean") &&
                    !constraintName.equals("none")) {
                    authMap.put("constraintVal", "true");
                } else {
                    authMap.put("constraintVal", "");
                }
            } else {
                authMap.put("constraintVal", constraintValue);
            }
            authList.add(authMap);
            ctr++;
        }
        outputMap.put("authData", authList);
    }
}
