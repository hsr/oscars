package net.es.oscars.coord.jobs;

import net.es.oscars.coord.events.CoordEvent;
import net.es.oscars.coord.events.CoordEventListener;

public class CoordEventStub {
        public CoordEvent event;
        public CoordEventListener listener;
        
        public CoordEventStub (CoordEvent event, CoordEventListener listener) {
            this.event = event;
            this.listener = listener;
        }
}
