package net.es.oscars.pss.util;


import org.apache.log4j.Logger;

import net.es.oscars.pss.api.CircuitService;
import net.es.oscars.pss.api.ConnectorDirectory;
import net.es.oscars.pss.api.DeviceAddressResolver;
import net.es.oscars.pss.api.DeviceConnectorMap;
import net.es.oscars.pss.api.DeviceModelMap;
import net.es.oscars.pss.api.Notifier;
import net.es.oscars.pss.api.Validator;
import net.es.oscars.pss.api.Verifier;
import net.es.oscars.pss.api.Workflow;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.BaseConfig;
import net.es.oscars.pss.beans.definitions.CircuitServiceDefinition;
import net.es.oscars.pss.config.ConfigHolder;

/**
 * responsible for configuring & loading the various PSS agent classes
 * @author haniotak
 *
 */
public class ClassFactory {
   
    private ConnectorDirectory connectorDirectory;
    private DeviceConnectorMap deviceConnectorMap;
    private DeviceModelMap deviceModelMap;
    private DeviceAddressResolver deviceResolver;
    private Notifier notifier;
    private Verifier verifier;
    private Validator validator;
    private Workflow workflow;
    private CircuitService circuitService;
    
    private static ClassFactory instance;
    private Logger log = Logger.getLogger(ClassFactory.class);

    /**
     * singleton constructor
     * @return
     */
    private ClassFactory() {
    }

    public static ClassFactory getInstance() {
        if (instance == null) {
            instance = new ClassFactory();
        }
        return instance;
    }

    public void health() throws PSSException {
        if (connectorDirectory == null) {
            throw new PSSException("connectorDirectory not set");
        } else if (deviceConnectorMap == null) {
            throw new PSSException("deviceConnectorMap not set");
        } else if (deviceModelMap == null) {
            throw new PSSException("deviceModelMap not set");
        } else if (deviceResolver == null) {
            throw new PSSException("deviceResolver not set");
        } else if (notifier == null) {
            throw new PSSException("notifier not set");
        } else if (verifier == null) {
            throw new PSSException("verifier not set");
        } else if (validator == null) {
            throw new PSSException("validator not set");
        } else if (circuitService == null) {
            throw new PSSException("circuitService not set");
        } else if (workflow == null) {
            throw new PSSException("workflow not set");
        }
        log.debug("healthy ClassFactory");
    }


