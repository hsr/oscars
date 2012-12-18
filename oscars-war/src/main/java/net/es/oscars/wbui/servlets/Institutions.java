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


public class Institutions extends HttpServlet {
    private Logger log = Logger.getLogger(Institutions.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "Institutions";
        this.log.info(methodName + ":start");
        PrintWriter out = response.getWriter();

        ServletCore core = (ServletCore)
            getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZClient authZClient = core.getAuthZClient();

        UserSession userSession = new UserSession(core);
        String[] ops = request.getQueryString().split("=");
        if (ops.length != 2) {
            this.log.error("Incorrect input from Institutions page");
            ServletUtils.handleFailure(out, "incorrect input from Institutions page", methodName);
            return;
        }
        String opName = ops[1];

        response.setContentType("application/json");
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, request,
                                     methodName);
        if (sessionReply == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }
        String userName = sessionReply.getUserName();
        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                                sessionReply.getAttributes(), "AAA", "modify");
            if (authVal.equals("DENIED")) {
                this.log.warn(userName + " has no permission to modify Institutions table.");
                ServletUtils.handleFailure(out, "no permission to modify Institutions table", methodName);
                return;
            }
            String saveName = request.getParameter("saveName");
            if (saveName != null) {
                saveName = saveName.trim();
            }
            String institutionEditName = request.getParameter("institutionEditName").trim();
            if (opName.equals("add")) {
                methodName = "InstitutionAdd";
                this.addInstitution(institutionEditName, authNPolicyClient);
                outputMap.put("status", "Added institution: " + institutionEditName);
            } else if (opName.equals("modify")) {
                methodName = "InstitutionModify";
                this.modifyInstitution(saveName, institutionEditName,
                                       authNPolicyClient);
                outputMap.put("status", "Changed institution name from " + saveName + " to " + institutionEditName);
            } else if (opName.equals("delete")) {
                methodName = "InstitutionDelete";
                this.deleteInstitution(institutionEditName, authNPolicyClient);
                outputMap.put("status", "Deleted institution: " + institutionEditName);
            } else {
                methodName = "InstitutionList";
                outputMap.put("status", "Institutions management");
            }
            // always output latest list
            this.outputInstitutions(outputMap, authNPolicyClient);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ":end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * outputInstitutions - gets the initial list of institutions.
     *
     * @param outputMap Map containing JSON data
     */
    public void outputInstitutions(Map<String, Object> outputMap,
                                   AuthNPolicyClient client)
            throws OSCARSServiceException {

        EmptyArg req = new EmptyArg();
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("listInsts", soapReq);
        ListInstsReply reply = (ListInstsReply) resp[0];
        List<String> institutionNames = reply.getName();
        ArrayList<HashMap<String,String>> institutionList =
            new ArrayList<HashMap<String,String>>();
        int ctr = 0;
        for (String name: institutionNames) {
            HashMap<String,String> institutionMap =
                new HashMap<String,String>();
            institutionMap.put("id", Integer.toString(ctr));
            institutionMap.put("name", name);
            institutionList.add(institutionMap);
            ctr++;
        }
        outputMap.put("institutionData", institutionList);
    }

    /**
     * addInstitution - add an institution if it doesn't already exist.
     *
     * @param newName string with name of new institution
     * @throws OSCARSServiceException
     */
    public void addInstitution(String newName, AuthNPolicyClient client)
            throws OSCARSServiceException {

        Object[] soapReq = new Object[]{newName};
        Object[] resp = client.invoke("addInst", soapReq);
    }

    /**
     * modifyInstitution - change an institution's name.
     *
     * @param oldName string with old name of institution
     * @param newName string with new name of institution
     * @throws OSCARSServiceException
     */
    public void modifyInstitution(String oldName, String newName,
                                  AuthNPolicyClient client)
            throws OSCARSServiceException {

        ModifyInstParams req = new ModifyInstParams();
        req.setOldName(oldName);
        req.setNewName(newName);
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("modifyInst", soapReq);
   }

    /**
     * deleteInstitution - delete an institution, but only if no users
     *     currently belong to it
     *
     * @param institutionName string with name of institution to delete
     * @throws OSCARSServiceException
     */
    public void deleteInstitution(String institutionName,
                                  AuthNPolicyClient client)
            throws OSCARSServiceException {

        Object[] soapReq = new Object[]{institutionName};
        Object[] resp = client.invoke("removeInst", soapReq);
    }
}
