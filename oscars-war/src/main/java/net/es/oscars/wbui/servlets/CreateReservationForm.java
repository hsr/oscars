package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;

import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.clients.AuthZPolicyClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.sf.json.*;

/**
 *  checks the user's permission to input internal hops and adds it 
 *  to the output map.
 *  
 * @author David Robertson, mrt
 *
 */

public class CreateReservationForm extends HttpServlet {
    private Logger log = Logger.getLogger(CreateReservationForm.class);

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

       
        String methodName = "CreateReservationForm";
        log.info(methodName + "start");
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        ServletCore core = (ServletCore)
            getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZClient authZClient = core.getAuthZClient();
        UserSession userSession = new UserSession(core);
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, request,
                                     methodName);
        if (sessionReply == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }        
        String userName = sessionReply.getUserName();
        if (userName == null) {
            return;
        }
        List<AttributeType> userAttributes = sessionReply.getAttributes();
        String authVal = null;
        CheckAccessReply checkAccessReply;
        try {
            checkAccessReply = ServletUtils.checkAccess(authZClient,
                    userAttributes, "Reservations", "create");
            authVal = checkAccessReply.getPermission();
            if (authVal == null || authVal.equals("DENIED")) {
                String errorMsg = "User "+userName+" has no permission to create reservations";
                this.log.warn(errorMsg);
                ServletUtils.handleFailure(out, errorMsg, methodName);
                return;
            }
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            // this form does not reset status
            outputMap.put("method", methodName);
            outputMap.put("success", Boolean.TRUE);
            this.contentSection(outputMap, checkAccessReply, out);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void contentSection(Map<String, Object> outputMap, 
                               CheckAccessReply checkAccessReply, 
                               PrintWriter out)  {
        String methodName = "CreateReservationForm.contentSection";
        // check to see if user may specify path elements
        AuthConditions conds = checkAccessReply.getConditions();
        for (AuthConditionType ac: conds.getAuthCondition()){
            if (ac.getName().equals(AuthZConstants.INT_HOPS_ALLOWED))
            {
                outputMap.put("authorizedWarningDisplay", Boolean.TRUE);
                outputMap.put("authorizedPathDisplay", Boolean.TRUE);
            } else {
                outputMap.put("authorizedWarningDisplay", Boolean.FALSE);
                outputMap.put("authorizedPathDisplay", Boolean.FALSE);
            }
        }
    }
}
