package net.es.oscars.coord.req;

import net.es.oscars.api.soap.gen.v06.GetErrorReportContent;
import net.es.oscars.api.soap.gen.v06.GetErrorReportResponseContent;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import org.apache.log4j.Logger;

public class GetErrRepRequest extends CoordRequest <GetErrorReportContent, GetErrorReportResponseContent> {

    private static final long       serialVersionUID  = 1L;
    //private String     GRI     = null;
    private static final Logger LOG = Logger.getLogger(GetErrRepRequest.class.getName());

    /**
     *
     * @param name - name for this instance of CoordRequest
     * @param authConds  results of the checkAuthorization call for this user and action
     * @param getErrorReq contains the transactionId of the transaction to be queried
     */
    public GetErrRepRequest(String name, AuthConditions authConds, GetErrorReportContent getErrorReq) {
        super(name,getErrorReq.getMessageProperties().getGlobalTransactionId(),
                "unused",authConds);
        //this.GRI = getErrorReq.getGlobalReservationId();
        this.setRequestData(getErrorReq);
    }

    /**
     * sends a synchronous getErrorReport message to the ResourceManager
     * were set in the constructor: authDecision, GetErrorReportContent
     * @return GerErrorReportContent set in this.ResultData.
     */
    public void execute(){
        String method = "getErrorReportRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId());
        LOG.debug(netLogger.start(method));

        // Call the ResourceManager to get the errorReport
        GetErrorReportResponseContent getErrorRep = null;;
        Object [] res =  null;
        try {
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();
            Object [] req = new Object[]{this.getAuthConditions(),this.getRequestData()};
            res = rmClient.invoke("getErrorReport",req);
            getErrorRep = (GetErrorReportResponseContent)res[0];
            this.setResultData(getErrorRep);
            this.executed();
        } catch (OSCARSServiceException ex ){
            this.fail(ex);
            LOG.debug(netLogger.error(method, ErrSev.MINOR, " catching OSCARSServiceException setting fail  " + 
                                       ex.getMessage()));
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message == null){
                message = ex.toString();
            }
            this.fail(new OSCARSServiceException(message, "system"));
            LOG.debug(netLogger.error(method, ErrSev.MINOR, "catching exception setting fail  "  +
                                      ex.getMessage()));
        }
        LOG.debug(netLogger.end(method));
    }
}
