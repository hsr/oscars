package net.es.oscars.database.hibernate;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/** 
 * This class takes its implementation from a comment on the page:
 * http://www.juixe.com/techknow/index.php/2007/02/01/jakarta-commons-lang-builders/
 */
public class HibernateBean {

    /** identifier field */
    protected Integer id;

    /**
     * @return id An Integer with the reservation's primary key
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id An Integer with the primary key
     */ 
    public void setId(Integer id) { this.id = id; }


    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = getClass();
        if (o == null || thisClass != o.getClass()) {
            return false;
        }
        HibernateBean castOther = (HibernateBean) o;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }
}
