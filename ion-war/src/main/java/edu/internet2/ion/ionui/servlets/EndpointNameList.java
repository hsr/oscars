package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

//import net.es.oscars.PropHandler;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.es.oscars.wbui.servlets.UserSession;
import net.sf.json.JSONObject;

import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.es.oscars.utils.clients.CoordClient;

import net.es.oscars.wbui.servlets.CheckSessionReply;
import net.es.oscars.logging.ErrSev;


import au.com.bytecode.opencsv.CSVReader;

public class EndpointNameList extends HttpServlet{
    private Logger log = Logger.getLogger(this.getClass());
    private String dataUrl;
    private String localDomain;
    private boolean configError;
    
    //private final String DEFAULT_PROPS_FILE = "ion.properties";
    
    public void init(){
    	Map<String,Object> endPointsData = IONUIUtils.getEndpointURL();
    	this.dataUrl = (String) endPointsData.get("endpointData.url");
         if(this.dataUrl == null){
                 this.log.error("You must set endpointData.url in " +
                         "ion.properties and restart your server");
                 this.configError = true;

        }
        
        this.localDomain =
        	 (String) endPointsData.get("endpointData.localDomain");
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        String methodName = "EndpointNameList";
        JSONObject jsonObj = new JSONObject();
        response.setContentType("application/json");
        
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Server Error: Unable to load " +
                    "endpoint list. Please contact site administrator", methodName);
            return;
        }
       
        //check user session
        /*
        UserSession userSession = new UserSession();
        String userName = userSession.checkSession(out, request, method);
        if (userName == null) {
            return;
        }*///commented for porting
        
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
        //AuthZClient authZClient = core.getAuthZClient();
        
        CheckSessionReply sessionReply = IONUIUtils.getUserSession(request, methodName, out, core);
        String userName = sessionReply.getUserName();
        this.log.debug("userName from sessionReply="+  userName);
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
            return;
        }
        //end new addition
        
        //If there is a config error then return
        if(this.configError){
            this.log.error("You have a configuration error. Check startup" +
                    " log messages for details.");
            ServletUtils.handleFailure(out, "Server Error: The server has " +
                    "a configuration error. Please contact the administrator",
                    methodName);
            return;
        }
        
        CSVReader csvReader = null;
        try {
          URL dataUrl = new URL(this.dataUrl);
          csvReader = new CSVReader(new InputStreamReader(dataUrl.openStream()));
        }catch (MalformedURLException e) {
            this.log.error(e.getMessage());
            this.log.error("Server Error: The server has a configuration" +
                    " problem. Please contact the site administrator");
        } catch (IOException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Server Error: Unable to load " +
                    "endpoint list. Please contact site administrator", 
                    methodName);
            return;
        }
        
        jsonObj.put("identifier", "name");
        ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
        ArrayList<String> unsortedNames = new ArrayList<String>();
        String[] record = null;
        boolean isSource = "1".equals(request.getParameter("isSource"));
        try {
            csvReader.readNext();//remove header
            while((record = csvReader.readNext()) != null){
                //skip endpoints not in the local domain if source
                if(isSource && this.localDomain != null && 
                        (record.length < 2 || record[1] == null || 
                        ((!record[1].startsWith("urn:ogf:network:domain="+this.localDomain)) &&
                        (!record[1].startsWith("urn:ogf:network:"+this.localDomain))))){
                    continue;
                }
                unsortedNames.add(record[0]);
            }
            Collections.sort(unsortedNames);
            for(String name : unsortedNames){
                HashMap<String, String> item = new HashMap<String, String>();
                item.put("name", name);
                items.add(item);
            }
        } catch (IOException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Server Error: Unable to read " +
                    "endpoint list. Please contact site administrator", 
                    methodName);
            return;
        }
        jsonObj.put("items", items);
        out.println("{}&&" + jsonObj);
    }
    
   
    public void doPost(HttpServletRequest request, HttpServletResponse response){
        this.doGet(request, response);
    }
}
