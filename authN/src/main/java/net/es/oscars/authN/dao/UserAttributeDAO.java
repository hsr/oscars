package net.es.oscars.authN.dao;


import java.util.ArrayList;
import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.beans.UserAttribute;
import net.es.oscars.authN.common.AuthNException;

import  org.apache.log4j.Logger;

/** UserAttributeDAO is the data access object for the authN.userAttributes table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */

    public class UserAttributeDAO
    extends GenericHibernateDAO<UserAttribute, Integer> {

    private String dbname;

    /**
     * Constructor
     *
     * @param dbname
     */
    public UserAttributeDAO(String dbname) {
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    /**
     * Get the list of all the attributes for a user
     *
     * @param userId Index into the users table
     * @return  List of attributes
     */
    public List<UserAttribute> getAttributesByUser(int userId) {

        String hsql = "from UserAttribute "  +
                      "where userId = :userId" ;
        List<UserAttribute> userAttrs = this.getSession().createQuery(hsql)
                          .setInteger("userId", userId)
                          .list();
        return userAttrs;

 }
    /**
     * Get the list of all the uses who have a given attribute
     *
     * @param attrValue String attribute value
     * @return  List of Users
     */
    public List<User> getUsersByAttribute(String attrValue)
        throws AuthNException {

        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        int attributeId = attrDAO.getIdByValue(attrValue);
        String hsql = "from UserAttribute "  +
                      "where attributeId = :attributeId" ;
        List<UserAttribute> userAttrs = this.getSession().createQuery(hsql)
                          .setInteger("attributeId", attributeId )
                          .list();
        ArrayList<User> users = new ArrayList<User>();
        if (userAttrs == null) {
            return users;
        }
        UserDAO userDAO = new UserDAO(this.dbname);
        for (UserAttribute ua : userAttrs) {
            users.add(userDAO.findById(ua.getUser().getId(), false));
        }
        return users;
 }

    /**
     * Removes all the attributes for a user, used when deleting a user
     *
     * @param userId the id of the user whose attributes are to be removed
     *
     */
    public void removeAllAttributes(int userId) {

        List<UserAttribute> userAttrs = getAttributesByUser(userId);
        if (userAttrs != null) {
            for (UserAttribute ua: userAttrs) {
                super.remove(ua);
            }
        }
    }

     /**
      * Removes a userAttribute, given a user and attribute name.
      * @param login String with the user login name.
      * @param attributeValue String with the attribute value.
      * @return status String with deletion status.
      * @throws AuthNException.
      */
     public String remove(String login, String attributeValue)
                   throws AuthNException {

         String status = null;

         UserDAO userDAO = new UserDAO(this.dbname);
         User user = (User) userDAO.queryByParam("login", login);

         AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
         Attribute attribute = (Attribute) attributeDAO.queryByParam(
                                                        "value", attributeValue);
         if (user != null && attribute != null) {
             String hsql = "from UserAttribute " +
             "where userId = :userId and " +
             "attributeId = :attributeId";
             UserAttribute rp = (UserAttribute) this.getSession().createQuery(hsql)
             .setInteger("userId", user.getId())
             .setInteger("attributeId", attribute.getId())
             .setMaxResults(1)
             .uniqueResult();
             super.remove(rp);
             status = "attribute removed";
         } else {
             status = "user or attribute does not exist";
         }
         return status;
     }

     /**
      * Removes a userAttribute, given a user id and attribute id.
      * @param userId int containing the userId.
      * @param attrId int containing the attribute id.

      */
     public void remove(int userId, int attrId) {

         String hsql = "from UserAttribute " +
                       "where userId = :userId and " +
                       "attributeId = :attributeId";
         UserAttribute rp = (UserAttribute) this.getSession().createQuery(hsql)
                       .setInteger("userId", userId)
                       .setInteger("attributeId", attrId)
                       .setMaxResults(1)
                       .uniqueResult();
         super.remove(rp);
    }
}




