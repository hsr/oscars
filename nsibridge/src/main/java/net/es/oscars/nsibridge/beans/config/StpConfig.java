package net.es.oscars.nsibridge.beans.config;

public class StpConfig {
    private String stpId;
    private String oscarsId;
    private Integer oscarsVlan;
    public StpConfig() {

    }

    public String getStpId() {
        return stpId;
    }

    public void setStpId(String stpId) {
        this.stpId = stpId;
    }

    public String getOscarsId() {
        return oscarsId;
    }

    public void setOscarsId(String oscarsId) {
        this.oscarsId = oscarsId;
    }

    public Integer getOscarsVlan() {
        return oscarsVlan;
    }

    public void setOscarsVlan(Integer oscarsVlan) {
        this.oscarsVlan = oscarsVlan;
    }
}
