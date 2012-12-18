package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.sharedConstants.StateEngineValues;


/**
 * CreateResvForwarder sends a CreateReservation message to the next IDC (if any).
 * This is done when a path is calculated
 * 
 * @author lomax
 *
 */
public class CancelResvForwarder extends ForwarderAction <CancelResContent,Object> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public CancelResvForwarder (String name, 
                                CoordRequest request,
                                String destDomainId,
                                CancelResContent cancelContent) {
        super (name, request, destDomainId, cancelContent);
    }
    
    public void execute()  {
        // Send ResCancel to the next IDC
        try {

            if ((this.getDestDomainId() != null) && ( ! PathTools.getLocalDomainId().equals(this.getDestDomainId()))) {
                
                InternalAPIWorker.getInstance().sendCancelReservation(this.getCoordRequest(), 
                                                                      this.getRequestData(),
                                                                      this.getDestDomainId());
            }
        } catch (OSCARSServiceException e) {
            this.fail(e);
        }
        this.setResultData(null);           
        this.executed();
    }  
}
