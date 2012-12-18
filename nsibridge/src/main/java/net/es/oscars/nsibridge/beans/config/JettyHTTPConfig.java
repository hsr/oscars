package net.es.oscars.nsibridge.beans.config;


import java.util.ArrayList;
import java.util.List;

public class JettyHTTPConfig {
    private Integer port = 8080;
    private String hostname = "localhost";

    public JettyHTTPConfig(){

    };

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

}
