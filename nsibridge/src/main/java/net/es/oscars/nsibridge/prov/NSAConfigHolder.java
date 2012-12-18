package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.config.ProviderNSAConfig;
import net.es.oscars.nsibridge.beans.config.StpConfig;

public class NSAConfigHolder {
    private NSAConfigHolder() {}
    private static NSAConfigHolder instance;
    public static NSAConfigHolder getInstance() {
        if (instance == null) instance = new NSAConfigHolder();
        return instance;
    }

    private ProviderNSAConfig nsaConfig;
    private StpConfig[] stpConfigs;

    public ProviderNSAConfig getNsaConfig() {
        return nsaConfig;
    }

    public void setNsaConfig(ProviderNSAConfig nsaConfig) {
        this.nsaConfig = nsaConfig;
    }

    public StpConfig[] getStpConfigs() {
        return stpConfigs;
    }

    public void setStpConfigs(StpConfig[] stpConfigs) {
        this.stpConfigs = stpConfigs;
    }
}
