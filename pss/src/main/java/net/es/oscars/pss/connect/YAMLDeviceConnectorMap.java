package net.es.oscars.pss.connect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.api.ConnectorDirectory;
import net.es.oscars.pss.api.DeviceConnectorMap;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class YAMLDeviceConnectorMap implements DeviceConnectorMap {
    private HashMap<String, String> deviceConnectors;
    private static Logger log = Logger.getLogger(YAMLDeviceConnectorMap.class);

    public Connector getDeviceConnector(String deviceId) throws PSSException {
        if (deviceConnectors == null) {
            throw new PSSException("no device connector mapping set");
        }
        String connectorId = deviceConnectors.get(deviceId);
        if (connectorId == null) {
            throw new PSSException("connector id not found for: "+connectorId);
        }
        ConnectorDirectory cd = ClassFactory.getInstance().getConnectorDirectory();
        if (cd == null) {
            throw new PSSException("connector directory not set");
        }
        
        Connector conn = cd.getConnector(connectorId);
        return conn;
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
            Map entries = null;
            try {
                entries = (Map) Yaml.load(propFile);
            } catch (NullPointerException e) {
                throw new PSSException(e);
            }

            deviceConnectors = new HashMap<String, String>();
            deviceConnectors.putAll(entries);
            for (String device : deviceConnectors.keySet()) {
                log.debug("defined device "+device+" connector: "+deviceConnectors.get(device));
            }
            
        } catch (ConfigException e) {
            throw new PSSException(e);
        } catch (FileNotFoundException e) {
            throw new PSSException(e);
        }
        
        
    }

}
