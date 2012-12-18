package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.sharedConstants.StateEngineValues;


/**
 * CreatePathForwarder sends a CreatePath message to the next IDC (if any).
 * This is done during path setup, after the local PSSPathSetup has been sent
 * 
 * @author lomax
 *
 */
public class CreatePathForwarder extends ForwarderAction <CreatePathContent,Object> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public CreatePathForwarder (String name,
                                CoordRequest request,
                                String destDomainId,
                                CreatePathContent createPathContent) {
        super (name, request, destDomainId, createPathContent);
    }
    
    public void execute()  {
        // Send CreatePatj to the next IDC
        try {
            if ((this.getDestDomainId() != null) && ( ! PathTools.getLocalDomainId().equals(this.getDestDomainId()))) {
                
                InternalAPIWorker.getInstance().sendCreatePath (this.getCoordRequest(), 
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
