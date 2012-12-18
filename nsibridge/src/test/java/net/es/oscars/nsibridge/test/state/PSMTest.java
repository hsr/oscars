package net.es.oscars.nsibridge.test.state;


import net.es.oscars.nsibridge.common.ConfigManager;
import net.es.oscars.nsibridge.prov.OscarsProxy;

import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ContextConfig;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;

public class PSMTest {
    private static final Logger LOG = Logger.getLogger(PSMTest.class);
    @BeforeSuite
    public void init() throws Exception {
        ContextConfig.getInstance().setContext(ConfigDefaults.CTX_TESTING);
        ContextConfig.getInstance().loadManifest(new File("./config/manifest.yaml"));
        OscarsProxy.getInstance().setOscarsConfig(ConfigManager.getInstance().getOscarsConfig("config/oscars.yaml"));

        OscarsProxy.getInstance().initialize();
    }

/*
    @Test (expectedExceptions = NullPointerException.class)
    public void noTH() throws Exception {
        ProviderSM sm = new ProviderSM("noTH");
        sm.process(PSM_Event.RSV_RQ);
    }

    @Test (expectedExceptions = StateException.class)
    public void badAfterInit() throws Exception {
        String connId = "badAfterInit";

        ProviderSM sm = new ProviderSM(connId);
        PSM_TransitionHandler th = new PSM_TransitionHandler();
        th.setMdl(new LeafProviderModel(connId));
        sm.setTransitionHandler(th);
        sm.process(PSM_Event.ACT_FL);
    }

    @Test (expectedExceptions = StateException.class)
    public void badAfterRsvRQ() throws Exception {
        String connId = "badAfterRsvRQ";
        ProviderSM sm = new ProviderSM(connId);
        PSM_TransitionHandler th = new PSM_TransitionHandler();
        th.setMdl(new LeafProviderModel(connId));
        sm.setTransitionHandler(th);
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.ACT_OK);
    }

    @Test
    public void rsvFail() throws Exception {
        String connId = "rsvFail";
        ProviderSM sm = new ProviderSM(connId);
        PSM_TransitionHandler th = new PSM_TransitionHandler();
        th.setMdl(new LeafProviderModel(connId));
        sm.setTransitionHandler(th);
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.RSV_FL);
    }


    @Test
    public void simpleWorkflow() throws Exception {
        String connId = "simple";
        ProviderSM sm = new ProviderSM(connId);
        PSM_TransitionHandler th = new PSM_TransitionHandler();
        th.setMdl(new LeafProviderModel(connId));
        sm.setTransitionHandler(th);
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.RSV_OK);

        sm.process(PSM_Event.PROV_RQ);
        sm.process(PSM_Event.PROV_OK);
        sm.process(PSM_Event.START_TIME);
        sm.process(PSM_Event.ACT_OK);

        sm.process(PSM_Event.END_TIME);
    }

    @Test
    public void provisionedWorkflow() throws Exception {
        String connId = "provision";
        ProviderSM sm = new ProviderSM(connId);
        PSM_TransitionHandler th = new PSM_TransitionHandler();
        th.setMdl(new LeafProviderModel(connId));
        sm.setTransitionHandler(th);
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.RSV_OK);

        sm.process(PSM_Event.START_TIME);
        sm.process(PSM_Event.PROV_RQ);
        sm.process(PSM_Event.ACT_OK);


        sm.process(PSM_Event.END_TIME);
    }

    @Test
    public void actThenReleaseWorkflow() throws Exception {
        String connId = "actRelease";

        ProviderSM sm = new ProviderSM(connId);
        PSM_TransitionHandler th = new PSM_TransitionHandler();
        th.setMdl(new LeafProviderModel(connId));
        sm.setTransitionHandler(th);
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.RSV_OK);

        sm.process(PSM_Event.PROV_RQ);
        sm.process(PSM_Event.PROV_OK);
        sm.process(PSM_Event.START_TIME);
        sm.process(PSM_Event.ACT_OK);

        sm.process(PSM_Event.REL_RQ);
        sm.process(PSM_Event.REL_OK);

        sm.process(PSM_Event.END_TIME);
    }
*/

}
