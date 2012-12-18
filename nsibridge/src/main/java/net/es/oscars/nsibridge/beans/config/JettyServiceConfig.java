package net.es.oscars.nsibridge.beans.config;

import java.util.ArrayList;
import java.util.List;

public class JettyServiceConfig {
    private String path;
    private String implementor;
    public JettyServiceConfig() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getImplementor() {
        return implementor;
    }

    public void setImplementor(String implementor) {
        this.implementor = implementor;
    }

}
