package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
*/
import net.sf.json.JSONObject;

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

import org.apache.log4j.Logger;


public class IONUserDelete extends HttpServlet{
    private Logger log = Logger.getLogger(IONUserDelete.class);
    
    public void init(){
	if(!DBUtil.loadJDBCDriver()){
       		this.log.error("Could not load local JDBC Driver");
        	return;
	}
	log.debug("JDBC driver loaded");
    }//end init
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String methodName= "IONUserDelete";
        PrintWriter out = response.getWriter();
        
        //authenticate
	/*
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
		organization = IONUIUtils.getUsersOrg(userAttributes);
                log.debug ("obtained user's org: "+ organization);

	} catch (Exception adminExcep) { 
		this.log.error("Exception when trying to find if admin:" +
			adminExcep.getMessage());
		ServletUtils.handleFailure(out, log, adminExcep, methodName);
                return;
	}

        //Get user query
        String username = request.getParameter("user");
        if(username == null){
                ServletUtils.handleFailure(out, "No user specified", methodName);
                return;
        }

        log.debug("Trying to delete user "+ username);

	/*
        //get to-be-deleted-user's institution
	try {
		Object[] soapReq = new Object[]{username};
                Object[] resp = authNPolicyClient.invoke("queryUser", soapReq);
                QueryUserReply reply = (QueryUserReply) resp[0];
                List<AttributeType> userAttrLocal =
                    reply.getUserAttributes().getSubjectAttributes();
                organization = IONUIUtils.getUsersOrg(userAttrLocal);
                log.debug ("obtained user's org: "+ organization);

	} catch (Exception e) {
		this.log.error("Exception when trying to find organization" +
			e.getMessage());
		ServletUtils.handleFailure(out, log, e, methodName);
		return;
	}
	*/

	/*
        //Get user query
        String username = request.getParameter("user");
        if(username == null){
            ServletUtils.handleFailure(out, "No user specified", methodName);
            return;
        }
       	*/
 
	Connection conn = null;
        try {
            //Connection conn = DriverManager.getConnection("jdbc:derby:ion");
	    conn = DBUtil.getDBConnection();
            PreparedStatement userStmt = conn.prepareStatement("SELECT username " +
                "FROM adminOrganizationUsers WHERE organization=? AND username=?");
            userStmt.setString(1, organization);
            userStmt.setString(2, username);
            ResultSet users = userStmt.executeQuery();
            if(!users.next()){
                ServletUtils.handleFailure(out, "You do not have permission to delete user", methodName);
                return;
            }
            conn.close();
        } catch (SQLException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Unable to load users", methodName);
            return;
        }
        
        //query rmi for users
	/*
        List<User> rmiUserList = null;
        try{
            HashMap<String, Object> userListRequest = new HashMap<String, Object>();
            userListRequest.put("objectType", ModelObject.USER);
            userListRequest.put("operation", ModelOperation.DELETE);
            userListRequest.put("username", username);
            HashMap<String, Object> userListRmiResp = rmiClient.manageAaaObjects(userListRequest);
            rmiUserList = (List<User>) userListRmiResp.get("users");
        }catch(Exception e){
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
	*/
	//commented above to replace with below
	try {
		Object[] soapReq = new Object[]{username};
		Object[] resp = authNPolicyClient.invoke("removeUser", soapReq);
	} catch (Exception oscarsExcep) {
		this.log.error(oscarsExcep.getMessage());
		ServletUtils.handleFailure(out, log, oscarsExcep, methodName);
            	return;
	}
		        
        //Delete user from local db
        try {
            //Connection conn = DriverManager.getConnection("jdbc:derby:ion");
	    conn = DBUtil.getDBConnection();
            PreparedStatement userStmt = conn.prepareStatement("DELETE FROM " +
                "adminOrganizationUsers WHERE organization=? AND username=?");
            userStmt.setString(1, organization);
            userStmt.setString(2, username);
            userStmt.execute();
            conn.close();
        } catch (SQLException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Unable to load users", methodName);
            return;
        }
        
	log.debug("Deleted user" + username + " from local db");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", new Boolean(true));
        jsonObject.put("username", username);

        out.println("{}&&" + jsonObject);
	this.log.info(netLogger.end(methodName));
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.doGet(request, response);
    }
}
