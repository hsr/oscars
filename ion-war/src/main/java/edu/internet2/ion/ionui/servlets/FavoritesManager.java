package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
//import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.es.oscars.logging.ErrSev;

import org.apache.log4j.Logger;

//import net.es.oscars.ConfigFinder;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.es.oscars.wbui.servlets.UserSession;
import net.sf.json.JSONObject;
import edu.internet2.db.DBUtil;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;


public class FavoritesManager extends HttpServlet{
    //private Logger log = Logger.getLogger(IONCreateReservation.class);
    private Logger log = Logger.getLogger(FavoritesManager.class);

/*   commented for ION addition 
    public void init(){
        ConfigFinder configFinder = new ConfigFinder();
        String derbyHome = "";
        try {
            derbyHome = configFinder.find(ConfigFinder.PROPERTIES_DIR, "derby");
        } catch (RemoteException e) {
            this.log.error(e.getMessage());
            return;
        }
        DerbyUtil.loadJDBCDriver(derbyHome);
    }
*/

    public void init(){
	if(!DBUtil.loadJDBCDriver()){
            this.log.error("Could not load local JDBC Driver");
            return;
        }
        log.debug("JDBC driver loaded");
    } 
 
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String methodName= "FavoritesManager";
        PrintWriter out = response.getWriter();
	
	/*comment for porting         
        //authenticate the reservation
        UserSession userSession = new UserSession();
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
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
	String userName = IONUIUtils.getUserNameFromSession(request, methodName, out, core);
        this.log.debug("userName from sessionReply="+  userName);
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
            return;
        }	

        response.setContentType("application/json");
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        
        //Get gri
        String gri = request.getParameter("gri");
        if(gri == null){
            ServletUtils.handleFailure(out, "GRI required to save/delete favorite", methodName);
            return;
        }
        
        Connection conn = null;
        boolean success = false;
        try{
            //conn = DriverManager.getConnection("jdbc:derby:ion"); //ION porting comments
	    conn = DBUtil.getDBConnection();
	    //debug 
	    this.log.debug("--DB connection obtained in FavoritesManager :"+ conn);
            PreparedStatement favStmt = null;
            if("true".equals(request.getParameter("favorite"))){
                favStmt = conn.prepareStatement("INSERT INTO favorites VALUES(DEFAULT, ?, ?)");
                this.log.info("adding favorite");
            }else{
                favStmt = conn.prepareStatement("DELETE FROM favorites WHERE login=? AND gri=?");
                this.log.info("deleting favorite");
            }
            favStmt.setString(1, userName);
            favStmt.setString(2, gri);
            favStmt.execute();
            success = true;
        }catch(SQLException e){
            this.log.error(e.getMessage());
        }finally{
            DBUtil.closeConnection(conn);
        }
        
        outputMap.put("success", new Boolean(success));
        if(!success){
            outputMap.put("status", "Unable to save favorite.");
        }
        
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        return;
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.doGet(request, response);
    }
}
