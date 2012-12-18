package net.es.oscars.coord.actions;

import org.apache.log4j.Logger;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;

import net.es.oscars.coord.workers.PSSWorker;
import net.es.oscars.utils.clients.PSSClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.sharedConstants.PSSConstants;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.pss.soap.gen.TeardownReqContent;
import net.es.oscars.coord.soap.gen.PSSReplyContent;

/*
 * action to send a teardown message to the PSS service
 */
public class PSSTeardownPathAction extends CoordAction <TeardownReqContent, PSSReplyContent> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public PSSTeardownPathAction (String name, CoordRequest request, TeardownReqContent req ) throws OSCARSServiceException {
        super (name, request, null);
        this.setRequestData(req);
    }
   /**
    * sends the teardown message to the PSS service
    * TeardownReqContent contains the resDetails for the reservation.
    */
    public void execute() {
        // Send a query to the PSS to tear down a path
        try {
            PSSWorker pssWorker = PSSWorker.getInstance();
            PSSClient pssClient = pssWorker.getPSSClient();

            Object[] req = {this.getRequestData()};
            pssClient.invoke(PSSConstants.PSS_TEARDOWN,req);
 
            // there is no reply message
            this.setResultData(null);
            this.executed();
        } catch (OSCARSServiceException ex) {
            this.fail(ex);
        }
    } 
    
    public void executed() {
        this.setState(State.PROCESSED);
    }
}
