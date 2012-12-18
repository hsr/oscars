package net.es.oscars.pss.eompls.resolve;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;


import org.ho.yaml.Yaml;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class YAMLEoMPLSDeviceResolver implements EoMPLSDeviceAddressResolver{
    private GenericConfig config = null;
    private HashMap<String, String> routerAddresses = null;
    private String yamlFilename = null;
    private Long lastUpdated = null;


    public YAMLEoMPLSDeviceResolver() {
        
    }
    
    public String getDeviceAddress(String deviceId) throws PSSException {
        if (this.config == null) {
            throw new PSSException("null config");
        } else if (this.yamlFilename == null) {
            throw new PSSException("null yaml filename");
        }
        this.checkIfConfigUpdated();

        if (routerAddresses == null) {
            throw new PSSException("empty config for routerAddresses");
        } else {
            String address = routerAddresses.get(deviceId.trim());
            if (address == null) {
                throw new PSSException("address not found for router: "+deviceId);
            } else {
                return address;
            }
        }
        
    }


    private void checkIfConfigUpdated() throws PSSException {
        File yamlFile = new File(yamlFilename);
        if (lastUpdated == null || yamlFile.lastModified() > lastUpdated) {
            this.loadYaml();
        }
    }

    private void loadYaml() throws PSSException {
        try {
            InputStream propFile =  new FileInputStream(new File(yamlFilename));
            routerAddresses = (HashMap<String, String>) Yaml.load(propFile);

        } catch (FileNotFoundException e) {
            throw new PSSException(e);
        }
        lastUpdated = new Date().getTime();
    }


    @SuppressWarnings({ "unchecked", "static-access" })
    public void setConfig(GenericConfig config) throws PSSException {
        if (config == null) {
            throw new PSSException("null config");
        } else if (config.getParams() == null) {
            throw new PSSException("no config parameters set");
        }
        
        this.config = config;
        String configFileName = (String)config.getParams().get("configFile");
        if (configFileName == null) {
            throw new PSSException("required configFile parameter not set");
        }
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        try {
            yamlFilename = cc.getFilePath(configFileName);
        } catch (ConfigException e) {
            throw new PSSException(e);
        }

        
        
    }


}
