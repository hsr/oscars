package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.xbill.DNS.LOCRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

//import net.es.oscars.PropHandler;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.sf.json.JSONObject;

public class NodeInfo extends HttpServlet{
    private Logger log = Logger.getLogger(this.getClass());
    private JSONObject jsonObj;
    //private final String DEFAULT_PROPS_FILE = "ion.properties";
    public static final String DEFAULT_ION_TOPOLOGY_CONFIG_FILE = 
		IONUIUtils.getTopologyFile();	//"ion_topology.yaml";
    
    public void loadLatLong(){
        this.jsonObj = new JSONObject();

	IONConfig cfg = new IONConfig(DEFAULT_ION_TOPOLOGY_CONFIG_FILE);
	Map rootCfg = cfg.getGroup("", false);

	this.log.warn("loadLatLong()");

	if (rootCfg != null) {
            Set rootKeys = rootCfg.keySet();
            Iterator keysIter = rootKeys.iterator();
	    this.log.warn("rootCfg != null");

            while(keysIter.hasNext()){
	        this.log.warn("keysIter.hasNext()");
                String key = (String)keysIter.next();
	        this.log.warn("keysIter.hasNext(): "+key);
		if (key.matches("node_.*")) {
			this.log.warn("key.matches: "+key);
			String[] parts = key.split("_", 2);
			String nodeName = parts[1];

			String name = null;
			if (cfg.getString(key+"/name", false) != null) {
				name = cfg.getString(key+"/name", false);
			}

			String latitude = null;
			if (cfg.getString(key+"/latitude", false) != null) {
				latitude = cfg.getString(key+"/latitude", false);
			}

			String longitude = null;
			if (cfg.getString(key+"/longitude", false) != null) {
				longitude = cfg.getString(key+"/longitude", false);
			}

			String city = null;
			if (cfg.getString(key+"/city", false) != null) {
				city = cfg.getString(key+"/city", false);
			}

			this.log.warn("name: "+name);
			this.log.warn("city: "+city);
			this.log.warn("latitude: "+latitude);
			this.log.warn("longitude: "+longitude);

			JSONObject nodeObj = new JSONObject();
			if (name != null) {
				nodeObj.put("name",name);
			}
			if (city != null) {
				nodeObj.put("city",city);
			}
			if (latitude != null) {
				try {
					nodeObj.put("latitude",Double.parseDouble(latitude));
				}catch(Exception e){
					this.log.warn("Latitude "+latitude+"' is not a number.");
				}
			}
			if (longitude != null) {
				try {
					nodeObj.put("longitude",Double.parseDouble(longitude));
				}catch(Exception e){
					this.log.warn("Longitude "+longitude+"' is not a number.");
				}
			}

			if (name != null) {
				this.lookupLatLong(name, nodeObj);
			}

			this.jsonObj.put(nodeName, nodeObj);
		}
            }
        }
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String method = "nodeInfo";
        
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, "Server Error: Unable to load " +
                    "endpoint data. Please contact site administrator", method);
            return;
        }
        
        this.loadLatLong();
        
        //print response
        response.setContentType("application/json");
        out.println("{}&&" + jsonObj);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.doGet(request, response);
    }
    
    private void lookupLatLong(String host, JSONObject nodeObj){
        Record[] records = null;
        try {
            records = new Lookup(host, Type.LOC).run();
        } catch (TextParseException e) {
            this.log.info("Problem getting DNS record for "+host+": " +e.getMessage());
        }
        if(records == null || records.length == 0){
            return;
        }
        LOCRecord locRec = (LOCRecord) records[0];
        if(!nodeObj.containsKey("latitude")){
            nodeObj.put("latitude", locRec.getLatitude());
        }
        if(!nodeObj.containsKey("longitude")){
            nodeObj.put("longitude", locRec.getLongitude());
        }
    }
}
