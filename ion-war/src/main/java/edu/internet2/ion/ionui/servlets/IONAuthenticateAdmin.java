package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/*
import net.es.oscars.aaa.Attribute;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;
import net.es.oscars.servlets.ServletUtils;
import net.es.oscars.servlets.UserSession;
*/

import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.AuthNPolicyClient;

import net.es.oscars.wbui.servlets.AuthenticateUser;
import net.es.oscars.wbui.servlets.UserSession;
import net.es.oscars.wbui.servlets.CheckSessionReply;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.authN.soap.gen.*;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.common.soap.gen.SubjectAttributes;

import org.apache.log4j.Logger;

import org.ogf.schema.network.topology.ctrlplane.*;
import net.es.oscars.coord.common.URNParser;
import net.es.oscars.coord.common.URNParserResult;
import oasis.names.tc.saml._2_0.assertion.AttributeType;


import net.sf.json.JSONObject;

public class IONAuthenticateAdmin extends HttpServlet{
    private Logger log = Logger.getLogger(IONAuthenticateAdmin.class);
   
    //moved to IONUIUtils 
    //final public static String ION_ADMIN_ATTR_NAME = "ION-administrator";
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String methodName = "IONAuthenticateAdmin";
        boolean isAdmin = false;
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

	/* //commented out for porting
        UserSession userSession = new UserSession();
        AaaRmiInterface rmiClient = null;
        String userName;
        String sesUserName = userSession.checkSession(null, rmiClient, request, methodName);
        if (sesUserName != null) {
            userName = sesUserName;
            rmiClient = userSession.getAaaInterface();
        } else {
            userName = request.getParameter("userName");
            rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        }
	*/
	
	//replace section above with this block
	String transId  = PathTools.getLocalDomainId() + "-IONUI-" + UUID.randomUUID().toString();
        OSCARSNetLogger netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_IONUI,transId);
        OSCARSNetLogger.setTlogger(netLogger);
        log.info(netLogger.start(methodName));
        ServletCore core = (ServletCore)
            getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        AuthNClient authNClient = core.getAuthNClient();
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZClient authZClient = core.getAuthZClient();

        UserSession userSession = new UserSession(core);
        String userName = request.getParameter("userName");
        //new addition ends	

        String password = request.getParameter("initialPassword");
        String sessionName = "";
        
        Random generator = new Random();
        int r = generator.nextInt();
        sessionName = String.valueOf(r);

	//addition
        //initialize for verifying login
	VerifyLoginReqType verifyLoginReq = new VerifyLoginReqType();
        LoginId login = new LoginId();
        login.setLoginName(userName);
        login.setPassword(password);
        verifyLoginReq.setTransactionId(transId);
        verifyLoginReq.setLoginId(login);
        Object[] req = new Object[]{verifyLoginReq};

        try {
	    //comment below block to replace for porting 
	    /*
            String loginUserName =
                rmiClient.verifyLogin(userName, password, sessionName);
            userName = (String) loginUserName;
	    */

            Object[] resp = authNClient.invoke("verifyLogin", req);
            VerifyReply reply = (VerifyReply) resp[0];
            SubjectAttributes attrs = reply.getSubjectAttributes();
            SessionOpParams setSessionReq = new SessionOpParams();
            setSessionReq.setUserName(userName);
            setSessionReq.setSessionName(sessionName);
            Object[] sessionReq = new Object[]{setSessionReq};
            Object[] sessionResp =
                authNPolicyClient.invoke("setSession", sessionReq);
		
	    //commented block to replace with following for porting 
	    /* 
            HashMap<String, Object> attrQueryParams = new HashMap<String, Object>();
            attrQueryParams.put("objectType", ModelObject.ATTRIBUTE);
            attrQueryParams.put("operation", ModelOperation.LIST);
            attrQueryParams.put("listBy", "username");
            attrQueryParams.put("username", userName);
            HashMap<String, Object> attrQueryResponse =
                rmiClient.manageAaaObjects(attrQueryParams);
            List<Attribute> attrs = (List<Attribute>) attrQueryResponse.get("attributes");
            for(Attribute attr : attrs){
                if(ION_ADMIN_ATTR_NAME.equals(attr.getName())){
                    isAdmin = true;
                    break;
                }
            }
	    */
	    //replaced above section with below for porting
	    List <AttributeType> allAttrs = attrs.getSubjectAttribute();
            isAdmin = IONUIUtils.isAdminUser(allAttrs);
            if(!isAdmin){
                throw new Exception("You do not have administrator privileges");
            }
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        log.debug("setting cookie name to " + userName);
        userSession.setCookie("userName", userName, response);
        log.debug("setting session name to " + sessionName);
        userSession.setCookie("sessionName", sessionName, response);

        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("status", userName + " signed in");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&& " + jsonObject);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.doGet(request, response);
    }
}
