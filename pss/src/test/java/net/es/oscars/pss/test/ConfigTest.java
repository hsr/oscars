package net.es.oscars.pss.test;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.testng.annotations.Test;

import java.io.File;

public class ConfigTest {
    @Test (expectedExceptions = PSSException.class)
    public void testBadHealth() throws PSSException {
        System.out.println("starting testBadHealth");
        // throw exception if asked for health without any config
        ClassFactory.getInstance().health();
        System.out.println("testBadHealth end");
    }

    @Test (dependsOnMethods={"testBadHealth" })
    public void testConfig() throws PSSException, ConfigException {
        // set up our configuration context
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        cc.loadManifest(new File("src/test/resources/"+ConfigDefaults.MANIFEST));
        cc.setContext(ConfigDefaults.CTX_TESTING);
        cc.setServiceName(ServiceNames.SVC_PSS);
        String configFilePath = cc.getFilePath(ConfigDefaults.CONFIG);

        // actually load the config
        ConfigHolder.loadConfig(configFilePath);
        

        // if you configure first, then ask health must be OK
        ClassFactory.getInstance().configure();
        //PSSAgentFactory.getInstance().health();
    }
}
