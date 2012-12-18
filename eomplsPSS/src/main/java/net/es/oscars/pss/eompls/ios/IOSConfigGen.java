package net.es.oscars.pss.eompls.ios;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.eompls.beans.LSP;
import net.es.oscars.pss.eompls.junos.SDNNameGenerator;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;

public class IOSConfigGen implements DeviceConfigGenerator {
    private Logger log = Logger.getLogger(IOSConfigGen.class);
   

    
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
        
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);
        
        if (sameDevice) {
            throw new PSSException("Same device crossconnects not supported on IOS");
        } else {
            return this.getLSPSetup(res, deviceId);
        }
    }
    
    
    private String getTeardown(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        
        ResDetails res = action.getRequest().getTeardownReq().getReservation();
        
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);
        
        if (sameDevice) {
            throw new PSSException("Same device crossconnects not supported on IOS");
        } else {
            return this.getLSPTeardown(res, deviceId);
        }
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String getLSPTeardown(ResDetails res, String deviceId) throws PSSException {
        String templateFile = "ios-lsp-teardown.txt";

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        
        String policyName;
        String pathName;
        String lspName;
        String ifceName;
        String ifceVlan;
        
        
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        String gri = res.getGlobalReservationId();
        

        policyName              = ng.getPolicyName(gri);
        pathName                = ng.getPathName(gri);
        lspName                 = ng.getLSPName(gri);
        
        
        ReservedConstraintType rc = res.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();

        List<CtrlPlaneHopContent> localHops;
        try {
            localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            throw new PSSException(e);
        }
        
        CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
        CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();
                
        String srcLinkId = ingressLink.getId();
        URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
        String dstLinkId = egressLink.getId();
        URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
        

        log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
        if (srcDeviceId.equals(deviceId)) {
            // forward direction
            log.debug("forward");
            ifceName = srcRes.getPortId();
            ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
        } else {
            // reverse direction
            log.debug("reverse");
            ifceName = dstRes.getPortId();
            ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
        }

        Map root = new HashMap();
        Map policy = new HashMap();
        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();

        // set up data model structure
        root.put("policy", policy);
        root.put("path", path);
        root.put("lsp", lsp);
        root.put("ifce", ifce);

        // fill in scalars
        policy.put("name", policyName);
        
        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);
        
        path.put("name", pathName);
        
        lsp.put("name", lspName);

        policy.put("name", policyName);

        
        
        
        String config       = EoMPLSUtils.generateConfig(root, templateFile);
        log.debug("getLSPSetup done");
        return config;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String getLSPSetup(ResDetails res, String deviceId) throws PSSException  {

        String templateFile = "ios-lsp-setup.txt";

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);

        String ifceName;
        String ifceDescription;
        String ifceVlan;
        String egressVlan;
        
        String policyName;
        Long lspBandwidth;
        String pathName;
        String lspName;
        
        String l2circuitVCID;
        String l2circuitEgress;
        String l2circuitDescription;

        
        EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */
        

        ReservedConstraintType rc = res.getReservedConstraint();
        Integer bw = rc.getBandwidth();
        PathInfo pi = rc.getPathInfo();
       
        List<CtrlPlaneHopContent> localHops;
        try {
            localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            throw new PSSException(e);
        }
        
        CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
        CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();
                
        String srcLinkId = ingressLink.getId();
        URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
        String dstLinkId = egressLink.getId();
        URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
        
        EoMPLSIfceAddressResolver iar = ecf.getEomplsIfceAddressResolver();
        EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();
        
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        String gri = res.getGlobalReservationId();
        

        
        // bandwidth in Mbps 
        lspBandwidth = 1000L*bw;
        
        String lspTargetDeviceId;
        boolean reverse = false;
        log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
        if (srcDeviceId.equals(deviceId)) {
            // forward direction
            log.debug("forward");
            ifceName = srcRes.getPortId();
            ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            egressVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            lspTargetDeviceId = dstRes.getNodeId();
        } else {
            // reverse direction
            log.debug("reverse");
            ifceName = dstRes.getPortId();
            ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            egressVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            lspTargetDeviceId = srcRes.getNodeId();
            reverse = true;
        }
        
        if (!ifceVlan.equals(egressVlan)) {
            throw new PSSException("Must specify same VLAN for ingress and egress if any edge is an IOS device");
        }
        
        LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);

    
        ifceDescription = ng.getInterfaceDescription(gri, lspBandwidth);

        policyName              = ng.getPolicyName(gri);
        pathName                = ng.getPathName(gri);
        lspName                 = ng.getLSPName(gri);
        l2circuitDescription    = ng.getL2CircuitDescription(gri);
        l2circuitEgress         = dar.getDeviceAddress(lspTargetDeviceId);
        l2circuitVCID           = EoMPLSUtils.genIOSVCId(ifceName, ifceVlan);

        // create data model objects
        Map root = new HashMap();
        Map policy = new HashMap();
        Map tunnel = new HashMap();
        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();
        Map l2circuit = new HashMap();

        // set up data model structure
        root.put("policy", policy);
        root.put("tunnel", tunnel);
        root.put("path", path);
        root.put("lsp", lsp);
        root.put("ifce", ifce);
        root.put("l2circuit", l2circuit);

        // fill in scalars
        
        policy.put("name", policyName);
        
        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);
        ifce.put("description", ifceDescription);
        
        path.put("hops", lspBean.getPathAddresses());
        path.put("name", pathName);
        path.put("egressLoopback", l2circuitEgress);
        
        lsp.put("name", lspName);
        lsp.put("from", lspBean.getFrom());
        lsp.put("to", lspBean.getTo());
        lsp.put("bandwidth", lspBandwidth);
        tunnel.put("description", l2circuitDescription);

        l2circuit.put("vcid", l2circuitVCID);
        
        
        policy.put("name", policyName);

        String config       = EoMPLSUtils.generateConfig(root, templateFile);

        log.debug("getLSPSetup done");
        return config;
    }
    
    
    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }


}
