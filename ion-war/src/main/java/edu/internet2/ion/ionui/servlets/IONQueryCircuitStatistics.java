package edu.internet2.ion.ionui.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/*
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.bss.BssRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiQueryResReply;
import net.es.oscars.servlets.ServletUtils;
import net.es.oscars.servlets.UserSession;
import net.es.oscars.bss.topology.*;
 */

import net.es.oscars.wbui.servlets.AuthenticateUser;
import net.es.oscars.wbui.servlets.UserSession;
import net.es.oscars.wbui.servlets.CheckSessionReply;
import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;
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
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.coord.common.URNParser;
import net.es.oscars.coord.common.URNParserResult;
import net.es.oscars.utils.topology.PathTools;


import net.es.oscars.api.soap.gen.v06.VlanTag;
import net.es.oscars.utils.soap.OSCARSServiceException;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import org.jdom.*;
import org.jdom.output.XMLOutputter;

import edu.internet2.perfsonar.*;

import org.ogf.schema.network.topology.ctrlplane.*;

public class IONQueryCircuitStatistics extends HttpServlet{
	private Logger log = Logger.getLogger(IONQueryCircuitStatistics.class);
	public static final String DEFAULT_ION_TOPOLOGY_CONFIG_FILE = IONUIUtils.getTopologyFile(); //"ion_topology.yaml";

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String methodName = "QueryCircuitStatistics";

		//UserSession userSession = new UserSession(); //comment for porting
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");

		/* //commenting for porting
                String userName = userSession.checkSession(out, request, "QueryReservation");
                if (userName == null) {
                        this.log.debug("No user session: cookies invalid");
                        ServletUtils.handleFailure(out, "Missing user session", method);
                        return;
                } 
		 */
		//new inclusion instead of the above block
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

		CheckSessionReply sessionReply = IONUIUtils.getUserSession(request, methodName, out, core);
		String userName = sessionReply.getUserName();
		this.log.debug("userName from sessionReply="+  userName);
		if (userName == null) {
			this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
			return;
		}
		//end new addition


		//replace below line
		//RmiQueryResReply rmiReply = new RmiQueryResReply();
		QueryResContent queryReq = new QueryResContent();
		Map<String, Object> outputMap = new HashMap<String, Object>();

		String gri = request.getParameter("gri");
		String locationId = request.getParameter("locationId");
		String measurementType = request.getParameter("measurementType");
		String startTimeStr = request.getParameter("startTime");
		String endTimeStr = request.getParameter("endTime");

		this.log.debug("Location ID: "+locationId);
		this.log.debug("GRI: "+gri);
		this.log.debug("measurementType: "+measurementType);

		if ("ingress".equals(locationId) == false && "egress".equals(locationId) == false) {
			this.log.debug("Unknown location: "+locationId);
			ServletUtils.handleFailure(out, "Unknown location specified", methodName);
			return;
		}

		if ("utilization".equals(measurementType) == false) {
			this.log.debug("Unknown measurementType: "+measurementType);
			ServletUtils.handleFailure(out, "Unknown measurement type specified", methodName);
			return;
		}

		Long startTime = null;
		if (startTimeStr != null && "".equals(startTimeStr) == false) {
			try {
				startTime = (Long) Long.valueOf(startTimeStr);
			} catch (Exception e) {
				ServletUtils.handleFailure(out, "Invalid start time specified", methodName);
				return;
			}
		}

		Long endTime = null;
		if (endTimeStr != null && "".equals(endTimeStr) == false) {
			try {
				endTime = (Long) Long.valueOf(endTimeStr);
			} catch (Exception e) {
				ServletUtils.handleFailure(out, "Invalid end time specified", methodName);
				return;
			}
		}

		IONConfig cfg = new IONConfig(DEFAULT_ION_TOPOLOGY_CONFIG_FILE);

		JSONArray data = new JSONArray();

		//Long max_bandwidth;
		int max_bandwidth; //changed to int for porting
		String urn = null;
		String hostName = null;
		String basePortName = null;
		String vlanPortName = null;
		String vlanTag = null;
		String measurementArchive = null;

		//new addition to initialize query parameters
		List<AttributeType> userAttributes =
			sessionReply.getAttributes();
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
		queryReq.setMessageProperties(msgProps);
		queryReq.setGlobalReservationId(gri);

