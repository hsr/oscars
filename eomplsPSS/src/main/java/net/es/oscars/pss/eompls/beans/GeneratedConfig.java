package net.es.oscars.pss.eompls.beans;

import net.es.oscars.database.hibernate.HibernateBean;

import java.io.Serializable;

public class GeneratedConfig extends HibernateBean implements Serializable {
    private static final long serialVersionUID = 8001;

    private String gri;
    private String config;
    private String phase;


    private String deviceId;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getGri() {
        return gri;
    }

    public void setGri(String gri) {
        this.gri = gri;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public GeneratedConfig() {}


}
