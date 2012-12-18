package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* commented for porting 
import net.es.oscars.ConfigFinder;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.User;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;
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

import net.es.oscars.logging.ErrSev;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import net.es.oscars.authN.soap.gen.policy.UserDetails;
import net.es.oscars.authN.soap.gen.policy.QueryUserReply;
import net.es.oscars.authN.soap.gen.policy.ListInstsReply;
import net.es.oscars.authCommonPolicy.soap.gen.AttrDetails;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import edu.internet2.db.DBUtil;

public class IONUserQuery extends HttpServlet{
	private Logger log = Logger.getLogger(IONUserQuery.class);

	public void init(){
		if(!DBUtil.loadJDBCDriver()){
			this.log.error("Could not load local JDBC Driver");
			return;
		}
		log.debug("JDBC driver loaded");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {

		String methodName= "IONUserQuery";
		PrintWriter out = response.getWriter();
		JSONObject jsonObject = new JSONObject();

		//authenticate
		/*//commented for porting
        UserSession userSession = new UserSession();
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            return;
        } 
		 */

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

		String sessionUser = sessionReply.getUserName();
		this.log.debug("userName from sessionReply="+  sessionUser);
		if (sessionUser == null) {
			this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
			return;
		}
		//end new addition

		response.setContentType("application/json");

		//commented below for porting, to use the block after these comments
		/*
        AaaRmiInterface rmiClient = userSession.getAaaInterface(); 

        //verify is admin
        String organization = "";
        boolean isAdmin = false;
        try {
            HashMap<String, Object> attrQueryParams = new HashMap<String, Object>();
            attrQueryParams.put("objectType", ModelObject.ATTRIBUTE);
            attrQueryParams.put("operation", ModelOperation.LIST);
            attrQueryParams.put("listBy", "username");
            attrQueryParams.put("username", userName);
            HashMap<String, Object> attrQueryResponse =
                rmiClient.manageAaaObjects(attrQueryParams);
            List<Attribute> attrs = (List<Attribute>) attrQueryResponse.get("attributes");
            for(Attribute attr : attrs){
                if(IONAuthenticateAdmin.ION_ADMIN_ATTR_NAME.equals(attr.getName())){
                    isAdmin = true;
                    break;
                }
            }
            if(!isAdmin){
                throw new Exception("You do not have administrator privileges");
            }

            //get user institution
            organization = rmiClient.getInstitution(userName);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
		 */

		//verify is admin
		String organization = "";
		boolean isAdmin = false;
		try {
			List<AttributeType> userAttributes = sessionReply.getAttributes();
			isAdmin = IONUIUtils.isAdminUser(userAttributes);
			if(!isAdmin){				
				throw new Exception("You do not have administrator privileges");
			}

			//get user institution
			organization = IONUIUtils.getUsersOrg(userAttributes);
			log.debug ("obtained user's org: "+ organization);

		} catch (Exception e) {
			ServletUtils.handleFailure(out, log, e, methodName);
			return;
		}

		//Get user query
		String userQueried = request.getParameter("user");
		if(userQueried == null){
			ServletUtils.handleFailure(out, "No user specified", methodName);
			return;
		}

		Connection conn =  null;
		try {
			//Connection conn = DriverManager.getConnection("jdbc:derby:ion");
			conn = DBUtil.getDBConnection();
			PreparedStatement userStmt = conn.prepareStatement("SELECT username " +
			"FROM adminOrganizationUsers WHERE organization=? AND username=?");
			userStmt.setString(1, organization);
			userStmt.setString(2, userQueried);
			ResultSet users = userStmt.executeQuery();
			if(!users.next()){
				ServletUtils.handleFailure(out, "You probably created this user using OSCARS WBUI, "+
						"and do not have permission to view details until you add ION related privileges", methodName);				
				return;
			}
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			this.log.error(e.getMessage());
			ServletUtils.handleFailure(out, "Unable to load users", methodName);
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException sqlEx) {
				ServletUtils.handleFailure(out, "Unable to close DB Conn", methodName);

			}
			return;
		}
		/*

        //query rmi for users
        List<User> rmiUserList = null;
        try{
            HashMap<String, Object> userListRequest = new HashMap<String, Object>();
            userListRequest.put("objectType", ModelObject.USER);
            userListRequest.put("operation", ModelOperation.LIST);
            userListRequest.put("listType", "single");
            userListRequest.put("username", username);
            HashMap<String, Object> userListRmiResp = rmiClient.manageAaaObjects(userListRequest);
            rmiUserList = (List<User>) userListRmiResp.get("users");
        }catch(Exception e){
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        if(rmiUserList != null && rmiUserList.size() > 0){
            User user = rmiUserList.get(0);
            jsonObject.put("login", user.getLogin());
            jsonObject.put("firstName", user.getFirstName());
            jsonObject.put("lastName", user.getLastName());
            jsonObject.put("certSubject", user.getCertSubject());
            jsonObject.put("organization", user.getInstitution().getName());
            jsonObject.put("description", user.getDescription());
            jsonObject.put("emailPrimary", user.getEmailPrimary());
            jsonObject.put("emailSecondary", user.getEmailSecondary());
            jsonObject.put("phonePrimary", user.getPhonePrimary());
            jsonObject.put("phoneSecondary", user.getPhoneSecondary());
            List<Attribute> attributesForUser = ServletUtils.getAttributesForUser(user.getLogin(), rmiClient, out, log);
            List<String> attrNames = new ArrayList<String>();
            for(Attribute attr : attributesForUser){
        	attrNames.add(attr.getName());
            }
            jsonObject.put("attributes", attrNames);
        }else{
            ServletUtils.handleFailure(out, "Unable to find user "+username, methodName);
            return;
        }
		 */

		
		//get User details
		UserDetails user = null;
		List<AttributeType> queryReplyAttrs = null;
		try{
			Object[] soapReq = new Object[]{userQueried};
			Object[] resp = authNPolicyClient.invoke("queryUser", soapReq);
			QueryUserReply queryReply = (QueryUserReply) resp[0];
			user = queryReply.getUserDetails();
			queryReplyAttrs =
				queryReply.getUserAttributes().getSubjectAttributes();
		}catch(Exception e){
			ServletUtils.handleFailure(out, log, e, methodName);
			return;
		}

		if (user != null) {
		jsonObject.put("login", user.getLogin());
		jsonObject.put("firstName", user.getFirstName());
		jsonObject.put("lastName", user.getLastName());
		jsonObject.put("certSubject", user.getCertSubject());
		jsonObject.put("organization", user.getInstitution());
		jsonObject.put("description", user.getDescription());
		jsonObject.put("emailPrimary", user.getEmailPrimary());
		jsonObject.put("emailSecondary", user.getEmailSecondary());
		jsonObject.put("phonePrimary", user.getPhonePrimary());
		jsonObject.put("phoneSecondary", user.getPhoneSecondary());
		List<String> attrNames = new ArrayList<String>();
		attrNames = getAttributesForUser(queryReplyAttrs);
		jsonObject.put("attributes", attrNames);
		}
		else { 
			ServletUtils.handleFailure(out, "No user '" + userQueried + "' found!!", methodName);
                        return;
		}
		out.println("{}&&" + jsonObject);
		this.log.info(methodName +":end");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		this.doGet(request, response);
	}

	/* Method to add attributes for user from a given list*/
	private List<String> getAttributesForUser( List<AttributeType> allAttributes ) {
		List<String> attributeList = new ArrayList<String>();
		// default is none
		/*
        attributeList.add("None");
        if (userAttributes.isEmpty()) {
            attributeList.add("true");
        } else {
            attributeList.add("false");
        } */
		//for (AttrDetails a: allAttributes) {
		for (AttributeType aa: allAttributes) {
			//attributeList.add(a.getValue() + " -> " + a.getDescription());
			List<Object> samlValues = aa.getAttributeValue();
			for (Object samlValue: samlValues) {
				String value = (String) samlValue;
				attributeList.add(value);
				this.log.info("saml value " + value);
			}
			/*            
            if (foundForUser) {
                attributeList.add("true");
            } else {
                attributeList.add("false");
            }
			 */

		}
		return attributeList;

	} //end method getAttributesforUser	
	
}
