package net.es.oscars.resourceManager.beans;

import java.util.*;
import java.io.Serializable;

import org.hibernate.Hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.database.hibernate.HibernateBean;

/**
 * PathElem is the Hibernate bean for the rm.pathElems table.
 */
public class PathElem extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private int seqNumber;

    /** nullable persistent field */
    private String urn;

    private Set<PathElemParam> pathElemParams = new HashSet<PathElemParam>();

    private HashMap<String, PathElemParam> pathElemParamMap = new HashMap<String, PathElemParam>();

    /** default constructor */
    public PathElem() { }

    public void initializePathElemParams() {
        if (!this.pathElemParamMap.isEmpty()) {
            return;
        }
        Iterator<PathElemParam> pathElemParamsIterator = this.pathElemParams.iterator();
        while (pathElemParamsIterator.hasNext()) {
            PathElemParam param = (PathElemParam) pathElemParamsIterator.next();
            String key = param.getSwcap() + param.getType();
            this.pathElemParamMap.put(key, param);
        }
    }
    
    /**
     * @return seqNumber int with this path element's position in list
     */
    public int getSeqNumber() {
        return this.seqNumber;
    }

    /**
     * @param num not actually settable
     */
    public void setSeqNumber(int num) {
        this.seqNumber = num;
    }


    /**
     * @return urn a string with this path element's associated urn
     */
    public String getUrn() { return this.urn; }

    /**
     * @param urn string with path element's associated urn
     */
    public void setUrn(String urn) {
        this.urn = urn;
    }

    /**
     * @return set of path elem parameters
     */
    public Set<PathElemParam> getPathElemParams() { return this.pathElemParams; }

    /**
     * @param swcap Generate a HashMap only with parameters of this swcap
     * @return HashMap keyed by type for each parameter with given swcap type
     * @throws RMException
     */
    public PathElemParam getPathElemParam(String swcap, String type) throws RMException {
        this.initializePathElemParams();
        if(!PathElemParamSwcap.isValid(swcap)){
            throw new RMException("Invalid PathElemParam swcap '" + swcap + "'");
        }else if(!PathElemParamType.isValid(type)){
            throw new RMException("Invalid PathElemParam type '" + type + "'");
        }

        if(!pathElemParamMap.containsKey(swcap+type)){
            return null;
        }

        return pathElemParamMap.get(swcap+type);
    }

    /**
     * @param pathElemParams set of path elem parameters
     */
    public void setPathElemParams(Set <PathElemParam>pathElemParams) {
        this.pathElemParams = pathElemParams;
    }

    public boolean addPathElemParam(PathElemParam pathElemParam) {
        if (this.pathElemParams.add(pathElemParam)) {
            pathElemParamMap.put(pathElemParam.getSwcap()+pathElemParam.getType(), pathElemParam);
            return true;
        } else {
            return false;
        }

    }

    public void removePathElemParam(PathElemParam pathElemParam) {
        this.pathElemParams.remove(pathElemParam);
        this.pathElemParamMap.remove(pathElemParam);
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        PathElem castOther = (PathElem) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getUrn(), castOther.getUrn())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    /**
     * Copies a pathElem; will not copy id and seqNumber.
     *
     * @param pe the pathElem to copy
     * @return the copy
     * @throws RMException 
     */
    public static PathElem copyPathElem(PathElem pathElem) throws RMException {
        PathElem copy = new PathElem();
        copy.setUrn(pathElem.getUrn());
        PathElem.copyPathElemParams(copy, pathElem, null);
        return copy;
    }

    /** Creates a copies of the PathElemParams of this object that match the swcap given
     *
     *@param dest the location to get the copied params
     * @param src the PathElem with the params to copy
     * @param swcap the type of PathElem params to copy. null if all params should be copied.
     * @throws RMException 
     */
    public static void copyPathElemParams(PathElem dest, PathElem src, String swcap) throws RMException {
        Iterator<PathElemParam> paramIterator = src.getPathElemParams().iterator();
        while(paramIterator.hasNext()){
            PathElemParam param = (PathElemParam) paramIterator.next();
            //swcap == null means copy all
            if(swcap != null && !swcap.equals(param.getSwcap())){
                continue;
            }
            PathElemParam paramCopy = dest.getPathElemParam(param.getSwcap(), param.getType());
            if(paramCopy == null){
                paramCopy = new PathElemParam();
                paramCopy.setSwcap(param.getSwcap());
                paramCopy.setType(param.getType());
                dest.addPathElemParam(paramCopy);
            }
            paramCopy.setValue(param.getValue());
        }
    }

}
