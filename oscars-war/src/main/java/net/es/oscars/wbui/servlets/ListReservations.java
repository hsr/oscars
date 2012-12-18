package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.NMWGParserUtil;
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
import net.es.oscars.common.soap.gen.SubjectAttributes;


public class ListReservations extends HttpServlet {
    private Logger log = Logger.getLogger(ListReservations.class);
    private OSCARSNetLogger netLogger = null;
    private static String methodName = "ListReservations";

    /**
     * Handles servlet request (both get and post) from list reservations form.
     *
     * @param requestRequest servlet request
     * @param response servlet response
     */
    public void
        doGet(HttpServletRequest servletRequest, HttpServletResponse response)
            throws IOException, ServletException {

        String transId  = PathTools.getLocalDomainId() + "-WBUI-" + UUID.randomUUID().toString();
        this.netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_WBUI,transId);
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.info(netLogger.start(methodName));
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

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
        if (sessionReply == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid"));
            return;
        }
        String userName = sessionReply.getUserName();
 
        if (userName == null) {
            this.log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid"));
            return;
        }

        List<AttributeType> userAttributes = sessionReply.getAttributes(); 
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
            this.log.debug("calling getParameters") ;
            listReq = this.getParameters(servletRequest);
            listReq.setMessageProperties(msgProps);
            // Send a listReservation request 
            Object[] req = new Object[]{subjectAttrs,listReq};
            Object[] res = coordClient.invoke("listReservations",req);
            listResponse = (ListReply) res[0];

            checkAccessReply = ServletUtils.checkAccess(authZClient,
                                userAttributes, "Reservations", "list");

