package edu.internet2.ion.ionui.servlets;

/* create Reservation servlet 
 * 
 * Author kkumar
 * This class can be changed to extend oscars-war/../CreateReservation
 * Methods
 *  toReservation() and handlePath() are essentially the same, but private 
 *  access in the super class.
 * Make it protected in the super-class.
 * doGet() is similar but for added functionality of favorites
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.db.DBUtil;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.Layer3Info;
import net.es.oscars.api.soap.gen.v06.MplsInfo;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.OptionalConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;

import net.es.oscars.wbui.servlets.ServletCore;
import net.es.oscars.wbui.servlets.ServletUtils;
import net.es.oscars.wbui.servlets.CheckSessionReply;



//commented below for ion porting
/*
import net.es.oscars.ConfigFinder;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Layer2Data;
import net.es.oscars.bss.topology.Layer3Data;
import net.es.oscars.bss.topology.MPLSData;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.bss.BssRmiInterface;
import net.es.oscars.servlets.ServletUtils;
import net.es.oscars.servlets.UserSession;
*/
import net.sf.json.JSONObject;

import net.es.oscars.utils.validator.DataValidator; // add for endpoints

public class IONCreateReservation extends HttpServlet{
//public class IONCreateReservation extends CreateReservation {
    private Logger log = Logger.getLogger(IONCreateReservation.class);
    
    public void init(){
    	if(!DBUtil.loadJDBCDriver()){
            this.log.error("Could not load local JDBC Driver");
            return;
        }
        log.debug("JDBC driver loaded");

	// LS-translation
	try {
		IONUIUtils.initPSHostLookup();
		log.debug("***PSHOMELOOKUP called");
	} catch (Exception oscarsServiceExcep) {
		log.error("Exception trying to initialise PS Host lookup" + oscarsServiceExcep);
	}
	// end check for LS
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        String methodName= "CreateReservation";
        PrintWriter out = response.getWriter();
        
        //authenticate the reservation
        /* comment for porting
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
        

        CheckSessionReply sessionReply = IONUIUtils.getUserSession(request, methodName, out, core);
        String userName = sessionReply.getUserName();
        this.log.debug("userName from sessionReply="+  userName);
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid, user null"));
            return;
        }
        //end new addition
        
        response.setContentType("application/json");
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        /*
        Reservation resv = null;
        Path requestedPath = null;
        try {
            resv = this.toReservation(userName, request);
            requestedPath = this.handlePath(request);
            resv.setPath(requestedPath);
        } catch (BSSException e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        
        String gri = null;
        try {
            BssRmiInterface rmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            gri = rmiClient.createReservation(resv, userName);
        } catch (Exception ex) {
            ServletUtils.handleFailure(out, log, ex, methodName);
            return;
        }
                
        */ //commenting for ION porting, to replace with block below
        
        List<AttributeType> userAttributes = sessionReply.getAttributes();
        SubjectAttributes subjectAttrs = new SubjectAttributes();
        List<AttributeType> reqAttrs = subjectAttrs.getSubjectAttribute();
        for (AttributeType attr: userAttributes) {
            reqAttrs.add(attr);
        }
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setOriginator(subjectAttrs);
        msgProps.setGlobalTransactionId(transId);
        ResCreateContent createReq = null;
        String gri = null;
        try {
            createReq = this.toReservation(userName, request);
            createReq.setMessageProperties(msgProps);
            // Send a createReservation request
	    DataValidator.validate(createReq, false); // add line to get endpoints 
	    this.log.debug("DataValidator called");
            Object[] req = new Object[]{subjectAttrs,createReq};
            Object[] res = coordClient.invoke("createReservation",req);
            CreateReply coordResponse = (CreateReply) res[0];
            gri = coordResponse.getGlobalReservationId();
        } catch (Exception exCreate) {
            this.log.error(netLogger.error(methodName, 
            		ErrSev.MAJOR,
            		"Exception invoking createReservation " +
            		exCreate.toString()));
            exCreate.printStackTrace();
            ServletUtils.handleFailure(out, log, exCreate, methodName);
            return;
        }
        
        outputMap.put("gri", gri);
        outputMap.put("status", "Submitted reservation with GRI " + gri);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        
        //Connect to derby and save favorites and endpoints
        try {
            //Connection conn = DriverManager.getConnection("jdbc:derby:ion");
            Connection conn =  DBUtil.getDBConnection();
            if("1".equals(request.getParameter("saveAsFavorite"))){
                PreparedStatement favStmt = 
                	conn.prepareStatement("INSERT INTO favorites VALUES(DEFAULT, ?, ?)");
                favStmt.setString(1, userName);
                favStmt.setString(2, gri);
                favStmt.execute();
            }
            
            PreparedStatement endpointStmt = 
            	conn.prepareStatement("INSERT INTO endpoints VALUES(DEFAULT, ?, ?, ?)");
            endpointStmt.setString(1, gri);
            endpointStmt.setString(2, request.getParameter("source"));
            endpointStmt.setString(3, request.getParameter("destination"));
            endpointStmt.execute();
            
            conn.close();
        } catch (SQLException e) {
            this.log.error(e.getMessage());
        }
        
        out.println("{}&&" + jsonObject);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.doGet(request, response);
    }

