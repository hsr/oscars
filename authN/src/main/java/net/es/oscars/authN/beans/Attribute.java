package net.es.oscars.authN.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Attribute is the Hibernate bean associated with the authN.attributes
 * table.
 */
public class Attribute extends HibernateBean implements Serializable {
    private static final long serialVersionUID = 4149;

    /** nullable persistent field */
    private String attrId;

    /** persistent field */
    private String value;

    /** persistent field */
    private String description;

    /** default constructor */
    public Attribute() { }

    /**
     * @return value a string with the attribute (non-database) id
     */ 
    public String getAttrId() { return this.attrId; }

    /**
     * @param attrId a string with the attribute id
     */ 
    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    /**
     * @return value a string with the attribute value
     */ 
    public String getValue() { return this.value; }

    /**
     * @param name A string with the attribute value
     */ 
    public void setValue(String value) { this.value = value; }


    /**
     * @return description a string with the attribute description
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description A string with the attribute description
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("value", getValue())
            .toString();
    }
}
