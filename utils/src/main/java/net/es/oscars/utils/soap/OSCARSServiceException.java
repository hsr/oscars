package net.es.oscars.utils.soap;

import java.lang.Exception;
import java.lang.NoSuchMethodException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.utils.sharedConstants.ErrorCodes;

@SuppressWarnings ("serial")
public class OSCARSServiceException extends Exception {
    
   // private Object faultInfo    = null;
    //private String type = "unknown";

    private ErrorReport errorReport;

    public OSCARSServiceException() {
           super();
           new OSCARSServiceException(ErrorCodes.UNKNOWN, "no msg", ErrorReport.UNKNOWN);
       }

    public OSCARSServiceException (String message) {
       super(message);
       this.errorReport = new ErrorReport(ErrorCodes.UNKNOWN, message, ErrorReport.UNKNOWN);
    }
    
    public OSCARSServiceException(String message, String type) {
        super(message);
        this.errorReport = new ErrorReport(ErrorCodes.UNKNOWN, message, type);
    }

    public OSCARSServiceException(String code, String message, String type) {
        super(message);
        this.errorReport = new ErrorReport(code, message, type);
    }

    public OSCARSServiceException(ErrorReport errRep) {
        super(errRep.getErrorMsg());
        this.errorReport = errRep;
    }

    public OSCARSServiceException (Exception e) {
        super(e.getMessage(),e);

        try {
            // Check if the exception object e  contains a FaultInfo.

            Method getFaultInfo = e.getClass().getMethod("getFaultInfo", (Class[]) null);
            try {
                if (e instanceof OSCARSFaultMessage) {
                    OSCARSFault of =  (OSCARSFault) getFaultInfo.invoke(e, (Object[]) null);
                    OSCARSFaultReport rep = of.getErrorReport();
                    if (rep != null) {
                        this.errorReport = ErrorReport.fault2report(rep);
                    } else {
                        this.errorReport = new ErrorReport(ErrorCodes.UNKNOWN,e.getMessage(),ErrorReport.UNKNOWN);
                    }
                } else if (e instanceof OSCARSServiceException ) {
                    ErrorReport er = ((OSCARSServiceException) e).getErrorReport();
                    if (er != null) {
                        this.errorReport = er;
                    } else {  // shouldn't happen
                         this.errorReport = new ErrorReport(ErrorCodes.UNKNOWN,e.getMessage(),ErrorReport.UNKNOWN);
                    }
                } else {
                    this.errorReport = new ErrorReport(ErrorCodes.UNKNOWN,e.getMessage(),ErrorReport.SYSTEM);
                }
                 return;
            } catch (InvocationTargetException e1) {
                // Since the existence of the method is checked earlier, this error should never happen
            } catch (IllegalAccessException e2) {
                // Since the existence of the method is checked earlier, this error should never happen
            }
        } catch (NoSuchMethodException ee) {
            // This is a standard Exception
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }
            this.errorReport = new ErrorReport(ErrorCodes.UNKNOWN,msg,ErrorReport.SYSTEM);
        }
    }

   /* public OSCARSServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    */
    /* for backwards  compatibility
    public Object getFaultInfo () {
        return (Object) this.errorReport;
    }
    */

    public ErrorReport getErrorReport() {
        return this.errorReport;
    }

    public void addErrorReport(ErrorReport errRep) {
            this.errorReport.add(errRep);
    }

    public String getType () {
        return this.errorReport.getErrorType();
    }
    
    public void setType (String type) {
        this.errorReport.setErrorType(type);
    }
}
