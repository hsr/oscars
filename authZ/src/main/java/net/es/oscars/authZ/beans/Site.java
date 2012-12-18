package net.es.oscars.authZ.beans;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Site is the Hibernate bean for the authZ.sites table.
 */
public class Site extends HibernateBean implements Serializable {
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String institutionName;

    /** persistent field */
    private String domainTopologyId;

    /** default constructor */
    public Site() { }

    /**
     * @return a string with the institution name
     */
    public String getInstitutionName() { return this.institutionName; }

    /**
     * @param institutionName a string with the institution name
     */
    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
    
    /**
     * @return a string with the domain name
     */
    public String getDomainTopologyId() { return this.domainTopologyId; }

    /**
     * @param domainName a string with the domain name
     */
    public void setDomainTopologyId(String domainTopologyId) {
        this.domainTopologyId = domainTopologyId;
    }
    
    //
    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Site castOther = (Site) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getDomainTopologyId(), castOther.getDomainTopologyId())
                .append(this.getInstitutionName(), castOther.getInstitutionName())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
