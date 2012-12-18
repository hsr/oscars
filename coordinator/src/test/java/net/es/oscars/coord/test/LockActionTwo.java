package net.es.oscars.coord.test;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.events.CoordEvent;
import net.es.oscars.coord.events.CoordEventListener;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.actions.LockAction;

public class LockActionTwo extends CoordRequest <String,Object > {

    public static final String HELLO_EVENT="LockTestHelloEvent";
    public static final String REQUESTED_LOCK_EVENT="LockTestHelloEvent";
    
    private static final long       serialVersionUID  = 1L;
    private String lockName = null;
    
    public class SyncAction extends CoordAction <String, Object> implements CoordEventListener {
        private static final long serialVersionUID = 1L;
        private boolean discoveredPeer = false;
        
        public SyncAction (CoordRequest<String, Object> request, String lockName) {
            super ("SyncAction-2", request,lockName);
            // Register itself as a listener to this event
            CoordEvent.getCoordEvent(LockActionOne.HELLO_EVENT_ONE).addListener(this);
        }
        
        public void execute () {
            CoordEvent.getCoordEvent(LockActionOne.HELLO_EVENT_TWO).trigger();
        }
        
        public void handleEvent(CoordEvent event) {
            if (event.getName() == LockActionOne.HELLO_EVENT_ONE) {
                // This is a notification from the LockActionTwo. 
                if (!this.discoveredPeer) {
                    // Send HELLO_BACK
                    this.discoveredPeer = true;
                    CoordEvent.getCoordEvent(LockActionOne.HELLO_EVENT_TWO).trigger();
                }
                // This action is now completed
                this.executed();
            }
        }        
    }
 
    public class ReadyAction extends CoordAction <String, Object> {
        private static final long serialVersionUID = 1L;
        
        public ReadyAction (CoordRequest<String, Object> request, String lockName) {
            super ("ReadyAction-2", request,lockName);
        }
        
        public void execute () {
            // Send REQUESTED_LOCK_EVENT
            CoordEvent.getCoordEvent(LockActionOne.REQUESTED_LOCK_EVENT).trigger();
        }
    }
 
    public class TerminatingAction extends CoordAction <String, Object> {
        private static final long serialVersionUID = 1L;
 
        public TerminatingAction (CoordRequest<String, Object> request, String lockName) {
            super ("TerminatingAction-2", request,lockName);
        }
        
        public void execute () {
            // Just set as executed
            this.executed();
        }
    }
    
    public LockActionTwo (String name, String lockName) {
        super ("LockActionTwo-2",null);
        this.lockName = lockName;        
    }

    public LockActionTwo(String gri, String name, String lockName) {
        super (gri, "LockActionTwo-2");
        this.lockName = lockName;
    }
    
    public void execute() {

        //---- Create the various actions of this request.
        // Wait for the peer to be ready
        SyncAction syncAction = new SyncAction (this, "SyncAction-2");
        this.add(syncAction);
        // Request to acquire the lock.
        LockAction lockAction = new LockAction (this, "LockAction-2", this.lockName);
        syncAction.add (lockAction);
        // Add the last action to be executed after the lock is granted
        TerminatingAction terminatingAction = new TerminatingAction (this, "TerminatingAction=2");
        lockAction.add(terminatingAction);
        
        // Signal the other peer that it can now release the lock. This action is to be executed at the same time as LockAction
        ReadyAction readyAction = new ReadyAction (this, "ReadyA2tion-2");
        syncAction.add (readyAction);
              
        this.process();
        // This action is completed
        this.executed();
    }
    
    
}
