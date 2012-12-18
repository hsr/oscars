package net.es.oscars.pss.api;


import net.es.oscars.pss.beans.PSSCommand;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

/**
 * @author haniotak
 *
 */
public interface Connector {
    public String sendCommand(PSSCommand command) throws PSSException;
    public void setConfig(GenericConfig config) throws PSSException;
}
