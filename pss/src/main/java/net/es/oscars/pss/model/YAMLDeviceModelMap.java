package net.es.oscars.pss.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

import net.es.oscars.pss.api.DeviceModelMap;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class YAMLDeviceModelMap implements DeviceModelMap {
    private static Logger log = Logger.getLogger(YAMLDeviceModelMap.class);
    private HashMap<String, String> deviceModels;
    public YAMLDeviceModelMap() {
        
    }
    public String getDeviceModel(String deviceId) throws PSSException {
        if (deviceModels == null) {
            throw new PSSException("empty device model map");
        }
        String modelId = deviceModels.get(deviceId);
        return modelId;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setConfig(GenericConfig config) throws PSSException {
        if (config == null) {
            throw new PSSException("null config");
        } else if (config.getParams() == null) {
            throw new PSSException("no config parameters set");
        }
        
        String configFileName = (String)config.getParams().get("configFile");
        if (configFileName == null) {
            throw new PSSException("required configFile parameter not set");
        }
        
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        try {
            String configFilePath = cc.getFilePath(configFileName);
            InputStream propFile =  new FileInputStream(new File(configFilePath));
            Map entries = (Map) Yaml.load(propFile);
            deviceModels = new HashMap<String, String>();
            deviceModels.putAll(entries);
            for (String device : deviceModels.keySet()) {
                log.debug("added device "+device+" with model "+deviceModels.get(device));
            }
            
        } catch (ConfigException e) {
            throw new PSSException(e);
        } catch (FileNotFoundException e) {
            throw new PSSException(e);
        }
        
    }
    

}
