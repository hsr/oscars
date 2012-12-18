package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.locks.CoordLock;
import net.es.oscars.coord.actions.CoordAction;


public class UnLockAction extends CoordAction <String, Object>{
    private static final long       serialVersionUID = 1L;
    
    private CoordLock lock = null;

    @SuppressWarnings("unchecked")
    public UnLockAction (CoordRequest request, String name, String lockName) {
        super (name, request, lockName);
        this.lock = CoordLock.getCoordLock(lockName);
    }
    
    
    public void execute() {
        // Request a lock
        if (this.lock != null) {
            lock.release (this);
            this.executed();
        }
    }    
    
    public void executed () {
        if (this.lock != null) {
            this.setState(State.PROCESSED);
        }
    }
}
