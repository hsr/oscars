package net.es.oscars.coord.actions;

import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.workers.NotifyWorker;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;


/**
 * Process a RESV_CREATE_COMPLETED event from another IDC
 * 
 * @author lomax
 *
 */
public class CreateResvCompletedAction extends CoordAction <ResDetails,Object> {

    private static final long       serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(CreateResvCompletedAction.class.getName());
    private OSCARSNetLogger netLogger = null;
    private static final String moduleName = ModuleName.COORD;
    
    @SuppressWarnings("unchecked")
    public CreateResvCompletedAction (String name, CoordRequest request, ResDetails resDetails) {
        super (name, request, resDetails);
    }
    
    public void execute()  {
        String method = "CreateResvCompletedAction.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CreateResvCompletedAction.moduleName,this.getCoordRequest().getTransactionId()); 
        netLogger.setGRI(this.getCoordRequest().getGRI());
        LOG.debug(netLogger.start(method));

        boolean localRes = true;
        String localDomain = PathTools.getLocalDomainId();
        boolean lastDomain = true;
        ResDetails resDetails = this.getRequestData();
        CtrlPlanePathContent reservedPath = resDetails.getReservedConstraint().getPathInfo().getPath();
        try {
            if (reservedPath != null) {
                localRes = PathTools.isPathLocalOnly(reservedPath);
                lastDomain = localDomain.equals(PathTools.getLastDomain(reservedPath));
            }

            // Store final version of reservation, previous domain vlans are now set
            RMStoreAction rmStoreAction = new RMStoreAction(this.getName() + "-RMStoreAction",
                                                            this.getCoordRequest(),
                                                            resDetails);
            rmStoreAction.execute();
            if (rmStoreAction.getState() == CoordAction.State.FAILED) {
                throw (OSCARSServiceException) rmStoreAction.getException();
            }
            if (!localRes && !lastDomain) {
                // Send CREATE_RESV_COMPLETED event to the next IDC
                String nextDomain = PathTools.getNextDomain (resDetails.getReservedConstraint().getPathInfo().getPath(),
                                                             PathTools.getLocalDomainId());
                ReservationCompletedForwarder forwarder = new ReservationCompletedForwarder (this.getName() + "-CreateResvCompletedForwarder",
                                                                                             this.getCoordRequest(),
                                                                                             NotifyRequestTypes.RESV_CREATE_COMPLETED,
                                                                                             nextDomain,
                                                                                             resDetails);
                forwarder.execute();
    
                if (forwarder.getState() == CoordAction.State.FAILED) {
                    throw (OSCARSServiceException) forwarder.getException();
                }
            }
        } catch (OSCARSServiceException e) {
            LOG.error (netLogger.error(method, ErrSev.CRITICAL, "caught exception " + e.getMessage()));
            // Fail the createPathRequest
            CoordRequest createResReq = CoordRequest.getCoordRequestByAlias("createPath");
            createResReq.failed(e);
            this.fail(e);
            return;
        }
        // Release the RuntimePCE global lock
        PCERuntimeAction.releaseMutex(this.getCoordRequest().getGRI());

        // notify that request is completed
         NotifyWorker.getInstance().sendInfo (this.getCoordRequest(),
                                              NotifyRequestTypes.RESV_CREATE_COMPLETED,
                                              resDetails);


        this.setResultData(null);           
        this.executed();
    }  
}
