package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.api.soap.gen.v06.TeardownPathContent;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.utils.sharedConstants.StateEngineValues;

/**
 * TeardownPathForwarder sends a TeardwonPath message to the next IDC (if any).
 * This is done during path teardwon, after the local PSSPathTeardown has been sent
 *
 * @author lomax
 *
 */

public class TeardownPathForwarder extends ForwarderAction <TeardownPathContent,Object> {

    private static final long       serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public TeardownPathForwarder (String name,
                                  CoordRequest request,
                                  String destDomainId,
                                  TeardownPathContent teardownPathContent) {
        super (name, request, destDomainId, teardownPathContent);
    }
    
    public void execute()  {
        // Send TeardownPath to the next IDC
        try {
            if ((this.getDestDomainId() != null) && ( ! PathTools.getLocalDomainId().equals(this.getDestDomainId()))) {
                
                InternalAPIWorker.getInstance().sendTeardownPath (this.getCoordRequest(), 
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