		CheckAccessReply checkAccessReply = null;
		ResDetails resv = null;
		//end new addn
		try {

			log.debug("Trying to query reservation for GRI:" + gri +
					", using req object" + queryReq);
			Object[] req = new Object[]{subjectAttrs,queryReq};
			Object[] res = coordClient.invoke("queryReservation",req);
			resv = ((QueryResReply) res[0]).getReservationDetails();

			UserRequestConstraintType uConstraint = resv.getUserRequestConstraint();
			if (uConstraint == null) {
				throw new OSCARSServiceException("invalid reservation, no reserved or requested path");
			}

			long currTime  = System.currentTimeMillis() / 1000;

			if (startTime == null && endTime == null) {
				startTime = uConstraint.getStartTime();
				endTime = uConstraint.getEndTime();

				if (endTime > currTime) {
					endTime = currTime;
				}

				// set a limit of the past day
				if (endTime - startTime > 86400) {
					startTime = endTime - 86400;
				}
			} else if (startTime == null) {
				startTime = uConstraint.getStartTime();
			} else if (endTime == null) {
				endTime = uConstraint.getEndTime();
				if (endTime > currTime) {
					endTime = currTime;
				}
			}

			/*
                        Path path = resv.getPath(PathType.LOCAL);
                        if (path == null) {
			    this.log.debug("Path == null");
                            ServletUtils.handleFailure(out, "No local path for reservation", method);
                            return;
                        }
			 */
			CtrlPlanePathContent path = null;
			PathInfo pathInfo = null;
			String pathType = null;
			ReservedConstraintType rConstraint = resv.getReservedConstraint();
			if (rConstraint !=  null) {
				pathInfo=rConstraint.getPathInfo();
				pathType = "reserved";
			} else {
				this.log.error("No path reserved, quitting without finding more statistics");
				ServletUtils.handleFailure(out, "No path reserved, quitting without finding more statistics", methodName);
				return;
			}

			/* TBD . How to find if layer2 ckt?
                        // Only handle layer 2 paths for now.
                        if (path.isLayer2() == false) {
			    this.log.debug("Path != layer2");
                            ServletUtils.handleFailure(out, "Only layer2 paths are handled for now", methodName);
                            return;
                        }
			 */
			pathInfo = rConstraint.getPathInfo();
			if ( pathInfo == null )	 {
				this.log.error("No Path Info for this reservation. Returning ");
				return;
			}
			path = pathInfo.getPath();
			this.log.debug("past getPath");
			if (path == null) {
				this.log.debug("Path isEmpty()");
				ServletUtils.handleFailure(out, "Empty path", methodName);
				return;
			}


			CtrlPlaneHopContent ingress          = null;
			String              ingress_urn      = null;
			CtrlPlaneHopContent egress           = null;
			String              egress_urn       = null;
			CtrlPlaneHopContent prevDomainHop    = null;
			String              prevDomainHopURN = null;
			ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
			for ( CtrlPlaneHopContent ctrlHop : hops ) {
				String curr_urn = null;
				CtrlPlaneLinkContent link = ctrlHop.getLink();
				if (link != null ) {
					curr_urn = link.getId();
				} else {
       					curr_urn = ctrlHop.getLinkIdRef();
				}

				this.log.debug("Current URN: "+curr_urn);

                        	URNParserResult urnFields = URNParser.parseTopoIdent(curr_urn, PathTools.getLocalDomainId());
                        	if (!urnFields.getDomainId().equals(PathTools.getLocalDomainId())) {
                        		if (prevDomainHop != null) {
                        			break;
                        		}
                        	}
                        	else {
                        		if (ingress == null) {
                        			ingress     = ctrlHop;
                        			ingress_urn =  curr_urn;
                        		}
                        		prevDomainHop    = ctrlHop;
                        		prevDomainHopURN = curr_urn;
                        	}
			}

			if (egress == null && prevDomainHop != null) {
             			egress      = prevDomainHop;
          			egress_urn  = prevDomainHopURN;
			}

			CtrlPlaneHopContent locationPoint = null;				
			if ("ingress".equals(locationId)) {
				this.log.debug("LocationPoint == ingress");
				locationPoint = ingress;
				urn           = ingress_urn;
			} else if ("egress".equals(locationId)) {
				this.log.debug("LocationPoint == egress");
				locationPoint = egress;
				urn           = egress_urn;
			} else {
				this.log.debug("Unknown Location");
				ServletUtils.handleFailure(out, "Unknown location", methodName);
				return;
			}

			this.log.debug("New Location URN: "+urn);

			CtrlPlaneLinkContent currentLink = locationPoint.getLink();
			CtrlPlaneSwcapContent swcapCont = currentLink. getSwitchingCapabilityDescriptors();

			CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcapCont.getSwitchingCapabilitySpecificInfo();
			String linkDescr = null;
			linkDescr = specInfo.getVlanRangeAvailability();
			//vlanRange works for Descr? check. TBD
			if (linkDescr != null) {
				vlanTag = linkDescr;
			} else {
				vlanTag = "untagged";
			}


			//Hashtable<String, String> urnFields = URNParser.parseTopoIdent(urn);
			//replacing above with below for porting
			URNParserResult urnFields = URNParser.parseTopoIdent(urn, PathTools.getLocalDomainId());
			String nodeId = urnFields.getNodeId();
			String portId = urnFields.getPortId();
			
			if (cfg.getString("node_"+nodeId+"/name", false) != null)  {
				hostName = cfg.getString("node_"+nodeId+"/name", false);
			}

			if (cfg.getString("node_"+nodeId+"/port_"+ portId + "/name", false) != null)  {
				basePortName = cfg.getString("node_"+ nodeId +
						"/port_"+ portId + "/name", false);
			}

			if (hostName == null) {
				hostName = nodeId; //urnFields.get("nodeId");
			}

			if (basePortName == null) {
				basePortName = portId; //urnFields.get("portId");
			}

			String vlanPortNameFormat = null;

			if (cfg.getString("node_"+nodeId+"/port_"+portId + "/vlanPortNameFormat", true) != null)  {
				vlanPortNameFormat = cfg.getString("node_"+nodeId+"/port_"+portId + "/vlanPortNameFormat", true);
			}

			if (vlanPortNameFormat == null) {
				vlanPortName = basePortName;
			} else {
				vlanPortName = vlanPortNameFormat;
				vlanPortName = vlanPortName.replaceAll("%p", basePortName);
				vlanPortName = vlanPortName.replaceAll("%v", vlanTag);
			}

			if (cfg.getString("node_"+nodeId+"/port_"+portId+ "/measurementArchive", true) != null)  {
				measurementArchive = cfg.getString("node_"+nodeId+"/port_"+portId + "/measurementArchive", true);
			}

			this.log.debug("vlanPortNameFormat: "+vlanPortNameFormat);
			this.log.debug("Location VLAN: "+vlanTag);

			//max_bandwidth = resv.getBandwidth() / 8;
			//commenting above to replace with below
			max_bandwidth = rConstraint.getBandwidth() / 8;
			
		} catch (Exception e) {
			this.log.error("Problem looking up circuit information");
			e.printStackTrace();
			ServletUtils.handleFailure(out, "Problem looking up circuit information", methodName);
			return;
		}


