package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import java.util.*;
import java.text.DateFormat;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;
import net.es.oscars.resourceManager.common.RMUtils;
import net.es.oscars.resourceManager.common.RMException;

/**
 * Reservation is the Hibernate bean for for the rm.reservations table.
 */
public class Reservation extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;
    
    /** persistent field */
    private Long startTime;

    /** persistent field */
    private Long endTime;

    /** persistent field */
    private Long createdTime;

    /** persistent field */
    private Long bandwidth;

    /** persistent field */
    private String login;

    /** persistent field */
    private String payloadSender;

    /** persistent field */
    private String status;

    /** persistent field */
    private Integer localStatus;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String statusMessage;

    /** nullable persistent field */
    private String globalReservationId;

    /** persistent field */
    private Token token;

    private Map<String,StdConstraint> constraintMap = new HashMap<String,StdConstraint>();
   // private Set<OptConstraint> optConstraintSet = new HashSet<OptConstraint>();

    private List<OptConstraint> optConstraintList = new ArrayList<OptConstraint>();
    
    private List<ErrorReportData> errorReports = new ArrayList<ErrorReportData>();

    /** default constructor */
    public Reservation() { }

    /**
     * @return startTime A Long with the reservation scheduled start time (Unix time)
     */
    public Long getStartTime() { return this.startTime; }

    /**
     * @param startTime A Long with the reservation scheduled start time
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
  

    /**
     * @return endTime A Long with the reservation end time
     */
    public Long getEndTime() { return this.endTime; }

    /**
     * @param endTime A Long with the reservation scheduled end time
     */
    public void setEndTime(Long endTime) { this.endTime = endTime; }

    /**
     * @return createdTime A Long with the reservation creation time
     */
    public Long getCreatedTime() { return this.createdTime; }

    /**
     * @param createdTime A Long with the reservation creation time
     */
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return bandwidth A Long with the reservation's requested bandwidth
     */
    public Long getBandwidth() { return this.bandwidth; }

    /**
     * @param bandwidth A Long with the reservation's requested bandwidth
     */
    public void setBandwidth(Long bandwidth) { this.bandwidth = bandwidth; }

    /**
     * @return login A String with the user's login name
     */
    public String getLogin() { return this.login; }

    /**
     * @param login A String with the user's login name
     */
    public void setLogin(String login) { 
        this.login = login;
    }

    /**
     * @return the payloadSender
     */
    public String getPayloadSender() {
        return payloadSender;
    }

    /**
     * @param payloadSender the payloadSender to set
     */
    public void setPayloadSender(String payloadSender) {
        this.payloadSender = payloadSender;
    }

    /**
     * @return status A String with the reservation's current status
     */
    public String getStatus() { return this.status; }

    /**
     * @param status A String with the reservation's current status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * @return status an Integer with the reservation's local status
     */
    public Integer getLocalStatus() {
        if (this.localStatus != null) {
            return this.localStatus;
        }
        return 0;
    }

    /**
     * @param status an Integer with the reservation's local status
     */
    public void setLocalStatus(Integer status) { this.localStatus = status; }

    /**
     * @return string with the reservation's description
     */
    public String getDescription() { return this.description; }

    /**
     * @param description string with the reservation's description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return string with the status of an op on the reservation
     */
    public String getStatusMessage() { return this.statusMessage; }

    /**
     * @param statusMessage string with the status of an op on the reservation
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * @return an String with the reservation's GRI
     */
    public String getGlobalReservationId() {
        return this.globalReservationId;
    }

    /**
     * @param globalReservationId an String with the reservation's GRI
     */
    public void setGlobalReservationId(String globalReservationId) {
        this.globalReservationId = globalReservationId;
    }

    public Map<String,StdConstraint> getConstraintMap() {
        return this.constraintMap;
    }

    public void setConstraintMap(Map<String,StdConstraint> constraintMap) {
        this.constraintMap = constraintMap;
    }

    public StdConstraint getConstraint(String constraintType) throws RMException {
        if (!ConstraintType.isStandard(constraintType)) {
            throw new RMException("Invalid constraintType: "+ constraintType);
        }
        return this.constraintMap.get(constraintType);
    }

    public void setConstraint(StdConstraint constraint) throws RMException {
        String constraintType = constraint.getConstraintType();
        if (constraintType == null) {
            throw new RMException("constraintType cannot be null");
        }
        else if (!ConstraintType.isStandard(constraintType)) {
            throw new RMException("Invalid constraintType: " + constraintType);
        }
        this.constraintMap.put(constraintType, constraint);
    }

    
    /*@S bhr */ 
    
    /**
     * @return set of optional constraints in this reservation.
     */
