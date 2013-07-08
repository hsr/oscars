package net.es.oscars.pss.eompls.alu;


import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.eompls.beans.LSP;
import net.es.oscars.pss.eompls.dao.GCUtils;
import net.es.oscars.pss.eompls.junos.SDNNameGenerator;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.eompls.util.VPLS_DomainIdentifiers;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.pss.util.VlanGroupConfig;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SR_VPLS_ConfigGen implements DeviceConfigGenerator {
    private Logger log = Logger.getLogger(SR_VPLS_ConfigGen.class);
    public SR_VPLS_ConfigGen() throws ConfigException, PSSException {
        VlanGroupConfig.configure();

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
    public String getSetup(PSSAction action, String deviceId) throws PSSException {
        log.debug("getSetup start");
        return this.onSetup(action, deviceId);
    }

    public String getTeardown(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        return this.onTeardown(action, deviceId);

    }

    private String onSetup(PSSAction action, String deviceId) throws PSSException {

        ResDetails res = action.getRequest().getSetupReq().getReservation();
        String gri = res.getGlobalReservationId();
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);

        String[] deviceIds =  { srcDeviceId, dstDeviceId };




        // TODO: just one SDP for now; fix for multipoint.
        Integer numSdps = 1;

        /*
        for both devices:
        - check if identifiers exist
        - generate identifiers as needed
        - generate setup and teardown config (unless it has been already generated)
         */

        VPLS_DomainIdentifiers gids = VPLS_DomainIdentifiers.reserve(gri);

        SR_VPLS_DeviceIdentifiers ids = SR_VPLS_DeviceIdentifiers.retrieve(gri, deviceId);
        if (ids == null) {
            log.info("no saved device identifiers found for gri: "+gri+" device: "+deviceId+", generating now");
            ids = SR_VPLS_DeviceIdentifiers.reserve(gri, deviceId, gids.getVplsId(), numSdps);
        }

        String devSetupConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.SETUP);
        if (devSetupConfig == null) {
            log.info("no saved setup config found for gri: "+gri+" device: "+deviceId+", generating now");
            SR_VPLS_TemplateParams params = this.getSetupTemplateParams(res, deviceId, gids, ids);
            devSetupConfig= this.generateConfig(params, ActionType.SETUP);
            GCUtils.storeDeviceConfig(gri, deviceId, ActionType.SETUP, devSetupConfig);
        }

        String devTeardownConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.TEARDOWN);
        if (devTeardownConfig == null) {
            log.info("no saved teardown config found for gri: "+gri+" device: "+deviceId+", generating now");
            SR_VPLS_TemplateParams params = this.getTeardownTemplateParams(res, deviceId, gids, ids);
            devTeardownConfig = this.generateConfig(params, ActionType.TEARDOWN);
            GCUtils.storeDeviceConfig(gri, deviceId, ActionType.TEARDOWN, devTeardownConfig);
        }

        String setupConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.SETUP);
        if (setupConfig == null) {
            this.onError("could not retrieve setup device config for gri: "+gri+" device: "+deviceId);
        }

        return setupConfig;
    }

    private String onTeardown(PSSAction action, String deviceId) throws PSSException {

        /*
       for this device:
       - check if identifiers exist
       - throw error if not
       - generate teardown config (unless it has been already generated)
       - release identifiers
        */
        ResDetails res = action.getRequest().getTeardownReq().getReservation();
        String gri = res.getGlobalReservationId();

        VPLS_DomainIdentifiers gids = VPLS_DomainIdentifiers.release(gri);


        SR_VPLS_DeviceIdentifiers ids = SR_VPLS_DeviceIdentifiers.retrieve(gri, deviceId);
        if (ids == null) {
            this.onError("no saved identifiers found for gri: "+gri+" device: "+deviceId);
        }

        String teardownConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.TEARDOWN);
        if (teardownConfig == null) {
            log.info("no saved teardown config found for gri: "+gri+" device: "+deviceId+", generating now");
            SR_VPLS_TemplateParams params = this.getTeardownTemplateParams(res, deviceId, gids, ids);
            teardownConfig = this.generateConfig(params, ActionType.TEARDOWN);
            GCUtils.storeDeviceConfig(gri, deviceId, ActionType.TEARDOWN, teardownConfig);
        }

        SR_VPLS_DeviceIdentifiers.release(gri, deviceId);
        return teardownConfig;

    }

    private void onError(String errStr) throws PSSException {
        log.error(errStr);
        throw new PSSException(errStr);
    }

    public String generateConfig(SR_VPLS_TemplateParams params, ActionType phase)  throws PSSException {
        String templateFile = null;

        if (phase.equals(ActionType.SETUP)) {
            templateFile = "alu-vpls-setup.txt";
        } else if (phase.equals(ActionType.TEARDOWN)) {
            templateFile = "alu-vpls-teardown.txt";
        } else {
            this.onError("invalid phase");
        }
        Map root = new HashMap();

        root.put("vpls", params.getVpls());
        root.put("ingqos", params.getIngqos());
        root.put("ifces", params.getIfces());
        root.put("paths", params.getPaths());
        root.put("sdps", params.getSdps());
        root.put("lsps", params.getLsps());

        String config = EoMPLSUtils.generateConfig(root, templateFile);
        return config;
    }


    private SR_VPLS_TemplateParams getTeardownTemplateParams(ResDetails res, String deviceId, VPLS_DomainIdentifiers gids, SR_VPLS_DeviceIdentifiers ids) throws PSSException  {
        String gri = res.getGlobalReservationId();

        ArrayList ifces = new ArrayList();
        ArrayList paths = new ArrayList();
        ArrayList lsps = new ArrayList();
        ArrayList sdps = new ArrayList();
        HashMap vpls = new HashMap();
        HashMap ingqos = new HashMap();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);


        ALUNameGenerator ng = ALUNameGenerator.getInstance();



        // fill in scalars
        /*
        1. vpls: id
        2. ingqos: id
        3. ifces: list_of <name, vlan>
        4. paths: list_of <name>
        5. lsps: list_of <name>
        6. sdps: list_of <id>
        */


        HashMap<String, ArrayList<SRIfceInfo>> allIfceInfos = this.getDeviceIfceInfo(res);
        ArrayList<SRIfceInfo> deviceIfceInfos = allIfceInfos.get(deviceId);
        for (SRIfceInfo ifceInfo : deviceIfceInfos) {
            Map ifce = new HashMap();
            ifce.put("name", ifceInfo.getName());
            ifce.put("vlan", ifceInfo.getVlan());
            ifces.add(ifce);
        }


        if (!sameDevice) {
            String pathName                = ng.getPathName(gri);
            String lspName                 = ng.getLSPName(gri);

            Map lsp = new HashMap();
            Map path = new HashMap();

            path.put("name", pathName);
            lsp.put("name", lspName);

            paths.add(path);
            lsps.add(lsp);
            for (Integer sdpId : ids.getSdpIds()) {
                Map sdp = new HashMap();
                sdp.put("id", sdpId.toString());
                sdps.add(sdp);
            }

        } else {
            paths = null;
            lsps = null;
            sdps = null;

        }

        ingqos.put("id", ids.getQosId().toString());
        vpls.put("id", gids.getVplsId().toString());

        SR_VPLS_TemplateParams params = new SR_VPLS_TemplateParams();
        params.setIfces(ifces);
        params.setIngqos(ingqos);
        params.setLsps(lsps);
        params.setPaths(paths);
        params.setSdps(sdps);
        params.setVpls(vpls);

        return params;

    }
    private SR_VPLS_TemplateParams getSetupTemplateParams(ResDetails res, String deviceId, VPLS_DomainIdentifiers gids, SR_VPLS_DeviceIdentifiers ids) throws PSSException  {

        ArrayList ifces = new ArrayList();
        ArrayList paths = new ArrayList();
        ArrayList lsps = new ArrayList();
        ArrayList sdps = new ArrayList();
        HashMap vpls = new HashMap();
        HashMap ingqos = new HashMap();


        String gri = res.getGlobalReservationId();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);

        System.out.println("gri: "+gri+" src device: " + srcDeviceId + " this one: " + deviceId);


        ALUNameGenerator ng = ALUNameGenerator.getInstance();
        SDNNameGenerator sdng = SDNNameGenerator.getInstance();

        EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
        /* *********************** */


        /* BEGIN POPULATING VALUES */
        /* *********************** */


        ReservedConstraintType rc = res.getReservedConstraint();
        Integer bw = rc.getBandwidth();
        PathInfo pi = rc.getPathInfo();
        String description = res.getDescription();

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




        // bandwidth in Mbps
        Long ingQosBandwidth = 1L*bw;


        if (!sameDevice) {
            String lspTargetDeviceId, lspOriginDeviceId;
            boolean reverse = false;
            log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
            if (srcDeviceId.equals(deviceId)) {
                // forward direction
                log.debug("forward");

                lspOriginDeviceId = srcRes.getNodeId();
                lspTargetDeviceId = dstRes.getNodeId();
            } else {
                // reverse direction
                log.debug("reverse");
                lspOriginDeviceId = dstRes.getNodeId();
                lspTargetDeviceId = srcRes.getNodeId();
                reverse = true;
            }
            LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);


            int i = 5;
            ArrayList hops = new ArrayList();
            for (String ipaddress : lspBean.getPathAddresses()) {
                Map hop = new HashMap();
                hop.put("address", ipaddress);
                hop.put("order", i);
                i += 5;
                hops.add(hop);
            }
            HashMap path = new HashMap();
            path.put("name", ng.getPathName(gri));
            path.put("hops", hops);
            paths.add(path);



            String lspFrom         = dar.getDeviceAddress(lspOriginDeviceId);
            String lspTo           = dar.getDeviceAddress(lspTargetDeviceId);

            HashMap lsp = new HashMap();
            lsp.put("from", lspFrom);
            lsp.put("to", lspTo);
            lsp.put("name", ng.getLSPName(gri));
            lsp.put("path", ng.getPathName(gri));
            lsps.add(lsp);


            for (Integer sdpId : ids.getSdpIds()) {
                HashMap sdp = new HashMap();
                sdp.put("id", sdpId.toString());
                sdp.put("description", gri);
                sdp.put("far_end", lspTo);
                sdp.put("lsp_name", ng.getLSPName(gri));
                sdps.add(sdp);
            }

        } else {
            sdps = null;
            paths = null;
            lsps = null;
        }

        /*
        1. vpls: id, description
        2. ingqos: id, description, bandwidth
        3. ifces: list_of <name, vlan>
        4. paths: list_of <name, hops>
                                 hops: list_of: <address, order>
        5. lsps: list_of <from, to, name, path>
        6. sdps: list_of <id, description, far_end, lsp_name>

        notes
           each sdp.lsp_name should correspond to an lsp.name,
              and the sdp.far_end for that should correspond to the lsp.to
        */

        String vplsId = gids.getVplsId().toString();
        vpls.put("id", vplsId);

        String vplsDesc = sdng.getVplsDescription(gri, ingQosBandwidth*1000000, description);
        vpls.put("description", vplsDesc);



        String qosId  = ids.getQosId().toString();
        ingqos.put("id", qosId);
        ingqos.put("description", gri);
        ingqos.put("bandwidth", ingQosBandwidth);


        HashMap<String, ArrayList<SRIfceInfo>> allIfceInfos = this.getDeviceIfceInfo(res);
        ArrayList<SRIfceInfo> deviceIfceInfos = allIfceInfos.get(deviceId);
        for (SRIfceInfo ifceInfo : deviceIfceInfos) {
            Map ifce = new HashMap();
            ifce.put("name", ifceInfo.getName());
            ifce.put("vlan", ifceInfo.getVlan());
            String sapDesc = sdng.getInterfaceDescription(gri, ingQosBandwidth*1000000, description);
            ifce.put("description", sapDesc);
            ifces.add(ifce);
        }



        SR_VPLS_TemplateParams params = new SR_VPLS_TemplateParams();
        params.setIfces(ifces);
        params.setIngqos(ingqos);
        params.setLsps(lsps);
        params.setPaths(paths);
        params.setSdps(sdps);
        params.setVpls(vpls);
        return params;

    }
    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }
    private HashMap<String, ArrayList<SRIfceInfo>> getDeviceIfceInfo(ResDetails res) throws PSSException {
        String gri = res.getGlobalReservationId();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);

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

        String srcVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
        String dstVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();

        ArrayList<String> srcVlans = VlanGroupConfig.getVlans(srcDeviceId, srcRes.getPortId(), srcVlan);
        ArrayList<String> dstVlans = VlanGroupConfig.getVlans(dstDeviceId, dstRes.getPortId(), dstVlan);


        HashMap<String, ArrayList<SRIfceInfo>> deviceIfceInfo = new HashMap<String, ArrayList<SRIfceInfo>>();
        deviceIfceInfo.put(srcDeviceId, new ArrayList<SRIfceInfo>());
        deviceIfceInfo.put(dstDeviceId, new ArrayList<SRIfceInfo>());
        for (String vlan : srcVlans) {
            SRIfceInfo aIfceInfo = new SRIfceInfo();
            aIfceInfo.setDescription(gri);
            aIfceInfo.setName(srcRes.getPortId());
            aIfceInfo.setVlan(vlan);
            log.debug("setting "+srcDeviceId+" : "+aIfceInfo.getName());
            deviceIfceInfo.get(srcDeviceId).add(aIfceInfo);
        }

        for (String vlan : dstVlans) {
            SRIfceInfo zIfceInfo = new SRIfceInfo();
            zIfceInfo.setDescription(gri);
            zIfceInfo.setName(dstRes.getPortId());
            zIfceInfo.setVlan(vlan);
            log.debug("setting "+dstDeviceId+" : "+zIfceInfo.getName());
            deviceIfceInfo.get(dstDeviceId).add(zIfceInfo);
        }
        return deviceIfceInfo;
    }


}
