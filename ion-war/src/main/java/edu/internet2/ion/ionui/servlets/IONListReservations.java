package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
//import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
//new imports
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.authN.soap.gen.*;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.common.soap.gen.SubjectAttributes;

import net.es.oscars.wbui.servlets.AuthenticateUser;
import net.es.oscars.wbui.servlets.UserSession;
import net.es.oscars.wbui.servlets.CheckSessionReply;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;

import net.sf.json.JSONObject;

import net.es.oscars.api.soap.gen.v06.VlanTag;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.Layer3Info;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.VlanTag;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import edu.internet2.db.DBUtil;


public class IONListReservations extends HttpServlet {
    private Logger log = Logger.getLogger(IONListReservations.class);
    
    final private int MAX_ROWS = 100;
    final private int MAX_MONTHS = 2;
    final private String DEFAULT_SORT_BY = "startTime desc";

    //porting addition
    //Make global coordinator client object to enable class-wide access
    CoordClient coordClient;
    //end porting addition

    
    public void init(){
	if(!DBUtil.loadJDBCDriver()){
            this.log.error("Could not load local JDBC Driver");
            return;
        }
        log.debug("JDBC driver loaded");
    }
    
    /**
     * Handles servlet request (both get and post) from list reservations form.
     *
     * @param request servlet request
     * @param response servlet response
     */
    public void
        doGet(HttpServletRequest servletRequest, HttpServletResponse response)
            throws IOException, ServletException {
        
        //initialize
        String methodName = "ListReservations";
/*        UserSession userSession = new UserSession(); */
        PrintWriter out = response.getWriter();
        HashMap<String,Object> outputMap = new HashMap<String,Object>();
        ArrayList<HashMap<String, Object>> favList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Boolean> favMap = new HashMap<String, Boolean>();
        
        //authenticate user
        response.setContentType("application/json");
/*        String userName = userSession.checkSession(out, servletRequest, methodName);
        if (userName == null) {
            this.log.warn("No user session: cookies invalid");
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
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZClient authZClient = core.getAuthZClient();
        UserSession userSession = new UserSession(core);
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, servletRequest,
                    methodName);

	//Debug
	this.log.debug("sessionReply="+  sessionReply);
        if (sessionReply == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid"));
            return;
        }
        String userName = sessionReply.getUserName();

	//Debug
	this.log.debug("userName from sessionReply="+  userName); 
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
            return;
        }


        //Get page
        int page = 0;
        try{
            if(servletRequest.getParameter("page") != null){
                page = Integer.parseInt(
                    servletRequest.getParameter("page"));
            }
        }catch(Exception e){}
        outputMap.put("page", page);
 
        //Get results per page
        int resultsPerPage = 10;
        try{
            if(servletRequest.getParameter("resultsPerPage") != null){
                resultsPerPage = Integer.parseInt(
                    servletRequest.getParameter("resultsPerPage"));
            }
        }catch(Exception e){}
        if(resultsPerPage > MAX_ROWS){
            resultsPerPage = MAX_ROWS;
        }
        outputMap.put("resultsPerPage", resultsPerPage);
        
        Connection conn = DBUtil.getDBConnection(); 
        
        //Get Parameters
/*        RmiListResRequest rmiRequest = 
            this.getParameters(servletRequest, outputMap,
                    favList, favMap, conn, userName);
        RmiListResReply rmiReply = new RmiListResReply();
 */
       
        /* RMI request will be null if sorting by favorites and favorites
         take up whole page */
/*
        if(rmiRequest != null){
            try {
                BssRmiInterface bssRmiClient =
                    RmiUtils.getBssRmiClient(methodName, log);
                rmiReply = bssRmiClient.listReservations(rmiRequest, userName);
            } catch (Exception e) {
                DerbyUtil.closeConnection(conn);
                ServletUtils.handleFailure(out, log, e, methodName);
                return;
            }
        }
        
        List<Reservation> reservations = rmiReply.getReservations();
*/

	List<AttributeType> userAttributes = sessionReply.getAttributes();
	//debug
	ListIterator iter = userAttributes.listIterator();
	while (iter.hasNext()) {
		this.log.debug("## elem:"+ iter.next());
	}
	//end debug
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
        CheckAccessReply checkAccessReply = null;
        try {
	    //this.getParameters(servletRequest, outputMap,
            //favList, favMap, conn, userName);
            listReq = this.getParameters(servletRequest, 
            		outputMap, favList, favMap, conn, userName,
			msgProps); //porting addition msgProps
            listReq.setMessageProperties(msgProps);
            // Send a listReservation request
            Object[] req = new Object[]{subjectAttrs,listReq};
            Object[] res = coordClient.invoke("listReservations",req);
            listResponse = (ListReply) res[0];
	    List<ResDetails> reservations = listResponse.getResDetails();
		    
	    this.outputReservations(outputMap, reservations, userName, 
	                resultsPerPage, page, favList, favMap, conn);
	    DBUtil.closeConnection(conn);
	
	    outputMap.put("status", "Reservations list");
	    outputMap.put("method", methodName);
	    outputMap.put("success", Boolean.TRUE);
	    JSONObject jsonObject = JSONObject.fromObject(outputMap);
	    out.println("{}&&" + jsonObject);

	} catch (Exception ex) {
		log.debug (netLogger.error(methodName,ErrSev.MAJOR, "caught " + ex.toString()));
		ex.printStackTrace();
            	ServletUtils.handleFailure(out, log, ex, methodName);
            	return;
        }

	
    }

