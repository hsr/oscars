package net.es.oscars.authN.beans;

import java.util.Set;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

public class Institution extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** persistent field */
    private String name;

    private Set users;
    
    /** default constructor */
    public Institution() { }

    /**
     * @return name A String with the institution name
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A String with the institution name
     */ 
    public void setName(String name) { this.name = name; }

    public void setUsers(Set users) {
        this.users = users;
    }

    public Set getUsers() {
        return this.users;
    }

    public void addUser(User user) {
        user.setInstitution(this);
        this.users.add(user);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
