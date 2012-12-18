package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

import org.hibernate.Hibernate;
import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Layer2Data is the Hibernate bean for for the rm.layer2Data table.
 */
public class Layer2Data extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

    /** persistent field */
    private String srcEndpoint;

    /** persistent field */
    private String destEndpoint;



    /** default constructor */
    public Layer2Data() { }


    /**
     * @return srcEndpoint a string with the source endpoint
     */
    public String getSrcEndpoint() { return this.srcEndpoint; }

    /**
     * @param srcEndpoint a string with the source endpoint
     */
    public void setSrcEndpoint(String srcEndpoint) {
        this.srcEndpoint = srcEndpoint;
    }


    /**
     * @return destEndpoint a string with the destination endpoint
     */
    public String getDestEndpoint() { return this.destEndpoint; }

    /**
     * @param destEndpoint a string with the destination endpoint
     */
    public void setDestEndpoint(String destEndpoint) {
        this.destEndpoint = destEndpoint;
    }




    public Layer2Data copy() {
        Layer2Data l2DataCopy = new Layer2Data();
        l2DataCopy.setSrcEndpoint(this.srcEndpoint);
        l2DataCopy.setDestEndpoint(this.destEndpoint);
        return l2DataCopy;
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Layer2Data castOther = (Layer2Data) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getSrcEndpoint(), castOther.getSrcEndpoint())
                .append(this.getDestEndpoint(), castOther.getDestEndpoint())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("srcEndpoint", this.getSrcEndpoint())
            .append("destEndpoint", this.getDestEndpoint())
            .toString();
    }
}
