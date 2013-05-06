package net.es.oscars.pss.openflowj.config;

import java.util.List;

import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

import org.openflow.protocol.OFMessage;

/**
 * Abstract class for generating an openflow configuration. Does some 
 * tricks to override limitations of PSS requiring a sring to be returned by a few methods.
 * @author alake
 *
 */
abstract public class OFConfigGen implements DeviceConfigGenerator{
    
    /**
     * Base method to conform to parts of PSS. Should never use this and instead 
     * getOFConfig.
     */
    public String getConfig(PSSAction action, String deviceId)
            throws PSSException {
        throw new PSSException("Not implemented by OFConfigGen. Use getOFConfig.");
    }
    
    /**
     * Base method to conform to parts of PSS. Should generally just leave as-is.
     */
    public void setConfig(GenericConfig config) throws PSSException {
        return;
    }
    
    /**
     * 
     * @param action the setup or teardown action to perform
     * @param nodeId the ID of the node to configure
     * @return a list of OpenFlow messages to send
     * @throws PSSException
     */
    abstract public List<OFMessage> getOFConfig(PSSAction action, String nodeId) throws PSSException;

}