    /*
    private Reservation
        toReservation(String userName, HttpServletRequest request)
           throws BSSException {

        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("startSeconds");
        if (strParam != null && !strParam.trim().equals("")) {
            seconds = Long.parseLong(strParam.trim());
        } else {
            throw new BSSException("error: start time is a required parameter");
        }
        resv.setStartTime(seconds);

        strParam = request.getParameter("endSeconds");
        if (strParam != null && !strParam.trim().equals("")) {
            seconds = Long.parseLong(strParam.trim());
        } else {
            throw new BSSException("error: end time is a required parameter");
        }
        resv.setEndTime(seconds);

        strParam = request.getParameter("bandwidth");
        if (strParam != null && !strParam.trim().equals("")) {
            bandwidth = Long.valueOf(strParam.trim()) * 1000000L;
        } else {
            throw new BSSException("error: bandwidth is a required parameter.");
        }
        resv.setBandwidth(bandwidth);

        String description = "";
        strParam = request.getParameter("description");
        if (strParam != null && !strParam.trim().equals("")) {
            description = strParam.trim();
        }

        strParam = request.getParameter("productionType");
        // if not blank, check box indicating production circuit was checked
        if (strParam != null && !strParam.trim().equals("")) {
            this.log.info("production reservation created");
            description = "[PRODUCTION CIRCUIT] " + description;
        } else {
            this.log.debug("non-production circuit");
        }
        resv.setDescription(description);
        return resv;
    }
*/ //commenting to replace with below
    
    private ResCreateContent
    toReservation(String userName, HttpServletRequest request)
       throws OSCARSServiceException {
    this.log.debug("toReservation:start");

    String strParam = null;
    Integer bandwidth = null;
    Long seconds = 0L;

    ResCreateContent resv = new ResCreateContent();
    UserRequestConstraintType userCon = new UserRequestConstraintType();

    // necessary type conversions performed here; validation done in
    // ReservationManager
    strParam = request.getParameter("startSeconds");
    if (strParam != null && !strParam.trim().equals("")) {
        seconds = Long.parseLong(strParam.trim());
    } else {
        throw new OSCARSServiceException("error: start time is a required parameter");
    }
    userCon.setStartTime(seconds);

    strParam = request.getParameter("endSeconds");
    if (strParam != null && !strParam.trim().equals("")) {
        seconds = Long.parseLong(strParam.trim());
    } else {
        throw new OSCARSServiceException("error: end time is a required parameter");
    }
    userCon.setEndTime(seconds);

    strParam = request.getParameter("bandwidth");
    if (strParam != null && !strParam.trim().equals("")) {
        bandwidth = Integer.valueOf(strParam.trim());
    } else {
        throw new OSCARSServiceException("error: bandwidth is a required parameter.");
    }
    userCon.setBandwidth(bandwidth);

    String description = "";
    strParam = request.getParameter("description");
    if (strParam != null && !strParam.trim().equals("")) {
        description = strParam.trim();
    }

    strParam = request.getParameter("productionType");
    // if not blank, check box indicating production circuit was checked
    if (strParam != null && !strParam.trim().equals("")) {  
    	this.log.info("production reservation created");
    description = "[PRODUCTION CIRCUIT] " + description;
    } else {
        this.log.debug("non-production circuit");
    }
    resv.setDescription(description);
    userCon.setPathInfo( handlePath(request));
    resv.setUserRequestConstraint(userCon);
    this.log.debug("toReservation:end");

    return resv;
}
    
