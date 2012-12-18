package net.es.oscars.authZ.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Hibernate bean for the authZ.rpcs table.
 */
public class Rpc extends HibernateBean implements Serializable {
    private static final long serialVersionUID = 4149;

    /** associated object*/
    private Constraint constraint;

    /** associated object*/
    private Permission permission;

    /** associated object*/
    private Resource resource;

    /** default constructor */
    public Rpc() { }

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

    public String toString() {
        return new ToStringBuilder(this)
            .append("resource", getResource().getName())
            .append("permissionId", getPermission().getName())
            .append("constraintId", getConstraint().getName())
            .toString();
    }
}
