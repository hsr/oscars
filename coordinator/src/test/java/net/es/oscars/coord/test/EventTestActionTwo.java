package net.es.oscars.coord.test;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.events.CoordEvent;
import net.es.oscars.coord.events.CoordEventListener;

public class EventTestActionTwo extends CoordRequest <String,Object > {

    private static final long       serialVersionUID  = 1L;
    private String eventName = null;
    private CoordEvent event = null;
    
    public EventTestActionTwo (String name, String eventName) {
        super (name,null);
        this.eventName = eventName;
    }

    public EventTestActionTwo(String gri, String name, String eventName) {
        super (gri, name);
        this.eventName = eventName;
    }
    
    public void execute() {
        // This action simply triggers the event.
        this.event = CoordEvent.getCoordEvent(this.eventName);
        if (this.event == null) {
            // This must not happen
            Thread.dumpStack();
            System.out.println ("Event is null !");
        } else {
            System.out.println ("Triggering event " + this.event.getName());
            event.trigger();
            this.executed();
        }
    }

    
}
