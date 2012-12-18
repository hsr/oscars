package net.es.oscars.resourceManager.scheduler;

import net.es.oscars.api.soap.gen.v06.ResDetails;

public interface ReservationScheduler {

    /**
     * Adds a reservation to the scheduling system. The reservation is encapsulated within a ResDetails.
     * The invoker is responsible for re-scheduling a given reservation when its content has changed. A reservation
     * can be added at any time. 
     * 
     * @param resDetails
     */
    public void schedule (ResDetails resDetails);
    
    /**
     * Tell the scheduler to forget about a reservation.
     * @param resDetails for the reservation
     */
    public void forget (ResDetails resDetails);
 
}
