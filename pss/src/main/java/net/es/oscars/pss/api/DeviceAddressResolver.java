package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public interface DeviceAddressResolver {
    public String getDeviceAddress(String deviceId) throws PSSException;
    public void setConfig(GenericConfig config) throws PSSException;
    
}
