package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;


/**
 * CommittedForwarder sends a InterDomainEvent message to the previous IDC (if any).
 * This is done during after a path has been calculated and is being committed.
 * 
 * @author lomax
 *
 */
public class CommittedForwarder extends ForwarderAction <ResDetails,Object> {

    private static final long       serialVersionUID = 1L;
    private String requestType = null;

    @SuppressWarnings("unchecked")
    public CommittedForwarder (String name,
                               CoordRequest request,
                               String requestType,
                               String destDomainId,
                               ResDetails resDetails) {
        super (name, request, destDomainId, resDetails);
        this.requestType = requestType;
    }
    
    public void execute()  {
        // Send Committed to the previous IDC
        try {
            if ((this.getDestDomainId() != null) && ( ! PathTools.getLocalDomainId().equals(this.getDestDomainId()))) {
                
                InternalAPIWorker.getInstance().sendEventContent(this.getCoordRequest(), 
                                                                 this.getRequestData(), 
                                                                 this.requestType,
                                                                 this.getDestDomainId());
            }
        } catch (OSCARSServiceException e) {
            this.fail(e);
        }
        this.setResultData(null);           
        this.executed();
    }  
}
