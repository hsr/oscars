package net.es.oscars.authZ.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.es.oscars.authZ.beans.Attribute;
import net.es.oscars.authZ.beans.Authorization;
import net.es.oscars.authZ.beans.Constraint;
import net.es.oscars.authZ.beans.Permission;
import net.es.oscars.authZ.beans.Resource;
import net.es.oscars.authZ.beans.Rpc;
import net.es.oscars.authZ.dao.AttributeDAO;
import net.es.oscars.authZ.dao.AuthorizationDAO;
import net.es.oscars.authZ.dao.ConstraintDAO;
import net.es.oscars.authZ.dao.PermissionDAO;
import net.es.oscars.authZ.dao.ResourceDAO;
import net.es.oscars.authZ.dao.RpcDAO;
import net.es.oscars.logging.OSCARSNetLogger;


/**
 * PolicyManager handles all authorization database related method calls.
 *
 * @author David Robertson, Mary Thompson
 */
public class PolicyManager {
    private Logger log;
    private String dbname;

    public PolicyManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /** Creates an authorization.
     *
     * @param attributeValue string with attribute value
     * @param resourceName string with resource name
     * @param permissionName string with permission name
     * @param constraintName string with constraint name
     * @param constraintValue string with constraint value
     */
    public void createAuthorization(String attributeValue, String resourceName,
                                String permissionName, String constraintName,
                                String constraintValue)
            throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "createAuthorization";
        this.log.debug(netLogger.start(event));
        AuthorizationDAO dao = new AuthorizationDAO(this.dbname);
        dao.create(attributeValue, resourceName, permissionName,
                                   constraintName, constraintValue);
        this.log.debug(netLogger.end(event));
    }

    /** Lists authorizations.
     *
     */
    public List<Authorization> listAuths(String attributeValue)
            throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "listAuths";
        this.log.debug(netLogger.start(event));
        AuthorizationDAO authorizationDAO = new AuthorizationDAO(this.dbname);
        List<Authorization> authorizations = new ArrayList<Authorization>();
        if (attributeValue != null) {
            authorizations = authorizationDAO.listAuthByAttr(attributeValue);
        } else {
            authorizations = authorizationDAO.orderedList();
        }
        this.log.debug(netLogger.end(event));
        return authorizations;
    }

    /** Modifies an authorization.
     *
     * @param oldAttributeValue string with old attribute value
     * @param oldResourceName string with old resource name
     * @param oldPermissionName string with old permission name
     * @param oldConstraintName string with old constraint name
     * @param attributeValue string with attribute value
     * @param resourceName string with resource name
     * @param permissionName string with permission name
     * @param constraintName string with constraint name
     * @param constraintValue string with constraint value
     */
    public void modifyAuthorization(String oldAttributeValue,
                String oldResourceName, String oldPermissionName,
                String oldConstraintName,
                String attributeValue, String resourceName,
                String permissionName, String constraintName,
                String constraintValue)
            throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "modifyAuthorization";
        this.log.debug(netLogger.start(event));
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        Authorization auth = authDAO.query(oldAttributeValue, oldResourceName,
                                          oldPermissionName, oldConstraintName);
        authDAO.update(auth, attributeValue, resourceName, permissionName,
                       constraintName, constraintValue);
        this.log.debug(netLogger.end(event));
    }

    /** Removes an authorization.
     *
     * @param attributeValue string with attribute value
     * @param resourceName string with resource name
     * @param permissionName string with permission name
     * @param constraintName string with constraint name
     */
    public void removeAuthorization(String attributeValue, String resourceName,
                                   String permissionName, String constraintName)
            throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "removeAuthorization";
        this.log.debug(netLogger.start(event));
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        authDAO.remove(attributeValue, resourceName, permissionName,
                       constraintName);
        this.log.debug(netLogger.end(event));
    }

    /** Creates an attribute.
     *
     * @param attribute an attribute instance containing attribute parameters
     */
    public void createAttribute(Attribute attribute)
            throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "createAttribute";
        this.log.debug(netLogger.start(event));
        AttributeDAO dao = new AttributeDAO(this.dbname);
        if (attribute == null) {
            throw new AuthZException("Invalid parameter: No attribute set");
        }
        Attribute oldAttribute = dao.queryByParam("value", attribute.getValue());
        if (oldAttribute != null) {
            this.log.info ("attribute with value: " + attribute.getValue() + " exists");
            throw new AuthZException("attribute with value: " + attribute.getValue() + " exists");
        }
        dao.create(attribute);
        this.log.debug(netLogger.end(event));
    }

    /** Lists attributes.
     *
     */
    public List<Attribute> listAttributes() throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "listAttributes";
        this.log.debug(netLogger.start(event));
        AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
        List<Attribute> attributes = attributeDAO.list();
        this.log.debug(netLogger.end(event));
        return attributes;
    }

    /** Modifies an attribute.
     *
     * @param attribute an attribute instance containing attribute parameters
     */
    public void modifyAttribute(Attribute oldAttribute, Attribute newAttribute)
            throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "modifyAttribute";
        this.log.debug(netLogger.start(event,oldAttribute.getValue()));
        AttributeDAO dao = new AttributeDAO(this.dbname);
        Attribute dbAttribute = dao.queryByParam("value",
                                                 oldAttribute.getValue());
        if (dbAttribute == null) {
            throw new AuthZException("Attribute " + oldAttribute.getValue() +
                                     " does not exist to be modified");
        }
        dbAttribute.setAttrId(newAttribute.getAttrId());
        dbAttribute.setValue(newAttribute.getValue());
        dbAttribute.setDescription(newAttribute.getDescription());
        dao.update(dbAttribute);
        this.log.debug(netLogger.end(event));
    }

    /** Removes an attribute.
     *
     * @param attribute string with attribute value
     */
    public void removeAttribute(String attributeValue)
            throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "removeAttribute";
        this.log.debug(netLogger.start(event,attributeValue));
        AttributeDAO dao = new AttributeDAO(this.dbname);
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        Attribute attribute = dao.queryByParam("value", attributeValue);
        if (attribute == null) {
            throw new AuthZException("Attribute " +
                    attributeValue + " does not exist to be deleted");
        }
        List<Authorization> auths = authDAO.listAuthByAttr(attributeValue);
        StringBuilder sb = new StringBuilder();
        if (auths.size() != 0) {
            sb.append(attributeValue + " has " + auths.size() +
                      " associated authorizations.  Cannot delete.");
            throw new AuthZException(sb.toString());
        }
        dao.remove(attribute);
        this.log.debug(netLogger.end(event));
    }

    /** Lists resources.
     *
     */
    public List<Resource> listResources() throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "listResources";
        this.log.debug(netLogger.start(event));
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        List<Resource> resources = resourceDAO.list();
        this.log.debug(netLogger.end(event));
        return resources;
    }

    /** Lists permissions.
     *
     */
    public List<Permission> listPermissions() throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "listPermissions";
        this.log.debug(netLogger.start(event));
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        List<Permission> permissions = permissionDAO.list();
        this.log.debug(netLogger.end(event));
        return permissions;
    }

    /** Lists constraints.
     *
     */
    public List<Constraint> listConstraints() throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "listConstraints";
        this.log.debug(netLogger.start(event));
        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        List<Constraint> constraints = constraintDAO.list();
        this.log.debug(netLogger.end(event));
        return constraints;
    }

    /** Lists rpcs.
     *
     */
    public List<Rpc> listRpcs() throws AuthZException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "listRpcs";
        this.log.debug(netLogger.start(event));
        RpcDAO rpcDAO = new RpcDAO(this.dbname);
        List<Rpc> rpcs = rpcDAO.list();
        this.log.debug(netLogger.end(event));
        return rpcs;
    }
}
