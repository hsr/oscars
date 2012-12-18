package net.es.oscars.nsibridge.beans.config;

import java.util.List;

public class JettyConfig {
    private JettyServiceConfig[] services;
    private JettyAuthConfig auth;
    private JettySSLConfig ssl;
    private JettyHTTPConfig http;
    public JettyConfig() {

    }

    public JettyServiceConfig[] getServices() {
        return services;
    }

    public void setServices(JettyServiceConfig[] services) {
        this.services = services;
    }

    public JettyAuthConfig getAuth() {
        return auth;
    }

    public void setAuth(JettyAuthConfig auth) {
        this.auth = auth;
    }

    public JettySSLConfig getSsl() {
        return ssl;
    }

    public void setSsl(JettySSLConfig ssl) {
        this.ssl = ssl;
    }

    public JettyHTTPConfig getHttp() {
        return http;
    }

    public void setHttp(JettyHTTPConfig http) {
        this.http = http;
    }
}
