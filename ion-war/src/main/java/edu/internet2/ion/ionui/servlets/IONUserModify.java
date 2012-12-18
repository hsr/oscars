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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
import net.es.oscars.ConfigFinder;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.User;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;
import net.es.oscars.servlets.ServletUtils;
import net.es.oscars.servlets.UserSession;
import net.sf.json.JSONObject;
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
import net.es.oscars.authN.soap.gen.policy.FullUserParams;


import org.apache.log4j.Logger;

import net.sf.json.JSONObject;
import edu.internet2.db.DBUtil;

public class IONUserModify extends HttpServlet{
	private Logger log = Logger.getLogger(IONUserModify.class);

	public void init(){
		if(!DBUtil.loadJDBCDriver()){
			this.log.error("Could not load local JDBC Driver");
			return;
		}
		log.debug("JDBC driver loaded");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {

		String methodName= "IONUserModify";
		PrintWriter out = response.getWriter();

		//authenticate
		/* //commented for porting
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
		String username = request.getParameter("username");
		if(username == null){
			ServletUtils.handleFailure(out, "No user specified", methodName);
			return;
		}

		Connection conn = null;
		try {
			conn = DBUtil.getDBConnection();
			PreparedStatement userStmt = conn.prepareStatement("SELECT username " +
			"FROM adminOrganizationUsers WHERE organization=? AND username=?");
			userStmt.setString(1, organization);
			userStmt.setString(2, username);
			ResultSet users = userStmt.executeQuery();
			log.debug("Modify users query " +
				"SELECT username " +
                        "FROM adminOrganizationUsers WHERE organization="+organization+
				" AND username=" + username);
			if(!users.next()){
				ServletUtils.handleFailure(out, "You do not have permission to modify user", methodName);
				log.error("----Did not find entry in adminOrg tables");
				return;
			}
			//debug else
			else {
				log.debug("---User found ");
			}
			conn.close();
		} catch (SQLException e) {
			this.log.error(e.getMessage());
			ServletUtils.handleFailure(out, "Unable to load users", methodName);
			return;
		}


		//get User details		
		List<AttributeType> queryReplyAttrs = null;
		UserDetails userCurrentDetails = null;
		List<AttributeType> userCurrentAttrs =  null;
		try{			
			//get current roles of user
			Object[] soapReq = new Object[]{username};
			Object[] resp = authNPolicyClient.invoke("queryUser", soapReq);
			QueryUserReply queryReply = (QueryUserReply) resp[0];
			userCurrentDetails = queryReply.getUserDetails();
			userCurrentAttrs =
				queryReply.getUserAttributes().getSubjectAttributes();
			log.debug(" Userquery: " +username);
		}catch(Exception e){
			ServletUtils.handleFailure(out, log, e, methodName);
			return;
		}

		if (userCurrentDetails == null) {
            		String msg = "User " + username + " does not exist";
            		ServletUtils.handleFailure(out, msg, methodName);
            		return;
        	}

		/*User user = IONUserAdd.processUserForm(request, methodName, out, rmiClient);
		if(user == null){ 
			return;
		}
		*/

		//commnted above to replace with below for porting
		UserDetails newUser =
			IONUserAdd.processUserForm(request, methodName, out, authNPolicyClient, username);
		if(newUser == null){
			log.error("New User details turns out to be null. Returning with no further action");
			return;
		}

		//attrs
		/*
		ArrayList<String> roles = new ArrayList<String>();
		if(request.getParameter("adminRole") != null && "1".equals(request.getParameter("adminRole"))){
			roles.add("ION-administrator");
		}
		if(request.getParameter("engineerRole") != null && "1".equals(request.getParameter("engineerRole"))){
			roles.add("OSCARS-site-administrator");
		}
		if(request.getParameter("operatorRole") != null && "1".equals(request.getParameter("operatorRole"))){
			roles.add("ION-operator");
		}
		if((request.getParameter("userRole") != null && "1".equals(request.getParameter("userRole"))) || roles.isEmpty()){
			roles.add("OSCARS-user");
		}
		 */

		//get current roles
		/*
		List<Attribute> attributesForUser =
			ServletUtils.getAttributesForUser(username, rmiClient, out, log);
		ArrayList<String> curRoles = new ArrayList<String>();
		for (Attribute attr : attributesForUser) {
			curRoles.add(attr.getName());
		}

		HashMap<String, Object> rmiParams = new HashMap<String, Object>();
		rmiParams.put("user", user);
		rmiParams.put("newRoles", roles);
		rmiParams.put("curRoles", curRoles);
		rmiParams.put("setPassword", setPassword);
		rmiParams.put("objectType", ModelObject.USER);
		rmiParams.put("operation", ModelOperation.MODIFY);
		try{
			HashMap<String, Object> rmiResult = 
				ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);
		}catch(Exception e){
			ServletUtils.handleFailure(out, "Unable to modify user", methodName);
			this.log.error(e.getMessage());
			return;
		}

		 */


		//roles = IONUIUtils.getUserRoles(queryReplyAttrs, roles);
		//log.error("--Roles size now"+ roles.size());

		//remove duplicates of roles


		//set password
		//TDB confirmation needed?
		/*
		Boolean setPassword = true;
		if(request.getParameter("password").matches("^\\*+$")){
			this.log.info("Set password is false");
			setPassword = false;
		}
		 */

		try{
			this.convertParams(request, userCurrentDetails);

			boolean setPassword = false;
			String password = request.getParameter("password");
			String confirmationPassword =
				request.getParameter("passwordConfirmation");
			// handle password modification if necessary
			// check will return null, if password is  not to be changed
			String newPassword = ServletUtils.checkPassword(password,
					confirmationPassword);
			if (newPassword != null) {
				this.log.info("changing password");
				userCurrentDetails.setPassword(newPassword);
				setPassword = true;
			}

			//create modify request
			List<AttrDetails> allAttributes =
				ServletUtils.getAllAttributes(authNPolicyClient, null);
			FullUserParams req = new FullUserParams();
			List<String> newRoles = new ArrayList<String>();
			List<String> curRoles = req.getCurAttributes();
			List<String> newReqRoles = req.getNewAttributes();

			//add new roles requested from UI into newRoles
			/*
         	RoleUtils roleUtils = new RoleUtils();
         	String roles[] = request.getParameterValues("attributeName");
         	for (int i=0; i < roles.length; i++) {
        	 	roles[i] = ServletUtils.dropDescription(roles[i].trim());
        	}
         	if (!roles[0].equals("None")) {
        	 	this.log.info("number of roles input is " + roles.length);
        	 	newRoles = roleUtils.checkRoles(roles, allAttributes);
         	}
			 */

			//HashSet <String> roles = new HashSet <String> ();
			//hashset to have non-duplicate values of roles
			if(request.getParameter("adminRole") != null && "1".equals(request.getParameter("adminRole"))){
				newRoles.add("ION-administrator");
			}
			if(request.getParameter("engineerRole") != null && "1".equals(request.getParameter("engineerRole"))){
				newRoles.add("OSCARS-site-administrator");
			}
			if(request.getParameter("operatorRole") != null && "1".equals(request.getParameter("operatorRole"))){
				newRoles.add("ION-operator");
			}
			if((request.getParameter("userRole") != null && "1".equals(request.getParameter("userRole")))
					|| newRoles.isEmpty()){
				newRoles.add("OSCARS-user");
			}

			//add current roles of user got from querying into modify-req params
			for (AttributeType attr: userCurrentAttrs) {
				String attrName = attr.getName();
				if (!attrName.equals("loginId") && !attrName.equals("institution")) {
					List<Object> samlValues = attr.getAttributeValue();
					for (Object samlValue: samlValues) {
						String value = (String) samlValue;
						curRoles.add(value);
					}
				}
			}

			//add in new roles into the request
			for (String role: newRoles) {
				this.log.info("new role: " + role);
				newReqRoles.add(role);
			}
			//send modify request

			req.setUserDetails(newUser);
			req.setPasswordChanged(setPassword);
			Object[] soapReq = new Object[]{req};
			Object[] resp = authNPolicyClient.invoke("modifyUser", soapReq);
		}  catch (Exception e) {
			ServletUtils.handleFailure(out, log, e, methodName);
			return;
		}

		/*


		 */

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("success", new Boolean(true));
		jsonObject.put("username", username);

		out.println("{}&&" + jsonObject);
		this.log.info(methodName + ":end");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		this.doGet(request, response);
	}


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
	} //end convert User section

}
