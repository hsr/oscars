package net.es.oscars.pss.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import net.es.oscars.pss.beans.definitions.DeviceModelService;
import net.es.oscars.pss.enums.ActionType;
import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

import net.es.oscars.pss.api.ConfigurationStore;
import net.es.oscars.pss.api.DefinitionStore;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.BaseConfig;
import net.es.oscars.pss.beans.definitions.CircuitServiceDefinition;
import net.es.oscars.pss.beans.definitions.DeviceModelDefinition;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class ConfigHolder implements DefinitionStore, ConfigurationStore {
    private static Logger log = Logger.getLogger(ConfigHolder.class);
    private static ConfigHolder instance;
    private BaseConfig baseConfig;
    private HashMap<String, CircuitServiceDefinition> circuitServiceDefs = new HashMap<String, CircuitServiceDefinition>();
    private HashMap<String, DeviceModelDefinition> deviceModelDefs = new HashMap<String, DeviceModelDefinition>();

    private ConfigHolder() {
    }
    
    public static ConfigHolder getInstance() {
        if (instance == null) {
            instance = new ConfigHolder();
        }
        return instance;
    }
    
    public BaseConfig getBaseConfig() {
        return baseConfig;
    }

    public CircuitServiceDefinition getCircuitServiceDefinition(String serviceId) throws PSSException {
        if (this.circuitServiceDefs == null) {
            throw new PSSException("Circuit service definitions null or not loaded");
        }
        return this.circuitServiceDefs.get(serviceId);
    }

    public DeviceModelDefinition getDeviceModelDefinition(String modelId) throws PSSException {
        if (this.deviceModelDefs == null) {
            throw new PSSException("Device model definitions null or not loaded");
        }
        return this.deviceModelDefs.get(modelId);
    }
    
    
    

    @SuppressWarnings("static-access")
    public static void loadConfig(String filename) throws ConfigException {
        ConfigHolder holder = ConfigHolder.getInstance();

        BaseConfig configuration = null;
        InputStream propFile = ConfigHolder.class.getClassLoader().getSystemResourceAsStream(filename);
        try {
            configuration = (BaseConfig) Yaml.loadType(propFile, BaseConfig.class);
        } catch (NullPointerException ex) {
            try {
                propFile = new FileInputStream(new File(filename));
                configuration = (BaseConfig) Yaml.loadType(propFile, BaseConfig.class);
                System.out.println("loaded base config from " +filename);
            } catch (FileNotFoundException e) {
                System.out.println("ConfigHelper: configuration file: "+ filename + " not found");
                e.printStackTrace();
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("ConfigHelper: configuration file: "+ filename + " not found");
            e.printStackTrace();
            System.exit(1);
        }
        holder.setBaseConfig(configuration);
        try {
            propFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        loadDefinitions();
    }
    

    
    
    @SuppressWarnings("static-access")
    protected static void loadDefinitions() throws ConfigException {
        ConfigHolder holder = ConfigHolder.getInstance();
        BaseConfig configuration = holder.getBaseConfig();

        String modelsDefFile = configuration.getDefinitions().getModels();
        
        String modelsDefFullPath = ContextConfig.getInstance(ServiceNames.SVC_PSS).getFilePath(modelsDefFile);
        System.err.println("models defined at: "+modelsDefFile+" full path: "+modelsDefFullPath);

        InputStream propFile = ConfigHolder.class.getClassLoader().getSystemResourceAsStream(modelsDefFullPath);
        DeviceModelDefinition[] mds = null;
        
        try {
            mds = (DeviceModelDefinition[]) Yaml.loadType(propFile,DeviceModelDefinition[].class);
        } catch (NullPointerException ex) {
            try {
                propFile = new FileInputStream(new File(modelsDefFullPath));
                mds = (DeviceModelDefinition[]) Yaml.loadType(propFile, DeviceModelDefinition[].class);
 
            } catch (FileNotFoundException e) {
                System.err.println("ConfigHelper: configuration file: "+ modelsDefFullPath + " not found");
                e.printStackTrace();
                System.exit(1);
            }
       } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            propFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        
        HashMap<String, DeviceModelDefinition> mdhm = new HashMap<String, DeviceModelDefinition>();
        for (DeviceModelDefinition md : mds) {
            System.err.println("loaded model definition: "+md.getId()+" "+md);
            mdhm.put(md.getId(), md);
            for (DeviceModelService dms : md.getServices()) {
                System.err.println("loaded model service: "+dms.getId()+" "+dms.getConfigGenerator());
                if (dms.getTemplateConfig() == null || dms.getTemplateConfig().getTemplates().isEmpty()) {
                    System.err.println("  no template config");
                } else {
                    HashMap<String, String> tpts = dms.getTemplateConfig().getTemplates();
                    for (String at : tpts.keySet()) {
                        System.err.println("   "+at+": "+tpts.get(at));
                    }

                }

            }
        }
        holder.setDeviceModelDefs(mdhm);
        
        if (mds.length == 0) {
            throw new ConfigException("no device models defined");
        }

        
        
        String servicesDefFile = configuration.getDefinitions().getServices();
        
        String servicesDefFullPath = ContextConfig.getInstance(ServiceNames.SVC_PSS).getFilePath(servicesDefFile);
        
        System.err.println("services defined at: "+servicesDefFile+" full path: "+servicesDefFullPath);

        propFile = ConfigHolder.class.getClassLoader().getSystemResourceAsStream(servicesDefFullPath);
        CircuitServiceDefinition[] cds = null;
        
        try {
            cds = (CircuitServiceDefinition[]) Yaml.loadType(propFile,CircuitServiceDefinition[].class);
        } catch (NullPointerException ex) {
            try {
                propFile = new FileInputStream(new File(servicesDefFullPath));
                cds = (CircuitServiceDefinition[]) Yaml.loadType(propFile,CircuitServiceDefinition[].class);
            } catch (FileNotFoundException e) {
                System.out.println("ConfigHelper: configuration file: "+ servicesDefFullPath + " not found");
                e.printStackTrace();
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            propFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (cds.length == 0) {
            throw new ConfigException("no circuit services defined");
        }
        
        HashMap<String, CircuitServiceDefinition> cdhm = new HashMap<String, CircuitServiceDefinition>();
        for (CircuitServiceDefinition cd : cds) {
            System.out.println("loaded service definition: "+cd.getId()+" "+cd.getImpl());
            cdhm.put(cd.getId(), cd);
        }
        holder.setCircuitServiceDefs(cdhm);
        
        
    }
    
    public void setBaseConfig(BaseConfig baseConfig) {
        this.baseConfig = baseConfig;
    }

    public void setCircuitServiceDefs(HashMap<String, CircuitServiceDefinition> circuitServiceDefs) {
        this.circuitServiceDefs = circuitServiceDefs;
    }

    public HashMap<String, CircuitServiceDefinition> getCircuitServiceDefs() {
        return circuitServiceDefs;
    }

    public void setDeviceModelDefs(HashMap<String, DeviceModelDefinition> deviceModelDefs) {
        this.deviceModelDefs = deviceModelDefs;
    }

    public HashMap<String, DeviceModelDefinition> getDeviceModelDefs() {
        return deviceModelDefs;
    }

    

}
