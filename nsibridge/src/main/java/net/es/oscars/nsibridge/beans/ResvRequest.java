package net.es.oscars.nsibridge.beans;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ReservationRequestCriteriaType;

public class ResvRequest extends GenericRequest {
    protected ReservationRequestCriteriaType criteria;
    protected String globalReservationId;
    protected String description;
    protected String connectionId;

    public ResvRequest() {

    }

    public ReservationRequestCriteriaType getCriteria() {
        return criteria;
    }

    public void setCriteria(ReservationRequestCriteriaType criteria) {
        this.criteria = criteria;
    }

    public String getGlobalReservationId() {
        return globalReservationId;
    }

    public void setGlobalReservationId(String globalReservationId) {
        this.globalReservationId = globalReservationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

}
