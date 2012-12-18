package net.es.oscars.coord.test;

import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.req.CoordRequest;
import java.lang.Exception;

public class StubAction extends CoordAction <String,String > {

    private static final long       serialVersionUID  = 1L;
    boolean failMode = false;
 
    public class StubActionException extends Exception {
        private static final long serialVersionUID = 1L;

        public StubActionException (String reason) {
            super (reason);
        }       
    }
 
    public StubAction (String name, CoordRequest request, boolean failMode) {
        super (name, request, null);
        this.failMode = failMode;
    }

    public void execute() {
        if (this.failMode) {
            this.fail(new StubActionException ("Testing Failure"));
            return;
        }
        this.executed();
    }    
}
