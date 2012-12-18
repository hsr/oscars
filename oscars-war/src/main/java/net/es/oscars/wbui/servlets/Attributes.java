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
import net.es.oscars.utils.clients.AuthZPolicyClient;
import net.es.oscars.authZ.soap.gen.policy.*;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.common.soap.gen.EmptyArg;
import net.es.oscars.authCommonPolicy.soap.gen.*;

/**
 * This class handles requests from the attributes tab.  It is not
 * sufficiently tested.  The attributes table is duplicated in the authN
 * and authZ databases, so there may be consistency problems.  In practice,
 * attributes are rarely changed.
 */
public class Attributes extends HttpServlet {
    private Logger log = Logger.getLogger(Attributes.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "Attributes";
        this.log.info(methodName + ".start");
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        ServletCore core = (ServletCore)
            getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        AuthZPolicyClient authZPolicyClient = core.getAuthZPolicyClient();
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
        List<AttributeType> userAttributes = sessionReply.getAttributes();

        String[] ops = request.getQueryString().split("=");
        if (ops.length != 2) {
            this.log.error("Incorrect input from Attributes page");
            ServletUtils.handleFailure(out, "incorrect input from Attributes page", methodName);
            return;
        }
        String opName = ops[1];

        Map<String, Object> outputMap = new HashMap<String, Object>();
        // TODO:  actions if parameters are null, based on op
        String saveAttrValue = request.getParameter("saveAttrName");
        if (saveAttrValue != null) {
            saveAttrValue = saveAttrValue.trim();
        }
        String saveAttrId = request.getParameter("saveAttrType");
        if (saveAttrId != null) {
            saveAttrId = saveAttrId.trim();
        }
        String saveAttrDescr = request.getParameter("saveAttrDescription");
        if (saveAttrDescr != null) {
            saveAttrDescr = saveAttrDescr.trim();
        }
        String attributeEditValue = request.getParameter("attributeEditName").trim();
        String attributeEditDescr = request.getParameter("attributeEditDescription").trim();
        String attributeEditId = request.getParameter("attributeTypes").trim();
        try {
            String authVal = ServletUtils.checkPermission(authZClient,
                               userAttributes, "AAA", "modify");
            if (authVal.equals("DENIED")) {
                String errorMsg = "User "+userName+" does not have permission to modify attributes.";
                this.log.warn(errorMsg);
                ServletUtils.handleFailure(out, errorMsg, methodName);
                return;
            }
            if (opName.equals("add")) {
                methodName = "AttributeAdd";
                this.addAttribute(attributeEditValue, attributeEditDescr,
                        attributeEditId, authNPolicyClient, authZPolicyClient);
                outputMap.put("status", "Added attribute: " + attributeEditValue);
            } else if (opName.equals("modify")) {
                methodName = "AttributeModify";
                this.modifyAttribute(saveAttrValue, attributeEditValue,
                        saveAttrDescr, attributeEditDescr,
                        saveAttrId, attributeEditId,
                        authNPolicyClient, authZPolicyClient);
                if (!saveAttrValue.equals(attributeEditValue)) {
                    outputMap.put("status", "Changed attribute name from " + saveAttrValue + " to " + attributeEditValue);
                } else {
                    outputMap.put("status", "Modified attribute " + saveAttrValue);
                }
            } else if (opName.equals("delete")) {
                methodName = "AttributeDelete";
                this.deleteAttribute(attributeEditId, attributeEditValue,
                       attributeEditDescr,  authNPolicyClient,
                       authZPolicyClient);
                outputMap.put("status", "Deleted attribute: " + attributeEditValue);
            } else {
                methodName = "AttributeList";
                outputMap.put("status", "Attributes management");
            }
            this.outputAttributes(outputMap, authNPolicyClient, authZPolicyClient);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        // always output latest list
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ".finish");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * outputAttributes - gets the initial list of attributes.
     *
     * @param outputMap Map containing JSON data
     */
    public void outputAttributes(Map<String, Object> outputMap,
                                 AuthNPolicyClient authNPolicyClient,
                                 AuthZPolicyClient authZPolicyClient)
            throws OSCARSServiceException {

        List<AttrDetails> authNAttributes =
            ServletUtils.getAllAttributes(authNPolicyClient, null);
        List<AttrDetails> authZAttributes =
            ServletUtils.getAllAttributes(null, authZPolicyClient);
        // check for same list
        boolean isInconsistent = false;
        if (authNAttributes.size() != authZAttributes.size()) {
            isInconsistent = true;
            log.debug("authN size " + authNAttributes.size() + " authZsize "+ authZAttributes.size());
        }
        if (!isInconsistent) {
            int ctr = 0;
            // depends on list attributes being ordered
            for (AttrDetails authNAttr: authNAttributes) {
                AttrDetails authZAttr = authZAttributes.get(ctr);
                if (!authNAttr.getAttrId().equals(authZAttr.getAttrId())) {
                    isInconsistent = true;
                    log.debug("attrrIds dont match " + ctr);
                    break;
                }
                if (!authNAttr.getValue().equals(authZAttr.getValue())) {
                    isInconsistent = true;
                    log.debug("attrValues dont match " + ctr);
                    break;
                }
                if (!authNAttr.getDescription().equals(authZAttr.getDescription())) {
                    isInconsistent = true;
                    log.debug("descriptions dont match " + ctr);
                    break;
                }
                ctr++;
            }
        }
        if (isInconsistent) {
            throw new OSCARSServiceException("FATAL:  inconsistent attributes " +
                                             "entries in databases");
        }
        ArrayList<HashMap<String,String>> attributeList =
            new ArrayList<HashMap<String,String>>();
        int ctr = 0;
        for (AttrDetails attribute: authNAttributes) {
            HashMap<String,String> attributeMap = new HashMap<String,String>();
            attributeMap.put("id", Integer.toString(ctr));
            attributeMap.put("name", attribute.getValue());
            attributeMap.put("description", attribute.getDescription());
            attributeMap.put("type", attribute.getAttrId());
            attributeList.add(attributeMap);
            ctr++;
        }
        outputMap.put("attributeData", attributeList);
    }

    /**
     * addAttribute - add an attribute if it doesn't already exist.
     *
     * @param newValue string with name of new attribute
     * @param newDescription string with description of new attribute
     * @param newId string with type of new attribute
     * @throws OSCARSServiceException
     */
    public void addAttribute(String newValue, String newDescription,
                     String newId, AuthNPolicyClient authNPolicyClient,
                     AuthZPolicyClient authZPolicyClient)
            throws OSCARSServiceException {

        AttrDetails attribute = new AttrDetails();
        attribute.setAttrId(newId);
        attribute.setValue(newValue);
        attribute.setDescription(newDescription);
        Object[] soapReq = new Object[]{attribute};
        Object[] resp = authNPolicyClient.invoke("addAttr", soapReq);
        try {
            resp = authZPolicyClient.invoke("addAttr", soapReq);
        } catch (OSCARSServiceException ex) {
            // try to undo
            soapReq = new Object[]{newValue};
            resp = authNPolicyClient.invoke("removeAttr", soapReq);
            throw new OSCARSServiceException("Unable to add attribute " +
               "to authZ database.  Databases may be inconsistent.");
        }
    }

    /**
     * modifyAttribute - change an attribute's name, description, and/or type.
     *
     * @param oldValue string with old name of attribute
     * @param newValue string new name of attribute
     * @param oldDescr string with old description
     * @param descr string with attribute description
     * @param oldAttrId string with old type of attribute
     * @param attrId string with type of attribute
     * @throws OSCARSServiceException
     */
    public void modifyAttribute(String oldValue, String newValue,
                    String oldDescr, String descr, String oldAttrId,
                    String attrId, AuthNPolicyClient authNPolicyClient,
                    AuthZPolicyClient authZPolicyClient)
            throws OSCARSServiceException {

        ModifyAttrDetails req = new ModifyAttrDetails();
        AttrDetails oldAttribute = new AttrDetails();
        // FIXME:  currently only value is used for lookup
        oldAttribute.setValue(oldValue);
        oldAttribute.setDescription(oldDescr);
        oldAttribute.setAttrId(oldAttrId);
        AttrDetails modAttribute = new AttrDetails();
        modAttribute.setAttrId(attrId);
        modAttribute.setValue(newValue);
        modAttribute.setDescription(descr);
        req.setOldAttrInfo(oldAttribute);
        req.setModAttrInfo(modAttribute);
        Object[] soapReq = new Object[]{req};
        Object[] resp = authNPolicyClient.invoke("modifyAttr", soapReq);
        try {
            resp = authZPolicyClient.invoke("modifyAttr", soapReq);
        } catch (OSCARSServiceException ex) {
            // try to undo
            oldAttribute.setValue(newValue);
            oldAttribute.setDescription(descr);
            oldAttribute.setAttrId(attrId);
            modAttribute.setValue(oldValue);
            modAttribute.setDescription(oldDescr);
            modAttribute.setAttrId(oldAttrId);
            resp = authNPolicyClient.invoke("modifyAttr", soapReq);
            throw new OSCARSServiceException("Unable to modify attribute " +
               "in authZ database.  Databases may be inconsistent.");
        }
    }

    /**
     * deleteAttribute - delete an attribute, but only if no users
     *     currently belong to it
     *
     * @param attrId used to add back attribute if remove fails
     * @param value string with name of attribute to delete
     * @param description used to add back attribute if remove fails
     * @param value
     * @throws OSCARSServiceException
     */
    public void deleteAttribute(String attrId,
                String value, String description,
                AuthNPolicyClient authNPolicyClient,
                AuthZPolicyClient authZPolicyClient)
            throws OSCARSServiceException {

        Object[] soapReq = new Object[]{value};
        Object[] resp = authNPolicyClient.invoke("removeAttr", soapReq);
        try {
            resp = authZPolicyClient.invoke("removeAttr", soapReq);
        } catch (OSCARSServiceException ex) {
            // try to undo
            AttrDetails attribute = new AttrDetails();
            attribute.setAttrId(attrId);
            attribute.setValue(value);
            attribute.setDescription(description);
            soapReq = new Object[]{attribute};
            resp = authNPolicyClient.invoke("addAttr", soapReq);
            throw new OSCARSServiceException("Unable to remove attribute " +
               "from authZ database.  Databases may be inconsistent.");
        }
    }
}
