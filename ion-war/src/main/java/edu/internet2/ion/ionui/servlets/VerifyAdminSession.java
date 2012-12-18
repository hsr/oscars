package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import net.es.oscars.servlets.AuthenticateUser;
import net.es.oscars.servlets.ServletUtils;
import net.es.oscars.servlets.UserSession;
*/
import net.es.oscars.wbui.servlets.AuthenticateUser;
import net.es.oscars.wbui.servlets.UserSession;
import net.es.oscars.wbui.servlets.CheckSessionReply;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.utils.topology.PathTools;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;

import net.sf.json.JSONObject;

public class VerifyAdminSession extends HttpServlet{
    private Logger log = Logger.getLogger(VerifyAdminSession.class);
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String methodName = "VerifyAdminSession";
        boolean isAdmin = false;
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
	/*
        UserSession userSession = new UserSession();
        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        String sesUserName = userSession.checkSession(out, rmiClient, request, methodName);
        if(sesUserName == null){
            return;
        }
 	*/       
	//replaced above with below for porting
	//new addition for porting
        String transId  = PathTools.getLocalDomainId() + "-IONUI-" + UUID.randomUUID().toString();
        OSCARSNetLogger netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_IONUI,transId);
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.info(netLogger.start(methodName));


        ServletCore core = (ServletCore)
                getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        CoordClient coordClient = core.getCoordClient();
        //get authZ client
        AuthZClient authZClient = core.getAuthZClient();
        //AuthNclient
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();

        CheckSessionReply sessionReply = IONUIUtils.getUserSession(request, methodName, out, core);
	if (sessionReply == null) {
		this.log.error(netLogger.error(methodName,ErrSev.MINOR,"No user session. Returning"));
     		return;
	}

        String userName = sessionReply.getUserName();
        this.log.debug("userName from sessionReply="+  userName);
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
            return;
        }
        //end new addition


        try {
	    /*
            HashMap<String, Object> attrQueryParams = new HashMap<String, Object>();
            attrQueryParams.put("objectType", ModelObject.ATTRIBUTE);
            attrQueryParams.put("operation", ModelOperation.LIST);
            attrQueryParams.put("listBy", "username");
            attrQueryParams.put("username", sesUserName);
            HashMap<String, Object> attrQueryResponse =
                rmiClient.manageAaaObjects(attrQueryParams);
            List<Attribute> attrs = (List<Attribute>) attrQueryResponse.get("attributes");
	    */
 	    //replaced above section with below for porting
	    List<AttributeType> userAttributes = sessionReply.getAttributes();
	    isAdmin = IONUIUtils.isAdminUser(userAttributes);
            if(!isAdmin){
                throw new Exception("You do not have administrator privileges");
            }
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("method", methodName);
        outputMap.put("status", "Login succeeded.");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&& " + jsonObject);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
            this.doGet(request, response);
    }
}
