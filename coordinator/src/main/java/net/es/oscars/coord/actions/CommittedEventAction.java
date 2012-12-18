package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.runtimepce.PCEData;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;


/**
 * Process a committed event from another IDC
 * 
 * @author lomax
 *
 */
public class CommittedEventAction extends CoordAction <ResDetails,Object> {

    private static final long       serialVersionUID = 1L;
    private String  type = null;

    @SuppressWarnings("unchecked")
    public CommittedEventAction (String name, CoordRequest request, String type, ResDetails resDetails) {
        super (name, request, resDetails);
        this.type = type;
    }
    
    public void execute()  {
        // Commit locally. The RuntimePCE will, in turn, when commit is completed, send the event to the previous IDC if any.
 
        ResDetails resDetails = this.getRequestData();
        PCERuntimeAction pceRuntimeAction = null;

        if (this.type.equals (NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED)) {

            // Start the commit phase
            pceRuntimeAction = new PCERuntimeAction (this.getName() + "-CreateCommit-PCERuntimeAction",
                                                     this.getCoordRequest(),
                                                     null,
                                                     this.getCoordRequest().getTransactionId(),
                                                     PCERequestTypes.PCE_CREATE_COMMIT);
        } else if (this.type.equals (NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED)) {

            // Start the commit phase
            pceRuntimeAction = new PCERuntimeAction (this.getName() + "-ModifyCommit-PCERuntimeAction",
                                                     this.getCoordRequest(),
                                                     null,
                                                     this.getCoordRequest().getTransactionId(),
                                                     PCERequestTypes.PCE_MODIFY_COMMIT);
        }

        PCEData pceData = new PCEData(resDetails.getUserRequestConstraint(),
                                      resDetails.getReservedConstraint(),
                                      resDetails.getOptionalConstraint(),
                                      null);

        pceRuntimeAction.setRequestData(pceData);

        this.add(pceRuntimeAction);
 
        this.setResultData(null);           
        this.executed();
    }  
}