	// XXX put in 'real' values
	long resolution = 30;
	String consolidationFunction = "AVERAGE";

	try {
		Map<Long, Map<String, Object>> entries = new HashMap<Long, Map<String, Object>>();
		String [] directions = { "in", "out" };
		PSNamespaces psNS = new PSNamespaces();

		for( String direction : directions ) {
			String ma_request = "";
			ma_request += "<nmwg:message type=\"SetupDataRequest\" xmlns:nmwg=\"http://ggf.org/ns/nmwg/base/2.0/\" id=\"msg0\">";
			ma_request += "<nmwg:metadata id=\"meta0\">";
			ma_request += "  <nmwg:subject>";
			ma_request += "    <nmwgt:interface xmlns:nmwgt=\"http://ggf.org/ns/nmwg/topology/2.0/\">";
			ma_request += "      <nmwgt:hostName>"+hostName+"</nmwgt:hostName>";
			ma_request += "      <nmwgt:ifName>"+vlanPortName+"</nmwgt:ifName>";
			ma_request += "      <nmwgt:direction>"+direction+"</nmwgt:direction>";
			ma_request += "    </nmwgt:interface>";
			ma_request += "  </nmwg:subject>";
			ma_request += "  <nmwg:eventType>http://ggf.org/ns/nmwg/characteristic/utilization/2.0</nmwg:eventType>";
			ma_request += "</nmwg:metadata>";
			ma_request += "<nmwg:metadata id=\"meta1\">";
			ma_request += "    <select:subject id=\"subject0\" metadataIdRef=\"meta0\" xmlns:select=\"http://ggf.org/ns/nmwg/ops/select/2.0/\"/>";
			ma_request += "    <select:parameters id=\"parameters.0\" xmlns:select=\"http://ggf.org/ns/nmwg/ops/select/2.0/\">";
			ma_request += "      <nmwg:parameter name=\"startTime\">" + startTime + "</nmwg:parameter>";
			ma_request += "      <nmwg:parameter name=\"endTime\">" + endTime + "</nmwg:parameter>";
			ma_request += "      <nmwg:parameter name=\"resolution\">" + resolution + "</nmwg:parameter>";
			ma_request += "      <nmwg:parameter name=\"consolidationFunction\">" + consolidationFunction + "</nmwg:parameter>";
			ma_request += "    </select:parameters>";
			ma_request += "    <nmwg:eventType>http://ggf.org/ns/nmwg/ops/select/2.0</nmwg:eventType>";
			ma_request += "</nmwg:metadata>";
			ma_request += "<nmwg:data id=\"data0\" metadataIdRef=\"meta1\" />";
			ma_request += "</nmwg:message>";

			this.log.debug("Creating client for " + measurementArchive);
			PSBaseClient client = new PSBaseClient(measurementArchive);
			this.log.debug("Done creating client for " + measurementArchive);

			this.log.debug("Sending request to " + measurementArchive);

			Element msg = client.sendMessage(ma_request);

			this.log.debug("Got response from " + measurementArchive);

			this.log.debug("Looking for descendants in direction "+direction+" from "+measurementArchive);
			Iterator datums = msg.getDescendants(new org.jdom.filter.ElementFilter("datum"));
			while(datums.hasNext()) {
				Element datum = (Element) datums.next();
				XMLOutputter outputter = new XMLOutputter();
				//					this.log.debug("Found datum in "+direction+" from "+measurementArchive+": "+outputter.outputString(datum));

				Attribute tsAttr = datum.getAttribute("timeValue");
				if (tsAttr == null) {
					this.log.debug("Found datum in "+direction+" from "+measurementArchive+": didn't have no-NS timeValue");
					tsAttr = datum.getAttribute("timeValue", psNS.NMWG);
				}
				if (tsAttr == null) {
					this.log.debug("Found datum in "+direction+" from "+measurementArchive+": didn't have ANY timeValue");
					continue;
				}

				Attribute valueAttr = datum.getAttribute("value");
				if (valueAttr == null) {
					this.log.debug("Found datum in "+direction+" from "+measurementArchive+": didn't have no-NS value");
					valueAttr = datum.getAttribute("value", psNS.NMWG);
				}
				if (valueAttr == null) {
					this.log.debug("Found datum in "+direction+" from "+measurementArchive+": didn't have ANY value");
					continue;
				}

				if ("nan".equals(valueAttr.getValue())) {
					continue;
				}

				Long ts = new Long(Long.parseLong(tsAttr.getValue()));
				Double value = new Double(Double.parseDouble(valueAttr.getValue()));

				Map<String, Object> entry = entries.get(ts);
				if (entry == null) {
					entry = new HashMap<String, Object>();
					entry.put("ts", ts);
					entries.put(ts, entry);
				}

				entry.put(direction, value);
			}
		}
		Object[] keys = entries.keySet().toArray();
		Arrays.sort(keys);
		for(Object key : keys) {
			data.element(entries.get((Long) key));
		}
	} catch (Exception e) {
		this.log.debug("Problem looking up circuit statistics");
		this.log.debug("Problem looking up circuit statistics: "+e);
		ServletUtils.handleFailure(out, "Problem looking up circuit statistics", methodName);
		return;
	}

