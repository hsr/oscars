package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * IdSequence is the Hibernate bean for for the rm.idsequence table.
 */
public class IdSequence extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

    /** persistent field */
    private Integer id;

    /** default constructor */
    public IdSequence() { }

    /**
     * @return the current id of this entry
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id the is to set
     */ 
    public void setId(Integer id) {
        this.id = id;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