/*    public Set <OptConstraint> getOptConstraintSet() {
        return this.optConstraintSet;
    }*/

    /**
     * @param optConstraints set of new constraints.  NOTE:  Don't use after
     *                  reservation has been made persistent.
     */
   /* public void setOptConstraintSet(Set<OptConstraint> optConstraints) {
        this.optConstraintSet = optConstraints;
    }*/

    /**
     * @param optConstraint new optConstraint in set, can only be added sequentially.
     */
    /*public void addOptConstraint( OptConstraint optConstraint) {
        this.optConstraintSet.add(optConstraint);
    }*/
   
     

    /**
     * @return set of errorReports in this reservation.
     */
    public List <OptConstraint> getOptConstraintList() {
         return this.optConstraintList;
    }

   /**
     * @param errorReports set of new errorReports.  NOTE:  Don't use after
                         reservation has been made persistent.
     */
    public void setOptConstraintList(List<OptConstraint> optConstraintList) {
          this.optConstraintList =optConstraintList;
    }

    /**
      * @param errorReport new errorReport in set, can only be added sequentially.
      */
      public void addOptConstraint(OptConstraint optConstraint) {
          this.optConstraintList.add(optConstraint);
      }



    
    
    /*@E bhr*/
    
    

    /**
      * @return set of errorReports in this reservation.
      */
     public List <ErrorReportData> getErrorReports() {
          return this.errorReports;
     }

    /**
      * @param errorReports set of new errorReports.  NOTE:  Don't use after
                          reservation has been made persistent.
      */
     public void setErrorReports(List<ErrorReportData> errorReports) {
           this.errorReports =errorReports;
     }

     /**
       * @param errorReport new errorReport in set, can only be added sequentially.
       */
       public void addErrorReport(ErrorReportData errorReport) {
           this.errorReports.add(errorReport);
       }
 
       
              
       
       

    /**
     * getPath - returns the reserved path if it exists, otherwise the requested path
     */
    public Path getPath() {
        try {
            StdConstraint constraint = this.getConstraint(ConstraintType.RESERVED);
            if (constraint != null) {
                return constraint.getPath();
            } else {
                constraint = this.getConstraint(ConstraintType.USER);
                return constraint.getPath();
            }
        } catch (RMException ex) {
            return null;
        }
    }
    /**
     * @return token instance associated with reservation
     */
    public Token getToken() { return this.token; }

    /**
     * @param token token instance to associate with this reservation
     */
    public void setToken(Token token) { this.token = token; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    // FIXME: dbname is not used here
    public String toString(String dbname) throws RMException {
        StringBuilder sb = new StringBuilder();
        String strParam = null;

        // this may be called from methods where the reservation has
        // not been completely set up, so more null checks are
        // necessary here
        if (this.getGlobalReservationId() != null) {
            sb.append("\nGRI: " + this.getGlobalReservationId() + "\n");
        }
        strParam = this.getDescription();
        if (strParam != null) {
            sb.append("description: " + strParam + "\n");
        }
        if (this.getLogin() != null) {
            sb.append("login: " + this.getLogin() + "\n");
        }
        if (this.getStatus() != null) {
            sb.append("status: " + this.getStatus() + "\n");
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

        StdConstraint constraint = this.getConstraint(ConstraintType.USER);
        if (constraint== null) {
            return sb.toString();
        }
        Path path = constraint.getPath();
        if (path == null) {
            return sb.toString();
        }
        sb.append("user requested path:\n");
        sb.append(RMUtils.pathDataToString(path));
        sb.append("interdomain hops: \n\n");
        sb.append(RMUtils.pathToString(path, true));
        
        constraint = this.getConstraint(ConstraintType.RESERVED);
        if (constraint== null) {
            return sb.toString();
        }
        path = constraint.getPath();
        if (path == null) {
            return sb.toString();
        }
        sb.append("reserved path:\n");
        sb.append(RMUtils.pathDataToString(path));
        sb.append("interdomain hops: \n\n");
        sb.append(RMUtils.pathToString(path, true));
        sb.append("\n");
        return sb.toString();
    }
}
