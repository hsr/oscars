package net.es.oscars.authZ.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Bean associated with schema for the authZ.authorizations table.
 */
public class Authorization extends HibernateBean implements Serializable {
    private static final long serialVersionUID = 4149;

    /** nullable persistent field */
    private String context;

    /** nullable persistent field */
    private Long updateTime;

    /** nullable persistent field */
    private String constraintValue;

    /** associated object */
    private Constraint constraint;

    /** associated object */
    private Permission permission;

    /** associated object */
    private Resource resource;

    /** associated object */
    private Attribute attribute;


    /** default constructor */
    public Authorization() { }

    /**
     * @return context A String corresponding to a currently unused field
     */
    public String getContext() { return this.context; }

    /**
     * @param context A String corresponding to a currently unused field
     */
    public void setContext(String context) { this.context = context; }


    /**
     * @return updateTime A Long instance with the last update time
     */
    public Long getUpdateTime() { return this.updateTime; }

    /**
     * @param updateTime A Long instance with the last update time
     */
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * @return constraintValue A String corresponding to a constraintValue for this authorization
     */
    public String getConstraintValue() { return this.constraintValue; }

    /**
     * @param constraintValue A String corresponding to a constraint for this authorization
     */
    public void setConstraintValue(String constraintValue ) { this.constraintValue = constraintValue; }

    /**
     * @return the constraint
     */
    public Constraint getConstraint() {
        return constraint;
    }

    /**
     * @param constraint the constraint to set
     */
    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    /**
     * @return the permission
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * @param permission the permission to set
     */
    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    /**
     * @return the resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * @return the attribute
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attribute", getAttribute().getValue())
            .append("resource", getResource().getName())
            .append("permission", getPermission().getName())
            .append("constraint", getConstraint().getName())
            .append("constraintValue", getConstraintValue())
            .toString();
    }
}
