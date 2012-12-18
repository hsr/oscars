package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import java.util.*;
import java.text.DateFormat;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;
import net.es.oscars.resourceManager.common.RMUtils;
import net.es.oscars.resourceManager.common.RMException;

/**
 * StdConstraint is the Hibernate bean for the rm.stdConstraints table.
 */
public class StdConstraint extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

    /** persistent field */
    private String constraintType = ConstraintType.USER;
    
    /** persistent field */
    private Long startTime;

    /** persistent field */
    private Long endTime;

    /** persistent field */
    private Long bandwidth;

    /** association (*/
    private Path path;
   // private Map<String,Path> pathMap = new HashMap<String,Path>();

    /** default constructor */
    public StdConstraint() { }

    /**
     * @return constraint type (currently local or interdomain)
     */
    public String getConstraintType() { return this.constraintType; }

    /**
     * @param constraintType constraint type (currently user or reserved)
     */
    public void setConstraintType(String conType) throws RMException {
        if (!ConstraintType.isStandard(conType)) {
            throw new RMException("Invalid ConstraintType: " + conType);
        }
        this.constraintType = conType;
    }
    /**
     * @return startTime A Long with the reservation start time (Unix time)
     */
    public Long getStartTime() { return this.startTime; }

    /**
     * @param startTime A Long with the reservation start time
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return endTime A Long with the reservation end time
     */
    public Long getEndTime() { return this.endTime; }

    /**
     * @param endTime A Long with the reservation end time
     */
    public void setEndTime(Long endTime) { this.endTime = endTime; }

    /**
     * @return bandwidth A Long with the reservation's requested bandwidth
     */
    public Long getBandwidth() { return this.bandwidth; }

    /**
     * @param bandwidth A Long with the reservation's requested bandwidth
     */
    public void setBandwidth(Long bandwidth) { this.bandwidth = bandwidth; }
    
    public Path getPath() {
        return this.path;
    }
    public void setPath(Path path){
        this.path = path;
    }
/*
    public Map<String,Path> getPathMap() {
        return this.pathMap;
    }

    public void setPathMap(Map<String, Path>  pathMap) {
        this.pathMap = pathMap;
    }
    
    public Path getPath(String pathType) throws RMException {
        if (!PathType.isValid(pathType)) {
            throw new RMException("Invalid pathType: "+pathType);
        }
        return this.pathMap.get(pathType);
    }
    public Path getPath() throws RMException {
        return this.pathMap.get(PathType.PRIMARY);
    }
    public void setPath(Path path) throws RMException {
        String pathType = path.getPathType();
        if (pathType == null) {
            path.setPathType(PathType.LOOSE);
        }
        else if (!PathType.isValid(pathType)) {
            throw new RMException("Invalid pathType: " + pathType);
        }
        this.pathMap.put(PathType.PRIMARY, path);
    }
*/
    public String toString(String dbname)  throws RMException {
        StringBuilder sb = new StringBuilder();

        // this may be called from methods where the constraint has
        // not been completely set up, so more null checks are
        // necessary here
        if (this.getConstraintType() != null) {
            sb.append("\nconstraintType: " + this.getConstraintType() + "\n");
        }
        
        Long tm = this.getStartTime() * 1000L;
        DateFormat df = DateFormat.getInstance();
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (tm != null) {
            Date date = new Date(tm);
            sb.append("start time: " + df.format(date) + " UTC\n");
        }
        tm = this.getEndTime() * 1000L;
        if (tm != null) {
            Date date = new Date(tm);
            sb.append("end time: " + df.format(date) + " UTC\n");
        }
        if (this.getBandwidth() != null) {
            sb.append("bandwidth: " + this.getBandwidth() + "\n");
        }

        Path path = this.getPath();
        if (path == null) {
            return sb.toString();
        }
        sb.append(RMUtils.pathDataToString(path));
        /* Only one path now. Do we need to sort out local from interdomain hops?  
        } else {
            Path path = this.getPath(PathType.LOCAL);
            if (path == null) {
                return sb.toString();
            }
            sb.append("intradomain hops: \n\n");
            sb.append(RMUtils.pathToString(path, false));
            sb.append(RMUtils.pathDataToString(path));
            path = this.getPath(PathType.INTERDOMAIN);
            sb.append("\ninterdomain hops: \n\n");
            sb.append(RMUtils.pathToString(path, true));
        } */
        sb.append("\n");
        return sb.toString();
    }
}
