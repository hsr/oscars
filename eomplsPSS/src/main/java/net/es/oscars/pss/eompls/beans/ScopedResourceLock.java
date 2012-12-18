package net.es.oscars.pss.eompls.beans;

import net.es.oscars.database.hibernate.HibernateBean;

import java.io.Serializable;

public class ScopedResourceLock extends HibernateBean implements Serializable {
    private static final long serialVersionUID = 8000;

    private String gri;
    private Integer resource;
    private String scope;
    private String rangeExpr;


    public String getGri() {
        return gri;
    }

    public void setGri(String gri) {
        this.gri = gri;
    }

    public Integer getResource() {
        return resource;
    }

    public void setResource(Integer resource) {
        this.resource = resource;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRangeExpr() {
        return rangeExpr;
    }

    public void setRangeExpr(String rangeExpr) {
        this.rangeExpr = rangeExpr;
    }


    public ScopedResourceLock() {}

    public String toString() {
        return("res: ["+resource+"] scope: ["+scope+"] gri: ["+gri+"] range: ["+rangeExpr+"]");
    }



}
