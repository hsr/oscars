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
import java.util.TreeMap;

import java.util.Map;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import net.es.oscars.PropHandler;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

/*
//Service names for Enpoint URL
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
*/

public class EndpointBrowser extends HttpServlet{
    private Logger log = Logger.getLogger(this.getClass());
    private String dataUrl;
    private String localDomain;
    private boolean configError;
    
    //private final String DEFAULT_PROPS_FILE = "ion.properties";
    private final int DEFAULT_RESULTS_PER_PAGE = 25;
    private final int NAME_INDEX = 0;
    
    public void init(){
        //NOTE: localDomain may be null so need to do null checks
    	//null checks not necessary - data expected to be present
    	Map<String,Object> endPointsData = IONUIUtils.getEndpointURL();
    	/*
	ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_IONUI);
        cc.setServiceName(ServiceNames.SVC_IONUI);
 
	        String configFilename = null;
	        try {
		    cc.setContext(System.getProperty("context"));
		    cc.loadManifest(ServiceNames.SVC_IONUI,  ConfigDefaults.MANIFEST);
	            configFilename = cc.getFilePath(
	                                            ConfigDefaults.CONFIG);
	        } catch (ConfigException e) {
	        	log.error("ConfigException when gettting ENDPOINTS data" + e);
	            //return null;
	        }
	        log.debug(" Endpoint to be read from config file:" + configFilename);
	        HashMap<String,Object> utilConfig = 
	        	(HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
		log.debug("Contexcxt:"+ cc.getContext() + "," + utilConfig + "," + configFilename);
	        HashMap<String,Object> endPointsData = null;
	        if(utilConfig.containsKey("endpoint")){
	        	endPointsData = (HashMap<String,Object>) utilConfig.get("endpoint");
	        }
	        else {
	        	log.error(" No endpoints found, is utilConfig null?" + utilConfig);
	        }
	        //to remove until here
	        
    	Iterator iterator = endPointsData.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            System.out.print( "(" + entry.getKey() + ": " + 
                              entry.getValue() + "), " );
        }
       */ 
	this.dataUrl = (String) endPointsData.get("endpointData.url");
        if(this.dataUrl == null){
		this.log.error("You must set endpointData.url in " +
			"ion.properties and restart your server");
     		this.configError = true;
	} 
        this.localDomain = (String) endPointsData.get("endpointData.localDomain"); // props.getProperty("localDomain");
    }
    
    /**
     * Processes requests that allows a user to browse endpoints.It accepts
     * the following parameters:
     *<ul>
     *   <li>cat - required. the category to browse or search. All means search/browse 
     *   all fields but print the name of matches</li>
     *   <li>catVal - select a category and prints the names of endpoints in 
     *   that category.</li>
     *   <li>query - a search string to match against the field specified by 'cat'. 
     *       If cat is set to 'all then all categories are examined</li>
     *   <li>page - a number starting at 0 indicating the page to print</li>
     *   <li>pageResults - a number indicating the number of results to print per page</li>
     *   <li>reverse - a value of 1 indicates results should be sorted in reverse alphabetical order</li>
     *</ul>
     *
     *The method returns a JSON structure with the following elements:
     * <ul>
     *  <li>results - a HasMap listing the categories or endpoints matched. 
     *  If categories then the key is the endpoint name and the value is the 
     *  number of endpoints in that category. If it is and endpoint the key is
     *   the name and the value is 1.</li>
     *   <li>totalResults - the number of categories matched</li>
     *   <li>totalPages - the total number of pages containing results</li>
     * </ul>
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        String method = "EndpointBrowser";
        JSONObject jsonObj = new JSONObject();
        //response.setContentType("application/json");
        
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Server Error: Unable to load " +
                    "endpoint data. Please contact site administrator", method);
            return;
        }
       
        //check user session
       /* UserSession userSession = new UserSession();
        String userName = userSession.checkSession(out, request, method);
        if (userName == null) {
            return;
        }*/
        
        //If there is a config error then return
        if(this.configError){
            this.log.error("You have a configuration error. Check startup" +
                    " log messages for details.");
            ServletUtils.handleFailure(out, "Server Error: The server has " +
                    "a configuration error. Please contact the administrator",
                    method);
            return;
        }
        
