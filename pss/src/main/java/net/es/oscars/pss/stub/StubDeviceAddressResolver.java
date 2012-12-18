package net.es.oscars.pss.stub;


import net.es.oscars.pss.api.DeviceAddressResolver;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public class StubDeviceAddressResolver implements DeviceAddressResolver{
    private GenericConfig config;
    
    public StubDeviceAddressResolver() {
    }
    
    public StubDeviceAddressResolver(GenericConfig config) {
        this.setConfig(config);
    }
    
    public String getDeviceAddress(String deviceId) throws PSSException {
        return deviceId;
        
    }
    public void setConfig(GenericConfig config) {
        this.config = config;
    }
    public GenericConfig getConfig() {
        return config;
    }

}
