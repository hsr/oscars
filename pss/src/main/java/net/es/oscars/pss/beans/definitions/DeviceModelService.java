package net.es.oscars.pss.beans.definitions;

public class DeviceModelService {
    private String id;
    private String configGenerator;
    public DeviceModelService() {
        
    }
    public void setConfigGenerator(String configGenerator) {
        this.configGenerator = configGenerator;
    }
    public String getConfigGenerator() {
        return configGenerator;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

}
