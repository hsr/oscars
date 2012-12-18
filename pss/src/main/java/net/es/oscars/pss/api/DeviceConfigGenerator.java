package net.es.oscars.pss.api;


import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

/**
 * @author haniotak
 *
 */
public interface DeviceConfigGenerator {
    public String getConfig(PSSAction action, String deviceId) throws PSSException;
    public void setConfig(GenericConfig config) throws PSSException;
}
