package net.es.oscars.authZ.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Resource is the Hibernate bean associated with the schema for the 
 * authZ.resources table.
 */
public class Resource extends HibernateBean implements Serializable {
    private static final long serialVersionUID = 4149;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private Long updateTime;

    /** default constructor */
    public Resource() { }

    /**
     * @return name A String with the name of this resource
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A String with the name of this resource
     */ 
    public void setName(String name) { this.name = name; }


    /**
     * @return description A String with the description of this resource
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description A String with the description of this resource
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return updateTime A Long with this row's last update time
     */ 
    public Long getUpdateTime() { return this.updateTime; }

    /**
     * @param updateTime A Long with this row's last update time
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
