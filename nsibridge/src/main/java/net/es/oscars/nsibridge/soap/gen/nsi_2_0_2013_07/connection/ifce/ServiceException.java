
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.7.6
 * 2013-07-29T09:59:17.434-07:00
 * Generated source version: 2.7.6
 */

@WebFault(name = "serviceException", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types")
public class ServiceException extends Exception {
    
    private net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType serviceException;

    public ServiceException() {
        super();
    }
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message, net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType serviceException) {
        super(message);
        this.serviceException = serviceException;
    }

    public ServiceException(String message, net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType serviceException, Throwable cause) {
        super(message, cause);
        this.serviceException = serviceException;
    }

    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType getFaultInfo() {
        return this.serviceException;
    }
}
