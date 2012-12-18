package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

//import net.es.oscars.bss.BSSException;
//import net.es.oscars.bss.Reservation;
//import net.es.oscars.rmi.RmiUtils;
//import net.es.oscars.rmi.bss.BssRmiInterface;
//import net.es.oscars.rmi.bss.xface.RmiQueryResReply;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
//new imports
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.common.soap.gen.SubjectAttributes;

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
import net.es.oscars.common.soap.gen.SubjectAttributes;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.common.soap.gen.MessagePropertiesType;

import net.sf.json.JSONObject;

import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.es.oscars.utils.soap.OSCARSServiceException;

import net.sf.json.JSONObject;

public class IONQueryReservationStatus extends HttpServlet{
    private Logger log = Logger.getLogger(IONQueryReservationStatus.class);
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "QueryReservationStatus";

	//ION porting
        //UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
	/* //commenting for porting
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }	
	*/
	//new addition for porting
        //can I move this block to a common place?
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
        
        CheckSessionReply sessionReply = IONUIUtils.getUserSession(request, methodName, out, core);
        String userName = sessionReply.getUserName();
        this.log.debug("userName from sessionReply="+  userName);
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
            return;
        }
        
        
       	//end new addition
	
        /*Commenting for porting 
        RmiQueryResReply rmiReply = new RmiQueryResReply();
        Map<String, Object> outputMap = new HashMap<String, Object>();
        String gri = request.getParameter("gri");
        try {
            BssRmiInterface bssRmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            rmiReply = bssRmiClient.queryReservation(gri, userName);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        try {
            this.contentSection(rmiReply, outputMap);
        } catch (BSSException e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        Reservation resv = rmiReply.getReservation();
        */
        //new additions for ION porting
        QueryResContent queryReq = new QueryResContent();
        Map<String, Object> outputMap = new HashMap<String, Object>();
        String gri = request.getParameter("gri");
        
        List<AttributeType> userAttributes = 
        	sessionReply.getAttributes();
        SubjectAttributes subjectAttrs = new SubjectAttributes();
        List<AttributeType> reqAttrs = subjectAttrs.getSubjectAttribute();
        for (AttributeType attr: userAttributes) {
            reqAttrs.add(attr);
        }
        
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setOriginator(subjectAttrs);
        msgProps.setGlobalTransactionId(transId);
        queryReq.setMessageProperties(msgProps);
        queryReq.setGlobalReservationId(gri);
        
        CheckAccessReply checkAccessReply = null;        
        ResDetails resDetails = null;
        ListRequest listReq = null;
        ListReply listResponse = null;
       
        try {	              
	        Object[] req = new Object[]{subjectAttrs,queryReq};
	        Object[] res = coordClient.invoke("queryReservation",req);
	        resDetails = ((QueryResReply) res[0]).getReservationDetails();
	        this.contentSection(resDetails, outputMap);
        } catch (Exception oscarsExcep) {
        	// any error will show up as an exception
            this.log.debug (netLogger.error(methodName,ErrSev.MAJOR, "caught " + oscarsExcep.toString()));
            oscarsExcep.printStackTrace();
            ServletUtils.handleFailure(out, this.log, oscarsExcep, methodName);
            return;
        }
        //end porting additions
	    
        
        /* porting comments 
	        if (resv.getStatusMessage() == null) {
	            outputMap.put("status", "Reservation details for " + gri);
	        } else {
	            outputMap.put("status", resv.getStatusMessage());
	        }*/


        //added below if in place of above
        //The 06 ResDetails only has "getstatus".
        //05-Reservation class has getstatus, as well as getStatusMessage
        //getStatus() should work
        if (resDetails.getStatus() == null) {
        	outputMap.put("status", "Reservation details for " + gri);
        } else {
        	outputMap.put("status", resDetails.getStatus());
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(netLogger.end(methodName));
      
        return;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.doGet(request, response);
    }

    /**
     * Only fills in those fields that might have changed due to change
     * in reservation status.
     */
    /*
    public void contentSection(RmiQueryResReply rmiReply, Map<String,Object> outputMap)
            throws BSSException {

        Reservation resv = rmiReply.getReservation();
        String status = resv.getStatus();
        outputMap.put("griReplace", resv.getGlobalReservationId());
        outputMap.put("statusReplace", status);
        outputMap.put("localStatusReplace", resv.getLocalStatus());
    }
    */
    public void contentSection(ResDetails resv, Map<String,Object> outputMap)
    throws OSCARSServiceException {
		
		String status = resv.getStatus(); //returns localStatus. ?TBD
		outputMap.put("griReplace", resv.getGlobalReservationId());
		outputMap.put("statusReplace", status);
		//outputMap.put("localStatusReplace", resv.getStatus()); //TBD what is localStatus?
	}
    
}
