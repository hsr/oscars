package net.es.oscars.coord.locks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import net.es.oscars.coord.actions.LockAction;
import net.es.oscars.coord.actions.UnLockAction;

public class CoordLock {

    private static HashMap<String, CoordLock> locks = new HashMap<String, CoordLock>();
    private String                  name            = null;
    private LockAction              holdingAction   = null;
    private LinkedList<LockAction>  pendingActions  = new LinkedList<LockAction>(); 
    
    public static CoordLock getCoordLock (String name) {
        synchronized (CoordLock.locks) {
            CoordLock lock = CoordLock.locks.get(name);
            if (lock != null) {
                // A lock object of the specified name already exists. Return it.
                return lock;
            }
            // No lock of the specified name has been yet created. Create it and return it.
            lock = new CoordLock (name);
            CoordLock.locks.put (name, lock);
            return lock;
        }
    }
    
    private CoordLock (String name) {
        this.name = name;
    }
    
    public String getName () {
        return this.name;
    }
 
    public synchronized LinkedList<LockAction> getPendingActions() {
        return this.pendingActions;
    }
 
    public synchronized void requestLock(LockAction lockAction) {
        if (this.holdingAction != null) {
            // Lock already taken. Queue the query
            this.pendingActions.add(lockAction);
        } else {
            // Lock is free. Grant it.
            this.grantLock(lockAction);
        }      
    }
    
    public void release(UnLockAction action) {
        synchronized (this) {
            if ((action == null) || (this.holdingAction == null)) {
                // This should not happen. 
                // TODO: generate the proper exception. 
                return;
            }
        
            if (action.getCoordRequest() != this.holdingAction.getCoordRequest()) {
                // Trying to release a lock held by a different request must fail (bug in software)
                throw new RuntimeException ("Trying to release a lock held by another request.");
            }
            
            this.holdingAction = null;
        }
        
        action.executed(); 
        this.grantNextJob();
    }
    
    public synchronized void forceRelease() {
        
    }
    
    private synchronized void grantNextJob() {
        try {
            if (this.holdingAction != null) {
                // This probably should not happen, but if it does, do nothing
                // TODO: verify if this is an error or a valid case
                return;
            }
            LockAction nextAction = this.pendingActions.removeFirst();
            this.grantLock (nextAction);
            return;
        } catch (NoSuchElementException e) {
            // No pending job.Nothing to do
        }
    }

    /**
     * This private method must be called while holding the object lock.
     * 
     * @param req
     */
    private void grantLock (LockAction action) {
        if (this.holdingAction != null) {
            // This should not happen. 
            // TODO: generate the proper exception. 
            return;
        }
        
        this.holdingAction = action;
        // Grant lock to the job/request
        action.executed();        
    }
}
