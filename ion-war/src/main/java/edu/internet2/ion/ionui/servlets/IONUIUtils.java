package edu.internet2.ion.ionui.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.Collections;
import java.util.Map;
import java.util.List;

import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.AuthNPolicyClient;

import net.es.oscars.wbui.servlets.AuthenticateUser;
import net.es.oscars.wbui.servlets.UserSession;
import net.es.oscars.wbui.servlets.CheckSessionReply;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;

//Service names for Endpoint URL
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.ErrSev;

import org.apache.log4j.Logger;

import org.ogf.schema.network.topology.ctrlplane.*;
import net.es.oscars.coord.common.URNParser;
import net.es.oscars.coord.common.URNParserResult;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
// for LS.kk
import net.es.oscars.utils.validator.EndpointValidator;
//end LS
public class IONUIUtils {
	private static Logger log =  Logger.getLogger(IONUIUtils.class);
	public static String ION_TOPOLOGY_FILE = "ion_topology.yaml";

	//constants that define admin role names
	public static String ION_ADMIN_ATTR_NAME = "ION-administrator";

	//constants for organization and role
	public static String ATTR_ORG = "institution";

	public static String ATTR_ROLE = "role";
	
	public static String getTopologyFile() {
		ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_IONUI);
		cc.setServiceName(ServiceNames.SVC_IONUI);

