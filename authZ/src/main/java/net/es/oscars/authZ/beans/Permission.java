package net.es.oscars.authZ.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Permission is the Hibernate bean associated with the
 * the schema for the authZ.permissions table.
 */
public class Permission extends HibernateBean implements Serializable {
    private static final long serialVersionUID = 4149;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private Long updateTime;

    /** default constructor */
    public Permission() { }

    /**
     * @return name A String with the permission name
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A String with the permission name
     */ 
    public void setName(String name) { this.name = name; }


    /**
     * @return description A String with the permission description
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description A String with the permission description
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return updateTime A Long instance with the last row update time
     */ 
    public Long getUpdateTime() { return this.updateTime; }

    /**
     * @param updateTime A Long instance with the last row update time
     */ 
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
