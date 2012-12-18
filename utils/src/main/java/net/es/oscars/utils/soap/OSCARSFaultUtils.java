package net.es.oscars.utils.soap;

import org.hibernate.Session;
import org.apache.log4j.Logger;

import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.sharedConstants.ErrorCodes;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * OSCARSFaultUtils called when a server catches an exception. It handles database cleanup,logging of
 * unexpected errors, maps the exception to an OSCARSFaultMessage, which is then thrown.
 *
 * @author  Mary Thompson
 */
public class OSCARSFaultUtils {

    /**
     * Handles exceptions in servers
     * 
     * @param ex Exception
     * @param session Session - if not null, current open session
     * @param log Logger - log in which to record system errors
     * @parma method String containing the method that was called
     * @throws OSCARSFaultMessage
     */
    public  static Void handleError(OSCARSServiceException ex, Session session, Logger log, String method ) throws OSCARSFaultMessage
    { 
        Boolean isUser = false;
        if  (ex.getType() != null) {
            if (ex.getType().equals("user") ||
                ex.getType().equals(ErrorReport.USER)) {
                    isUser = true;
            }
        }
        // it wouldn't compile without the return. Doesn't make sense to me
       return OSCARSFaultUtils.handleError(ex,isUser,session,log,method);
    }
 
    /**
     * Handles exceptions in servers
     * 
     * @param ex Exception
     * @param user Boolean true if exception is caused by user input
     * @param session Session - if not null, current open session
     * @param log Logger - log in which to record system errors
     * @param method String containing the method that was called
     * @throws OSCARSFaultMessage
     */
    public  static Void handleError(Exception ex, Boolean user, Session session, Logger log, String method ) throws OSCARSFaultMessage
    {
        if (session != null) {
            session.getTransaction().rollback();
        }
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "handleError";
        OSCARSFaultReport faultRep = null;

        // Check if the exception object e is an OSCARSServiceException and contains a ErrorReport.
        try {
            Method getErrorReport = ex.getClass().getMethod("getErrorReport", (Class[]) null);
            try {
                if (ex instanceof OSCARSServiceException) {
                    ErrorReport errorReport = (ErrorReport)getErrorReport.invoke(ex, (Object[]) null);
                    if (errorReport != null) {
                        faultRep = ErrorReport.report2fault(errorReport);
                    }
                }
            } catch (InvocationTargetException e1) {
                    // Since the existence of the method is checked earlier, this error should never happen
            } catch (IllegalAccessException e2) {
                    // Since the existence of the method is checked earlier, this error should never happen
            }
        } catch (NoSuchMethodException ee) {
            // This is a standard Exception - pass
        }
        if (faultRep == null) {
            faultRep = new OSCARSFaultReport();
            faultRep.setErrorCode(ErrorCodes.UNKNOWN);;
            faultRep.setErrorType(user ? ErrorReport.USER : ErrorReport.SYSTEM);
        }
        String message = ex.getMessage();
        if (!user) {
             // system error
            if (log !=  null){
                log.error(netLogger.error(event,ErrSev.MAJOR,"Exception thrown by " + method));
            }
            ex.printStackTrace();
            if (message == null ){
                // some runtime errors do not set message
                message=ex.toString();
            }
            faultRep.setErrorMsg(message);
        }
        OSCARSFault of = new OSCARSFault();
        of.setErrorReport(faultRep);
        //System.out.println("Creating new oscarsFaultMessage");
        OSCARSFaultMessage ofm =  new OSCARSFaultMessage(message,of);
        throw ofm;
    }
}
