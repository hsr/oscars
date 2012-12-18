package net.es.oscars.coord.events;


public interface CoordEventListener <P>{

    public void handleEvent (CoordEvent<P> event);
} 
