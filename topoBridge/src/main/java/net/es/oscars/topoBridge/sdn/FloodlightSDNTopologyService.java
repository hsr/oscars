/**
 * 
 */
package net.es.oscars.topoBridge.sdn;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

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

	public FloodlightSDNTopologyService(String tsIdentifier) {
		super(tsIdentifier);
		this.controllerURL = tsIdentifier.substring("sdn:floodlight:".length());
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.es.oscars.topoBridge.sdn.ISDNTopologyService#getTopology()
	 */
	@Override
	public Document getTopology() {
		System.out.println("Controller URL: " + this.controllerURL + ", tsIdentifier:" + this.getId());
		List<SDNLink> links = null;
		try {
			links = requestFloodlightTopology();
		} catch (Exception e) {
			System.out.println("Topology get failed" + e.getMessage());
		}

		try {
			return createNMWGDocument(links);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Requests Floodlight's network topology using the given controller URL,
	 * parse the JSON result and returns it as a list of SDNLink objects
	 * 
	 * @return List<SDNLink> List of links learned by Floodlight using LLDP
	 * @throws IOException
	 */
	public List<SDNLink> requestFloodlightTopology() throws IOException {
		//Logger log = Logger.getLogger(TopoBridgeCore.class);
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(controllerURL).path(
				"/wm/topology/links/json");

		System.out.println("Trying:" + target.getUri().getPath());
		
		String response = target.request().get(String.class);
		
		System.out.println("Got this topo:" + response);
		
		return SDNLink.extractSDNLinksFromJson(response);
		//return null;
	}

}
