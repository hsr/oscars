package net.es.oscars.pss.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.test.sim.SimRequest;
import net.es.oscars.pss.test.sim.SimRequestGenerator;
import net.es.oscars.pss.test.sim.Simulation;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.testng.annotations.*;

public class SchedulerTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testScheduler() throws ConfigException, PSSException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        cc.loadManifest(new File("src/test/resources/"+ConfigDefaults.MANIFEST));
        cc.setContext(ConfigDefaults.CTX_TESTING);
        cc.setServiceName(ServiceNames.SVC_PSS);
        String configFilePath = cc.getFilePath(ConfigDefaults.CONFIG);

        // actually load the config
        ConfigHolder.loadConfig(configFilePath);
        

        // if you configure first, then ask health must be OK
        ClassFactory.getInstance().configure();

        
        Simulation sim = Simulation.getInstance();
        SimRequestGenerator gen = new SimRequestGenerator();

        Map config = sim.getConfig();
        ArrayList<SimRequest> requests = gen.makeRequests(config);

        sim.setRequests(requests);
        
        try {
            sim.run();
        } catch (PSSException e) {
            e.printStackTrace();
        } catch (ConfigException e) {
            e.printStackTrace();
        }
    }


}
