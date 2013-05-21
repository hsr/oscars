package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
//import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.internet2.db.DBUtil;

import net.sf.json.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.api.soap.gen.v06.MplsInfo;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
//import net.es.oscars.api.soap.gen.v06.VlanTag;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;

import net.es.oscars.wbui.servlets.QueryReservation;
import net.es.oscars.resourceManager.common.RMUtils;
import net.es.oscars.resourceManager.beans.PathType;
import net.es.oscars.resourceManager.beans.Path;
import net.es.oscars.wbui.servlets.CheckSessionReply;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;

public class IONQueryReservation extends QueryReservation{
    private Logger log = Logger.getLogger(IONQueryReservation.class);
    
    public void init(){
    	if(!DBUtil.loadJDBCDriver()){
            this.log.error("Could not load local JDBC Driver");
            return;
        }
        log.debug("JDBC driver loaded");
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "QueryReservation";

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        //new addition for porting
        
        String transId  = PathTools.getLocalDomainId() + "-IONUI-" + UUID.randomUUID().toString();
        this.netLogger = new OSCARSNetLogger();
        this.netLogger.init(ServiceNames.SVC_IONUI,transId);
        OSCARSNetLogger.setTlogger(this.netLogger);
        this.log.info(this.netLogger.start(methodName));

        ServletCore core = (ServletCore)
        	getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        CoordClient coordClient = core.getCoordClient();
        //get authZ client
        AuthZClient authZClient = core.getAuthZClient();
        

        CheckSessionReply sessionReply = IONUIUtils.getUserSession(request, methodName, out, core);
        String userName = sessionReply.getUserName();
        this.log.debug("userName from sessionReply="+  userName);
        if (userName == null) {
            this.log.warn(this.netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
            return;
        }
        //end new addition

      //new additions for ION porting
        Map<String, Object> outputMap = new HashMap<String, Object>();
        String gri = request.getParameter("gri");
        QueryResContent queryReq = new QueryResContent();
        List<AttributeType> userAttributes = 
        	sessionReply.getAttributes();
        SubjectAttributes subjectAttrs = new SubjectAttributes();
        List<AttributeType> reqAttrs = subjectAttrs.getSubjectAttribute();
        for (AttributeType attr: userAttributes) {
            reqAttrs.add(attr);
        }
        ListRequest listReq = null;
        ListReply listResponse = null;
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setOriginator(subjectAttrs);
        msgProps.setGlobalTransactionId(transId);
        queryReq.setMessageProperties(msgProps);
        queryReq.setGlobalReservationId(gri);
        
        CheckAccessReply checkAccessReply = null;        
        ResDetails resDetails = null;
        String authVal = null;
        
        
        try {
        	log.debug("Trying to query reservation for GRI:" + gri + 
        			", using req object" + queryReq);
	        Object[] req = new Object[]{subjectAttrs,queryReq};
	        Object[] res = coordClient.invoke("queryReservation",req);
	        resDetails = ((QueryResReply) res[0]).getReservationDetails();
	       	List <OSCARSFaultReport> faultReports = ((QueryResReply) res[0]).getErrorReport();
 
	        // check to see if user is allowed to see the buttons allowing
            // reservation modification
            authVal = ServletUtils.checkPermission(authZClient,
                                                   userAttributes,
                                                   AuthZConstants.RESERVATIONS,
                                                   AuthZConstants.MODIFY);
	        // check to see if user is allowed to see the clone button, which
            // requires generic reservation create authorization
	       checkAccessReply  =  ServletUtils.checkAccess(authZClient,
                    userAttributes,
                    AuthZConstants.RESERVATIONS,
                    AuthZConstants.CREATE);
	        authVal = checkAccessReply.getPermission();
	        if (!authVal.equals(AuthZConstants.ACCESS_DENIED)) {
	        	outputMap.put("resvCloneDisplay", Boolean.TRUE);
	        } else {
	        	outputMap.put("resvCloneDisplay", Boolean.FALSE);
	        }

		//get content from super class
		this.contentSection(resDetails, faultReports, outputMap);
        } catch(Exception oscarsExcep) {
        	this.log.debug (this.netLogger.error(methodName,ErrSev.MAJOR, "caught " + oscarsExcep.toString()));
            oscarsExcep.printStackTrace();
            ServletUtils.handleFailure(out, this.log, oscarsExcep, methodName);
            return;
        }
        
        if (resDetails.getStatus() == null) {
            outputMap.put("status", "Reservation details for " + gri);
        } else {
            outputMap.put("status", resDetails.getStatus());
        }
        //get local status
        //TBD : getLocalStatus
        outputMap.put("localStatusReplace", resDetails.getStatus());
        //output paths
        
        try { 
        	this.outputRawPath(resDetails, outputMap);
        } catch (OSCARSServiceException e) {
        	ServletUtils.handleFailure(out, log, e, methodName);
        	return;
        }
       
        
        //Get extra info from Derby DB
        Connection conn = null;
        try {
            //conn = DriverManager.getConnection("jdbc:derby:ion");
        	conn = DBUtil.getDBConnection();
            //Get enpoint names
            PreparedStatement epStmt = conn.prepareStatement("SELECT source, destination FROM endpoints WHERE gri=?");
            //debug/ TBD change warn to debug
            log.warn("---Getting endpoints where gri ="+ gri);
            epStmt.setString(1, gri);
            ResultSet rs = epStmt.executeQuery();
            //debug/ TBD change warn to debug
            log.warn("ResultSet "+ rs);
            while ( rs.next() ) {
            	outputMap.put("sourceNameReplace", rs.getString(1));
            	outputMap.put("destinationNameReplace", rs.getString(2));
            	log.warn("--ResultSet next "+ rs.getString(1));
            }
            
            //Check if favorite
            PreparedStatement favStmt = conn.prepareStatement("SELECT id FROM favorites WHERE login=? AND gri=?");
            favStmt.setString(1, userName);
            favStmt.setString(2, gri);
            ResultSet favRs = favStmt.executeQuery();
            Boolean fav = favRs.next();
            outputMap.put("favorite", fav);
            log.warn("---FAV??"+ fav); 
        } catch (SQLException e) {
            this.log.error(e.getMessage());
        } finally{
            DBUtil.closeConnection(conn);
        }
        
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        log.info(this.netLogger.end(methodName));
        return;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.doGet(request, response);
    }

    public void outputRawPath(ResDetails resv, Map<String, Object> outputMap) 
    	throws OSCARSServiceException {
    	
    	  UserRequestConstraintType uConstraint = resv.getUserRequestConstraint();
          PathInfo pathInfo = null;
          String pathType = null;
          ReservedConstraintType rConstraint = resv.getReservedConstraint();
          if (rConstraint !=  null) {
              pathInfo=rConstraint.getPathInfo();
              pathType = "reserved";
              
          } else {
              uConstraint = resv.getUserRequestConstraint();
              if (uConstraint == null) {
                  throw new OSCARSServiceException("invalid reservation, no reserved or requested path");
              }
              pathInfo=uConstraint.getPathInfo();
              pathType="requested";
              System.out.println("no path reserved, using requested path ");
          }
          
          //newly added setType in pathInfo
          pathInfo.setPathType(pathType);
          org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent path = pathInfo.getPath();
          String [] pathStrs = IONUIUtils.getRawPathArray(path);
          //Java script takes care of handling empty/null pathStrs
          outputMap.put("rawPath", pathStrs);
          //get domains involved in this path
          String sAllDomains = IONUIUtils.getDomainsString(path);
          sAllDomains = ( (sAllDomains == null) || (sAllDomains.length() == 0) )?"Not found":sAllDomains;
          outputMap.put("rawInterPath",sAllDomains);
    }
}