    /**
     * configures the agent factory through YAML from the argument filename
     * loads and configures agent classes
     *
     * @param filename
     */
    public void configure() throws PSSException {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        BaseConfig baseConfig = ConfigHolder.getInstance().getBaseConfig();
        if (baseConfig == null) {
            log.error("No base configuration stanza!");
            System.err.println("No base configuration stanza!");
            System.exit(1);
        }
        
        String connectionDirectoryCN = baseConfig.getConnectorDirectory().getImpl();
        String deviceConnectorMapCN = baseConfig.getDeviceConnectorMap().getImpl();
        String deviceModelMapCN = baseConfig.getDeviceModelMap().getImpl();
        String deviceResolverCN = baseConfig.getDeviceResolve().getImpl();
        String notifierCN = baseConfig.getNotify().getImpl();
        String verifierCN = baseConfig.getVerify().getImpl();
        String validatorCN = baseConfig.getValidate().getImpl();
        String workflowCN = baseConfig.getWorkflow().getImpl();
        
        String circuitServiceId = baseConfig.getCircuitService().getId();
        CircuitServiceDefinition csdef = ConfigHolder.getInstance().getCircuitServiceDefinition(circuitServiceId);
        String circuitServiceCN = csdef.getImpl();
        Class<?> aClass = null;        
        try {
            if (this.connectorDirectory == null) { 
                aClass = cl.loadClass(connectionDirectoryCN);
                connectorDirectory = (ConnectorDirectory) aClass.newInstance();
                connectorDirectory.setConfig(baseConfig.getConnectorDirectory());
                log.debug("connectionDirectory loaded OK: "+connectionDirectoryCN);
            }
            
            

            if (this.deviceConnectorMap == null) { 
                aClass = cl.loadClass(deviceConnectorMapCN);
                deviceConnectorMap = (DeviceConnectorMap) aClass.newInstance();
                deviceConnectorMap.setConfig(baseConfig.getDeviceConnectorMap());
                log.debug("deviceConnectorMap loaded OK: "+deviceConnectorMapCN);
            }

            if (this.deviceResolver == null) { 
                aClass = cl.loadClass(deviceModelMapCN);
                deviceModelMap = (DeviceModelMap) aClass.newInstance();
                deviceModelMap.setConfig(baseConfig.getDeviceModelMap());
                log.debug("deviceModelMap loaded OK: "+deviceModelMapCN);
            }
            
            if (this.deviceResolver == null) { 
                aClass = cl.loadClass(deviceResolverCN);
                deviceResolver = (DeviceAddressResolver) aClass.newInstance();
                deviceResolver.setConfig(baseConfig.getDeviceResolve());
                log.debug("resolver loaded OK: "+deviceResolverCN);
            }
            
            if (this.notifier == null) { 
                aClass = cl.loadClass(notifierCN);
                notifier = (Notifier) aClass.newInstance();
                notifier.setConfig(baseConfig.getNotify());
                log.debug("notifier loaded OK: "+notifierCN);
            }
            if (this.verifier == null) { 
                aClass = cl.loadClass(verifierCN);
                verifier = (Verifier) aClass.newInstance();
                verifier.setConfig(baseConfig.getVerify());
                log.debug("verifier loaded OK: "+verifierCN);
            }
            
            if (this.workflow == null) { 
                aClass = cl.loadClass(validatorCN);
                validator = (Validator) aClass.newInstance();
                validator.setConfig(baseConfig.getValidate());
                log.debug("validator loaded OK: "+validatorCN);
            }
            
            if (this.workflow == null) { 
                aClass = cl.loadClass(workflowCN);
                workflow = (Workflow) aClass.newInstance();
                workflow.setConfig(baseConfig.getWorkflow());
                log.debug("workflow loaded OK: "+workflowCN);
            }
            
            if (this.circuitService == null) { 
                aClass = cl.loadClass(circuitServiceCN);
                circuitService = (CircuitService) aClass.newInstance();
                circuitService.setConfig(baseConfig.getCircuitService());
                log.debug("circuitService loaded OK: "+circuitServiceCN);
            }
            

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new PSSException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new PSSException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new PSSException(e);
        }
        
    }

    public Notifier getNotifier() {
        return notifier;
    }

    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public DeviceAddressResolver getDeviceResolver() {
        return deviceResolver;
    }

    public void setDeviceResolver(DeviceAddressResolver deviceResolver) {
        this.deviceResolver = deviceResolver;
    }

    public ConnectorDirectory getConnectorDirectory() {
        return connectorDirectory;
    }

    public void setConnectorDirectory(ConnectorDirectory connectorDirectory) {
        this.connectorDirectory = connectorDirectory;
    }

    public DeviceModelMap getDeviceModelMap() {
        return deviceModelMap;
    }

    public void setDeviceModelMap(DeviceModelMap deviceModelMap) {
        this.deviceModelMap = deviceModelMap;
    }

    public DeviceConnectorMap getDeviceConnectorMap() {
        return deviceConnectorMap;
    }

    public void setDeviceConnectorMap(DeviceConnectorMap deviceConnectorMap) {
        this.deviceConnectorMap = deviceConnectorMap;
    }

    public CircuitService getCircuitService() {
        return circuitService;
    }

    public void setCircuitService(CircuitService circuitService) {
        this.circuitService = circuitService;
    }

    public Verifier getVerifier() {
        return verifier;
    }

    public void setVerifier(Verifier verifier) {
        this.verifier = verifier;
    }




}
