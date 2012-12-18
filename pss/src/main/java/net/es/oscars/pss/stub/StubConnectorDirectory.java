package net.es.oscars.pss.stub;


import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.api.ConnectorDirectory;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public class StubConnectorDirectory implements ConnectorDirectory {

    public void setConfig(GenericConfig config) throws PSSException {

    }

    public Connector getConnector(String connectorId) throws PSSException {

        return null;
    }

}
