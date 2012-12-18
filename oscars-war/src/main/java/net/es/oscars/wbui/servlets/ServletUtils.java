package net.es.oscars.wbui.servlets;

import java.io.PrintWriter;
import java.util.*;

import net.es.oscars.utils.soap.ErrorReport;
import net.sf.json.*;
import org.apache.log4j.Logger;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.AuthZPolicyClient;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.CheckAccessParams;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.common.soap.gen.EmptyArg;
import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.authCommonPolicy.soap.gen.*;
import net.es.oscars.utils.soap.OSCARSServiceException;

public class ServletUtils {

    public static void handleFailure(PrintWriter out, String message, String method) {

        Map<String, Object> errorMap = new HashMap<String, Object>();
        errorMap.put("success", Boolean.FALSE);
        errorMap.put("status", message);
        errorMap.put("method",method);
        JSONObject jsonObject = JSONObject.fromObject(errorMap);
        if (out != null) {
            out.println("{}&&" + jsonObject);
        }
        return;
    }

    public static void handleFailure(PrintWriter out, Logger log, Exception ex, String method) {

        Map<String, Object> errorMap = new HashMap<String, Object>();
        errorMap.put("success", Boolean.FALSE);
        errorMap.put("method", method);
        if (ex instanceof OSCARSServiceException) {
             OSCARSServiceException os = (OSCARSServiceException)ex;
             ErrorReport errReport = os.getErrorReport();
             if (errReport !=null) {
                errorMap.put("status", errReport.getErrorType() + " error " + errReport.getErrorCode() +
                             ": "  + errReport.getErrorMsg());
            } else {
                errorMap.put("status", ex.getMessage());
            }
        } else {
            if (ex.getMessage() != null){
                errorMap.put("status", ex.getMessage());
            } else {
                errorMap.put("status", ex.toString());
            }
        }

        JSONObject jsonObject = JSONObject.fromObject(errorMap);
        if (out != null) {
            out.println("{}&&" + jsonObject);
        }
        return;
    }

    public static void fatalError(PrintWriter out, String method) {

        Map<String, Object> errorMap = new HashMap<String, Object>();
        errorMap.put("success", Boolean.FALSE);
        errorMap.put("status", "FATAL ERROR:  servlets not initialized " +
                               "properly, server restart required");
        errorMap.put("method",method);
        JSONObject jsonObject = JSONObject.fromObject(errorMap);
        if (out != null) {
            out.println("{}&&" + jsonObject);
        }
        return;
    }

    public static String checkPermission(AuthZClient client, List<AttributeType> attributes,
                    String resourceName, String permissionName)
        throws OSCARSServiceException {
        CheckAccessReply  accessReply = checkAccess(client, attributes, resourceName,permissionName);
        return accessReply.getPermission();
    }
    
    public static CheckAccessReply
        checkAccess(AuthZClient client, List<AttributeType> attributes,
                    String resourceName, String permissionName)
            throws OSCARSServiceException {

        CheckAccessParams req = new CheckAccessParams();
        SubjectAttributes subjectAttrs = new SubjectAttributes();
        List<AttributeType> reqAttrs = subjectAttrs.getSubjectAttribute();
        for (AttributeType attr: attributes) {
            reqAttrs.add(attr);
        }
        req.setSubjectAttrs(subjectAttrs);
        req.setResourceName(resourceName);
        req.setPermissionName(permissionName);
        Object[] soapReq = new Object[]{req};
        Object[] resp = client.invoke("checkAccess", soapReq);
        CheckAccessReply checkAccessReply = (CheckAccessReply) resp[0];
        return checkAccessReply;
    }

    public static List<AttrDetails> getAllAttributes(AuthNPolicyClient authNPolicyClient, AuthZPolicyClient authZPolicyClient)
        throws OSCARSServiceException {

        EmptyArg req = new EmptyArg();
        Object[] soapReq = new Object[]{req};
        // kludge for now
        Object[] resp = null;
        if (authNPolicyClient != null) {
            resp = authNPolicyClient.invoke("listAttrs", soapReq);
        } else {
            resp = authZPolicyClient.invoke("listAttrs", soapReq);
        }
        ListAttrsReply reply = (ListAttrsReply) resp[0];
        List<AttrDetails> attributes = reply.getAttribute();
        return attributes;
    }

    /**
     * Checks for proper confirmation of password change.
     *
     * @param password  A string with the desired password
     * @param confirmationPassword  A string with the confirmation password
     * @return String containing a new password, if the password and
     *     confirmationPassword agree and if the password is not null, blank or
     *     equal to "********".  Otherwise it returns null, and the user
     *     password should not be reset.
     */
    public static String checkPassword(String password,
                                       String confirmationPassword)
           throws Exception {

        // If the password needs to be updated, make sure there is a
        // confirmation password, and that it matches the given password.
        if ((password != null) && (!password.equals("")) &&
                (!password.equals("********"))) {
           if (confirmationPassword == null) {
                throw new Exception(
                    "Cannot update password without confirmation password");
            } else if (!confirmationPassword.equals(password)) {
                throw new Exception(
                     "Password and password confirmation do not match");
            }
           return password;
        }
        return null;
    }

    /**
     * CheckDN  check for the input DN to be in comma separated format starting
     *    with the CN element.
     * @param DN string containing the input DN
     * @return String returning the DN, possibily in reverse order
     */
    public static String checkDN(String DN) throws Exception {

        String[] dnElems = null;

        dnElems = DN.split(",");
        if (dnElems.length < 2)  {
            /* TODO look for / separated elements */
            throw new Exception
                    ("Please input cert issuer and subject names as comma separated elements");
         }
        if (dnElems[0].startsWith("CN")) { return DN;}
        /* otherwise reverse the order */
        String dn = " " + dnElems[0];
        for (int i = 1; i < dnElems.length; i++) {
            dn = dnElems[i] + "," + dn;
        }
        dn = dn.substring(1);
        return dn;
    }

    /**
     * removes the description part of the authorization input form fields
     * @param inputField A string with the complete field.
     * @return A string minus the description field of the parameter
     */
    public static String dropDescription(String inputField) {
     // assumes field name has a name followed by " -> description"
        String[] namePortions = inputField.split(" ->");
        return namePortions[0];
    }
}
