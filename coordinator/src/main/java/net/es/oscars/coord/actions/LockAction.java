package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.locks.CoordLock;


public class LockAction extends CoordAction <String, Object> {

    private static final long serialVersionUID = 1439115737928915954L;
    private CoordLock lock = null;

    @SuppressWarnings("unchecked")
    public LockAction (CoordRequest request, String name, String lockName) {
        super (name, request, lockName);
        this.lock = CoordLock.getCoordLock(lockName);
    }
    
    
    public void execute() {
        // Request a lock
        if (this.lock != null) {
            this.lock.requestLock (this);
        }
    }  
    
    public void executed() {
       if (this.lock != null) {
           this.setState(State.PROCESSED);
       }
    }
}
