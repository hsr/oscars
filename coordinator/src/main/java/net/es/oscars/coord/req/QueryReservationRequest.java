package net.es.oscars.coord.req;

import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.soap.OSCARSServiceException;

public class QueryReservationRequest extends CoordRequest <QueryResContent, QueryResReply> {
    
    private static final long       serialVersionUID  = 1L;
    //private String     GRI     = null;
    private static final Logger LOG = Logger.getLogger(QueryReservationRequest.class.getName());
    
    /**
     * 
     * @param name - name for this instance of CoordRequest
     * @param authConds  results of the checkAuthorization call for this user and action
     * @param queryReq contains the  GlobalReservationId of the reservation to be queried
     */
    public QueryReservationRequest(String name, AuthConditions authConds, QueryResContent queryReq) {
        super(name,queryReq.getMessageProperties().getGlobalTransactionId(),
                queryReq.getGlobalReservationId(),authConds);
        //this.GRI = queryReq.getGlobalReservationId();
        this.setRequestData(queryReq);
    }

    /**
     * sends a synchronous queryReservation message to the ResourceManager
     * were set in the constructor: authDecision, QueryResContent
     * @return ResDetails set in this.ResultData.
     */
    public void execute(){
        String method = "queryReservationRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method));

        // Call the ResourceManager to query the reservation
        QueryResReply queryRep = null;;
        Object [] res =  null;
        try {
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();
            Object [] req = new Object[]{this.getAuthConditions(),this.getRequestData()};
            res = rmClient.invoke("queryReservation",req);
            queryRep = (QueryResReply)res[0];
            this.setResultData(queryRep);
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
