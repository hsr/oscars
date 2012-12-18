package net.es.oscars.coord.actions;

import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import org.apache.log4j.Logger;

/**
 * RMQueryAction gets the resDetails for a GRI
 * Called by createReservation.failed to get the resDetails need fot the failure notification message
 * User: mrt
 * Date: 5/2/11
 */

public class RMQueryAction extends CoordAction <QueryResContent, QueryResReply> {

    private static final long       serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(RMQueryAction.class.getName());
    static final String moduleName = ModuleName.COORD;

    @SuppressWarnings("unchecked")
    public RMQueryAction (String name, CoordRequest request, String GRI)  {
        super (name, request, null);
        QueryResContent resContent = new QueryResContent();
        resContent.setGlobalReservationId(GRI);
        resContent.setMessageProperties(request.getMessageProperties());
        this.setRequestData(resContent);
    }
   /**
    * Send a store message to the Resource Manager that updates or stores a reservation
    * normally called synchronously
    *
    * params are set in the constructor:
    */
    public void execute()  {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        //netLogger.init(moduleName, this.getRequestData().getTransactionId());
        //netLogger.setGRI(this.getRequestData().getReservation().getGlobalReservationId());
        LOG.debug(netLogger.start("RMSQueryAction", "Query reservation" +
                                    this.getRequestData().getGlobalReservationId()));
        try {
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();
            // set AuthConditions to null
            Object[] req = {null,this.getRequestData()};
            Object[] res = rmClient.invoke("queryReservation",req);

            if ((res == null) || (res[0] == null)) {
                throw new OSCARSServiceException("RMQueryAction:No response from ResourceManager", "system");
            }
            QueryResReply queryResponse = (QueryResReply) res[0];
            this.setResultData(queryResponse);
            LOG.debug(netLogger.end("RMQueryAction"));
            this.executed();
        } catch (OSCARSServiceException ex) {
            this.setRequestData(null);
            this.fail(ex);
        }
    }

    public void executed() {
        this.setState(State.PROCESSED);
    }
}
