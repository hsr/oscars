package net.es.oscars.resourceManager.beans;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Token is the Hibernate bean for the rm.nodeAddresses table.
 */
public class Token extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String value;

    /** persistent field */
    private Reservation reservation;

    /** default constructor */
    public Token() { }

    /**
     * @return token value as a string
     */ 
    public String getValue() { return this.value; }

    /**
     * @param value a string with the token's value
     */ 
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return a Reservation instance (uses association)
     */ 
    public Reservation getReservation() { return this.reservation; }

    /**
     * @param reservation sets the reservation 
     */ 
    public void setReservation(Reservation reservation) { 
        this.reservation = reservation; 
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
