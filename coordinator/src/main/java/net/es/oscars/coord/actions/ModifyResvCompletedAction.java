package net.es.oscars.coord.actions;

import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.workers.NotifyWorker;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pss.soap.gen.ModifyReqContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;


/**
 * Process a RESV_MODIFY_COMPLETED event from another IDC
 * 
 * @author lomax
 *
 */
public class ModifyResvCompletedAction extends CoordAction <ResDetails,Object> {

    private static final long       serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(CreateResvCompletedAction.class.getName());
    private OSCARSNetLogger netLogger = null;
    private static final String moduleName = ModuleName.COORD;
    
    @SuppressWarnings("unchecked")
    public ModifyResvCompletedAction (String name, CoordRequest request, ResDetails resDetails) {
        super (name, request, resDetails);
    }
    
    public void execute()  {
        String method = "ModifyResvCompletedAction.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModifyResvCompletedAction.moduleName,this.getCoordRequest().getTransactionId());
        netLogger.setGRI(this.getCoordRequest().getGRI());
        LOG.debug(netLogger.start(method));

        boolean localRes = true;
        String localDomain = PathTools.getLocalDomainId();
        boolean lastDomain = true;
        ResDetails resDetails = this.getRequestData();
        CtrlPlanePathContent reservedPath = resDetails.getReservedConstraint().getPathInfo().getPath();
        Coordinator coordinator = null;
        try {
            coordinator = Coordinator.getInstance();
            if (reservedPath != null) {
                localRes = PathTools.isPathLocalOnly(reservedPath);
                String domain = PathTools.getLastDomain(reservedPath);
                lastDomain = localDomain.equals(domain);
            }
        } catch (OSCARSServiceException e) {
            LOG.error (netLogger.error(method, ErrSev.MAJOR,"Cannot process reservedPath " +
                                       this.getCoordRequest().getGRI()));
            this.fail(e);
            return;
        }
        
        //if reservation is ACTIVE, then contact PSS
        if(coordinator.isAllowActiveModify() && resDetails.getStatus().equals(StateEngineValues.ACTIVE)){
            ModifyReqContent pssModifyRequest = new ModifyReqContent();
            pssModifyRequest.setTransactionId(this.getCoordRequest().getMessageProperties().getGlobalTransactionId());
            pssModifyRequest.setReservation(resDetails);
            try {
                pssModifyRequest.setCallbackEndpoint(coordinator.getCallbackEndpoint());
                PSSModifyPathAction pssModifyAction = new PSSModifyPathAction(
                        this.getName() + "-PSSModifyAction",
                        this.getCoordRequest(),
                        pssModifyRequest
                        );
                pssModifyAction.execute();
                if (pssModifyAction.getState() == CoordAction.State.FAILED) {
                    LOG.error(netLogger.error(method,ErrSev.MINOR, "pssModifyAction failed."));
                    this.fail(pssModifyAction.getException());
                    return;
                }
            } catch (OSCARSServiceException e) {
                LOG.error (netLogger.error(method, ErrSev.CRITICAL,"Error trying to send modify to PSS" + e));
                this.fail(e);
                return;
            }
        }
        
        // store reservation, previous domain vlans may have changed
        RMStoreAction rmStoreAction = new RMStoreAction(this.getName() + "-RMStoreAction",
                                                        this.getCoordRequest(),
                                                        resDetails);
        rmStoreAction.execute();
        if (rmStoreAction.getState() == CoordAction.State.FAILED) {
            LOG.error(netLogger.error(method,ErrSev.MINOR, "rmStoreAction failed."));
            this.fail(rmStoreAction.getException());
            return;
        }

        if (!localRes && !lastDomain) {
            try {
                // Send RESV_MODIFY_COMPLETED event to the next IDC
                String nextDomain = PathTools.getNextDomain (resDetails.getReservedConstraint().getPathInfo().getPath(),
                                                             PathTools.getLocalDomainId());
                ReservationCompletedForwarder forwarder = new ReservationCompletedForwarder (this.getName() + "-ModifyResvCompletedForwarder",
                                                                                             this.getCoordRequest(),
                                                                                             NotifyRequestTypes.RESV_MODIFY_COMPLETED,
                                                                                             nextDomain,
                                                                                             resDetails);
                forwarder.execute();
    
                if (forwarder.getState() == CoordAction.State.FAILED) {
                    LOG.error(netLogger.error(method,ErrSev.MAJOR,
                                              "notifyRequest failed in PCERuntimeAction.setResultData with exception " +
                                              forwarder.getException().getMessage()));
                    this.fail(forwarder.getException());
                    return;
                }
            } catch (OSCARSServiceException e) {
                LOG.error (netLogger.error(method, ErrSev.CRITICAL,"Cannot forward message " + e));
                this.fail(e);
                return;                        
            }
        }

        // Release the RuntimePCE global lock
        PCERuntimeAction.releaseMutex(this.getCoordRequest().getGRI());
        // notify that request is completed
        NotifyWorker.getInstance().sendInfo (this.getCoordRequest(),
                                                      NotifyRequestTypes.RESV_MODIFY_COMPLETED,
                                                      resDetails);

        this.setResultData(null);           
        this.executed();
    }  
}
