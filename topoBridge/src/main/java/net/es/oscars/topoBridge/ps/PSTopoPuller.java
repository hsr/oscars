package net.es.oscars.topoBridge.ps;

import java.io.IOException;

import edu.internet2.perfsonar.PSException;
import edu.internet2.perfsonar.TSLookupClient;
import org.apache.commons.httpclient.HttpException;
import org.jdom.Element;


public class PSTopoPuller {

    public Element pullTopology(String[] topoServers, String[] homeLookupServers, String[] globalLookupServers, String domainId, String namespace) throws HttpException, IOException, PSException {
        TSLookupClient psClient = new TSLookupClient();
        psClient.setTSList(topoServers);
        psClient.setHLSList(homeLookupServers);
        psClient.setGLSList(globalLookupServers);
        Element domain = psClient.getDomain(domainId, namespace);
        if (domain == null) {
            throw new IOException("No domain data from topology server for domain id: "+domainId);
        }
        return domain;
    }
}
