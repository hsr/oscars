package net.es.oscars.nsibridge.beans;


public class NSIConnection {
    private String connectionId;

    private String oscarsGri;

    public String getOscarsGri() {
        return oscarsGri;
    }

    public void setOscarsGri(String oscarsGri) {
        this.oscarsGri = oscarsGri;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
