package net.es.oscars.nsibridge.common;




import net.es.oscars.nsibridge.config.oscars.OscarsConfig;
import net.es.oscars.nsibridge.beans.config.JettyConfig;
import net.es.oscars.nsibridge.beans.config.ProviderNSAConfig;
import net.es.oscars.nsibridge.beans.config.StpConfig;
import net.es.oscars.utils.config.ConfigHelper;


public class ConfigManager {

    private static ConfigManager instance;

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private ConfigManager() {
    }


    public JettyConfig getJettyConfig(String filename) {
        JettyConfig config = ConfigHelper.getConfiguration(filename, JettyConfig.class);
        return config;

    }


    public OscarsConfig getOscarsConfig(String filename) {
        OscarsConfig config = ConfigHelper.getConfiguration(filename, OscarsConfig.class);
        return config;
    }

    public ProviderNSAConfig getNSAConfig(String filename) {
        ProviderNSAConfig config = ConfigHelper.getConfiguration(filename, ProviderNSAConfig.class);
        return config;
    }

    public StpConfig[] getStpConfig(String filename) {
        StpConfig[] config = new StpConfig[0];
        config = ConfigHelper.getConfiguration(filename, config.getClass());
        return config;

    }


}
