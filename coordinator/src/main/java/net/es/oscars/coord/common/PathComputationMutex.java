package net.es.oscars.coord.common;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.actions.CoordAction.State;
import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;

import org.apache.log4j.Logger;


/**
 * This class implements a mutual exclusion on the path computation part of the IDC: one one path can be computed at any given time.
 * The current implementation is rather simple, barely only encapsulating an object used as a lock. That allows to track who has the lock and
 * add log messages. If it is needed to add more semantics, this class will hold the implementation.
 **/


public class PathComputationMutex {
    
    private static final long       serialVersionUID  = 178923479L;
    private static final Logger LOG = Logger.getLogger(PathComputationMutex.class.getName());
    
    private boolean locked = false;
    private String  holdingGRI = null;    
    private List<CoordAction> pendingActions;
    
    public PathComputationMutex() {
        this.locked = false;
        this.holdingGRI = null;
        this.pendingActions= new ArrayList<CoordAction>();
    }
    

    @SuppressWarnings("unchecked")
    public synchronized boolean get (CoordAction action) throws InterruptedException {
        CoordRequest request = action.getCoordRequest();
        assert (request != null);
        String gri = request.getGRI();
        assert (gri != null);

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "getMutex";
        LOG.debug(netLogger.start(event, request.getName() + " requestGRI= " + gri +
                                  " holding gri= " + (this.holdingGRI != null ? this.holdingGRI : "None")));
        if ((this.locked) && ( ! this.holdingGRI.equals(gri))) {
            // Lock is already taken. Block until getting signaled
            LOG.debug(netLogger.getMsg(event,"wait for lock " + request.getName()  + 
                                       " holding gri= " + (this.holdingGRI != null ? this.holdingGRI : "None")));
            this.pendingActions.add(action);
            return false;
        }
        LOG.debug(netLogger.end(event, gri + " mutex is granted"));
        this.locked = true;
        this.holdingGRI = gri;
        return true;
    }
    
    public void release(String gri) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "releaseMutex";
        CoordAction nextAction = null;
        
        /* only synchronize this portion, otherwise potential deadlock if 
         * nextAction fails to schedule
         */
        synchronized(this) {
            if (this.holdingGRI == null) { return; }
            LOG.debug(netLogger.start(event, " holding gri= " + (this.holdingGRI != null ? this.holdingGRI : "None")));
            if ( ! gri.equals(this.holdingGRI)) {
                // do nothing.
                return;
            }
            if (this.locked) {
                this.locked = false;
                this.holdingGRI = null;
            }
            
            if(!this.pendingActions.isEmpty()){
                nextAction = this.pendingActions.remove(0);
                if(nextAction != null && nextAction.getCoordRequest() != null){
                    this.locked = true;
                    this.holdingGRI = nextAction.getCoordRequest().getGRI();
                }
            }
        }
        
        if(nextAction != null){
            LOG.debug("nextActionGRI=" + this.holdingGRI);
            nextAction.setState(State.UNPROCESSED);
            nextAction.process();
        }
        
        LOG.debug(netLogger.end(event));
    }
    
}
