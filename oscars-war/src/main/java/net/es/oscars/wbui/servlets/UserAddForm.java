package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.common.soap.gen.EmptyArg;
import net.es.oscars.authCommonPolicy.soap.gen.*;


public class UserAddForm extends HttpServlet {
    private Logger log = Logger.getLogger(UserAddForm.class);

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserAddForm";
        this.log.info( methodName + ".start");
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
        String authVal = null;
        try {
            authVal = ServletUtils.checkPermission(authZClient,
                               sessionReply.getAttributes(), "Users", "modify");
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        if (!authVal.equals("ALLUSERS")) {
            String errorMsg = "User "+userName+" is not allowed to add a new user";
            log.warn(errorMsg);
            ServletUtils.handleFailure(out, errorMsg, methodName);
            return;
        }

        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            this.outputAttributeMenu(outputMap, authNPolicyClient);
            this.outputInstitutionMenu(outputMap, authNPolicyClient);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("status", "Add a user");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ":end");
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }




    public void outputAttributeMenu(Map<String, Object> outputMap,
                      AuthNPolicyClient client) throws OSCARSServiceException {

        this.log.debug("outputAttributeMenu.start");
        List<AttrDetails> attributes =
            ServletUtils.getAllAttributes(client, null);
        List<String> attributeList = new ArrayList<String>();
        // default is none
        attributeList.add("None");
        attributeList.add("true");
        for (AttrDetails a: attributes) {
            attributeList.add(a.getValue() + " -> " + a.getDescription());
            attributeList.add("false");
        }
        outputMap.put("newAttributeNameMenu", attributeList);
        this.log.debug("outputAttributeMenu.finish");
    }

    public void outputInstitutionMenu(Map<String, Object> outputMap,
                      AuthNPolicyClient client) throws OSCARSServiceException {

        this.log.debug("outputInstitutionMenu.start");
        EmptyArg req = new EmptyArg();
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("listInsts", soapReq);
        ListInstsReply reply = (ListInstsReply) resp[0];
        List<String> names = reply.getName();
        List<String> institutionList = new ArrayList<String>();
        int ctr = 0;
        // default is first in list
        for (String name: names) {
            institutionList.add(name);
            if (ctr == 0) {
                institutionList.add("true");
            } else {
                institutionList.add("false");
            }
            ctr++;
        }
        outputMap.put("newInstitutionMenu", institutionList);
        this.log.debug("outputInstitutionMenu.finish");
    }
}
