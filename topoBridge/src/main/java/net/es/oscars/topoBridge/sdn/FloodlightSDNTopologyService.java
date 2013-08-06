/**
 * 
 */
package net.es.oscars.topoBridge.sdn;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.es.oscars.pss.sdn.common.SDNLink;

import org.jdom.Document;

/**
 * 
 * Implements the Floodlight ISDNTopologyService interface, responsible for
 * retrieving the network topology form Floodlight running state.
 * 
 * @author Henrique Rodrigues <hsr@cs.ucsd.edu>
 * 
 */
public class FloodlightSDNTopologyService extends BaseSDNTopologyService
		implements ISDNTopologyService {
	private String controllerURL;

	public FloodlightSDNTopologyService(String controllerURL) {
		this.controllerURL = controllerURL;
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.es.oscars.topoBridge.sdn.ISDNTopologyService#getTopology()
	 */
	@Override
	public Document getTopology() {
		List<SDNLink> links = null;
		try {
			links = requestFloodlightTopology();
		} catch (Exception e) {
			System.out.println("Topology get failed");
		}

		return createNMWGDocument(links);
	}

	/**
	 * Requests Floodlight's network topology using the given controller URL,
	 * parse the JSON result and returns it as a list of SDNLink objects
	 * 
	 * @return List<SDNLink> List of links learned by Floodlight using LLDP
	 * @throws IOException
	 */
	public List<SDNLink> requestFloodlightTopology() throws IOException {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(controllerURL).path(
				"/wm/topology/links/json");

		Invocation.Builder invocationBuilder = target
				.request(MediaType.APPLICATION_JSON);
		Response r = invocationBuilder.get();

		return SDNLink.extractSDNLinksFromJson(r.readEntity(String.class));
	}

	public Document createNMWGDocument(List<SDNLink> links) {
		// TODO
		return null;
	}

}