    public void doPost(HttpServletRequest servletRequest,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(servletRequest, response);
    }

    public ListRequest getParameters(
                HttpServletRequest servletRequest, 
                Map<String, Object> outputMap, 
                ArrayList<HashMap<String, Object>> favList,
                HashMap<String, Boolean> favMap,
                Connection conn,
                String userName,
		//porting addition. 
		MessagePropertiesType msgProps
		//end addition
            ) {

    	ListRequest rmiRequest = null;
        
        //Get statuses 
        List<String> statuses = this.getStatuses(servletRequest);
        
        //Get sortBy
        String sortBy = servletRequest.getParameter("sortBy");
        if(sortBy == null){
            sortBy = DEFAULT_SORT_BY;
        }
        outputMap.put("sortBy", sortBy);
        
        //if sortBy favorites then have to so some extra work
        int favOffset = -1;
        int favCtr = 0;
       
        if(sortBy.startsWith("favorite") && conn != null){
            try{
                //If sorting by favorite then get the full list
            	/* commenting for porting changes
		BssRmiInterface bssRmiClient =
			RmiUtils.getBssRmiClient("QueryReservation", log);
                */
		//replace with below
		//coordClient now class member
		//end new porting addition
 
                PreparedStatement favListStmt = conn.prepareStatement(
                        "SELECT gri FROM favorites WHERE login=? ORDER BY gri " + 
                        (sortBy.toLowerCase().endsWith("desc")?"DESC":"ASC"));
                favListStmt.setString(1, userName);
                ResultSet rs = favListStmt.executeQuery();
		//porting addition 
		//prepare request object for GRI based request
		QueryResContent queryReq = new QueryResContent();
        	//MessagePropertiesType msgProps = new MessagePropertiesType();
		MessagePropertiesType localMsgProps = new MessagePropertiesType();
		SubjectAttributes subjectAttrs = msgProps.getOriginator();
        	localMsgProps.setOriginator(subjectAttrs);
        	localMsgProps.setGlobalTransactionId(msgProps.getGlobalTransactionId());
        	queryReq.setMessageProperties(localMsgProps);
        	ResDetails resDetails = null;
		this.log.debug("--Obtained RS"+ rs);
		//end porting additions
		
                while(rs.next()){
                    String favGri = rs.getString(1);
                    favCtr++;
                    favMap.put(favGri, new Boolean(true));
		    /* commenting for porting changes
                    RmiQueryResReply rmiReply = 
                        bssRmiClient.queryReservation(favGri, userName);
                    Reservation resv = rmiReply.getReservation();
		    */
		    //new for porting
		    //set GRI
		    queryReq.setGlobalReservationId(favGri);
		    resDetails=null;
		    Object[] req = new Object[]{subjectAttrs,queryReq};
            	    Object[] res = coordClient.invoke("queryReservation",req); 
	   	    resDetails = ((QueryResReply) res[0]).getReservationDetails(); 
		    //end porting addition
			
		    /* commenting below section to replace for porting
                    if(resv == null || (!statuses.contains(resv.getStatus()))){ 
                        continue; 
                    }
                    HashMap<String,Object> resvMap = new HashMap<String,Object>();
                    resvMap.put("gri", resv.getGlobalReservationId());
                    resvMap.put("description", resv.getDescription());
                    resvMap.put("user", resv.getLogin());
                    resvMap.put("startTime", resv.getStartTime());
                    resvMap.put("endTime", resv.getEndTime());
                    resvMap.put("status", resv.getStatus());
                    resvMap.put("localStatus", resv.getLocalStatus());
                    resvMap.put("favorite", new Boolean(true));
                    favList.add(resvMap);
                    favMap.put(favGri, new Boolean(true));
		    */

		    //new for porting
		    if(resDetails == null || (!statuses.contains(resDetails.getStatus()))){
                        continue;
                    }
                    UserRequestConstraintType uConstraint = resDetails.getUserRequestConstraint();

                    HashMap<String,Object> resvMap = new HashMap<String,Object>();
                    resvMap.put("gri", resDetails.getGlobalReservationId());
                    resvMap.put("description", resDetails.getDescription());
                    resvMap.put("user", resDetails.getLogin());
                    resvMap.put("startTime", uConstraint.getStartTime());
                    resvMap.put("endTime", uConstraint.getEndTime());
                    resvMap.put("status", resDetails.getStatus());
                    //TBD get localstatus
                    //resvMap.put("localStatus", resDetails.getLocalStatus());
		    favList.add(resvMap);
                    favMap.put(favGri, new Boolean(true));
                }
            } catch(SQLException e){
                //don't let favorites crash the system
		log.error("SQLException getting/searching by Favorites: " + e.getMessage());
	    } catch (OSCARSServiceException oscExcep) {
		//probably from coordclient.invoke failure
		this.log.error("Co-ordinator client probably cannot invoke query succesfully:" +
			oscExcep.getMessage() );
	    } catch  (Exception e) { //(RemoteException e) { //porting changed RemoteException to e
                //don't let favorites crash the system
                this.log.error("Exception while getting/listing ckts" + e.getMessage());
            }
        }

        //set to plus one so we can determine if there is another page
        //TODO: Make listreservations return total number of results
        long startTime = System.currentTimeMillis()/1000;
        // XXX: only grab circuits from the last year
        //startTime -= 365*24*60*60;
        // XXX: only grab circuits from the last three months
        startTime -= MAX_MONTHS*30*24*60*60;

        //rmiRequest = new RmiListResRequest();
        //ION replace with below
        ListRequest listReq = new ListRequest();
        //add favorites so make sure get enough rows to complete page
//        rmiRequest.setNumRequested(resultsPerPage+favCtr+1);
//        rmiRequest.setResOffset(offset);
        /*
        rmiRequest.setSortBy(sortBy.replace("favorite", "startTime"));
        rmiRequest.setStatuses(statuses);
        rmiRequest.setStartTime(startTime);
        */
        //ION TBD. SortBy??? NO SortBy in listRequest xml item?
        //listReq.setSortBy(sortBy.replace("favorite", "startTime"));
       
        //listReq.setStatuses(statuses);
        //ION replaced by below
        List<String> reqStatuses = listReq.getResStatus();
        for (String status : this.getStatuses(servletRequest)) {
        	reqStatuses.add(status);
        }
        listReq.setStartTime(startTime);

        //return rmiRequest;
        //ION replaced with below
        return listReq;
    }

