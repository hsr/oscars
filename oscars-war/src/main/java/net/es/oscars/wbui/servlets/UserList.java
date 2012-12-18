package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import net.sf.json.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.common.soap.gen.EmptyArg;
import net.es.oscars.authCommonPolicy.soap.gen.*;

public class UserList extends HttpServlet {
    private Logger log = Logger.getLogger(UserList.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserList";
        this.log.debug(methodName + ".start");
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

        Map<String, Object> outputMap = new HashMap<String, Object>();
        String authVal = null;
        try {
            authVal = ServletUtils.checkPermission(authZClient,
                               sessionReply.getAttributes(), "Users", "query");
            // if allowed to see all users, show help information on clicking on
            // row to see user details
            this.log.debug("authVal is " + authVal);
            if  (authVal.equals("ALLUSERS")) {
                outputMap.put("userRowSelectableDisplay", Boolean.TRUE);
            } else {
                outputMap.put("userRowSelectableDisplay", Boolean.FALSE);
            }
            outputMap.put("status", "User list");
            this.outputUsers(outputMap, userName, request, authNPolicyClient,
                             authZClient, sessionReply.getAttributes());
        } catch (Exception e) {
            log.error("caught exception " + e.toString());
            e.printStackTrace();
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        log.debug("calling JSONObject");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.debug(methodName + ".finish");
    }

    /**
     * Checks access and gets the list of users if allowed.
     *
     * @param outputMap Map containing JSON data
     * @param userName String containing name of user making request
     * @param request HttpServletRequest with form parameters
     * @throws OSCARSServiceException
     */
    public void outputUsers(Map<String, Object> outputMap, String userName,
                HttpServletRequest request, AuthNPolicyClient authNPolicyClient,
                AuthZClient authZClient, List<AttributeType> attributes)
            throws OSCARSServiceException {

        this.log.debug("outputUsers.start");
        String attributeName = request.getParameter("attributeName");
        if (attributeName != null) {
            attributeName = attributeName.trim();
        } else {
            attributeName = "";
        }
        String attrsUpdated = request.getParameter("userListAttrsUpdated");
        if (attrsUpdated != null) {
            attrsUpdated = attrsUpdated.trim();
        } else {
            attrsUpdated = "";
        }
        String authVal = ServletUtils.checkPermission(authZClient, attributes,
                                                  "Users", "list");
        String aaaVal = ServletUtils.checkPermission(authZClient, attributes,
                                                  "AAA", "list");

        if (authVal.equals("ALLUSERS")) {
            // check to see if need to (re)display menu
            if (attributeName.equals("") ||
                ((attrsUpdated != null) && !attrsUpdated.equals(""))) {
                if (!aaaVal.equals("DENIED")) {
                    outputMap.put("attributeInfoDisplay", Boolean.TRUE);
                    outputMap.put("attributeMenuDisplay", Boolean.TRUE);
                    this.outputAttributeMenu(outputMap, authNPolicyClient);
                } else {
                    outputMap.put("attributeInfoDisplay", Boolean.FALSE);
                    outputMap.put("attributeMenuDisplay", Boolean.FALSE);
                }
            }
        // authVal will never be SELFONLY because the user list tab will
        // not be displayed in that case
        } else {
            throw new OSCARSServiceException("no permission to list users");
        }
        ListUsersParams req = new ListUsersParams();
        if (!attributeName.equals("") && !attributeName.equals("Any") &&
            !aaaVal.equals("DENIED")) {
            req.setAttribute(attributeName);
        }
        Object[] soapReq = new Object[]{req};
        Object[] resp = authNPolicyClient.invoke("listUsers", soapReq);
        ListUsersReply reply= (ListUsersReply) resp[0];
        List<UserDetails> users = reply.getUserDetails();
        ArrayList<HashMap<String,String>> userList =
            new ArrayList<HashMap<String,String>>();
        int ctr = 0;
        for (UserDetails user: users) {
            HashMap<String,String> userMap = new HashMap<String,String>();
            userMap.put("id", Integer.toString(ctr));
            userMap.put("login", user.getLogin());
            userMap.put("lastName", user.getLastName());
            userMap.put("firstName", user.getFirstName());
            userMap.put("organization", user.getInstitution());
            userMap.put("phone", user.getPhonePrimary());
            userList.add(userMap);
            ctr++;
        }
        outputMap.put("userData", userList);
        this.log.debug("outputUsers.finish");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void outputAttributeMenu(Map<String, Object> outputMap,
                                    AuthNPolicyClient client)
            throws OSCARSServiceException {

        this.log.debug("outputAttributeMenu.start");
        List<AttrDetails> attributes =
            ServletUtils.getAllAttributes(client, null);
        List<String> attrList = new ArrayList<String>();
        attrList.add("Any");
        attrList.add("true");
        for (AttrDetails attr: attributes) {
            attrList.add(attr.getValue());
            attrList.add("false");
        }
        outputMap.put("attributeMenu", attrList);
        this.log.debug("outputAttributeMenu.finish");
    }
}
