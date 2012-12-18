package net.es.oscars.pss.eompls.api;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public interface EoMPLSDeviceAddressResolver {
    public String getDeviceAddress(String deviceId) throws PSSException;
    public void setConfig(GenericConfig config) throws PSSException;
    
}
