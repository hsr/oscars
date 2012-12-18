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

public class UserAdd extends HttpServlet {
    private Logger log = Logger.getLogger(UserAdd.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserAdd";
        this.log.info(methodName + ":start");
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

        String authVal = null;
        String errMsg = null;
        try {
            authVal = ServletUtils.checkPermission(authZClient,
                               sessionReply.getAttributes(), "Users", "create");
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        RoleUtils roleUtils = new RoleUtils();
        if (!authVal.equals("ALLUSERS")) {
            errMsg = "not allowed to add a new user";
        }
        if (profileName.equals(userName)) {
            errMsg = "can't add another account for onself";
        }
        if (errMsg != null) {
            ServletUtils.handleFailure(out, errMsg, methodName);
            return;
        }
        UserDetails userDetails = null;
        try {
            userDetails = this.toUser(out, profileName, request);
        } catch (Exception e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, e.getMessage(), methodName);
            return;
        }
        try {
            List<AttrDetails> attributes =
                ServletUtils.getAllAttributes(authNPolicyClient, null);
            List<String> addRoles = null;
            String roles[] = request.getParameterValues("attributeName");
            for (int i=0; i < roles.length; i++) {
                roles[i] = ServletUtils.dropDescription(roles[i].trim());
            }
            // will be only one parameter value due to constraints
            // on client side
            if (roles[0].equals("None")) {
                this.log.debug("roles = null");
                addRoles = new ArrayList<String>();
            } else {
                this.log.debug("number of roles input is "+roles.length);
                addRoles = roleUtils.checkRoles(roles, attributes);
            }
            FullUserParams req = new FullUserParams();
            req.setUserDetails(userDetails);
            List<String> newAttributes = req.getNewAttributes();
            for (String addRole: addRoles) {
                newAttributes.add(addRole);
            }
            Object[] soapReq = new Object[]{req};
            Object[] resp = authNPolicyClient.invoke("addUser", soapReq);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "User " + profileName +  " successfully created");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ".finish");
    }



    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public UserDetails
        toUser(PrintWriter out, String userName, HttpServletRequest request)
           throws Exception {

        String DN;
        String password;

        UserDetails user = new UserDetails();
        user.setLogin(userName);
        String strParam = request.getParameter("certIssuer");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = ServletUtils.checkDN(strParam);
        }
        else { DN = ""; }
        user.setCertIssuer(DN);
        strParam = request.getParameter("certSubject");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = ServletUtils.checkDN(strParam);
        }
        else { DN = ""; }
        user.setCertSubject(DN);
        // required fields by client, so always filled in
        user.setLastName(request.getParameter("lastName"));
        user.setFirstName(request.getParameter("firstName"));
        user.setEmailPrimary(request.getParameter("emailPrimary"));
        user.setPhonePrimary(request.getParameter("phonePrimary"));
        password = ServletUtils.checkPassword(request.getParameter("password"), request.getParameter("passwordConfirmation"));
        user.setPassword(password);
        // doesn't matter if blank
        user.setDescription(request.getParameter("description"));
        user.setEmailSecondary(request.getParameter("emailSecondary"));
        user.setPhoneSecondary(request.getParameter("phoneSecondary"));
        user.setInstitution(request.getParameter("institutionName"));
        return user;
    }
}
