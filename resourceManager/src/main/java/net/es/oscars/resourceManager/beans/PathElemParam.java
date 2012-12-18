package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import org.hibernate.Hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;


/**
 * Hibernate bean mapping the rm.pathElemParams table.
 */
public class PathElemParam extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String swcap;

    /** persistent field */
    private String type;

    /** persistent field */
    private String value;

    /** default constructor */
    public PathElemParam() {}

    /**
     * @return string describing the switching capability
     */
    public String getSwcap() {
        return this.swcap;
    }

    /**
     * @param swap string describing the switching capability
     */
    public void setSwcap(String swcap) {
        this.swcap = swcap;
    }

    /**
     * @return a string with the type of path elem param
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type a string with the type of path elem param
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return a string with path elem param value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param value a string with path elem param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        PathElemParam castOther = (PathElemParam) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getSwcap(), castOther.getSwcap())
                .append(this.getType(), castOther.getType())
                .append(this.getValue(), castOther.getValue())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}
