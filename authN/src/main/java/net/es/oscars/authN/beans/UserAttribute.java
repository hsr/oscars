package net.es.oscars.authN.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * UserAttribute is adapted from a Middlegen class automatically generated
 * from the schema for the aaa.attributes table.
 */
public class UserAttribute extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 5025;

    /** associated object*/
    private User user;

    /** associated object*/
    private Attribute attribute;

    /** default constructor */
    public UserAttribute() { }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the attribute
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("user", getUser().getLogin())
            .append("attribute", getAttribute().getValue())
            .toString();
    }
}
