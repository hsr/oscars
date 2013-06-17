package net.es.oscars.pss.beans.definitions;

import net.es.oscars.pss.beans.config.TemplateConfig;

public class DeviceModelService {
    private String id;
    private String configGenerator;
    private TemplateConfig templateConfig;
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

    public TemplateConfig getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig;
    }
}
