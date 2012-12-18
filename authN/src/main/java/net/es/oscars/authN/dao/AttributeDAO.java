package net.es.oscars.authN.dao;

import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authN.common.AuthNException;
import net.es.oscars.authN.beans.Attribute;

/** AttributeDAO is the data access object for the authN.attributes table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class AttributeDAO extends GenericHibernateDAO<Attribute, Integer> {

    public AttributeDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /**
     * Retrieves a list of attributes by alphabetical order of value.
     * Ordering needed for comparison of attributes from authN and authZ in
     * client.  (TODO: attrId)
     *
     * @return list of attributes
     */
    public List<Attribute> list() {
        String sql = "select * from attributes " +
                     "order by value";
        List<Attribute> attributes =
               (List<Attribute>) this.getSession().createSQLQuery(sql)
                                             .addEntity(Attribute.class)
                                             .list();
        return attributes;
    }

    /**
     * Given an attribute database id, return the attribute value
     *
     * @param id  int with attribute id
     * @return a string with name of attribute
     *
     * This is currently only called by AuthorizationDAO.listAuthByUser
     */
    public String getAttributeValue(int id)  {
        Attribute attr = super.findById(id, false);
        if (attr != null ) {
           return attr.getValue();
        } else {
            return "unknown attribute";
        }
    }

    /**
     * given an attribute value, return the attribute database id
     *
     * @param value string value of the attribute
     * @return an Integer containing the attribute id
     */
    public Integer getIdByValue(String value) throws AuthNException {
        Attribute attr = super.queryByParam("value", value);
        if (attr != null ) {
            return attr.getId();
        } else {
            throw new AuthNException ("No attribute with value "+ value);
        }
    }
}