		String configFilename = null;
		try {
			cc.setContext(System.getProperty("context"));
			cc.loadManifest(ServiceNames.SVC_IONUI,  ConfigDefaults.MANIFEST);
			configFilename = cc.getFilePath(ION_TOPOLOGY_FILE);
		} catch (ConfigException e) {
			log.error("ConfigException when getting ION Topo file path:" + e);
			//return null;
		}
		log.debug("ION Topology file is:" + configFilename);
		return configFilename;
	}

	/* 
	 * Method to get endpoint URL 
	 */
	public static Map<String, Object> getEndpointURL() {
		Map<String,Object> endPointsData = Collections.synchronizedMap(new HashMap());
		ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_IONUI);
		cc.setServiceName(ServiceNames.SVC_IONUI);

		String configFilename = null;
		try {
			cc.setContext(System.getProperty("context"));
			cc.loadManifest(ServiceNames.SVC_IONUI,  ConfigDefaults.MANIFEST);
			configFilename = cc.getFilePath(ConfigDefaults.CONFIG);
		} catch (ConfigException e) {
			log.error("ConfigException when gettting ENDPOINTS data" + e);
			//return null;
		}
		log.debug("Endpoint to be read from config file:" + configFilename);
		HashMap<String,Object> utilConfig =
			(HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
		log.debug("Context:"+ cc.getContext() + "," + utilConfig + "," + configFilename);
		if(utilConfig.containsKey("endpoint")){
			endPointsData = (HashMap<String,Object>) utilConfig.get("endpoint");
		}
		else {
			log.error("No endpoints found, is utilConfig null?" + utilConfig);
		}
		//to remove until here

		/*
        	Iterator iterator = endPointsData.entrySet().iterator();
        	while (iterator.hasNext()) {
            		Map.Entry entry = (Map.Entry) iterator.next();
            		log.debug( "(" + entry.getKey() + ": " +
                              entry.getValue() + "), " );
        	}		 
		 */
		return endPointsData;
	}

	/* get String userName */	
	public static String getUserNameFromSession(HttpServletRequest request, String methodName,
			PrintWriter out, ServletCore core) {
		AuthNClient authNClient = core.getAuthNClient();
		AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
		AuthZClient authZClient = core.getAuthZClient();

		UserSession userSession = new UserSession(core);
		CheckSessionReply sessionReply =
			userSession.checkSession(out, authNPolicyClient, request,
					methodName);
		if (sessionReply == null) {
			log.warn("No user session: cookies invalid???");
			return  null;
		}
		return sessionReply.getUserName();	
	}


	/* Utility method to get a CheckSessionReply object */
	public static CheckSessionReply getUserSession(HttpServletRequest request, String methodName,
			PrintWriter out, ServletCore core) {
		AuthNClient authNClient = core.getAuthNClient();
		AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
		AuthZClient authZClient = core.getAuthZClient();

		UserSession userSession = new UserSession(core);
		CheckSessionReply sessionReply =
			userSession.checkSession(out, authNPolicyClient, request,
					methodName);
		if (sessionReply == null) {
			log.warn("No user session: cookies invalid???");
			return  null;
		}
		return sessionReply;
	} //end CheckSessionReply



	/*utility method to get Paths in a string format */
	public static String [] getRawPathArray (CtrlPlanePathContent path) {
		if (path == null) {
			log.warn("Path input parameter null! Returning ");
			return null;
		}

		List <CtrlPlaneHopContent> hops = path.getHop();

		if (hops == null) {
			log.warn("Hops null for this path! Returning ");
			return null;
		}

		java.util.ListIterator <CtrlPlaneHopContent>  listIterator = hops.listIterator();
		String arrPaths[] = new String[hops.size()];
		int iIndex = 0;
		String pathId = null;
		while(listIterator.hasNext()) {
			CtrlPlaneHopContent tempHopObj = (CtrlPlaneHopContent)(listIterator.next());
			CtrlPlaneLinkContent tempObjHop = (CtrlPlaneLinkContent)tempHopObj.getLink();

			if (tempObjHop != null)
				pathId = tempHopObj.getId();
			else //path was not found. could be a failed ckt	
				log.debug("Path not found"); 
			log.debug("PathId=" + pathId);		
			arrPaths[iIndex] = pathId;	
			iIndex++;
		}
		return arrPaths;
	}

	/* utility method to get Domains involved in path */
	public static String getDomainsString (CtrlPlanePathContent path) {

		java.util.HashSet <String> domains = new HashSet<String>();
		java.util.List <CtrlPlaneHopContent> hops = null;

		if (path != null)
			hops = path.getHop();
		else {
			log.warn("Path input parameter null! Returning ");
			return null;
		}

		if (hops == null) {
			log.warn("Hops null for this path! Returning ");
			return null;
		}

		java.util.ListIterator<CtrlPlaneHopContent> listIter = 
			hops.listIterator();

		//add to unique list of domains
		String domainId = "" ;
		while(listIter.hasNext()) {			
			CtrlPlaneHopContent tempHopObj = (CtrlPlaneHopContent)(listIter.next());
			if (tempHopObj == null)
				continue;
			/*
			log.debug ("----- domainId" + tempHopObj.getDomainIdRef() + 
					"node Id" + tempHopObj.getNodeIdRef() +
							",portId" + tempHopObj.getPortIdRef() +
							",linkId" + tempHopObj.getLinkIdRef()  +
								"for domain " + tempHopObj.getDomain() + 
								" for node " + tempHopObj.getNode()  +
								" for link " + ((CtrlPlaneLinkContent)tempHopObj.getLink()).getId() + 
								" remote ID:" + ((CtrlPlaneLinkContent)tempHopObj.getLink()).getRemoteLinkId()+ 
								" Hop id:" +  tempHopObj.getId() +
								"path id " + path.getId()
								);
			 */
			CtrlPlaneLinkContent linkObj = (CtrlPlaneLinkContent)tempHopObj.getLink();
			if (linkObj == null)
				continue;
			String linkURN = linkObj.getId();
			//not checking for null/empty linkURN
			URNParserResult urnFields = URNParser.parseTopoIdent(linkURN, PathTools.getLocalDomainId());
			domainId = urnFields.getDomainId(); 
			domains.add(domainId);			
		}

		//form string of domains with a newline divider
		Iterator iter = domains.iterator();
		String sAllDomains = "";
		while (iter.hasNext())		
			sAllDomains = "Domain: "+ iter.next() + "\n";
		return sAllDomains;		
	}


	/* Method to determine whether a given user has administrative role */
	public static boolean isAdminUser(List <AttributeType> userAttributes) {
		boolean isAdmin = false;
		for(AttributeType attr : userAttributes){
			log.debug("UserAttr=" + attr.getName()+
					"attrValue " + attr.getAttributeValue());
			//check for user role
			if ( (attr != null) && (attr.getName().equals(ATTR_ROLE)) ) {
				List<Object> samlValues = attr.getAttributeValue();
				for (Object samlValue: samlValues) {
					String value = (String) samlValue;
					log.debug("saml value " + value);
					if(ION_ADMIN_ATTR_NAME.equals(value)){
						isAdmin = true;
						break;
					}
				}
				if (isAdmin) break; //break out of outer loop
			}
		}
		return isAdmin;
	} //end isAdminUSer method

	public static String getUsersOrg(List <AttributeType> userAttributes) {
		String orgName = "";	
		for(AttributeType attr : userAttributes){
			log.error("UserAttr=" + attr.getName()+
					"attrValue " + attr.getAttributeValue());
			//check for user orgranization 
			if ( (attr != null) && (attr.getName().equals(ATTR_ORG)) ) {
				List<Object> samlValues = attr.getAttributeValue();
				for (Object samlValue: samlValues) {
					orgName = (String) samlValue;
					log.debug("Org user belongs to " + orgName);
				}
				break; //quit out of loop, organization found
			}
		}
		return orgName;
	} //end getUsersOrg method

	/*
	public static HashSet<String> getUserRoles(List <AttributeType> userAttributes,
			HashSet<E> roles) {
		String tempRole = "";
		log.debug("Current size of roles array"+ roles.size());
		
		for(AttributeType attr : userAttributes){
			log.debug("UserAttr=" + attr.getName()+
					"attrValue " + attr.getAttributeValue());
			//check for user orgranization 
			if ( (attr != null) && (attr.getName().equals(ATTR_ROLE)) ) {
				List<Object> samlValues = attr.getAttributeValue();
				for (Object samlValue: samlValues) {
					tempRole = (String) samlValue;
					roles.add(tempRole);
					log.error("Role " + tempRole + " added ");
				}
			}
		}
		return roles;
	} //end method
	*/

	public static void initPSHostLookup() throws OSCARSServiceException {
	
	HashMap<String,Object> lookupMap          = null;
 	String configFilename= null;
	ContextConfig          cc                 = null;
	cc = ContextConfig.getInstance(ServiceNames.SVC_IONUI);

	try {
 		configFilename = cc.getFilePath(ServiceNames.SVC_LOOKUP,cc.getContext(),
                    ConfigDefaults.CONFIG);
	} catch (ConfigException e) {
            throw new OSCARSServiceException (e);
        }

        lookupMap = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        log.debug(netLogger.start("IONUIUtils: initPSHostLookup"));

        //get perfSONAR lookup settings from lookup module
        Map perfsonar = (Map) lookupMap.get("perfsonar");
        if(perfsonar == null){
            //no host lookup, so server only accepts URNs
            log.debug(netLogger.end("initPSHostLookup"));
            return;
        }
        HashMap<String, String> logFieldMap = new HashMap<String, String>();
        String hintsFile = null;
        List<String> globalLookupServices = null;
        List<String> homeLookupServices = null;

        if(perfsonar.containsKey("globalHintsFile")){
            hintsFile = (String) perfsonar.get("globalHintsFile");
            logFieldMap.put("hints", hintsFile);
        }

        if(perfsonar.containsKey("globalLookupServices")){
            globalLookupServices = (List<String>) perfsonar.get("globalLookupServices");
            logFieldMap.put("gLS", OSCARSNetLogger.serializeList(globalLookupServices));
        }

        if(perfsonar.containsKey("homeLookupServices")){
            homeLookupServices = (List<String>) perfsonar.get("homeLookupServices");
            logFieldMap.put("hLS", OSCARSNetLogger.serializeList(homeLookupServices));
	}
	try {
           EndpointValidator.init(hintsFile, globalLookupServices, homeLookupServices);
        } catch (Exception e) {
            log.debug(netLogger.error("initPSHostLookup",ErrSev.CRITICAL, e.getMessage(), null, logFieldMap));
            throw new OSCARSServiceException (e);
        }

   	}
	

}
