package net.es.oscars.pss.beans.definitions;

public class DeviceModelDefinition {
    private String id;
    private DeviceModelService[] services;
    
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    
    public void setServices(DeviceModelService[] services) {
        this.services = services;
    }
    public DeviceModelService[] getServices() {
        return services;
    }
    
    public DeviceModelService getService(String serviceId) {
        for (DeviceModelService dms : services) {
            if (dms.getId().equals(serviceId)) {
                return dms;
            }
        }
        return null;
    }
    
    public DeviceModelDefinition() {
        
    }
    
}
