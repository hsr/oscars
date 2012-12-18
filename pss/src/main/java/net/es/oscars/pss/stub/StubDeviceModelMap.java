package net.es.oscars.pss.stub;


import net.es.oscars.pss.api.DeviceModelMap;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public class StubDeviceModelMap implements DeviceModelMap {
    public StubDeviceModelMap() {
        
    }
    public String getDeviceModel(String deviceId) throws PSSException {
        return null;
    }
    
    public void setConfig(GenericConfig config) throws PSSException {

    }
    

}
