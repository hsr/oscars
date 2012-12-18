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

import org.apache.log4j.Logger;

import edu.internet2.db.DBUtil;

/*//comment for porting
import net.es.oscars.ConfigFinder;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.User;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;
import net.es.oscars.servlets.ServletUtils;
import net.es.oscars.servlets.UserSession;
*/

import net.sf.json.JSONObject;

//new imports
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
import net.es.oscars.logging.OSCARSNetLogger;
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
import net.es.oscars.authN.soap.gen.policy.ListUsersParams;
import net.es.oscars.authN.soap.gen.policy.ListUsersReply;

import org.apache.log4j.Logger;


public class IONUserList extends HttpServlet{
    private Logger log = Logger.getLogger(IONUserList.class);
    
    public void init(){
	if(!DBUtil.loadJDBCDriver()){
            this.log.error("Could not load local JDBC Driver");
            return;
        }
        log.debug("JDBC driver loaded");
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String methodName= "IONUserList";
        PrintWriter out = response.getWriter();
        
        //authenticate the reservation
	/* //porting comment
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
        String userName = sessionReply.getUserName();
        this.log.debug("userName from sessionReply="+  userName);
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
            return;
        }
        //end new addition

        response.setContentType("application/json");
        
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
	//comment above for porting. Replaced by below block
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
	    //get user institution from SessionReply object itself?
            organization = IONUIUtils.getUsersOrg(userAttributes); 
            /*Object[] soapReq = new Object[]{userName};
            Object[] resp = authNPolicyClient.invoke("listInsts", soapReq);
            ListInstsReply reply = (ListInstsReply) resp[0];
            List<String> institutions = reply.getName();
            log.error ("obtained institutions list:"+ institutions);
	    */
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }



        //query derby for allowed users
        HashMap<String, Boolean> userMap = new HashMap<String, Boolean>();
	Connection conn = null;
        try {
            //Connection conn = DriverManager.getConnection("jdbc:derby:ion");
	    conn = DBUtil.getDBConnection();
            PreparedStatement userStmt = conn.prepareStatement("SELECT username " +
                "FROM adminOrganizationUsers WHERE organization=?");

            this.log.debug("SELECT username FROM adminOrganizationUsers WHERE organization="+organization);

            userStmt.setString(1, organization);
            ResultSet users = userStmt.executeQuery();
            while(users.next()){
                userMap.put(users.getString(1), true);
                this.log.debug("Got user: "+users.getString(1));
            }
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
        
        //query rmi for users
	//commenting below block to replace for porting
	/*
        List<User> rmiUserList = null;
        try{
            HashMap<String, Object> userListRequest = new HashMap<String, Object>();
            userListRequest.put("objectType", ModelObject.USER);
            userListRequest.put("operation", ModelOperation.LIST);
            userListRequest.put("listType", "plain");
            HashMap<String, Object> userListRmiResp = rmiClient.manageAaaObjects(userListRequest);
            rmiUserList = (List<User>) userListRmiResp.get("users");
        }catch(Exception e){
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Unable to load users", methodName);
            return;
        }
	*/
	List <UserDetails> allUsers = null;
	try {
	     ListUsersParams req = new ListUsersParams();
	     //TBD do we need any atributes?
             //    req.setAttribute(attributeName);
             Object[] soapReq = new Object[]{req};
             Object[] resp = authNPolicyClient.invoke("listUsers", soapReq);
             ListUsersReply reply= (ListUsersReply) resp[0];
             allUsers = reply.getUserDetails();
	} catch (OSCARSServiceException osEx) {
	     log.error("Exception while getting all users: " + osEx.toString());
     	     osEx.printStackTrace();
    	     ServletUtils.handleFailure(out, log, osEx, methodName);
	     return;
	}


        // add all the users in our institution to the allowed user list
	/*
	for(User user : rmiUserList){
             if (user.getInstitution().getName().equals(organization)) {
                 userMap.put(user.getLogin(), true);
             }
        }
	*/ //comment above to replace with below for porting
	for(UserDetails user : allUsers){
             if (user.getInstitution().equals(organization)) {
                 userMap.put(user.getLogin(), true);
		 //TBD change to debug
		 log.debug ("-Adding user " + user.getLogin() + " to the list");
             }
        }


        //combine user lists
        Map<String, Object> outputMap = new HashMap<String, Object>();
        ArrayList<HashMap<String,String>> userList =
            new ArrayList<HashMap<String,String>>();
        String sortField = "login";
        boolean sortAsc = true;
        if(request.getParameter("sortBy") != null){
            String[] sortParts = request.getParameter("sortBy").split(" ");
            if(sortParts.length >= 1){
                sortField = sortParts[0];
            }
            if(sortParts.length == 2){
                sortAsc = "asc".equals(sortParts[1]);
            }
        }
	
	//commented 2 lines below for porting
	/*
        User[] sortedUsers = new User[rmiUserList.size()];
        rmiUserList.toArray(sortedUsers);
	*/
        UserDetails [] sortedUsers = new UserDetails[allUsers.size()];
        allUsers.toArray(sortedUsers);

        int page = 0;
        try{
            page = Integer.parseInt(request.getParameter("page"));
        }catch(Exception e){}
        int resultsPerPage = 10;
        try{
            resultsPerPage = Integer.parseInt(request.getParameter("resultsPerPage"));
        }catch(Exception e){}

        int start = page * resultsPerPage;
        int num_seen = 0;
        boolean hasNextPage = false;

	//commented below line for porting and replaced 
        //Arrays.sort(sortedUsers, new UserComparator<User>(sortField, sortAsc));
        Arrays.sort(sortedUsers, new UserComparator<UserDetails>(sortField, sortAsc));
        for (int i = 0; i < sortedUsers.length; i++) {
            //this.log.debug("Got user from RMI "+sortedUsers[i].getLogin());
            this.log.debug("Got user from SQL-DB "+sortedUsers[i].getLogin());
            if(!userMap.containsKey(sortedUsers[i].getLogin())){
                this.log.debug("User "+sortedUsers[i].getLogin()+" not found");
                continue;
            }

            num_seen++;
            if (num_seen < start) {
                continue;
            }

            if (num_seen > start + resultsPerPage) {
                hasNextPage = true;
                break;
            }

            HashMap<String,String> userDataMap = new HashMap<String,String>();
            userDataMap.put("login", sortedUsers[i].getLogin());
            userDataMap.put("lastName", sortedUsers[i].getLastName());
            userDataMap.put("firstName", sortedUsers[i].getFirstName());
            //userDataMap.put("organization", sortedUsers[i].getInstitution().getName());
	    //commented above to replace with below for porting
            userDataMap.put("organization", sortedUsers[i].getInstitution());
            userDataMap.put("phone", sortedUsers[i].getPhonePrimary());
            userDataMap.put("email", sortedUsers[i].getEmailPrimary());
            userList.add(userDataMap);
        }
        outputMap.put("page", new Integer(page));
        outputMap.put("hasNextPage", new Boolean(hasNextPage));
        outputMap.put("userData", userList);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);

        out.println("{}&&" + jsonObject);
	this.log.debug(methodName + ".finish");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.doGet(request, response);
    }
}
