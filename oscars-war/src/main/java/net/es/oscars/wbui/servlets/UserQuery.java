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

public class UserQuery extends HttpServlet {
    private Logger log = Logger.getLogger(UserQuery.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserQuery";
        this.log.info(methodName + ".start");

        boolean self =  false; // is query about the current user
        boolean modifyAllowed = false;
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
        List<AttributeType> userAttributes = sessionReply.getAttributes();
        // get here by clicking on a name in the users list
        if ((profileName != null) && !profileName.equals("")) {
            this.log.debug("profileName: " + profileName);
            if (profileName.equals(userName)) {
                self = true;
            } else {
                self = false;
            }
        } else { // profileName is null - get here by clicking on tab navigation
            this.log.debug("profileName is null, using " + userName);
            profileName = userName;
            self=true;
        }

        Map<String, Object> outputMap = new HashMap<String, Object>();
        if (!self) {
            outputMap.put("userDeleteDisplay", Boolean.TRUE);
        } else {
            outputMap.put("userDeleteDisplay", Boolean.FALSE);
        }
        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                               userAttributes, "Users", "query");
            if (authVal.equals("ALLUSERS") || (self && authVal.equals("SELFONLY"))) {
                // either have permission to see others OR see self
             } else {
                 log.warn(userName + "has no permisson to query users");
                ServletUtils.handleFailure(out,"no permission to query users", methodName);
                return;
            }
            /* check to see if user has modify permission for this user
             *     used by contentSection to set the action on submit
             */
            authVal = ServletUtils.checkPermission(authZClient,
                               userAttributes, "Users", "modify");

            if (authVal.equals("ALLUSERS") ||
                (self && authVal.equals("SELFONLY"))) {
                modifyAllowed = true;
            } else {
                modifyAllowed = false;
            }
            String targetUser = null;
            if (self) {
                targetUser = userName;
            } else {
                targetUser = profileName;
            }
            Object[] soapReq = new Object[]{targetUser};
            Object[] resp = authNPolicyClient.invoke("queryUser", soapReq);
            QueryUserReply queryReply = (QueryUserReply) resp[0];
            UserDetails userDetails = queryReply.getUserDetails();
            List<AttributeType> queryReplyAttrs =
                queryReply.getUserAttributes().getSubjectAttributes();
            List<AttrDetails> allAttributes =
                ServletUtils.getAllAttributes(authNPolicyClient, null);
            EmptyArg req = new EmptyArg();
            soapReq = new Object[]{req};
            resp = authNPolicyClient.invoke("listInsts", soapReq);
            ListInstsReply reply = (ListInstsReply) resp[0];
            List<String> institutions = reply.getName();
            this.contentSection(outputMap, userDetails, modifyAllowed,
                    authVal.equals("ALLUSERS"),
                    institutions, queryReplyAttrs, allAttributes);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("status", "Profile for user " + profileName);
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
     * writes out the parameter values that are the result of a user query
     *
     * @param outputMap map with parameter values for userPane
     * @param user the user whose information is being displayed
     * @param modifyAllowed - true if the  user displaying this information has
     *                        permission to modify it
     * @param modifyRights true if the  user displaying this information has
     *                     permission to modify the target user's attributes
     * @param insts list of all institution names
     * @param userAttributes all the attributes of the target user
     * @param allAttributes list of all attributes
     */
    public void
        contentSection(Map<String, Object> outputMap, UserDetails user,
                       boolean modifyAllowed, boolean modifyRights,
                       List<String> insts, List<AttributeType> userAttributes,
                       List<AttrDetails> allAttributes) {

        this.log.debug("contentSection: start");
        if (modifyAllowed) {
            outputMap.put("allowModify", Boolean.TRUE);
            outputMap.put("userHeader",
                          "Editing profile for user: " + user.getLogin());
        } else {
            outputMap.put("allowModify", Boolean.FALSE);
            outputMap.put("userHeader", "Profile for user: " + user.getLogin());
        }
        outputMap.put("profileName", user.getLogin());
        this.log.info("user login: " + user.getLogin());
        String strParam = user.getPassword();
        if (strParam != null) {
            outputMap.put("password", "********");
        }
        if (strParam != null) {
           outputMap.put("passwordConfirmation", "********");
        }
        outputMap.put("firstName", user.getFirstName());
        outputMap.put("lastName", user.getLastName());
        strParam = user.getCertSubject();
        if (strParam != null) {
           outputMap.put("certSubject", strParam);
        }
        strParam = user.getCertIssuer();
        if (strParam != null) {
           outputMap.put("certIssuer", strParam);
        }
        this.outputInstitutionMenu(outputMap, insts, user);
        this.outputAttributeMenu(outputMap, userAttributes, allAttributes, modifyRights);

        strParam = user.getDescription();
        if (strParam != null) {
           outputMap.put("description", strParam);
        }
        outputMap.put("emailPrimary", user.getEmailPrimary());
        strParam = user.getEmailSecondary();
        if (strParam != null) {
           outputMap.put("emailSecondary", strParam);
        }
        outputMap.put("phonePrimary", user.getPhonePrimary());
        strParam = user.getPhoneSecondary();
        if (strParam != null) {
           outputMap.put("phoneSecondary", strParam);
        }
        this.log.debug("contentSection: finish");
    }

    public void
        outputInstitutionMenu(Map<String, Object> outputMap,
                             List<String> insts, UserDetails user) {

        String institutionName = user.getInstitution();
        int ctr = 0;
        List<String> institutionList = new ArrayList<String>();
        for (String i: insts) {
            institutionList.add(i);
            // use first in list if no institution associated with user
            if ((ctr == 0) && institutionName.equals("")) {
                institutionList.add("true");
            } else if (i.equals(institutionName)) {
                institutionList.add("true");
            } else {
                institutionList.add("false");
            }
            ctr++;
        }
        outputMap.put("institutionMenu", institutionList);
    }

    public void
        outputAttributeMenu(Map<String, Object> outputMap,
                List<AttributeType> userAttributes,
                List<AttrDetails> allAttributes, boolean modify) {

        List<String> attributeList = new ArrayList<String>();
        // default is none
        attributeList.add("None");
        if (userAttributes.isEmpty()) {
            attributeList.add("true");
        } else {
            attributeList.add("false");
        }
        for (AttrDetails a: allAttributes) {
            attributeList.add(a.getValue() + " -> " + a.getDescription());
            boolean foundForUser = false;
            for (AttributeType aa: userAttributes) {
                List<Object> samlValues = aa.getAttributeValue();
                for (Object samlValue: samlValues) {
                    String value = (String) samlValue;
                    this.log.info("saml value " + value);
                    if (a.getValue().equals(value)) {
                        foundForUser = true;
                        break;
                    }
                }
            }
            if (foundForUser) {
                attributeList.add("true");
            } else {
                attributeList.add("false");
            }
        }
        if (modify) {
            outputMap.put("attributeNameEnable", Boolean.TRUE);
        } else {
            outputMap.put("attributeNameEnable", Boolean.FALSE);
        }
        outputMap.put("attributeNameMenu", attributeList);
    }
}
