package net.es.oscars.nsibridge.beans;

public class RelRequest extends GenericRequest {
    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    private String connectionId;

}
