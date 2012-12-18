package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;


/**
 * ReservationCompletedForwarder sends a IDE Event message to the previous IDC (if any).
 * This is done during after a path has been calculated or modified and is being committed.
 * 
 * @author lomax
 *
 */
public class ReservationCompletedForwarder extends ForwarderAction <ResDetails,Object> {

    private static final long       serialVersionUID = 1L;
    private String type = null;  // set to either RESV_MODIFY_COMPLETED or RESV_CREATE_COMPLETED

    @SuppressWarnings("unchecked")
    public ReservationCompletedForwarder (String name,
                                          CoordRequest request,
                                          String type,
                                          String destDomainId,
                                          ResDetails resDetails) {
        super (name, request, destDomainId, resDetails);
        this.type = type;
    }
    
    public void execute()  {
        // Send Committed to the previous IDC
        try {
            if ((this.getDestDomainId() != null) && ( ! PathTools.getLocalDomainId().equals(this.getDestDomainId()))) {
                
                InternalAPIWorker.getInstance().sendEventContent(this.getCoordRequest(), 
                                                                 this.getRequestData(), 
                                                                 this.type,
                                                                 this.getDestDomainId());
            }
        } catch (OSCARSServiceException e) {
            this.fail(e);
        }
        this.setResultData(null);           
        this.executed();
    }  
}