	outputMap.put("hostName", hostName);
	outputMap.put("basePortName", basePortName);
	outputMap.put("vlanPortName", vlanPortName);
	outputMap.put("urn", urn);
	outputMap.put("vlanTag", vlanTag);
	outputMap.put("measurementArchive", measurementArchive);
	outputMap.put("locationId", locationId);
	outputMap.put("method", methodName);
	outputMap.put("success", Boolean.TRUE);
	outputMap.put("status", "OK");
	JSONObject jsonObject = JSONObject.fromObject(outputMap);
	jsonObject.put("data", data);
	out.println("{}&&" + jsonObject);
	
	this.log.info(methodName +":end");

	return;
}

public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	this.doGet(request, response);
}

/**
 * Only fills in those fields that might have changed due to change
 * in reservation status.
 */
/*
        public void contentSection(RmiQueryResReply rmiReply, Map<String,Object> outputMap) throws BSSException {
                Reservation resv = rmiReply.getReservation();
                String status = resv.getStatus();
                outputMap.put("griReplace", resv.getGlobalReservationId());
                outputMap.put("statusfReplace", status);
                outputMap.put("localStatusReplace", resv.getLocalStatus());
        }
 */
/*
	public void 
		contentSection(ResDetails resv,
               Map<String,Object> outputMap) throws OSCARSServiceException {
		 String status = resv.getStatus();
		 outputMap.put("griReplace", resv.getGlobalReservationId());
	     outputMap.put("statusReplace", status);
	     outputMap.put("localStatusReplace", resv.getLocalStatus());
	}
*/
	
}