    /**
     * Gets list of statuses selected from menu.
     *
     * @param servletRequest servlet request
     * @return list of statuses to send to BSS
     */
    public List<String> getStatuses(HttpServletRequest servletRequest) {

        List<String> statuses = new ArrayList<String>();
        String paramStatuses[] = servletRequest.getParameterValues("statuses");
        if (paramStatuses != null) {
            for (int i=0 ; i < paramStatuses.length; i++) {
                statuses.add(paramStatuses[i]);
            }
        }
        return statuses;
    }

    /**
     * Gets list of parameter values from a space separated paramater string
     *
     * @param servletRequest servlet request
     * @param paramName string with parameter name
     * @return list of parameters to send to BSS
     */
    public List<String> getList(HttpServletRequest servletRequest,
                                String paramName) {

        List<String> paramList = new ArrayList<String>();
        String param = servletRequest.getParameter(paramName);
        if ((param != null) && !param.trim().equals("")) {
            String[] paramTags = param.trim().split(" ");
            for (int i=0; i < paramTags.length; i++) {
                paramList.add(paramTags[i]);
            }
        }
        return paramList;
    }

    /**
     * Formats reservation data sent back by list request from the reservation
     * manager into grid format that Dojo understands.
     *
     * @param outputMap map containing grid data
     * @param reservations list of reservations satisfying search criteria
     * @param conn 
     * @param favMap2 
     */
    public void outputReservations(Map<String, Object> outputMap,
                                   List<ResDetails> reservations, 
                                   String userName,
                                   int resultsPerPage,
                                   int page,
                                   ArrayList<HashMap<String,Object>> favList, 
                                   HashMap<String, Boolean> favMap, 
                                   Connection conn) {
        ArrayList<HashMap<String,Object>> resvList =
            new ArrayList<HashMap<String,Object>>();
        int printCount = 0;
        int skipCount = 0;
        int adjResultsPerPage = resultsPerPage;
        int start_offset = page*resultsPerPage;

        boolean is_final = false;

        //if sort by favorites then favorites should be first
        for(HashMap<String,Object> resvMap: favList){
            if (skipCount < start_offset) {
                skipCount++;
                continue;
            }

            resvList.add(resvMap);
            printCount++;
            if (printCount == resultsPerPage) {
                break;
            }
        }
        
        //List the reservations
        if(reservations != null && printCount < resultsPerPage){
            for (int i = 0; i < reservations.size() && printCount < resultsPerPage; i++) {
                if(favMap.containsKey(reservations.get(i).getGlobalReservationId())){
                    continue;
                }

                if (skipCount < start_offset) {
                    skipCount++;
                    continue;
                }

                printCount++;

                // XXX: hack, we need a better way to do this.
                if (i + 1 == reservations.size()) {
                    is_final = true;
                }
		
		ResDetails currentResv = reservations.get(i);
		
		//new line
		UserRequestConstraintType uConstraint = currentResv.getUserRequestConstraint();

                HashMap<String,Object> resvMap = new HashMap<String,Object>();
                resvMap.put("gri", currentResv.getGlobalReservationId());
                resvMap.put("description", currentResv.getDescription());
                resvMap.put("user", currentResv.getLogin());
                resvMap.put("startTime", uConstraint.getStartTime());
                resvMap.put("endTime", uConstraint.getEndTime());
                resvMap.put("status", currentResv.getStatus());
                //TBD get localstatus
                //resvMap.put("localStatus", currentResv.getLocalStatus());
                if(conn != null){
                    try{
		       PreparedStatement favStmt = conn.prepareStatement(
                                "SELECT id FROM favorites WHERE login=? AND gri=?"
                                );
                       favStmt.setString(1, userName);
                       favStmt.setString(2, 
                               reservations.get(i).getGlobalReservationId());
                       ResultSet rs = favStmt.executeQuery();
                       resvMap.put("favorite", new Boolean(rs.next()));
                       resvList.add(resvMap);
                    }catch(SQLException e){
                        //don't let favorites break list
                        this.log.error("Exception listing favorites:"+
				e.getMessage());
                    }
                }
            }
	    //kk
            if (reservations.size() == 0)
                is_final=true;
            //end kk
        }
        //kk
        else if (reservations == null) {
           is_final = true;
        } //end kk

        //NOTE: This value is not accurate when sorting by favorites
        outputMap.put("totalRowsReplace", "Total rows: " + printCount);
        outputMap.put("hasNextPage", new Boolean(!is_final));
        outputMap.put("resvData", resvList);
    }
}
