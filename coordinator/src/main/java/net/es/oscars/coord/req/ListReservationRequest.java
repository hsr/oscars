package net.es.oscars.coord.req;

import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.soap.OSCARSServiceException;

public class ListReservationRequest extends CoordRequest <ListRequest, ListReply>{
    
    private static final long       serialVersionUID  = 1L;
    private static final Logger LOG = Logger.getLogger(ListReservationRequest.class.getName());

    public ListReservationRequest( String name, AuthConditions authConds, ListRequest listRequest ) {
        super (name, listRequest.getMessageProperties().getGlobalTransactionId());
        this.setAuthConditions(authConds);
        this.setRequestData(listRequest);
    }

    /**
     * sends a synchronous ListReservation message to the ResourceManager
     */
    public void execute() {
        String method = "ListReservationRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method));

        ListRequest listRequest = this.getRequestData();
        ListReply listReply = null;
        try {
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();
            Object [] req = new Object[]{this.getAuthConditions(),listRequest};
            Object [] res = rmClient.invoke("listReservations",req);
            listReply = (ListReply)res[0];
            this.setResultData(listReply);
            this.executed();
        } catch (OSCARSServiceException ex){
            this.fail (new OSCARSServiceException(method +" caught exception "+ ex.getMessage(), ex.getType()));
            LOG.warn(netLogger.error(method, ErrSev.MINOR, "caught OSCARSServiceException " + ex.getMessage()));
        }
        LOG.debug(netLogger.end(method));
    }
}
