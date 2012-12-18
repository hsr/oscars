package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.utils.clients.AuthZPolicyClient;
import net.es.oscars.authZ.soap.gen.policy.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.common.soap.gen.EmptyArg;
import net.es.oscars.authCommonPolicy.soap.gen.*;


/**
 * Class that sets menu options in attributes, resources, permissions, and
 * constraints menus.  May be called more than once, if attributes
 * modified.  Initially called on user login.
 */
public class AuthorizationForm extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationForm.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        String methodName = "AuthorizationForm";
        log.info(methodName + ":start");
        PrintWriter out = response.getWriter();
        ServletCore core = (ServletCore)
            getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZClient authZClient = core.getAuthZClient();
        AuthZPolicyClient authZPolicyClient = core.getAuthZPolicyClient();
        UserSession userSession = new UserSession(core);

        response.setContentType("application/json");
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, request, methodName);
        if (sessionReply == null) {
            log.warn("No user session: cookies invalid");
            return;
        }
        String userName = sessionReply.getUserName();
        String attrsUpdated = request.getParameter("authAttrsUpdated");
        if (attrsUpdated != null) {
            attrsUpdated = attrsUpdated.trim();
        } else {
            attrsUpdated = "";
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                                sessionReply.getAttributes(), "AAA", "modify");
            if (authVal == null || authVal.equals("DENIED")) {
                log.warn(userName + " not authorized to perform admin operations");
                ServletUtils.handleFailure(out, "not authorized to perform admin operations", methodName);
                return;
            }
            this.outputAttributeMenu(outputMap, authZPolicyClient);
            String rpcParam = request.getParameter("rpc");

            // Make sure to update these exactly once.
            // rpc being unset makes sure they get updated in the beginning.
            // If just the attributes have been updated, don't redisplay.
            if (((rpcParam == null) || (rpcParam.trim().equals("")) ||
                    attrsUpdated.equals(""))) {
                this.outputResourceMenu(outputMap, authZPolicyClient);
                this.outputPermissionMenu(outputMap, authZPolicyClient);
                this.outputConstraintMenu(outputMap, authZPolicyClient);
            }
            if ((rpcParam == null) || rpcParam.trim().equals("")) {
                this.outputRpcs(outputMap, authZPolicyClient);
            }
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("method", methodName);
        // this form does not reset status
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        log.info(methodName + ":end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void outputAttributeMenu(Map<String, Object> outputMap,
            AuthZPolicyClient client)
                throws OSCARSServiceException {

        this.log.debug("outputAttributeMenu.start");
        List<AttrDetails> attributes =
            ServletUtils.getAllAttributes(null, client);
        List<String> attributeList = new ArrayList<String>();
        int ctr = 0;
        for (AttrDetails attribute: attributes) {
            attributeList.add(attribute.getValue());
            if (ctr == 0) {
                attributeList.add("true");
            } else {
                attributeList.add("false");
            }
            ctr++;
        }
        outputMap.put("authAttributeNameMenu", attributeList);
        this.log.debug("outputAttributeMenu.finish");
    }

    public void outputResourceMenu(Map<String, Object> outputMap,
                      AuthZPolicyClient client) throws OSCARSServiceException {

        this.log.debug("outputResourceMenu.start");
        EmptyArg req = new EmptyArg();
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("listResources", soapReq);
        ListResourcesReply reply = (ListResourcesReply) resp[0];
        List<ResourceDetails> resources = reply.getResource();
        List<String> resourceList = new ArrayList<String>();
        int ctr = 0;
        for (ResourceDetails resource: resources) {
            resourceList.add(resource.getName());
            if (ctr == 0) {
                resourceList.add("true");
            } else {
                resourceList.add("false");
            }
            ctr++;
        }
        outputMap.put("resourceNameMenu", resourceList);
        this.log.debug("outputResourceMenu.finish");
    }

    public void outputPermissionMenu(Map<String, Object> outputMap,
                      AuthZPolicyClient client) throws OSCARSServiceException {

        this.log.debug("outputPermissionMenu.start");
        EmptyArg req = new EmptyArg();
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("listPermissions", soapReq);
        ListPermissionsReply reply = (ListPermissionsReply) resp[0];
        List<PermissionDetails> permissions = reply.getPermission();
        List<String> permissionList = new ArrayList<String>();
        int ctr = 0;
        for (PermissionDetails permission: permissions) {
            permissionList.add(permission.getName());
            if (ctr == 0) {
                permissionList.add("true");
            } else {
                permissionList.add("false");
            }
            ctr++;
        }
        outputMap.put("permissionNameMenu", permissionList);
        this.log.debug("outputPermissionMenu.finish");
    }

    public void
        outputConstraintMenu(Map<String, Object> outputMap,
                AuthZPolicyClient client) throws OSCARSServiceException  {

        this.log.debug("outputConstraintMenu.start");
        EmptyArg req = new EmptyArg();
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("listConstraints", soapReq);
        ListConstraintsReply reply = (ListConstraintsReply) resp[0];
        List<ConstraintDetails> constraints = reply.getConstraint();
        List<String> constraintList = new ArrayList<String>();
        int ctr = 0;
        for (ConstraintDetails constraint: constraints) {
            constraintList.add(constraint.getName());
            if (ctr == 0) {
                constraintList.add("true");
            } else {
                constraintList.add("false");
            }
            ctr++;
        }
        outputMap.put("constraintNameMenu", constraintList);
        this.log.debug("outputConstraintMenu.finish");
    }

    /**
     * Outputs permitted resource/permission/constraint triplets, along
     * with constraint type.   Could
     * eventually be used in a split container on the right side of the
     * authorization details page, assuming the grid would display in
     * such a case.
     *
     * @param outputMap Map containing JSON data
     */
    public void outputRpcs(Map<String, Object> outputMap,
                      AuthZPolicyClient client) throws OSCARSServiceException {

        this.log.info("outputRpcs.start");
        EmptyArg req = new EmptyArg();
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("listRpcs", soapReq);
        ListRpcsReply reply = (ListRpcsReply) resp[0];
        List<RpcDetails> rpcs = reply.getRpc();
        ArrayList<ArrayList<String>> rpcList = new ArrayList<ArrayList<String>>();
        for (RpcDetails rpc: rpcs) {
            ArrayList<String> rpcEntry = new ArrayList<String>();
            rpcEntry.add(rpc.getResourceName());
            rpcEntry.add(rpc.getPermissionName());
            rpcEntry.add(rpc.getConstraintName());
            rpcEntry.add(rpc.getConstraintType());
            rpcList.add(rpcEntry);
        }
        outputMap.put("rpcData", rpcList);
        this.log.info("outputRpcs.finish");
    }
}
