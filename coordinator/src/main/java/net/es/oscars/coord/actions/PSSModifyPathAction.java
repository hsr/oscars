package net.es.oscars.coord.actions;

import org.apache.log4j.Logger;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.req.PathRequest;
import net.es.oscars.coord.soap.gen.PSSReplyContent;
import net.es.oscars.coord.actions.CoordAction;

import net.es.oscars.coord.workers.PSSWorker;
import net.es.oscars.utils.clients.PSSClient;
import net.es.oscars.utils.sharedConstants.PSSConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.pss.soap.gen.ModifyReqContent;
/**
 * Action that sends a path modify message to the PSS service.
 * @author alake, lomax
 * @param ModifyReqContent - class for out going message content
 * @param PSSReplyContent   - class for response message,  not used as there is no response message
 *
 */
public class PSSModifyPathAction extends CoordAction <ModifyReqContent, PSSReplyContent> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public PSSModifyPathAction (String name, CoordRequest request, ModifyReqContent modifyReq ) 
            throws OSCARSServiceException {
        super (name, request, null);
        this.setRequestData(modifyReq);
    }
   /**
    * Use pssWorker to send a modify message to the PathSetup Module to instantiate the circuit
    */
    public void execute()  {
        try {
            
            PSSWorker pssWorker = PSSWorker.getInstance();
            PSSClient pssClient = pssWorker.getPSSClient();
            
            //register request so we can handle the reply later on
            MessagePropertiesType msgProps = new MessagePropertiesType();
            msgProps.setGlobalTransactionId(this.getRequestData().getTransactionId());
            PathRequest.getPathRequest(PathRequest.PSS_MODIFY_PATH +  
                    "-" + this.getRequestData().getReservation().getGlobalReservationId(),
                    msgProps, this.getRequestData().getReservation());

            Object[] req = {this.getRequestData()};
            pssClient.invoke(PSSConstants.PSS_MODIFY,req);

            this.setResultData(null);            
            this.executed();
            
        } catch (OSCARSServiceException ex) {
            this.fail(ex);
        }
    } 
}
