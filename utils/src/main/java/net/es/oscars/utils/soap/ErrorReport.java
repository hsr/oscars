package net.es.oscars.utils.soap;

import java.util.LinkedList;
import java.util.Date;

import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.utils.topology.PathTools;

/**
 * ErrorReport; used to store details about errors
 * User: mrt
 * Date: 4/8/1
 *
 */
public class ErrorReport extends LinkedList <ErrorReport> {

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


    public ErrorReport () {
        this.timestamp = System.currentTimeMillis()/1000L;
        this.setDomainId(PathTools.getLocalDomainId());
    }
    public ErrorReport (String errMsg, String errType) {
        this.errorMsg = errMsg;
        this.errorType = errType;
        this.timestamp = System.currentTimeMillis()/1000L;
        this.setDomainId(PathTools.getLocalDomainId());
    }

    public ErrorReport (String code, String errMsg, String errType) {
        this.errorMsg = errMsg;
        this.errorType = errType;
        this.errorCode = code;
        this.timestamp = System.currentTimeMillis()/1000L;
        this.setDomainId(PathTools.getLocalDomainId());
    }
    public ErrorReport(String errCode, String errMsg, String errType, String GRI,
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

    public static ErrorReport fault2report( OSCARSFaultReport errFault){
        if (errFault == null) {
            return new ErrorReport();
        }
        ErrorReport errRep = new ErrorReport(errFault.getErrorCode(),
                                             errFault.getErrorMsg(),
                                             errFault.getErrorType(),
                                             errFault.getGri(),
                                             errFault.getTransId(),
                                             errFault.getTimestamp(),
                                             errFault.getModuleName(),
                                             errFault.getDomainId()
                   );

        return errRep;
    }

    public static OSCARSFaultReport report2fault( ErrorReport errRep){
        if (errRep == null ){
            return new OSCARSFaultReport();
        }
        OSCARSFaultReport faultRep = new OSCARSFaultReport();
        faultRep.setErrorCode(errRep.getErrorCode());
        faultRep.setErrorMsg(errRep.getErrorMsg());
        faultRep.setErrorType(errRep.getErrorType());
        faultRep.setGri(errRep.getGRI());
        faultRep.setTransId(errRep.getTransId());
        faultRep.setTimestamp(errRep.getTimestamp());
        faultRep.setModuleName(errRep.getModuleName());
        if (errRep.getDomainId() != null) {
            faultRep.setDomainId(errRep.getDomainId());
        } else {
            faultRep.setDomainId(PathTools.getLocalDomainId());
        }
        return faultRep;
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

    public String toString() {
        return "Code: " + errorCode +  " Type: "  + errorType +
                "\n Msg: " + errorMsg + "\n ModuleName: " + moduleName +
                " DomainId: "  + domainId + " GRI: " + GRI + " TransID: " + transId +
                "\n Timestamp: " + new Date(timestamp*1000);
    }

}
