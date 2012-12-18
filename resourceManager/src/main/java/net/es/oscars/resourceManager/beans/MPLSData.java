package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.Hibernate;
import net.es.oscars.database.hibernate.HibernateBean;

/**
 * MPLSData is the Hibernate bean for for the rm.mplsData table.
 */
public class MPLSData extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

    /** persistent field */
    private Long burstLimit;

    /** persistent field */
    private String lspClass;

    /** default constructor */
    public MPLSData() { }


    /**
     * @return burstLimit a Long with the burst limit for policing
     */ 
    public Long getBurstLimit() { return this.burstLimit; }

    /**
     * @param burstLimit a Long with the burst limit
     */ 
    public void setBurstLimit(Long burstLimit) {
        this.burstLimit = burstLimit;
    }


    /**
     * @return lspClass a String with the circuit's LSP class
     */ 
    public String getLspClass() { return this.lspClass; }

    /**
     * @param lspClass a String with the circuit's LSP class
     */ 
    public void setLspClass(String lspClass) { this.lspClass = lspClass; }


    public MPLSData copy(){
        MPLSData mplsDataCopy = new MPLSData();
        mplsDataCopy.setBurstLimit(this.burstLimit);    
        mplsDataCopy.setLspClass(this.lspClass);
        return mplsDataCopy;
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        MPLSData castOther = (MPLSData) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getBurstLimit(), castOther.getBurstLimit())
                .append(this.getLspClass(), castOther.getLspClass())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("burstLimit", this.getBurstLimit())
            .append("lspClass", this.getLspClass())
            .toString();
    }
}
