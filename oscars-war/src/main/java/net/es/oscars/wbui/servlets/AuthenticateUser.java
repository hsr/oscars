package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.http.*;
import net.sf.json.*;

import org.apache.log4j.*;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.authN.soap.gen.*;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

public class AuthenticateUser extends HttpServlet {
    private Logger log = Logger.getLogger(AuthenticateUser.class);

    public void init() throws ServletException {
        ServletCore core = ServletCore.getInstance();
        getServletContext().setAttribute(ServletCore.CORE, core);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "AuthenticateUser";
        String transId  = PathTools.getLocalDomainId() + "-WBUI-" + UUID.randomUUID().toString();
        OSCARSNetLogger netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_WBUI,transId);
        OSCARSNetLogger.setTlogger(netLogger);
        log.info(netLogger.start(methodName));
        PrintWriter out = response.getWriter();
        ServletCore core = (ServletCore)
                getServletContext().getAttribute(ServletCore.CORE);
        // if setup during init failed, unlikely to work on later
        // servlet calls, so not using getInstance
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        AuthNClient authNClient = core.getAuthNClient();
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZClient authZClient = core.getAuthZClient();

        UserSession userSession = new UserSession(core);
        String userName = request.getParameter("userName");
        String password = request.getParameter("initialPassword");
        String guestLogin = userSession.getGuestLogin();
        String sessionName = "";
        if (userName != null && guestLogin != null &&
                userName.equals(guestLogin)) {
            sessionName = "1234567890";
        } else {
            Random generator = new Random();
            int r = generator.nextInt();
            sessionName = String.valueOf(r);
        }
        VerifyLoginReqType verifyLoginReq = new VerifyLoginReqType();
        LoginId login = new LoginId();
        login.setLoginName(userName);
        login.setPassword(password);
        verifyLoginReq.setTransactionId(transId);
        verifyLoginReq.setLoginId(login);
        Object[] req = new Object[]{verifyLoginReq};
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        try {
            Object[] resp = authNClient.invoke("verifyLogin", req);
            VerifyReply reply = (VerifyReply) resp[0];
            SubjectAttributes attrs = reply.getSubjectAttributes();
            SessionOpParams setSessionReq = new SessionOpParams();
            setSessionReq.setUserName(userName);
            setSessionReq.setSessionName(sessionName);
            Object[] sessionReq = new Object[]{setSessionReq};
            Object[] sessionResp =
                    authNPolicyClient.invoke("setSession", sessionReq);
            this.handleDisplay(authZClient, attrs, userName, outputMap, out);
        } catch (OSCARSServiceException e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        log.debug("setting cookie name to " + userName);
        userSession.setCookie("userName", userName, response);
        log.debug("setting session name to " + sessionName);
        userSession.setCookie("sessionName", sessionName, response);

        response.setContentType("application/json");

        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("status", userName + " signed in.  Use tabs " + "to navigate to different pages.");
        outputMap.put("optionalConstraints", core.getOptConstraints());
        
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&& " + jsonObject);
        log.info("user " + userName + " logged in");
        log.info(methodName + ".finish");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * Used to indicate which tabs can be displayed.  All except
     * Login/Logout require some form of authorization.  Also controls
     * what help information is displayed on login page.
     *
     * @param client authorization module SOAP client
     * @param attrs list of SAML attributes
     * @param userName String with user's login name
     * @param outputMap map indicating what information to display based on
     *                  user authorization
     * @param out handles output back to browser
     */
    public void handleDisplay(AuthZClient client, SubjectAttributes attrs,
            String userName, Map<String, Object> outputMap, PrintWriter out)
                    throws OSCARSServiceException {

        this.log.debug("handleDisplay.start");
        Map<String, Object> authorizedTabs = new HashMap<String, Object>();
        // for special cases where user may be able to view grid but not
        // go to a different tab upon selecting a row
        Map<String, Object> selectableRows = new HashMap<String, Object>();

        HashMap<String, ArrayList<String>> permsToCheck = new HashMap<String, ArrayList<String>>();

        CheckMultiAccessParams req = new CheckMultiAccessParams();
        req.setSubjectAttrs(attrs);

        List<ReqPermType> reqPermTypes = req.getReqPermissions();
        ReqPermType rp = new ReqPermType();
        rp.setResource("Users");
        List<String> permissions = rp.getReqAction();
        permissions.add("list");
        permissions.add("query");
        permissions.add("create");
        reqPermTypes.add(rp);

        rp = new ReqPermType();
        rp.setResource("AAA");
        permissions = rp.getReqAction();
        permissions.add("modify");
        reqPermTypes.add(rp);

        rp = new ReqPermType();
        rp.setResource("Reservations");
        permissions = rp.getReqAction();
        permissions.add("create");
        permissions.add("list");
        permissions.add("query");
        reqPermTypes.add(rp);

        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("checkMultiAccess", soapReq); 
        MultiAccessPerms reply = (MultiAccessPerms) resp[0];
        List<MultiAccessPerm> accessPerms = reply.getAccessPerm();

        Map<String, HashMap<String, String>> resourcePerms =
                new HashMap<String, HashMap<String,String>>();
        for (MultiAccessPerm accessPerm: accessPerms) {
            HashMap<String, String> permMap = new HashMap<String, String>();
            List<PermType> permTypes = accessPerm.getPermissionGranted();
            for (PermType permType: permTypes) {
                permMap.put(permType.getPermission(), permType.getAction());
            }
            resourcePerms.put(accessPerm.getResource(), permMap);

            //debug stuff
            outputMap.put("specifyPath", Boolean.FALSE);
            outputMap.put("displayPath", Boolean.FALSE);
            for(PermType permGranted : accessPerm.getPermissionGranted()){
                if(permGranted.getConditions() != null){
                    for(AuthConditionType authCond : permGranted.getConditions().getAuthCondition()){
                        if(AuthZConstants.RESERVATIONS.equals(accessPerm.getResource()) &&
                                AuthZConstants.CREATE.equals(permGranted.getPermission()) &&
                                AuthZConstants.SPEC_PATH_ELEMS.equals(authCond.getName()) && 
                                authCond.getConditionValue() != null &&
                                !authCond.getConditionValue().isEmpty() &&
                                "true".equals(authCond.getConditionValue().get(0))){
                            outputMap.put("specifyPath", Boolean.TRUE);
                        }else if(AuthZConstants.RESERVATIONS.equals(accessPerm.getResource()) &&
                                AuthZConstants.QUERY.equals(permGranted.getPermission()) &&
                                AuthZConstants.INT_HOPS_ALLOWED.equals(authCond.getName()) && 
                                authCond.getConditionValue() != null &&
                                !authCond.getConditionValue().isEmpty() &&
                                "true".equals(authCond.getConditionValue().get(0))){
                            outputMap.put("displayPath", Boolean.TRUE);
                        }
                    }
                }
            }
        }
        String authVal = resourcePerms.get("Reservations").get("list");
        if (authVal == null) {
            authVal = "DENIED";
        }
        if (!authVal.equals("DENIED")) {
            authorizedTabs.put("reservationsPane", Boolean.TRUE);
            outputMap.put("reservationsDisplay", Boolean.TRUE);
        } else {
            outputMap.put("reservationsDisplay", Boolean.FALSE);
        }


        authVal = resourcePerms.get("Reservations").get("query");
        if (authVal == null) {
            authVal = "DENIED";
        }
        this.log.debug("Reservations:query:"+authVal.toString());

        if (!authVal.equals("DENIED"))  {
            authorizedTabs.put("reservationDetailsPane", Boolean.TRUE);
        }

        authVal = resourcePerms.get("Reservations").get("create");
        if (authVal == null) {
            authVal = "DENIED";
        }
        if (!authVal.equals("DENIED"))  {
            authorizedTabs.put("reservationCreatePane", Boolean.TRUE);
            outputMap.put("createReservationDisplay", Boolean.TRUE);
        } else {
            outputMap.put("createReservationDisplay", Boolean.FALSE);
        }



        String authQueryVal = resourcePerms.get("Users").get("query");
        if (authQueryVal == null) {
            authQueryVal = "DENIED";
        }
        if (!authQueryVal.equals("DENIED"))  {
            authorizedTabs.put("userProfilePane", Boolean.TRUE);
        }

        authVal = resourcePerms.get("Users").get("list");
        if (authVal == null) {
            authVal = "DENIED";
        }
        if (authVal.equals("ALLUSERS"))  {
            authorizedTabs.put("userListPane", Boolean.TRUE);
            if (authQueryVal.equals("ALLUSERS")) {
                selectableRows.put("users", Boolean.TRUE);
                outputMap.put("authUsersDisplay", Boolean.TRUE);
                outputMap.put("unAuthUsersDisplay", Boolean.FALSE);
                outputMap.put("userProfileDisplay", Boolean.FALSE);
            } else if (authQueryVal.equals("SELFONLY")) {
                outputMap.put("userProfileDisplay", Boolean.TRUE);
                outputMap.put("unAuthUsersDisplay", Boolean.TRUE);
                outputMap.put("authUsersDisplay", Boolean.FALSE);
            }
        } else if (!authQueryVal.equals("DENIED")) {
            outputMap.put("userProfileDisplay", Boolean.TRUE);
            outputMap.put("authUsersDisplay", Boolean.FALSE);
            outputMap.put("unAuthUsersDisplay", Boolean.FALSE);
        }

        authVal = resourcePerms.get("Users").get("create");
        if (authVal == null) {
            authVal = "DENIED";
        }
        if (!authVal.equals("DENIED"))  {
            authorizedTabs.put("userAddPane", Boolean.TRUE);
            outputMap.put("addUserDisplay", Boolean.TRUE);
        } else {
            outputMap.put("addUserDisplay", Boolean.FALSE);
        }

        authVal = resourcePerms.get("AAA").get("modify");
        if (authVal == null) {
            authVal = "DENIED";
        }
        if (!authVal.equals("DENIED"))  {
            authorizedTabs.put("institutionsPane", Boolean.TRUE);
            authorizedTabs.put("attributesPane", Boolean.TRUE);
            authorizedTabs.put("authorizationsPane", Boolean.TRUE);
            authorizedTabs.put("authDetailsPane", Boolean.TRUE);
        }
        outputMap.put("authorizedTabs", authorizedTabs);
        outputMap.put("selectableRows", selectableRows);
        this.log.debug("handleDisplay.finish");
    }
}
