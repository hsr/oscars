package net.es.oscars.pss.eompls.test;


import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.PSSRequest;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.eompls.alu.ALUNameGenerator;
import net.es.oscars.pss.eompls.alu.SR_VPLS_DeviceIdentifiers;
import net.es.oscars.pss.eompls.alu.SR_VPLS_ConfigGen;

import net.es.oscars.pss.eompls.alu.SR_VPLS_TemplateParams;
import net.es.oscars.pss.eompls.common.EoMPLSPSSCore;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.junos.MX_VPLS_ConfigGen;
import net.es.oscars.pss.eompls.junos.MX_VPLS_TemplateParams;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.eompls.util.VPLS_DomainIdentifiers;
import net.es.oscars.pss.soap.gen.SetupReqContent;
import net.es.oscars.pss.soap.gen.TeardownReqContent;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import org.apache.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@Test
public class VPLSTest {
    private Logger log = Logger.getLogger(VPLSTest.class);

    @BeforeClass(groups = { "alu-vpls", "mx-vpls", "vpls" })
    private void configure() throws ConfigException, PSSException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        cc.loadManifest(new File("src/test/resources/"+ ConfigDefaults.MANIFEST));
        cc.setContext(ConfigDefaults.CTX_TESTING);
        cc.setServiceName(ServiceNames.SVC_PSS);

        try {
            String configFn = cc.getFilePath("config.yaml");
            ConfigHolder.loadConfig(configFn);
            ClassFactory.getInstance().configure();



            String eoMPLSConfigFilePath = cc.getFilePath("config-eompls.yaml");
            EoMPLSConfigHolder.loadConfig(eoMPLSConfigFilePath);
            EoMPLSClassFactory.getInstance().configure();
        } catch (ConfigException ex ) {
            ex.printStackTrace();
            log.debug ("skipping Tests, eompls is  not configured");
            throw new SkipException("skipping Tests, eompls is  not configured");
        }

