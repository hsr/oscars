package net.es.oscars.pss.connect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.api.ConnectorDirectory;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class YAMLConnectorDirectory implements ConnectorDirectory {
    private HashMap<String, GenericConfig> connectorDirEntries;
    private static Logger log = Logger.getLogger(YAMLConnectorDirectory.class);
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
            GenericConfig[] entries = null;
            entries = (GenericConfig[]) Yaml.loadType(propFile, GenericConfig[].class);
            connectorDirEntries = new HashMap<String, GenericConfig>();
            for (GenericConfig entry : entries) {
                connectorDirEntries.put(entry.getId(), entry);
                log.debug("loaded connector definition: "+entry.getId());
            }
        } catch (ConfigException e) {
            throw new PSSException(e);
        } catch (FileNotFoundException e) {
            throw new PSSException(e);
        }


        
    }

    @SuppressWarnings("rawtypes")
    public Connector getConnector(String connectorId) throws PSSException {
        if (this.connectorDirEntries == null) {
            throw new PSSException("null connector directory");
        }
        GenericConfig entry = connectorDirEntries.get(connectorId);
        if (entry == null) {
            throw new PSSException("No directory entry for "+connectorId);
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String connectorCN = entry.getImpl();
        Connector conn = null;
        try {
            Class aClass = cl.loadClass(connectorCN);
            conn = (Connector) aClass.newInstance();
            conn.setConfig(entry);
            
        } catch (ClassNotFoundException e) {
            throw new PSSException(e);
        } catch (InstantiationException e) {
            throw new PSSException(e);
        } catch (IllegalAccessException e) {
            throw new PSSException(e);
        }
        return conn;
    }

}