        //Open CSV file
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
                    "endpoint data. Please contact site administrator", 
                    method);
            return;
        }
        
        //Initialization
        boolean isSource = "1".equals(request.getParameter("isSource"));
        HashMap<String, Integer> fieldIndex = new HashMap<String, Integer>();
        TreeMap<String,Integer> resultMap = null;
        ArrayList<HashMap<String,String>> results = new ArrayList<HashMap<String,String>>();
        int reverse = 0;
        if("1".equals(request.getParameter("reverse"))){
            resultMap = new TreeMap<String,Integer>(Collections.reverseOrder());
            reverse = 1;
        }else{
            resultMap = new TreeMap<String,Integer>();
        }
        
        String[] record = null;
        try {
            //get fields 
            record = csvReader.readNext();
            String lastHeader = "";
            fieldIndex.put("all", -1); //special field
            for(int i = 0; i < record.length; i++){
                fieldIndex.put(record[i], i);
                lastHeader = record[i];
            }
            
            //check that a category has been specified
            String category = request.getParameter("cat");
            if(category == null || (!fieldIndex.containsKey(category))){
                ServletUtils.handleFailure(out, "Invalid category specified '" +
                        category + "'", method);
                return;
            }
            
            //find matching results
            String query = request.getParameter("query");
            String catVal = request.getParameter("catVal");
            while((record = csvReader.readNext()) != null){
                //skip endpoints not in the local domain if source
                if(isSource && this.localDomain != null && 
                        (record.length < 2 || record[1] == null || 
                        ((!record[1].startsWith("urn:ogf:network:domain="+this.localDomain)) &&
                        (!record[1].startsWith("urn:ogf:network:"+this.localDomain))))){
                    continue;
                }
                int minField = "all".equals(category) ? 0 : fieldIndex.get(category);
                int maxField = (lastHeader.equals(category) ||"all".equals(category))
                    ? record.length : fieldIndex.get(category)+1;
                for(int i = minField; i < maxField; i++){
                    if("".equals(record[i])){
                        continue;
                    }else if(query != null && !(record[i].toLowerCase().contains(query.toLowerCase()))){
                        //skip if doesn't match search
                        continue;
                    }else if(catVal != null && !(record[i].toLowerCase().equals(catVal.toLowerCase()))){
                        //skip if not correct category
                        continue;
                    }
                    
                    //add name otherwise add category
                    if(catVal != null || query != null){
                        resultMap.put(record[NAME_INDEX], 1);
                    }else{
                        int currCatCount = resultMap.containsKey(record[i]) ?
                            resultMap.get(record[i]) : 0;
                        resultMap.put(record[i], ++currCatCount);
                    }
                }
            }
            
            //paginate results and return
            ArrayList<String> catKeys = new ArrayList<String>(resultMap.keySet());
            int numResults = DEFAULT_RESULTS_PER_PAGE;
            if(request.getParameter("pageResults") != null){
                try{
                    numResults = Integer.parseInt(request.getParameter("pageResults"));
                    numResults = numResults <= 0 ? DEFAULT_RESULTS_PER_PAGE : numResults;
                }catch(Exception e){}//keep default
            }
            
            int page = 0;
            int pageStart = 0;
            if(request.getParameter("page") != null){
                try{
                    page = Integer.parseInt(request.getParameter("page"));
                    page = (page < 0 ? 0 : page);
                    pageStart= page * numResults;
                }catch(Exception e){}//keep default
            }
            for(int i = pageStart; i < (pageStart+numResults) && i < catKeys.size(); i++){
                HashMap<String,String> result = new HashMap<String,String>();
                result.put("value", catKeys.get(i));
                result.put("subCount", resultMap.get(catKeys.get(i))+"");
                results.add(result);
            }
            jsonObj.put("results", results);
            jsonObj.put("totalResults", resultMap.size());
            jsonObj.put("totalPages", Math.ceil((resultMap.size()+0.0)/numResults));
            jsonObj.put("cat", category);
            jsonObj.put("catVal", catVal);
            jsonObj.put("page", page);
            jsonObj.put("pageResults", numResults);
            jsonObj.put("reverse", reverse);
            jsonObj.put("query", query);
            jsonObj.put("success", Boolean.TRUE);
        } catch (IOException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Server Error: Unable to read " +
                    "endpoint data. Please contact site administrator", 
                    method);
            return;
        }
        
        out.println("{}&&" + jsonObj);
    }
    
   
    public void doPost(HttpServletRequest request, HttpServletResponse response){
        this.doGet(request, response);
    }
}