    /**
     * Takes form parameters and builds Path structures.
     *
     * @param request HttpServletRequest
     * @return requestedPath a Path instance with layer 2 or 3 information
     */
    /* Commented method to replace with the below version */
    /*
    private Path handlePath(HttpServletRequest request)
            throws BSSException {

        String strParam = null;

        List<PathElem> pathElems = new ArrayList<PathElem>();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("wbui", true);
        String defaultLayer = props.getProperty("defaultLayer");

        Path requestedPath = new Path();
        requestedPath.setPathType(PathType.REQUESTED);
        requestedPath.setPathSetupMode("timer-automatic");
        requestedPath.setPathElems(pathElems);

        String explicitPath = "";
        String source = null;
        String destination = null;
        strParam = request.getParameter("source");
        if ((strParam != null) && !strParam.trim().equals("")) {
            source = strParam.trim();
        } else {
            throw new BSSException("error:  source is a required parameter");
        }
        strParam = request.getParameter("destination");
        if ((strParam != null) && !strParam.trim().equals("")) {
            destination = strParam.trim();
        } else {
            throw new BSSException("error:  destination is a required parameter");
        }
        strParam = request.getParameter("explicitPath");
        if (strParam != null && !strParam.trim().equals("")) {
            explicitPath = strParam.trim();
            this.log.debug("explicit path: " + explicitPath);

            String[] hops = explicitPath.split("\\s+");
            for (int i = 0; i < hops.length; i++) {
                hops[i] = hops[i].trim();
                if (hops[i].equals(" ") || hops[i].equals("")) {
                    continue;
                }
                PathElem pathElem = new PathElem();
                // these can currently be either topology identifiers
                // or IP addresses
                pathElem.setUrn(hops[i]);
                this.log.debug("explicit path hop: " + hops[i]);
                pathElems.add(pathElem);
            }
        }
        String srcVlan = "";
        strParam = request.getParameter("srcVlan");
        if (strParam != null && !strParam.trim().equals("")) {
            srcVlan = strParam.trim();
        }
        boolean layer2 = false;
        // TODO: support VLAN translation

        if (!srcVlan.equals("") ||
              (defaultLayer !=  null && defaultLayer.equals("2"))) {
            layer2 = true;

            Layer2Data layer2Data = new Layer2Data();
            srcVlan = (srcVlan == null||srcVlan.equals("") ? "any" : srcVlan);
            String destVlan = "";
            strParam = request.getParameter("destVlan");
            if (strParam != null && !strParam.trim().equals("")) {
                destVlan = strParam.trim();
            } else {
                destVlan = srcVlan;
            }
            // src and dest default to tagged
            String taggedSrcVlan = "Tagged";
            strParam = request.getParameter("taggedSrcVlan");
            if (strParam != null && !strParam.trim().equals("")) {
                taggedSrcVlan = strParam.trim();
            }
            String taggedDestVlan = "Tagged";
            strParam = request.getParameter("taggedDestVlan");
            if (strParam != null && !strParam.trim().equals("")) {
                taggedDestVlan = strParam.trim();
            }
            boolean tagged = taggedSrcVlan.equals("Tagged");
            if (!tagged) {
                srcVlan = "0";
            }
            tagged = taggedDestVlan.equals("Tagged");
            if (!tagged) {
                destVlan = "0";
            }

            layer2Data.setSrcEndpoint(source);
            layer2Data.setDestEndpoint(destination);
            requestedPath.setLayer2Data(layer2Data);

            // If no explicit path for layer 2, we must fill this in
            if (pathElems.isEmpty()) {
                PathElem srcpe = new PathElem();
                srcpe.setUrn(source);
                PathElem dstpe = new PathElem();
                dstpe.setUrn(destination);
                pathElems.add(srcpe);
                pathElems.add(dstpe);
            }
            PathElemParam srcVlanParam = new PathElemParam();
            srcVlanParam.setSwcap(PathElemParamSwcap.L2SC);
            srcVlanParam.setType(PathElemParamType.L2SC_VLAN_RANGE);
            srcVlanParam.setValue(srcVlan);
            PathElemParam destVlanParam = new PathElemParam();
            destVlanParam.setSwcap(PathElemParamSwcap.L2SC);
            destVlanParam.setType(PathElemParamType.L2SC_VLAN_RANGE);
            destVlanParam.setValue(destVlan);

            requestedPath.getPathElems().get(0).addPathElemParam(srcVlanParam);
            requestedPath.getPathElems().get(requestedPath.getPathElems().size()-1).addPathElemParam(destVlanParam);
            return requestedPath;
        }
        if (!layer2) {
            Layer3Data layer3Data = new Layer3Data();
            // VLAN id wasn't supplied with layer 2 id
            if (source.startsWith("urn:ogf:network")) {
                throw new BSSException("VLAN tag not supplied for layer 2 reservation");
            }
            layer3Data.setSrcHost(source);
            layer3Data.setDestHost(destination);

            strParam = request.getParameter("srcPort");
            if ((strParam != null) && !strParam.trim().equals("")) {
                layer3Data.setSrcIpPort(Integer.valueOf(strParam.trim()));
            } else {
                layer3Data.setSrcIpPort(0);
            }
            strParam = request.getParameter("destPort");
            if ((strParam != null) && !strParam.trim().equals("")) {
                layer3Data.setDestIpPort(Integer.valueOf(strParam.trim()));
            } else {
                layer3Data.setDestIpPort(0);
            }
            strParam = request.getParameter("protocol");
            if ((strParam != null) && !strParam.trim().equals("")) {
                layer3Data.setProtocol(strParam.trim());
            }
            strParam = request.getParameter("dscp");
            if ((strParam != null) && !strParam.trim().equals("")) {
                layer3Data.setDscp(strParam.trim());
            }
            requestedPath.setLayer3Data(layer3Data);
        }
        MPLSData mplsData = new MPLSData();
        mplsData.setBurstLimit(10000000L);
        requestedPath.setMplsData(mplsData);
        return requestedPath;
    }
    */
    
    
    private PathInfo handlePath(HttpServletRequest request)
    throws OSCARSServiceException {

    	this.log.debug("handlePath:start");
    	String strParam = null;

    	//List<PathElem> pathElems = new ArrayList<PathElem>();
    	//PropHandler propHandler = new PropHandler("oscars.properties");
    	//Properties props = propHandler.getPropertyGroup("wbui", true);
    	//String defaultLayer = props.getProperty("defaultLayer");
    	String defaultLayer = "2";  // that's all we are implementing for now

    	PathInfo requestedPath = new PathInfo();
    	String[] inHops = {};
    	requestedPath.setPathType("loose");
    	requestedPath.setPathSetupMode("timer-automatic");

    	String explicitPath = "";
    	String source = null;
    	String destination = null;
    	strParam = request.getParameter("source");
    	if ((strParam != null) && !strParam.trim().equals("")) {
    		source = strParam.trim();
    	} else {
    		throw new OSCARSServiceException("error:  source is a required parameter");
    	}
    	strParam = request.getParameter("destination");
    	if ((strParam != null) && !strParam.trim().equals("")) {
    		destination = strParam.trim();
    	} else {
    		throw new OSCARSServiceException("error:  destination is a required parameter");
    	}
    	CtrlPlanePathContent path = new CtrlPlanePathContent ();
    	List<CtrlPlaneHopContent> pathHops = path.getHop();
    	strParam = request.getParameter("explicitPath");
    	if (strParam != null && !strParam.trim().equals("")) {
    		explicitPath = strParam.trim();
    		this.log.debug("explicit path: " + explicitPath);

    		inHops = explicitPath.split("\\s+");
    		for (int i = 0; i < inHops.length; i++) {
    			inHops[i] = inHops[i].trim();
    			if (inHops[i].equals(" ") || inHops[i].equals("")) {
    				continue;
    			}
    			for (String hop : inHops) {
    				CtrlPlaneHopContent cpHop = new CtrlPlaneHopContent();
    				cpHop.setLinkIdRef(hop);
    				pathHops.add(cpHop);
    			}
    			requestedPath.setPath(path);
    		}
    	}
    	String srcVlan = "";
    	strParam = request.getParameter("srcVlan");
    	if (strParam != null && !strParam.trim().equals("")) {
    		srcVlan = strParam.trim();
    	}
    	boolean layer2 = false;
    	// TODO: support VLAN translation

    	if (!srcVlan.equals("") ||
    			(defaultLayer !=  null && defaultLayer.equals("2"))) {
    		layer2 = true;
    		this.log.debug("handlePath. in layer2 processing");

    		Layer2Info layer2Info = new Layer2Info();
    		srcVlan = (srcVlan == null||srcVlan.equals("") ? "any" : srcVlan);
    		String destVlan = "";
    		strParam = request.getParameter("destVlan");
    		if (strParam != null && !strParam.trim().equals("")) {
    			destVlan = strParam.trim();
    		} else {
    			destVlan = srcVlan;
    		}
    		// src and dest default to tagged
    		String taggedSrcVlan = "Tagged";
    		strParam = request.getParameter("taggedSrcVlan");
    		if (strParam != null && !strParam.trim().equals("")) {
    			taggedSrcVlan = strParam.trim();
    		}
    		String taggedDestVlan = "Tagged";
    		strParam = request.getParameter("taggedDestVlan");
    		if (strParam != null && !strParam.trim().equals("")) {
    			taggedDestVlan = strParam.trim();
    		}
    		boolean tagged = taggedSrcVlan.equals("Tagged");
    		if (!tagged) {
    			srcVlan = "0";
    		}
    		tagged = taggedDestVlan.equals("Tagged");
    		if (!tagged) {
    			destVlan = "0";
    		}

    		layer2Info.setSrcEndpoint(source);
    		layer2Info.setDestEndpoint(destination);
    		requestedPath.setLayer2Info(layer2Info);

    		// If no explicit path for layer 2, we must fill this in
    		if (pathHops.isEmpty()) {
    			CtrlPlaneHopContent sourceHop = new CtrlPlaneHopContent();
    			sourceHop.setLinkIdRef(source);
    			pathHops.add(sourceHop);
    			CtrlPlaneHopContent destHop = new CtrlPlaneHopContent();
    			destHop.setLinkIdRef(destination);
    			pathHops.add(destHop);
    		}
    		/* Need to set a linkContent rather than a linkIdRef in order to store link params
			    PathElemParam srcVlanParam = new PathElemParam();
			    srcVlanParam.setSwcap(PathElemParamSwcap.L2SC);
			    srcVlanParam.setType(PathElemParamType.L2SC_VLAN_RANGE);
			    srcVlanParam.setValue(srcVlan);
			    PathElemParam destVlanParam = new PathElemParam();
			    destVlanParam.setSwcap(PathElemParamSwcap.L2SC);
			    destVlanParam.setType(PathElemParamType.L2SC_VLAN_RANGE);
			    destVlanParam.setValue(destVlan);
			    requestedPath.getPathElems().get(0).addPathElemParam(srcVlanParam);
			    requestedPath.getPathElems().get(requestedPath.getPathElems().size()-1).addPathElemParam(destVlanParam);
    		 */
    	}
    	if (!layer2) {
    		return null;
    		/* not implemented yet
			    Layer3Data layer3Data = new Layer3Data();
			    // VLAN id wasn't supplied with layer 2 id
			    if (source.startsWith("urn:ogf:network")) {
			        throw new OSCARSServiceException("VLAN tag not supplied for layer 2 reservation");
			    }
			    layer3Data.setSrcHost(source);
			    layer3Data.setDestHost(destination);
			
			    strParam = request.getParameter("srcPort");
			    if ((strParam != null) && !strParam.trim().equals("")) {
			        layer3Data.setSrcIpPort(Integer.valueOf(strParam.trim()));
			    } else {
			        layer3Data.setSrcIpPort(0);
			    }
			    strParam = request.getParameter("destPort");
			    if ((strParam != null) && !strParam.trim().equals("")) {
			        layer3Data.setDestIpPort(Integer.valueOf(strParam.trim()));
			    } else {
			        layer3Data.setDestIpPort(0);
			    }
			    strParam = request.getParameter("protocol");
			    if ((strParam != null) && !strParam.trim().equals("")) {
			        layer3Data.setProtocol(strParam.trim());
			    }
			    strParam = request.getParameter("dscp");
			    if ((strParam != null) && !strParam.trim().equals("")) {
			        layer3Data.setDscp(strParam.trim());
			    }
			    requestedPath.setLayer3Data(layer3Data);
    		 */
    	}
    	this.log.debug("handlePath:end");
    	return requestedPath;
    }

}
