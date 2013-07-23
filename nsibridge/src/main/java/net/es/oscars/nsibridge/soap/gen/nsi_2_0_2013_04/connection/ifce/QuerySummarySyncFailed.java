
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.7.6
 * 2013-07-23T11:23:01.061-07:00
 * Generated source version: 2.7.6
 */

@WebFault(name = "querySummarySyncFailed", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types")
public class QuerySummarySyncFailed extends Exception {
    
    private net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryFailedType querySummarySyncFailed;

    public QuerySummarySyncFailed() {
        super();
    }
    
    public QuerySummarySyncFailed(String message) {
        super(message);
    }
    
    public QuerySummarySyncFailed(String message, Throwable cause) {
        super(message, cause);
    }

    public QuerySummarySyncFailed(String message, net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryFailedType querySummarySyncFailed) {
        super(message);
        this.querySummarySyncFailed = querySummarySyncFailed;
    }

    public QuerySummarySyncFailed(String message, net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryFailedType querySummarySyncFailed, Throwable cause) {
        super(message, cause);
        this.querySummarySyncFailed = querySummarySyncFailed;
    }

    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryFailedType getFaultInfo() {
        return this.querySummarySyncFailed;
    }
}
