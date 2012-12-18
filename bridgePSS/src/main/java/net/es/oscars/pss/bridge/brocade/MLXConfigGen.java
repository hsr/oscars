package net.es.oscars.pss.bridge.brocade;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.bridge.beans.DeviceBridge;
import net.es.oscars.pss.bridge.util.BridgeUtils;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.util.TemplateUtils;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class MLXConfigGen implements DeviceConfigGenerator {
    private Logger log = Logger.getLogger(MLXConfigGen.class);
    private HashMap<String, String> ifceAliases;
   
    @SuppressWarnings("unchecked")
    public MLXConfigGen() throws ConfigException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        cc.loadManifest(ServiceNames.SVC_PSS,  ConfigDefaults.MANIFEST); // manifest.yaml
        String configFilePath = cc.getFilePath("config-ifce-aliases.yaml");
        System.out.println("loading ifce aliases from "+configFilePath);
        MLXConfigGen.class.getClassLoader();
        try {
            InputStream propFile = new FileInputStream(new File(configFilePath));
            ifceAliases = (HashMap<String, String>) Yaml.load(propFile);
            try {
                propFile.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            System.err.println("ifce aliases file: "+ configFilePath + " not found");
            e.printStackTrace();
            System.exit(1);
        }

        
        for (String alias : ifceAliases.keySet()) {
            log.debug("alias "+alias+" maps to "+ifceAliases.get(alias));
        }

    }
    
    public String getConfig(PSSAction action, String deviceId) throws PSSException {
        switch (action.getActionType()) {
            case SETUP :
                return this.getSetup(action, deviceId);
            case TEARDOWN:
                return this.getTeardown(action, deviceId);
            case STATUS:
                return this.getStatus(action, deviceId);
            case MODIFY:
                throw new PSSException("Modify not supported");
        }
        throw new PSSException("Invalid action type");
    }
    
    private String getStatus(PSSAction action, String deviceId) throws PSSException {
        action.setStatus(ActionStatus.SUCCESS);
        return "";
    }
    private String getSetup(PSSAction action, String deviceId) throws PSSException {
        log.debug("getSetup start");
        
        ResDetails res = action.getRequest().getSetupReq().getReservation();
        
        return this.getLSPSetup(res, deviceId);
    }
    
    
    private String getTeardown(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        
        ResDetails res = action.getRequest().getTeardownReq().getReservation();
        
        return this.getLSPTeardown(res, deviceId);
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String getLSPTeardown(ResDetails res, String deviceId) throws PSSException {
        String templateFile = "brocade-bridge-teardown.txt";

        
        String portA;
        String portZ;
        String ifceVlan;
        
        
        DeviceBridge db = BridgeUtils.getDeviceBridge(deviceId, res);
        portA = db.getPortA();
        portZ = db.getPortZ();
        if (this.ifceAliases.keySet().contains(portA)) {
            portA = this.ifceAliases.get(portA);
        }
        if (this.ifceAliases.keySet().contains(portZ)) {
            portZ = this.ifceAliases.get(portZ);
        }
        
        ifceVlan = db.getVlanA();
        if (!ifceVlan.equals(db.getVlanZ())) {
            throw new PSSException("different VLANs not supported");
        }
       

        Map root = new HashMap();

        root.put("vlan", ifceVlan);
        root.put("portA", portA);
        root.put("portZ", portZ);
        
        String config       = TemplateUtils.generateConfig(root, templateFile);
        log.debug("getLSPTeardown done");
        return config;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String getLSPSetup(ResDetails res, String deviceId) throws PSSException  {

        String templateFile = "brocade-bridge-setup.txt";

        String portA;
        String portZ;
        String ifceVlan;
        
        
        DeviceBridge db = BridgeUtils.getDeviceBridge(deviceId, res);
        portA = db.getPortA();
        portZ = db.getPortZ();
        if (this.ifceAliases.keySet().contains(portA)) {
            portA = this.ifceAliases.get(portA);
        }
        if (this.ifceAliases.keySet().contains(portZ)) {
            portZ = this.ifceAliases.get(portZ);
        }

        ifceVlan = db.getVlanA();
        if (!ifceVlan.equals(db.getVlanZ())) {
            throw new PSSException("different VLANs not supported");
        }
       

        Map root = new HashMap();

        root.put("vlan", ifceVlan);
        root.put("portA", portA);
        root.put("portZ", portZ);
        
        String config       = TemplateUtils.generateConfig(root, templateFile);
        log.debug("getLSPSetup done");
        return config;
    }
    
    
    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }
    
 

}