        log.debug("db: "+ EoMPLSPSSCore.getInstance().getDbname());

    }

    @Test(groups = { "alu-vpls-template"} )
    private void testALUVPLSTemplate() throws PSSException, ConfigException {
        SR_VPLS_ConfigGen cg = new SR_VPLS_ConfigGen();
        ALUNameGenerator ng = ALUNameGenerator.getInstance();
        Integer sdpNum = 2;

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

        String gri = "es.net-3912";
        String portA = "1/5/2";
        // String portB = "1/5/2";
        String vlanA = "3005";
        String vlanB = "3006";
        Integer bandwidth = 5000;

        VPLS_DomainIdentifiers gids = VPLS_DomainIdentifiers.reserve(gri);
        SR_VPLS_DeviceIdentifiers ids = SR_VPLS_DeviceIdentifiers.reserve(gri, "aleph", gids.getVplsId(), 2);

        String vplsId = gids.getVplsId().toString();
        String ingQosId = ids.getQosId().toString();

        String pathNameA = ng.getPathName(gri)+"-1";
        String pathNameB = ng.getPathName(gri)+"-2";
        String lspNameA = ng.getLSPName(gri)+"-1";
        String lspNameB = ng.getLSPName(gri)+"-2";
        String sdpIdA = ids.getSdpIds().get(0).toString();

        String sdpIdB = ids.getSdpIds().get(1).toString();
        String sdpDescA = gri+"-1";
        String sdpDescB = gri+"-2";

        SR_VPLS_TemplateParams params = new SR_VPLS_TemplateParams();


        params.getVpls().put("id", vplsId);
        params.getVpls().put("description", gri);


        params.getIngqos().put("id", ingQosId);
        params.getIngqos().put("description", gri);
        params.getIngqos().put("bandwidth", bandwidth);


        HashMap ifceA = new HashMap();
        ifceA.put("name", portA);
        ifceA.put("vlan", vlanA);
        params.getIfces().add(ifceA);
        /*
        HashMap ifceB = new HashMap();
        ifceB.put("name", portB);
        ifceB.put("vlan", vlanB);
        cg.getIfces().add(ifceB);
        */

        HashMap pathA = new HashMap();
        pathA.put("name", pathNameA);
        ArrayList hopsA = new ArrayList();
        pathA.put("hops", hopsA);
        HashMap hopA1 = new HashMap();
        hopsA.add(hopA1);
        hopA1.put("address", "10.32.0.69");
        hopA1.put("order", "5");
        params.getPaths().add(pathA);

        HashMap pathB = new HashMap();
        pathB.put("name", pathNameB);
        ArrayList hopsB = new ArrayList();
        pathB.put("hops", hopsB);
        HashMap hopB1 = new HashMap();
        hopsB.add(hopB1);
        HashMap hopB2 = new HashMap();
        hopsB.add(hopB2);
        hopB1.put("address", "10.32.0.69");
        hopB1.put("order", "5");
        hopB2.put("address", "10.32.0.13");
        hopB2.put("order", "10");
        params.getPaths().add(pathB);

        HashMap lspA = new HashMap();
        lspA.put("name", lspNameA);
        lspA.put("from", "10.96.0.8");
        lspA.put("to", "10.96.0.2");
        lspA.put("path", pathNameA);
        params.getLsps().add(lspA);

        HashMap lspB = new HashMap();
        lspB.put("name", lspNameB);
        lspB.put("from", "10.96.0.8");
        lspB.put("to", "10.96.0.4");
        lspB.put("path", pathNameB);
        params.getLsps().add(lspB);


        HashMap sdpA = new HashMap();
        sdpA.put("id", sdpIdA);
        sdpA.put("description", sdpDescA);
        sdpA.put("far_end", "10.96.0.2");
        sdpA.put("lsp_name", lspNameB);
        params.getSdps().add(sdpA);


        HashMap sdpB = new HashMap();
        sdpB.put("id", sdpIdB);
        sdpB.put("description", sdpDescB);
        sdpB.put("far_end", "10.96.0.4");
        sdpB.put("lsp_name", lspNameB);
        params.getSdps().add(sdpB);



        String setup = cg.generateConfig(params, ActionType.SETUP);
        System.out.println(setup);
        String teardown = cg.generateConfig(params, ActionType.TEARDOWN);
        System.out.println(teardown);

    }


    @Test(groups = { "mx-vpls-template" })
    private void testJunosVPLSTemplate() throws PSSException{
        MX_VPLS_ConfigGen cg = new MX_VPLS_ConfigGen();
        MX_VPLS_TemplateParams params = new MX_VPLS_TemplateParams();

        /*

        setup:
        1. policy (string)
        2. community: name, id
        3. filters: stats, policing
        4. policer: name, bandwidth_limit, burst_size_limit
        5. vpls: name, id

        6. ifces: list_of <name, vlan, description>
        7. paths: list_of <name, hops>
                                 hops: list of string >
        8. lsps: list_of <name, from, to, path, neighbor, bandwidth>
        */


        /*
        teardown:
        1. policy (string)
        2. community: name
        3. filters: stats, policing
        4. policer: name
        5. vpls: name

        6. ifces: list_of <name, vlan>
        7. paths: list_of <name>
        8. lsps: list_of <name>
        */

        params.setPolicy("test_policy");

        params.getCommunity().put("name", "test_community");
        params.getCommunity().put("members", "3306");

        params.getFilters().put("stats", "test_stats");
        params.getFilters().put("policing", "test_policing");

        params.getPolicer().put("name", "test_policer");
        params.getPolicer().put("bandwidth_limit", 500);
        params.getPolicer().put("burst_size_limit", 50);

        params.getVpls().put("name", "test_vpls");
        params.getVpls().put("id", VPLS_DomainIdentifiers.reserve("aa-13132").toString());



        HashMap ifceA = new HashMap();
        ifceA.put("name", "xe-11/2/0");
        ifceA.put("vlan", "3003");
        ifceA.put("description", "ifceA");
        params.getIfces().add(ifceA);

        HashMap ifceB = new HashMap();
        ifceB.put("name", "xe-11/2/0");
        ifceB.put("vlan", "3005");
        ifceB.put("description", "ifceB");
        params.getIfces().add(ifceB);



        HashMap pathA = new HashMap();
        pathA.put("name", "pathA");
        ArrayList hopsA = new ArrayList();
        pathA.put("hops", hopsA);
        hopsA.add("10.32.0.61");
        hopsA.add("10.32.0.18");
        params.getPaths().add(pathA);

        HashMap pathB = new HashMap();
        pathB.put("name", "pathB");
        ArrayList hopsB = new ArrayList();
        pathB.put("hops", hopsB);
        hopsB.add("10.32.0.70");
        params.getPaths().add(pathB);


        HashMap lspA = new HashMap();
        lspA.put("name", "lspA");
        lspA.put("from", "10.96.0.2");
        lspA.put("to", "10.32.0.18");
        lspA.put("neighbor", "10.96.0.4");
        lspA.put("path", "pathA");
        lspA.put("bandwidth", 500);
        params.getLsps().add(lspA);

        HashMap lspB = new HashMap();
        lspB.put("name", "lspB");
        lspB.put("from", "10.96.0.2");
        lspB.put("to", "10.32.0.70");
        lspB.put("neighbor", "10.96.0.8");
        lspB.put("path", "pathB");
        lspB.put("bandwidth", 500);
        params.getLsps().add(lspB);

        String setup = cg.generateConfig(params, ActionType.SETUP);
        System.out.println(setup);
        String teardown = cg.generateConfig(params, ActionType.TEARDOWN);
        System.out.println(teardown);

    }

    @Test(groups = { "vpls" })

    public void testWithResDetails() throws PSSException, ConfigException  {

        String output;

        PSSAction action = new PSSAction();
        PSSRequest req = new PSSRequest();
        action.setRequest(req);

        SetupReqContent srq = new SetupReqContent();
        TeardownReqContent trq = new TeardownReqContent();

        req.setSetupReq(srq);
        req.setTeardownReq(trq);



        String srcDeviceId;
        String dstDeviceId;
        ResDetails rd;
        MX_VPLS_ConfigGen mxcg = new MX_VPLS_ConfigGen();
        SR_VPLS_ConfigGen alucg = new SR_VPLS_ConfigGen();

/*
        rd = RequestFactory.getALU_ALU("foo.net-771");
        srq.setReservation(rd);
        trq.setReservation(rd);
        String srcDeviceId = EoMPLSUtils.getDeviceId(rd, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(rd, true);
        log.debug("setting up src for ALU-ALU");
        output     = alucg.getSetup(action, srcDeviceId);
        log.debug("done setting up src for ALU-ALU");
        System.out.println(output);


        log.debug("starting teardown for ALU-ALU");
        output  = alucg.getTeardown(action, srcDeviceId);
        System.out.println(output);
        log.debug("done with teardown for ALU-ALU");


        rd = RequestFactory.getALU_MX("foo.net-2212");
        srcDeviceId = EoMPLSUtils.getDeviceId(rd, false);
        dstDeviceId = EoMPLSUtils.getDeviceId(rd, true);
        srq.setReservation(rd);
        trq.setReservation(rd);

        log.debug("starting ALU setup for ALU-MX");
        output    = alucg.getSetup(action, srcDeviceId);
        System.out.println(output);
        log.debug("starting ALU teardown for ALU-MX");
        output = alucg.getTeardown(action, srcDeviceId);
        System.out.println(output);

        log.debug("starting MX setup for ALU-MX");
        output     = mxcg.getSetup(action, dstDeviceId);
        System.out.println(output);
        log.debug("starting MX teardown for ALU-MX");
        output  = mxcg.getTeardown(action, dstDeviceId);
        System.out.println(output);

        rd = RequestFactory.getSameMX();
        srq.setReservation(rd);
        trq.setReservation(rd);
        srcDeviceId = EoMPLSUtils.getDeviceId(rd, false);
        log.debug("starting setup same MX");
        output     = mxcg.getSetup(action, srcDeviceId);
        System.out.println(output);
        log.debug("done setup same MX");
        log.debug("starting teardown same MX");
        output    = mxcg.getTeardown(action, srcDeviceId);
        System.out.println(output);
        log.debug("done teardown same MX");

*/
        rd = RequestFactory.getSameALU();
        srq.setReservation(rd);
        trq.setReservation(rd);
        srcDeviceId = EoMPLSUtils.getDeviceId(rd, false);
        log.debug("starting setup same ALU");
        output     = alucg.getSetup(action, srcDeviceId);
        System.out.println(output);
        log.debug("done setup same ALU");
        log.debug("starting teardown same ALU");
        output    = alucg.getTeardown(action, srcDeviceId);
        System.out.println(output);
        log.debug("done teardown same ALU");

        /*
        rd = RequestFactory.getMX_MX();
        srq.setReservation(rd);
        trq.setReservation(rd);
        srcDeviceId = EoMPLSUtils.getDeviceId(rd, false);
        dstDeviceId = EoMPLSUtils.getDeviceId(rd, true);
        log.debug("starting setup MX-MX");
        output     = mxcg.getSetup(action, srcDeviceId);
        System.out.println(output);
        output     = mxcg.getSetup(action, dstDeviceId);
        System.out.println(output);
        log.debug("done setup same MX-MX");
        log.debug("starting teardown MX-MX");
        output    = mxcg.getTeardown(action, srcDeviceId);
        System.out.println(output);
        output    = mxcg.getTeardown(action, dstDeviceId);
        System.out.println(output);
        log.debug("done teardown same MX-MX");
    */

    }


}
