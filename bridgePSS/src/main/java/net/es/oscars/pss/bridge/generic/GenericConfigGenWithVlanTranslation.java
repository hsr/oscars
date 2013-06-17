package net.es.oscars.pss.bridge.generic;


import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.api.TemplateDeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.beans.config.TemplateConfig;
import net.es.oscars.pss.bridge.beans.DeviceBridge;
import net.es.oscars.pss.bridge.util.BridgeUtils;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.util.TemplateUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GenericConfigGenWithVlanTranslation implements TemplateDeviceConfigGenerator {
    private Logger log = Logger.getLogger(GenericConfigGenWithVlanTranslation.class);
    private TemplateConfig templateConfig;

    public TemplateConfig getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig;
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

    
    
    private String getTeardown(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        
        ResDetails res = action.getRequest().getTeardownReq().getReservation();
        if (templateConfig == null) {
            throw new PSSException("no root template config!");
        } else if (templateConfig.getTemplates() == null || templateConfig.getTemplates().isEmpty()) {
            throw new PSSException("no template config!");

        } else if (templateConfig.getTemplates().get(ActionType.TEARDOWN.toString()) == null) {
            throw new PSSException("no template config for SETUP");
        }
        String templateFile = templateConfig.getTemplates().get(ActionType.TEARDOWN.toString());




        String portA;
        String portZ;
        String vlanA;
        String vlanZ;
        String gri = res.getGlobalReservationId();
        String description = res.getDescription();
        int bandwidth = res.getReservedConstraint().getBandwidth();



        DeviceBridge db = BridgeUtils.getDeviceBridge(deviceId, res);
        portA = db.getPortA();
        portZ = db.getPortZ();
        vlanA = db.getVlanA();
        vlanZ = db.getVlanZ();


        Map root = new HashMap();

        root.put("vlanA", vlanA);
        root.put("vlanZ", vlanZ);
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("description", description);
        root.put("gri", gri);
        root.put("bandwidth", bandwidth);


        String config       = TemplateUtils.generateConfig(root, templateFile);
        log.debug("getTeardown done");
        return config;



    }
    private String getSetup(PSSAction action, String deviceId) throws PSSException {
        log.debug("getSetup start");

        ResDetails res = action.getRequest().getSetupReq().getReservation();
        if (templateConfig == null) {
            throw new PSSException("no root template config!");
        } else if (templateConfig.getTemplates() == null || templateConfig.getTemplates().isEmpty()) {
            throw new PSSException("no template config!");

        } else if (templateConfig.getTemplates().get(ActionType.SETUP.toString()) == null) {
            throw new PSSException("no template config for SETUP");
        }
        String templateFile = templateConfig.getTemplates().get(ActionType.SETUP.toString());




        String portA;
        String portZ;
        String vlanA;
        String vlanZ;
        String gri = res.getGlobalReservationId();
        String description = res.getDescription();
        int bandwidth = res.getReservedConstraint().getBandwidth();



        DeviceBridge db = BridgeUtils.getDeviceBridge(deviceId, res);
        portA = db.getPortA();
        portZ = db.getPortZ();
        vlanA = db.getVlanA();
        vlanZ = db.getVlanZ();


        Map root = new HashMap();

        root.put("vlanA", vlanA);
        root.put("vlanZ", vlanZ);
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("description", description);
        root.put("gri", gri);
        root.put("bandwidth", bandwidth);


        String config       = TemplateUtils.generateConfig(root, templateFile);
        log.debug("getSetup done");
        return config;
    }
    
    
    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }
    
 

}
