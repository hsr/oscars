package net.es.oscars.pss.beans;

import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.beans.definitions.DeviceModelDefinition;

public class Device {
    private String id;
    private DeviceModelDefinition model;
    private Connector connector;
    
    
    public void setModel(DeviceModelDefinition model) {
        this.model = model;
    }
    public DeviceModelDefinition getModel() {
        return model;
    }
    public void setConnector(Connector connector) {
        this.connector = connector;
    }
    public Connector getConnector() {
        return connector;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    

    
    
}
