package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
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
import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.User;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;
import net.es.oscars.servlets.ServletUtils;
import net.es.oscars.servlets.UserSession;
 */ // commented for porting
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
import net.es.oscars.authN.soap.gen.policy.FullUserParams;
import net.es.oscars.authN.soap.gen.policy.UserDetails;
import net.es.oscars.authN.soap.gen.policy.QueryUserReply;
import net.es.oscars.authN.soap.gen.policy.ListInstsReply;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import edu.internet2.db.DBUtil;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

public class IONUserAdd extends HttpServlet{
	private Logger log = Logger.getLogger(IONUserAdd.class);

	private static final String[] FIELDS = {"username", "password", "passwordConfirmation", 
		"certSubject", "firstName", "lastName", "institutionName", "emailPrimary", 
		"emailSecondary", "phonePrimary", "phoneSecondary", "description"};

	private static final String[] REQ_FIELDS = {"username", "password", "passwordConfirmation", 
		"firstName", "lastName", "institutionName", "emailPrimary", "phonePrimary"};

	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {

		String methodName= "IONUserAdd";
		PrintWriter out = response.getWriter();

		//authenticate the reservation

		//comment below to change for porting
		/*
        UserSession userSession = new UserSession();
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            return;
        }
		 */

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
			this.log.error(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
			return;
		}
		//end new addition


		//commented below block to use new code for porting
		/*
        AaaRmiInterface rmiClient = userSession.getAaaInterface();

        //verify is admin
        boolean isAdmin = false;
        String organization = "";
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

		//new block starts
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
		//new block ends


		//User newUser = processUserForm(request, methodName, out, rmiClient);
		//comment above for porting to include below
		UserDetails newUser = processUserForm(request, methodName, out, authNPolicyClient, userName);
		if(newUser == null){ 
			log.error("New User details turns out to be null. Returning with no further action");
			return;
		}

		//call rmi to add user
		HashMap<String, Object> rmiParams = new HashMap<String, Object>();
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

		/*
        rmiParams.put("addRoles", roles);
        rmiParams.put("objectType", ModelObject.USER);
        rmiParams.put("operation", ModelOperation.ADD);
        rmiParams.put("user", newUser);
        try{
            HashMap<String, Object> rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);
		 */

		log.debug("Starting to add user...");
		FullUserParams req = new FullUserParams();
		req.setUserDetails(newUser);
		List<String> newAttributes = req.getNewAttributes();
		for (String addRole: roles) {
			log.debug("..Adding role:" + addRole);
			newAttributes.add(addRole);
		}
		try {
			Object[] soapReq = new Object[]{req};
			Object[] resp = authNPolicyClient.invoke("addUser", soapReq);
		} catch(Exception e){
			ServletUtils.handleFailure(out, "Unable to add user", methodName);
			this.log.error(" Exception when adding new user" + e); //.getMessage());
			e.printStackTrace();
			return;
		}

		//add user to local db
		Connection conn = null;
		try {
			//Connection conn = DriverManager.getConnection("jdbc:derby:ion");
			conn = DBUtil.getDBConnection();
			PreparedStatement userStmt = conn.prepareStatement("INSERT INTO " +
			"adminOrganizationUsers VALUES(DEFAULT,?,?)");
			userStmt.setString(1, organization);
			userStmt.setString(2, newUser.getLogin());
			userStmt.execute();
			conn.close();
		} catch (SQLException e) {
			this.log.error(e.getMessage());
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException sqlEx) {
				ServletUtils.handleFailure(out, "Unable to close DB Conn", methodName);
			}
			ServletUtils.handleFailure(out, "Unable to load users", methodName);
			return;
		}

