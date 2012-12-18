package net.es.oscars.pss.stub;


import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.api.DeviceConnectorMap;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public class StubDeviceConnectorMap implements DeviceConnectorMap {

    public Connector getDeviceConnector(String deviceId) throws PSSException {
        return null;
    }

    public void setConfig(GenericConfig config) throws PSSException {
    }

}
