package net.es.oscars.authN.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Iterator;


import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.authN.beans.Institution;
import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.beans.UserAttribute;
import net.es.oscars.authN.dao.AttributeDAO;
import net.es.oscars.authN.dao.InstitutionDAO;
import net.es.oscars.authN.dao.UserAttributeDAO;
import net.es.oscars.authN.dao.UserDAO;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;

import org.apache.log4j.Logger;

import oasis.names.tc.saml._2_0.assertion.AttributeType;


/**
 * PolicyManager handles all authN database related method calls.
 *
 * @author David Robertson, Mary Thompson, Jason Lee, Evangelos Chaniotakis
 */
public class PolicyManager {
    private Logger log;
    private String dbname;
    private String salt;
    private AuthNUtils authNUtils;

    public PolicyManager(String dbname, String salt) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        this.salt = salt;
        this.authNUtils = new AuthNUtils(dbname);
    }

    /**
     * Sets current session cookie hash in the user's users table entry
     *
     * @param userName a string with the user's login name
     * @param sessionName a string with session name
     */

    public void setSession(String userName, String sessionName)
            throws AuthNException {

        User user = null;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "setSession";
        
        this.log.debug(netLogger.start(event,"for user " + userName));
        UserDAO userDAO = new UserDAO(this.dbname);
        user = userDAO.query(userName);
        if (user == null) {
            throw new AuthNException(
                    "setSession: User not registered " + userName + ".");
        }
        user.setCookieHash(sessionName);
        userDAO.update(user);
        this.log.debug(netLogger.end(event));
    }

    /**
     * Checks session cookie for validity and returns attributes.
     *
     * @param userName string with user's login name
     * @param sessionName string with session cookie value
     * @return attributes list of user attributes
     */
    public List<AttributeType>
        validSession(String userName, String sessionName)
            throws AuthNException {

        UserDAO userDAO = new UserDAO(this.dbname);
        User user = userDAO.query(userName);
        if (user == null) {
            throw new AuthNException(
                    "User not registered " + userName + ".");
        }
        // duplicate user lookup
        boolean valid = userDAO.validSession(userName, sessionName);
        if (!valid) {
            throw new AuthNException("Your login session has an error. " +
                "There is a problem with the login for user " + userName +
                ".  Please try logging in again");
        }
        // gets attributes including loginId and institution.
        List<AttributeType> attributes =
            this.authNUtils.getAttributesForUser(user.getLogin());
        return attributes;
    }

    /**
     * Returns attributes for user.
     *
     * @param userName string with user's login name
     * @return attributes list of user attributes
     */
    public List<AttributeType> queryUserAttrs(String userName)
            throws AuthNException {

        UserDAO userDAO = new UserDAO(this.dbname);
        User user = userDAO.query(userName);
        if (user == null) {
            throw new AuthNException(
                    "User not registered " + userName + ".");
        }
        List<Attribute> attributes =
            this.getAttributesForUser(user.getLogin());
        List<AttributeType> attrTypes = new ArrayList<AttributeType>();
        AttributeType at = null;
        for (Attribute attr : attributes) {
            at = new AttributeType();
            at.setName(attr.getAttrId()); // e.g. role, privilege ...
            at.getAttributeValue().add(attr.getValue());// e.g. OSCARS_Engineer, setPathElements
            attrTypes.add(at);
        } 
        return attrTypes;
    }

    /** Creates a system user.
     *
     * @param user a user instance containing user parameters
     * @param institutionName string with name of institution
     * @param roles a list of attributes for the user
     */
    public void createUser(User user, String institutionName,
                           List<String> roles)
            throws AuthNException {
/* TODO change roles to attrDetails where description should be nillable */
        User currentUser = null;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "createUser";
        
        this.log.debug(netLogger.start(event,  user.getLogin()));
        String userName = user.getLogin();

        UserDAO userDAO = new UserDAO(this.dbname);
        currentUser = userDAO.query(userName);
        if (currentUser != null) {
            throw new AuthNException("User " + userName + " already exists.");
        }
        if (user.getCertSubject() != null && !user.getCertSubject().equals("")) {
            currentUser = userDAO.fromDN(user.getCertSubject());
            if (currentUser != null){
                throw new AuthNException("User with DN " + user.getCertSubject() + " already exists." );
            }
        }
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        Institution inst = institutionDAO.queryByParam("name", institutionName);
        if (inst == null) {
            throw new AuthNException("Institution "+institutionName+" not found!");
        }
        user.setInstitution(inst);
        // encrypt the password before persisting it to the database
        String encryptedPwd = Jcrypt.crypt(this.salt, user.getPassword());
        user.setPassword(encryptedPwd);
        userDAO.create(user);
        // add any attributes for this user to the UserAttributes table
        List<String> unknownAttrs = new ArrayList<String>();
        if (roles != null) {
            UserAttributeDAO uaDAO = new UserAttributeDAO(this.dbname);
            AttributeDAO attrDAO = new AttributeDAO(this.dbname);
            for (String attrName: roles) {
                UserAttribute ua = new UserAttribute();
                ua.setUser(user);
                Attribute attr = attrDAO.queryByParam("value", attrName);
                if (attr != null) {
                    ua.setAttribute(attr);
                    uaDAO.create(ua);
                } else {
                    unknownAttrs.add(attrName);
                }
            }
            if (!unknownAttrs.isEmpty()) {
                throw new AuthNException("The following attributes are not recognized: " +
                                        unknownAttrs.toString());
            }
        }
        this.log.debug(netLogger.end(event));
    }

    /**
     * Finds a system user based on their login name.
     *
     * @param userName a user's login name
     * @return user the corresponding user, if one exists
     */
    public User queryUser(String userName) {
        User user = null;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "queryUser";
        
        this.log.debug(netLogger.start(event, userName));
        UserDAO userDAO = new UserDAO(this.dbname);
        user = userDAO.query(userName);
        this.log.debug(netLogger.end(event));
        return user;
    }

    /**
     * Return list of users.
     *
     * @param attributeValue if non-null, return all users with a given attribute
     * @param institutionName return all users associated with a given institution
     *
     * @return either list of all users, or list given a constraint
     */
    public List<User> listUsers(String attributeValue, String institutionName)
            throws AuthNException {
/* TODO change attributeValue to attributeId and attributeValue  or attrDetails*/
        List<User> users = null;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "listUsers";
        
        this.log.debug(netLogger.start(event));
        if (attributeValue != null) {
            UserAttributeDAO dao = new UserAttributeDAO(this.dbname);
            users = dao.getUsersByAttribute(attributeValue);
        } else if (institutionName != null) {
            // TODO implement this
        } else {
            UserDAO userDAO = new UserDAO(this.dbname);
            users = userDAO.list();
        }
        this.log.debug(netLogger.end(event));
        return users;
    }

    /**
     * Removes a user and all their attributes
     *
     * @param userName a string with the user's login name
     */
    public void removeUser(String userName) throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "removeUser";
        
        this.log.debug(netLogger.start(event,  userName));
        UserDAO userDAO = new UserDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);

        User user = userDAO.query(userName); // check to make sure user exists
        if (user == null) {
            throw new AuthNException("Cannot remove user " + userName +
                                   ". The user does not exist.");
        }
        int userId = user.getId();
        userAttrDAO.removeAllAttributes(userId);
        userDAO.remove(user);
        this.log.debug(netLogger.end(event));
    }

    /**
     * Updates a user's profile in the database.
     *
     * @param modifiedUser a transient user instance with modified field(s).
     * @param newPassword - if true the password in user is new and needs to be
     *      encrypted; if false the password is the current, already encrypted
     *      value.
     */
    public void modifyUser(User modifiedUser, List<String> curRoles,
                           List<String> newRoles, String institutionName,
                           Boolean newPassword)
            throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "modifyUser";
        
        this.log.debug(netLogger.start(event, modifiedUser.getLogin()));
        UserDAO userDAO = new UserDAO(this.dbname);
        String userName = modifiedUser.getLogin();

        // check whether this person is in the database
        User user = userDAO.query(userName);
        if (user == null) {
            throw new AuthNException("No such user " + userName + ".");
        }
        // make sure institution is set properly
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        Institution modifiedInst = institutionDAO.queryByParam(
                                       "name", institutionName);
        if (modifiedInst == null) {
            throw new AuthNException("Institution " + institutionName +
                                   " not found!");
        }
        // check that any modification to the subjectName does not duplicate another user
        String modCertSub = modifiedUser.getCertSubject();
        if (modCertSub != null && !modCertSub.equals("")) {
            if ((user.getCertSubject() == null) || (!user.getCertSubject().equals(modCertSub))) {
                User tmpUser = userDAO.fromDN(modCertSub);
                if (tmpUser != null) {
                    throw new AuthNException("User with DN " + user.getCertSubject() + " already exists.");
                }
            }
        }
        user.setInstitution(modifiedInst);
        user.setCertIssuer(modifiedUser.getCertIssuer());
        user.setCertSubject(modifiedUser.getCertSubject());
        user.setLastName(modifiedUser.getLastName());
        user.setFirstName(modifiedUser.getFirstName());
        user.setEmailPrimary(modifiedUser.getEmailPrimary());
        user.setPhonePrimary(modifiedUser.getPhonePrimary());
        user.setDescription(modifiedUser.getDescription());
        user.setEmailSecondary(modifiedUser.getEmailSecondary());
        user.setPhoneSecondary(modifiedUser.getPhoneSecondary());
        // make sure password is set properly
        if (newPassword.booleanValue()) {
            // encrypt user's new password before persisting
            String encryptedPwd = Jcrypt.crypt(this.salt, modifiedUser.getPassword());
            user.setPassword(encryptedPwd);
        }
        // persist to the database
        userDAO.update(user);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        for (String newRoleItem: newRoles) {
            if (!curRoles.contains(newRoleItem)) {
                this.log.debug(netLogger.getMsg(event,"adding user attribute "+ newRoleItem +
                              " for user " + user.getLogin()));
                this.addUserAttribute(newRoleItem, user);
            }
        }
        for (String curRoleItem: curRoles){
           if (!newRoles.contains(curRoleItem)) {
                this.log.debug(netLogger.getMsg(event,"removing user attribute " + curRoleItem));
                userAttrDAO.remove(user.getLogin(), curRoleItem);
            }
        }
        this.log.debug(netLogger.end(event));
    }

    /**
     * Returns the institution of the user
     *
     * @param login String login name of the user
     *
     * @return String name of the institution of the user, null if user not found
     */
    public String getUserInstitution (String login){
        UserDAO userDAO = new UserDAO(this.dbname);
        User user = userDAO.query(login);
        if (user == null) {
            return null;
        }
        return user.getInstitution().getName();
    }

    /**
     * Retrieves all institutions that current users could belong to.
     *
     * @return a list of institutions
     */
    public List<Institution> listInstitutions() {
        List<Institution> institutions = null;

        //this.log.debug("listInstitutions.start");
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        institutions = institutionDAO.list();
        //this.log.debug("listInstitutions.finish");
        return institutions;
    }

    /**
     * Create institution.
     *
     * @param institutionName string with the new institution's name
     */
    public void createInstitution(String institutionName)
            throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "createInstitution";
        
        this.log.debug(netLogger.start(event,  institutionName));
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        Institution oldInstitution = institutionDAO.queryByParam("name", institutionName);
        if (oldInstitution != null) {
            this.log.info(netLogger.error( event, ErrSev.MINOR, "duplicate institution: " + institutionName));
            throw new AuthNException("institution already exists: " + institutionName);
        }
        Institution institution = new Institution();
        institution.setName(institutionName);
        institutionDAO.create(institution);
        this.log.debug(netLogger.end(event));
    }

    /**
     * Modifies institution name.
     *
     * @param oldName string with the institution's old name
     * @param newName string with the institution's new name
     */
    public void modifyInstitution(String oldName, String newName)
            throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "modifyInstitution";
        
        this.log.debug(netLogger.start(event,oldName));
        
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        Institution institution = institutionDAO.queryByParam("name", oldName);
        if (institution == null) {
            throw new AuthNException("Institution not found");
        }
        institution.setName(newName);
        institutionDAO.update(institution);
        this.log.debug(netLogger.end(event, "remamed to " + newName));
    }

    /**
     * Removes an institution if no users currently belong to it.
     *
     * @param institutionName a string with the institution's name
     */
    public void removeInstitution(String institutionName)
            throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "removeInstitution";
        
        this.log.debug(netLogger.start(event, institutionName));
        UserDAO userDAO = new UserDAO(this.dbname);
        InstitutionDAO instDAO = new InstitutionDAO(this.dbname);

        Institution inst = instDAO.queryByParam("name", institutionName);
        if (inst == null) {
            throw new AuthNException("Cannot remove institution " +
                                   institutionName +
                                   ". The institution does not exist.");
        }
        Set<User> users = (Set<User>) inst.getUsers();
        StringBuilder sb = new StringBuilder();
        if (users.size() != 0) {
            sb.append(institutionName + " has existing users: ");
            Iterator<User> iter = users.iterator();
            while (iter.hasNext()) {
                User user = (User) iter.next();
                sb.append(user.getLogin() + " ");
            }
            throw new AuthNException(sb.toString());
        }
        instDAO.remove(inst);
        this.log.debug(netLogger.end(event));
    }

    /** Creates an attribute.
     *
     * @param attribute an attribute instance containing attribute parameters
     */

    /* TODO fix and query by attrId as well */
    public void createAttribute(Attribute attribute)
            throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "createAttribute";
        this.log.debug(netLogger.start(event, attribute.getValue()));
        AttributeDAO dao = new AttributeDAO(this.dbname);
        if (attribute == null) {
            throw new AuthNException("Invalid parameter: No attribute set");
        }
        Attribute oldAttribute = dao.queryByParam("value", attribute.getValue());
        if (oldAttribute != null) {
            this.log.info (netLogger.error(event, ErrSev.MINOR, "attribute with value: " + 
                                           attribute.getValue() + " exists"));
            throw new AuthNException("attribute with value: " + attribute.getValue() + " exists");
        }
        dao.create(attribute);
        this.log.debug(netLogger.end(event));
    }

    /** Lists attributes.
     *
     * @param param if non-null, order query by this parameter
     * @param value value of parameter (currently just a user name)
     */
    public List<Attribute> listAttributes(String param, String value)
            throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "listAttributes";
        this.log.debug(netLogger.start(event));
        List<Attribute> attributes;
        if (param == null || param.equals("plain")) {
            AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
            attributes = attributeDAO.list();
        } else if (param.equals("username")) {
            if (value == null) {
                throw new AuthNException("Invalid parameter to listAttributes: No username");
            }
            attributes = this.getAttributesForUser(value);
        } else {
            throw new AuthNException("Invalid parameter to listAttributes: unknown parameter: " + param);
        }
        this.log.debug(netLogger.end(event));
        return attributes;
    }

    /** Modifies an attribute.
     *
     * @param attribute an attribute instance containing attribute parameters
     */
    public void modifyAttribute(Attribute oldAttribute, Attribute newAttribute)
            throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "modifyAttribute";
        
        this.log.debug(netLogger.start(event,  oldAttribute.getValue()));
        AttributeDAO dao = new AttributeDAO(this.dbname);
        Attribute dbAttribute = dao.queryByParam("value",
                                                 oldAttribute.getValue());
        if (dbAttribute == null) {
            throw new AuthNException("Attribute " + oldAttribute.getValue() +
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
            throws AuthNException {

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "removeAttribute";
        
        this.log.debug(netLogger.start(event, attributeValue));
        AttributeDAO dao = new AttributeDAO(this.dbname);
        UserAttributeDAO userAttributeDAO =
            new UserAttributeDAO(this.dbname);
        Attribute attribute = dao.queryByParam("value", attributeValue);
        if (attribute == null) {
            throw new AuthNException("Attribute " +
                    attributeValue + " does not exist to be deleted");
        }
        List<User> users =
            userAttributeDAO.getUsersByAttribute(attributeValue);
        StringBuilder sb = new StringBuilder();
        if (users.size() != 0) {
            sb.append(attributeValue + " has existing users: ");
            for (User user: users) {
                sb.append(user.getLogin() + " ");
            }
            sb.append(".  There are " + users.size() +
                      " existing users with this attribute.");
            throw new AuthNException(sb.toString());
        }
        dao.remove(attribute);
        this.log.debug(netLogger.end(event));
    }

    /**
     * Gets all the attributes associated with a user.  These are in the format
     *    stored in the database.
     *
     * @param userName string with user's login name
     * @return list of attributes for user
     */
    public List<Attribute> getAttributesForUser(String targetUser) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        UserDAO userDAO = new UserDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        // sanity check
        User user = userDAO.query(targetUser);
        if (user == null) {
            return attributes;
        }
        List<UserAttribute> userAttributes = userAttrDAO.getAttributesByUser(user.getId());
        if (userAttributes == null) {
            return attributes;
        }
        for (UserAttribute ua : userAttributes) {
            attributes.add(ua.getAttribute());
        }
        return attributes;
    }

    private void addUserAttribute(String attrValue, User user) {
// TODO use attrValue,attrId
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        UserAttribute userAttr = new UserAttribute();
        Attribute attr = attrDAO.queryByParam("value", attrValue);
        userAttr.setAttribute(attr);
        userAttr.setUser(user);
        userAttrDAO.create(userAttr);
    }
}
