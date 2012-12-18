package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

//import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.wbui.servlets.AuthenticateUser;
import net.es.oscars.wbui.servlets.UserSession;
import net.es.oscars.wbui.servlets.CheckSessionReply;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;

import net.sf.json.JSONObject;

//new imports 
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.authN.soap.gen.*;
import net.es.oscars.authN.soap.gen.policy.*;
//import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

public class VerifySession extends HttpServlet{
    private Logger log = Logger.getLogger(VerifySession.class);
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String methodName = "VerifySession";
        
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        /*
        UserSession userSession = new UserSession();
        AaaRmiInterface rmiClient = null;
        String sesUserName = userSession.checkSession(out, rmiClient, request, methodName);
        if(sesUserName == null){
            return;
        }
        */
        
        String transId = 
        		PathTools.getLocalDomainId() + "-IONUI-" + UUID.randomUUID().toString();
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
        //AuthZClient authZClient = core.getAuthZClient();

        UserSession userSession = new UserSession(core);
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, request,
                                     methodName);
        if (sessionReply == null) {
            this.log.warn("No user session: cookies invalid???");
            return;
        }
        
	outputMap.put("success", Boolean.TRUE);
        outputMap.put("method", methodName);
        outputMap.put("status", "Login succeeded.");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&& " + jsonObject);
        //new 
        this.log.info(methodName +":end");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
            this.doGet(request, response);
    }
}