		JSONObject jsonObject =new JSONObject();
		jsonObject.put("success", new Boolean("true"));
		jsonObject.put("login", request.getParameter("username"));
		out.println("{}&&" + jsonObject);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		this.doGet(request, response);
	}


	//method to process user input and create an UserDetails object
	public static UserDetails processUserForm(HttpServletRequest request, String methodName, PrintWriter out, AuthNPolicyClient client, String userName) {
		//check null fields
		for(String field : REQ_FIELDS){
			if(request.getParameter(field) == null) {
				ServletUtils.handleFailure(out, field + " cannot be null.", methodName);
				return null;            
			}
		}

		//check password        
		if(!request.getParameter("password").equals(request.getParameter("passwordConfirmation"))) {
			ServletUtils.handleFailure(out, "Password fields do not match", methodName);
			return null;
		}

		//check institution
		List <String> insts = null;
		String userInputInst = "";
		try {
			Object[] soapReq = new Object[]{userName};
			Object[] resp = client.invoke("listInsts", soapReq);
			ListInstsReply reply = (ListInstsReply) resp[0];
			insts = reply.getName();
			//log.error ("obtained institutions list:"+ institutions);

			boolean instExists = false;
			userInputInst = request.getParameter("institutionName");
			for(String inst : insts){
				if(inst.equals(userInputInst)){
					//log.debug("Institution of new user already exists.");
					instExists = true;
					break;
				}
			}
			//add institutions to MySql db if it does not already exist
			if(!instExists){
				soapReq = new Object[]{userInputInst};
				resp = client.invoke("addInst", soapReq);
				//log.debug("Adding institute "+ userInputInst + " to MySQL Db");
			}
		}catch(Exception e){
			e.printStackTrace();
			ServletUtils.handleFailure(out, "Unable to set institution", methodName);
			return null;
		}

		//build user
		UserDetails newUser = new UserDetails();
		try {
			newUser.setLogin(request.getParameter("username"));
			System.out.println("LOGINNN:"+ newUser.getLogin());
			String psWord = ServletUtils.checkPassword(request.getParameter("password"), request.getParameter("passwordConfirmation"));
			newUser.setPassword(psWord);
			newUser.setCertSubject(request.getParameter("certSubject"));
			newUser.setFirstName(request.getParameter("firstName"));
			newUser.setLastName(request.getParameter("lastName"));
			newUser.setInstitution(userInputInst);
			newUser.setEmailPrimary(request.getParameter("emailPrimary"));
			newUser.setEmailSecondary(request.getParameter("emailSecondary"));
			newUser.setPhonePrimary(request.getParameter("phonePrimary"));
			newUser.setPhoneSecondary(request.getParameter("phoneSecondary"));
			newUser.setDescription(request.getParameter("description"));
		} catch (Exception e) {
			e.printStackTrace();
			ServletUtils.handleFailure(out, "Unable to create new User", methodName);
			return null; 
		}
		return newUser;
	}

	//commented out to use above version for porting
	/*
  public static User processUserForm(HttpServletRequest request, String methodName, 
	    PrintWriter out, AaaRmiInterface rmiClient){
	//check null fields
        for(String field : REQ_FIELDS){
            if(request.getParameter(field) == null){
        	 ServletUtils.handleFailure(out, field + " cannot be null.", methodName);
        	 return null;
            }
        }

        //check password
        if(!request.getParameter("password").equals(request.getParameter("passwordConfirmation"))){
            ServletUtils.handleFailure(out, "Password fields do not match", methodName);
            return null;
        }

        //check institution
        try {
            HashMap<String, Object> instQueryParams = new HashMap<String, Object>();
            instQueryParams.put("objectType", ModelObject.INSTITUTION);
            instQueryParams.put("operation", ModelOperation.LIST);
            instQueryParams.put("listType", "plain");
            HashMap<String, Object> instQueryResponse =
                rmiClient.manageAaaObjects(instQueryParams);
            List<Institution> insts = (List<Institution>) instQueryResponse.get("institutions");
            boolean instExists = false;
            for(Institution inst : insts){
        	if(inst.getName().equals(request.getParameter("institutionName"))){
        	    instExists = true;
        	    break;
        	}
            }
            if(!instExists){
        	HashMap<String, Object> instAddParams = new HashMap<String, Object>();
        	instAddParams.put("objectType", ModelObject.INSTITUTION);
        	instAddParams.put("operation", ModelOperation.ADD);
        	instAddParams.put("institutionName", request.getParameter("institutionName"));
                rmiClient.manageAaaObjects(instAddParams);
            }
        }catch(Exception e){
            e.printStackTrace();
            ServletUtils.handleFailure(out, "Unable to set institution", methodName);
            return null;
        }

        //build user
        User newUser = new User();
        newUser.setLogin(request.getParameter("username"));
        newUser.setPassword(request.getParameter("password"));
        newUser.setCertSubject(request.getParameter("certSubject"));
        newUser.setFirstName(request.getParameter("firstName"));
        newUser.setLastName(request.getParameter("lastName"));
        Institution inst = new Institution();
        inst.setName(request.getParameter("institutionName"));
        newUser.setInstitution(inst);
        newUser.setEmailPrimary(request.getParameter("emailPrimary"));
        newUser.setEmailSecondary(request.getParameter("emailSecondary"));
        newUser.setPhonePrimary(request.getParameter("phonePrimary"));
        newUser.setPhoneSecondary(request.getParameter("phoneSecondary"));
        newUser.setDescription(request.getParameter("description"));

        return newUser;
    }
	 */ //end comment for porting
}
