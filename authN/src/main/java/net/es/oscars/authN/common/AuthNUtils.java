package net.es.oscars.authN.common;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.beans.UserAttribute;
import net.es.oscars.authN.dao.UserAttributeDAO;
import net.es.oscars.authN.dao.UserDAO;
import net.es.oscars.utils.sharedConstants.AuthZConstants;

import org.apache.log4j.Logger;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

/**
 * AuthNUtils handles functionality common to authN and authN policy manager.
 *
 * @author David Robertson, Mary Thompson
 */
public class AuthNUtils {
    private Logger log;
    private String dbname;

    public AuthNUtils(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /**
     * Gets all the attributes associated with a user
     *
     * @param targetUser string with user's login name
     * @return List<String> attributes for user. Always returns loginId and
     *      institution as attributes. 
     */
    public List<AttributeType> getAttributesForUser(String targetUser) {

        UserDAO userDAO = new UserDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        User user = userDAO.query(targetUser);
        if (user == null) {
            return null;
        }
        List<UserAttribute> userAttributes = userAttrDAO.getAttributesByUser(user.getId());

        // For now just have one value per attribute
        List<AttributeType> attributes = new ArrayList<AttributeType>();
        AttributeType at = new AttributeType();
        at.setName(AuthZConstants.LOGIN_ID);
        at.getAttributeValue().add(user.getLogin());
        attributes.add(at);
        at = new AttributeType();
        at.setName(AuthZConstants.INSTITUTION);
        at.getAttributeValue().add(user.getInstitution().getName());
        attributes.add(at);
        for (UserAttribute ua : userAttributes) {
            at = new AttributeType();
            at.setName(ua.getAttribute().getAttrId()); // e.g. role, privilege ...
            at.getAttributeValue().add(ua.getAttribute().getValue());// e.g. OSCARS_Engineer, setPathElements
            attributes.add(at);
        }
        return attributes;
    }
}