            // only display user search field if can look at other users'
            // reservations
            HashMap<String,Object> outputMap = new HashMap<String,Object>();
            String authVal = checkAccessReply.getPermission();
            if (authVal.equals(AuthZConstants.ACCESS_MYSITE)) {
                outputMap.put("resvLoginDisplay", Boolean.TRUE);
            } else if (authVal.equals(AuthZConstants.ACCESS_SELFONLY)){
                outputMap.put("resvLoginDisplay", Boolean.FALSE);
            } else {
                outputMap.put("resvLoginDisplay", Boolean.TRUE);
            }
            List<ResDetails> resDetails = listResponse.getResDetails();
            AuthConditions conds = checkAccessReply.getConditions();
            boolean intHopsAllowed = false;
            for (AuthConditionType ac: conds.getAuthCondition()){
                if (ac.getName().equals(AuthZConstants.INT_HOPS_ALLOWED))
                    intHopsAllowed = true;
            }
            this.outputReservations(outputMap, intHopsAllowed, resDetails);
            outputMap.put("totalRowsReplace", "Total rows: " + resDetails.size());
            outputMap.put("status", "Reservations list");
            outputMap.put("method", methodName);
            outputMap.put("success", Boolean.TRUE);
            JSONObject jsonObject = JSONObject.fromObject(outputMap);
            out.println("{}&&" + jsonObject);
            this.log.info(netLogger.end(methodName));
        } catch (Exception ex) {
            // any error will show up as an exception
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

    public ListRequest getParameters(HttpServletRequest servletRequest) {

        this.log.debug(netLogger.start(methodName + ":getParams"));
        ListRequest listReq = new ListRequest();
        Long startTimeSeconds = null;
        Long endTimeSeconds = null;
        int numRowsReq = 0;  // if default, return all results

        String numRowParam = servletRequest.getParameter("numRows");
        String loginEntered = servletRequest.getParameter("resvLogin");
        String description = servletRequest.getParameter("resvDescription");
        String startTimeStr = servletRequest.getParameter("startTimeSeconds");
        String endTimeStr = servletRequest.getParameter("endTimeSeconds");

        if (!startTimeStr.equals("")) {
            startTimeSeconds = Long.valueOf(startTimeStr.trim());
        }
        if (!endTimeStr.equals("")) {
            endTimeSeconds = Long.valueOf(endTimeStr.trim());
        }
        if (!numRowParam.trim().equals("") && !numRowParam.equals("all")) {
            numRowsReq = Integer.parseInt(numRowParam);
        }
        listReq.setResRequested(numRowsReq);

        if (!loginEntered.trim().equals("")) {
            listReq.setUser(loginEntered.trim());
        }

        if (!description.trim().equals("")) {
            listReq.setDescription(description.trim());
        }
        listReq.setStartTime(startTimeSeconds);
        listReq.setEndTime(endTimeSeconds);
        List<String> statuses = listReq.getResStatus();
        for (String status : this.getStatuses(servletRequest)) {
            statuses.add(status);
        }
        List<VlanTag> vlans = listReq.getVlanTag();
        for (String vtag: this.getList(servletRequest, "vlanSearch")) {
            VlanTag vt = new VlanTag();
            log.debug(netLogger.getMsg(methodName, "vlan: " + vtag));
            vt.setValue(vtag);
            vt.setTagged(true);
            vlans.add(vt);
        }
        List<String> linkIds = listReq.getLinkId();
        for (String linkId : this.getList(servletRequest, "linkIds")) {
            linkIds.add(linkId);
        }
        return listReq;
    }

    /**
     * Gets list of statuses selected from menu.
     *
     * @param servletRequest servlet request
     * @return list of statuses to send to coordService:listReservations
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
            String[] paramTags = param.trim().split("\\s+" );
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
     */
    public void outputReservations(Map<String, Object> outputMap,
                                   boolean intHopsAllowed,
                                   List<ResDetails> reservations)  throws OSCARSServiceException {

        InetAddress inetAddress = null;
        String gri = "";
        String source = null;
        String hostName = null;
        String destination = null;

        ArrayList<HashMap<String,Object>> resvList =
            new ArrayList<HashMap<String,Object>>();
        int ctr = 0;
        for (ResDetails resv: reservations) {
            log.debug("starting output for " + resv.getGlobalReservationId());
            HashMap<String,Object> resvMap = new HashMap<String,Object>();
            CtrlPlanePathContent path = null;
            UserRequestConstraintType uConstraint = resv.getUserRequestConstraint();
            PathInfo pathInfo = null;
            String pathType = null;
            ReservedConstraintType rConstraint = resv.getReservedConstraint();
            if (rConstraint !=  null) {
                pathInfo=rConstraint.getPathInfo();
                pathType = "reserved";
            } else {
                uConstraint = resv.getUserRequestConstraint();
                if (uConstraint == null) {
                    throw new OSCARSServiceException("invalid reservation, no reserved or requested path");
                }
                pathInfo=uConstraint.getPathInfo();
                pathType="requested";
                this.log.debug("no path reserved, using requested path ");
            }
            path = pathInfo.getPath();
            //this.log.debug("past getPath");
            if (path == null) {
                this.log.debug("path is null");
            }
            List<CtrlPlaneHopContent> localHops = PathTools.getLocalHops(path, PathTools.getLocalDomainId());
            String localSrc = "n/a";
            String localDest = "n/a";
            if(localHops != null && !localHops.isEmpty()){
                localSrc = NMWGParserUtil.getURN(localHops.get(0));
                localDest = NMWGParserUtil.getURN(localHops.get(localHops.size() - 1));
            }
           
            gri = resv.getGlobalReservationId();
            Layer3Info layer3Info = null;
            Layer2Info layer2Info = null;
            // NOTE:  using local path for this info for now
           
            if (path != null ) {
                layer3Info = pathInfo.getLayer3Info();
                layer2Info = pathInfo.getLayer2Info();
                //log.debug("layer2Info set to " + layer2Info);
                //log.debug("layer3Info set to " + layer3Info);
            }
            resvMap.put("id", Integer.toString(ctr));
            resvMap.put("gri", gri);
            resvMap.put("status", resv.getStatus());
            Integer mbps = uConstraint.getBandwidth();

            String bandwidthField = mbps.toString() + "Mbps";
            //log.debug("bandwidth is " + bandwidthField);
            resvMap.put("bandwidth", bandwidthField);
            // entries are converted on the fly on the client to standard
            // date and time format before the model's data is set
            resvMap.put("startTime", uConstraint.getStartTime());
            if (layer2Info != null) {
                resvMap.put("source", layer2Info.getSrcEndpoint());
            } else if (layer3Info != null) {
                if(path == null || path.getHop() == null || path.getHop().isEmpty()){
                    resvMap.put("source", "none");
                }else{
                    resvMap.put("source", NMWGParserUtil.getURN(path.getHop().get(0)));
                }
            }
            if (localSrc != null) {
                resvMap.put("localSource", localSrc);
            } else {
                resvMap.put("localSource", "");
            }
            // start of second sub-row
            //log.debug ("start of second sub-row");
            resvMap.put("user", resv.getLogin());
            // assumes just layer2 src and dest vlan tags for now -- mrt
            String vlanTag = "";
            // note this code assumes only one vlan tag for the whole path
            if (layer2Info != null  && layer2Info.getSrcVtag() != null) {
               // log.debug("getting vlan from layer2");
                vlanTag = layer2Info.getSrcVtag().getValue();
                if (!vlanTag.equals("")) {
                    // if not a range
                    if (!vlanTag.contains("-") && (!"any".equals(vlanTag))) {
                        String vlanStr = "";
                        try{
                            vlanStr = Math.abs(Integer.parseInt(vlanTag)) +"";
                        } catch(Exception e) {
                            resvMap.put("vlan", "");
                        }
                        resvMap.put("vlan", vlanStr);
                    } else {
                        resvMap.put("vlan", "");
                    }
                } else {
                    resvMap.put("vlan", "");
                }
            }
            resvMap.put("endTime", uConstraint.getEndTime());
            if (layer2Info != null) {
                resvMap.put("destination", layer2Info.getDestEndpoint());
            } else if (layer3Info != null) {
                if(path == null || path.getHop() == null || path.getHop().isEmpty()){
                    resvMap.put("destination", "none");
                }else{
                    resvMap.put("destination", NMWGParserUtil.getURN(path.getHop().get(path.getHop().size() - 1)));
                }
            }
            if (localDest != null) {
                if (layer2Info != null) {
                    resvMap.put("localDestination", localDest);
                } else {
                    resvMap.put("localDestination", localDest);
                }
            } else {
                resvMap.put("localDestination", "");
            }
            log.debug("adding reservation to list " + gri);
            resvList.add(resvMap);
            ctr++;
        }
        log.debug("got " + ctr + " reservations");
        outputMap.put("resvData", resvList);
    }
}
