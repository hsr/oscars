package net.es.oscars.pss.api;


import net.es.oscars.pss.beans.config.TemplateConfig;

public interface TemplateDeviceConfigGenerator extends DeviceConfigGenerator {
    public void setTemplateConfig(TemplateConfig templateConfig);
}
