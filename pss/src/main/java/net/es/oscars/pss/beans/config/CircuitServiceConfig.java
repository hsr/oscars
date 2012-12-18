package net.es.oscars.pss.beans.config;

import java.util.HashMap;

public class CircuitServiceConfig {
    private boolean stub;
    private boolean logRequest;
    private boolean logResponse;
    private String id;
    private HashMap<String, String> params;

    public boolean isStub() {
        return stub;
    }
    public void setStub(boolean stub) {
        this.stub = stub;
    }
    public boolean isLogRequest() {
        return logRequest;
    }
    public void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }
    public boolean isLogResponse() {
        return logResponse;
    }
    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }
    public HashMap<String, String> getParams() {
        return params;
    }

}
