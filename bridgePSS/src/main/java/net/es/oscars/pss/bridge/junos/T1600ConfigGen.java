package net.es.oscars.pss.bridge.junos;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.bridge.beans.DeviceBridge;
import net.es.oscars.pss.bridge.util.BridgeUtils;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.util.TemplateUtils;

public class T1600ConfigGen implements DeviceConfigGenerator {
    private Logger log = Logger.getLogger(T1600ConfigGen.class);
   

    
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
        String templateFile = "junos-t1600-bridge-teardown.txt";

        
        String portA;
        String portZ;
        String ifceVlan;
        
        
        DeviceBridge db = BridgeUtils.getDeviceBridge(deviceId, res);
        portA = db.getPortA();
        portZ = db.getPortZ();
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

        String templateFile = "junos-t1600-bridge-setup.txt";

        String portA;
        String portZ;
        String ifceVlan;
        
        
        DeviceBridge db = BridgeUtils.getDeviceBridge(deviceId, res);
        portA = db.getPortA();
        portZ = db.getPortZ();
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
