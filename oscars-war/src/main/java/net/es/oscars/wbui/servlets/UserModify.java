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

public class UserModify extends HttpServlet {
    private Logger log = Logger.getLogger(UserModify.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserModify";
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
        List<AttributeType> attributes = sessionReply.getAttributes();
        List<AttributeType> userAttributes = null;

        boolean self = false; // is user modifying own profile
        boolean setPassword = false;
        Map<String, Object> outputMap = new HashMap<String, Object>();
        // got here by clicking on a name in the user list
        if (profileName != null) {
            if (profileName.equals(userName)) {
                self = true;
            } else {
                self = false;
            }
        } else { // profileName is null - clicked on userProfile nav tab
            profileName = userName;
            self = true;
        }
        // just in case renamed to oneself
        if (!self) {
            outputMap.put("userDeleteDisplay", Boolean.TRUE);
        } else {
            outputMap.put("userDeleteDisplay", Boolean.FALSE);
        }
        FullUserParams req = new FullUserParams();
        UserDetails userDetails = null;
        List<String> newRoles = new ArrayList<String>();
        List<String> curRoles = req.getCurAttributes();
        List<String> newReqRoles = req.getNewAttributes();
        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                               attributes, "Users", "modify");
            if (authVal.equals("ALLUSERS") ||
                    (self && authVal.equals("SELFONLY"))) {
                Object[] soapReq = new Object[]{profileName};
                Object[] resp = authNPolicyClient.invoke("queryUser", soapReq);
                QueryUserReply reply = (QueryUserReply) resp[0];
                userDetails = reply.getUserDetails();
                userAttributes =
                    reply.getUserAttributes().getSubjectAttributes();
            } else {
                ServletUtils.handleFailure(out,"no permission to modify users",
                                           methodName);
                log.warn(userName + " does not have permission to modify users");
                return;
            }
            if (userDetails == null) {
                String msg = "User " + profileName + " does not exist";
                ServletUtils.handleFailure(out, msg, methodName);
            }
            List<AttrDetails> allAttributes =
                ServletUtils.getAllAttributes(authNPolicyClient, null);

            this.convertParams(request, userDetails);
            String password = request.getParameter("password");
            String confirmationPassword =
                request.getParameter("passwordConfirmation");
            // handle password modification if necessary
            // check will return null, if password is  not to be changed
            String newPassword = ServletUtils.checkPassword(password,
                                                          confirmationPassword);
            if (newPassword != null) {
                this.log.info("changing password");
                userDetails.setPassword(newPassword);
                setPassword = true;
            }

            // see if any attributes need to be added or removed
            if (authVal.equals("ALLUSERS")) {
                RoleUtils roleUtils = new RoleUtils();
                String roles[] = request.getParameterValues("attributeName");
                for (int i=0; i < roles.length; i++) {
                    roles[i] = ServletUtils.dropDescription(roles[i].trim());
                }
                if (!roles[0].equals("None")) {
                    this.log.info("number of roles input is " + roles.length);
                    newRoles = roleUtils.checkRoles(roles, allAttributes);
                }
                for (AttributeType attr: userAttributes) {
                    String attrName = attr.getName();
                    if (!attrName.equals("loginId") && !attrName.equals("institution")) {
                        List<Object> samlValues = attr.getAttributeValue();
                        for (Object samlValue: samlValues) {
                            String value = (String) samlValue;
                            curRoles.add(value);
                        }
                    }
                }
            }
            for (String role: newRoles) {
                this.log.info("new role: " + role);
                newReqRoles.add(role);
            }
            req.setUserDetails(userDetails);
            req.setPasswordChanged(setPassword);
            Object[] soapReq = new Object[]{req};
            Object[] resp = authNPolicyClient.invoke("modifyUser", soapReq);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("status", "Profile for user " + profileName +
                      " successfully modified");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ":end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * Changes the value of user to correspond to the new input values
     *
     * @param request - input from modifyUser form
     * @param user in/out as the current user specified in profile name
     *        modified by the parameters in the request.
     * @throws Exception
     */
    public void convertParams(HttpServletRequest request, UserDetails user)
            throws Exception {

        String strParam = null;
        String DN = null;

        strParam = request.getParameter("institutionName");
        if (strParam != null) {
            user.setInstitution(strParam);
        }
        strParam = request.getParameter("certIssuer");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = ServletUtils.checkDN(strParam);
        }
        // allow setting existent non-required field to null
        if ((DN != null) || (user.getCertIssuer() != null)) {
            user.setCertIssuer(DN);
        }
        strParam = request.getParameter("certSubject");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = ServletUtils.checkDN(strParam);
        }
        if ((DN != null) || (user.getCertSubject() != null)) {
            user.setCertSubject(DN);
        }
        // required fields by client
        strParam = request.getParameter("lastName");
        if (strParam != null) { user.setLastName(strParam); }
        strParam = request.getParameter("firstName");
        if (strParam != null) { user.setFirstName(strParam); }
        strParam = request.getParameter("emailPrimary");
        if (strParam != null) { user.setEmailPrimary(strParam); }
        strParam = request.getParameter("phonePrimary");
        if (strParam != null) { user.setPhonePrimary(strParam); }
        // doesn't matter if blank
        strParam = request.getParameter("description");
        if ((strParam != null) || (user.getDescription() != null)) {
            user.setDescription(strParam);
        }
        strParam = request.getParameter("emailSecondary");
        if ((strParam != null) || (user.getEmailSecondary() != null)) {
            user.setEmailSecondary(strParam);
        }
        strParam = request.getParameter("phoneSecondary");
        if ((strParam != null) || (user.getPhoneSecondary() != null)) {
            user.setPhoneSecondary(strParam);
        }
    }
}
