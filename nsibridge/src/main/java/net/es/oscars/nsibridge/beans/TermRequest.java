package net.es.oscars.nsibridge.beans;

public class TermRequest extends GenericRequest {
    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    private String connectionId;

}
