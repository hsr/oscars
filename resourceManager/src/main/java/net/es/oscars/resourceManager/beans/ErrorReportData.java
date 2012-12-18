package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import java.util.Date;
import net.es.oscars.database.hibernate.HibernateBean;

/**
 * ErrorReportData; Hibernate bean for errorReports table
 * User: mrt
 * Date: 4/8/1
 *
 */
public class ErrorReportData extends HibernateBean implements Serializable {

    private int seqNumber;          //  Order of this errorReport in errorReport list
    private String errorCode;       // defined in shared/utils/OSCARSErrors;
    private String errorMsg;        // free format message from exception
    private String errorType = UNKNOWN;       // SYSTEM, USER or UNKNOWN
    private String GRI;             // GlobalReservationId
    private String transId;         // transaction id
    private Long   timestamp;       // seconds since epoch
    private String moduleName;      // where error occurred
    private String domainId;        // where error occurred

    public static String USER = "user";
    public static String SYSTEM = "system";
    public static String UNKNOWN = "unknown";


    public ErrorReportData () {
        this.timestamp = System.currentTimeMillis()/1000L;
    }
    public ErrorReportData (String errMsg, String errType) {
        this.errorMsg = errMsg;
        this.errorType = errType;
        this.timestamp = System.currentTimeMillis()/1000L;
    }

    public ErrorReportData (String code, String errMsg, String errType) {
        this.errorMsg = errMsg;
        this.errorType = errType;
        this.errorCode = code;
        this.timestamp = System.currentTimeMillis()/1000L;
    }
    public ErrorReportData (String errCode, String errMsg, String errType, String GRI,
                       String transId, Long timestamp, String moduleName, String domainId) {
        this.errorCode = errCode;
        if (errType != null){
            this.errorType = errType;
        }
        this.errorMsg = errMsg;
        this.GRI = GRI;
        this.transId = transId;
        if (timestamp != null){
            this.timestamp = timestamp;
        } else {
            this.timestamp = System.currentTimeMillis()/1000L;
        }
        this.moduleName = moduleName;
        this.domainId = domainId;
    }

    /**
        * @return seqNumber int with this report's position in list
        */
       public int getseqNumber() {
           return this.seqNumber;
       }

       /**
        * @param num not actually settable
        */
       public void setseqNumber(int num) {
           this.seqNumber = num;
       }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errCode) {
        errorCode = errCode;
    }

   public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errType) {
        errorType = errType;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errMsg) {
        errorMsg = errMsg;
    }

    public String getGRI() {
        return GRI;
    }

    public void setGRI(String GRI) {
        this.GRI = GRI;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId){
        this.transId = transId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName){
           this.moduleName = moduleName;
    }

    public String getDomainId () {
        return domainId;

    }
    public void setDomainId(String domainId){
        this.domainId = domainId;
    }

       // need to override superclass because dealing with transient
    // instances as well -- don't think this is used --mrt
    public boolean equals(Object o) {
        return true;
    }

    public String toString() {
        return "Code: " + errorCode +  " Type: "  + errorType +
                "\n Msg: " + errorMsg + "\n ModuleName: " + moduleName +
                " DomainId: "  + domainId + " GRI: " + GRI + " TransID: " + transId +
                "\n Timestamp: " + new Date(timestamp*1000);
    }

}
