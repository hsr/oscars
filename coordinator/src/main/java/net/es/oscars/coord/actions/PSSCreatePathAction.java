package net.es.oscars.coord.actions;

import org.apache.log4j.Logger;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.soap.gen.PSSReplyContent;
import net.es.oscars.coord.actions.CoordAction;

import net.es.oscars.coord.workers.PSSWorker;
import net.es.oscars.utils.clients.PSSClient;
import net.es.oscars.utils.sharedConstants.PSSConstants;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.pss.soap.gen.SetupReqContent;
/**
 * Action that sends a pathsetup message to the PSS service.
 * @author lomax
 * @param SetupReqContent - class for out going message content
 * @param PSSReplyContent   - class for repsonse message,  not used as there is no reponse message
 *
 */
public class PSSCreatePathAction extends CoordAction <SetupReqContent, PSSReplyContent> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public PSSCreatePathAction (String name, CoordRequest request, SetupReqContent setupReq ) 
            throws OSCARSServiceException {
        super (name, request, null);
        this.setRequestData(setupReq);
    }
   /**
    * Use pssWorker to send a setup message to the PathSetup Module to instantiate the circuit
    */
    public void execute()  {
        try {
            PSSWorker pssWorker = PSSWorker.getInstance();
            PSSClient pssClient = pssWorker.getPSSClient();

            Object[] req = {this.getRequestData()};
            pssClient.invoke(PSSConstants.PSS_SETUP,req);

            this.setResultData(null);            
            this.executed();
            
        } catch (OSCARSServiceException ex) {
            this.fail(ex);
        }
    } 
}
