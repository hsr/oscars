package net.es.oscars.pss.beans.config;

import java.util.HashMap;

public class GenericConfig {
    private String id;
    private String impl;
    private HashMap<String, Object> params;

    public void setImpl(String impl) {
        this.impl = impl;
    }

    public String getImpl() {
        return impl;
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

   
}
