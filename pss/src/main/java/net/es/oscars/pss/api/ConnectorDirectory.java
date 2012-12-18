package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public interface ConnectorDirectory {
    public Connector getConnector(String connectorId) throws PSSException;
    public void setConfig(GenericConfig config) throws PSSException;

}
