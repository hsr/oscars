package net.es.oscars.coord.test;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.events.CoordEvent;
import net.es.oscars.coord.events.CoordEventListener;

public class EventTestActionOne extends CoordRequest <String,Object > implements CoordEventListener {

    private static final long       serialVersionUID  = 1L;
    private String eventName = null;
    private CoordEvent event = null;
    
    public EventTestActionOne (String name, String eventName) {
        super (name, null);
        this.eventName = eventName;
    }

    public EventTestActionOne(String gri, String name, String eventName) {
        super (gri, name);
        this.eventName = eventName;
    }
    
    public void execute() {
        // This action simply listens for an event, and is completed when the event is triggered.
        this.event = CoordEvent.getCoordEvent(this.eventName);
        if (this.event == null) {
            // This must not happen
            Thread.dumpStack();
            System.out.println ("Event is null !");
        } else {
            // Add self as an event listener
            this.event.addListener(this);
        }
    }

    public void handleEvent(CoordEvent event) {
        if (event.getName() == this.eventName) {
            // This is the event we are looking for.
            System.out.println ("Event " + event.getName() + " has been received");
            this.executed();
        } else {
            Thread.dumpStack();
            System.out.println ("Got incorrect event: " + event.getName());
        }
    }
    
}
