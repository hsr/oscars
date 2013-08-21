/**
 * 
 */
package net.es.oscars.topoBridge.sdn;

import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.restlet.resource.ClientResource;

/**
 * 
 * Implements the Floodlight ISDNTopologyService interface, responsible for
 * retrieving the network topology form Floodlight. Base singleton class is
 * BaseSDNTopologyService and the interface implemented by this class is
 * ISDNTopologyService.
 * 
 * @author Henrique Rodrigues <hsr@cs.ucsd.edu>
 * 
 */
public class FloodlightSDNTopologyService extends BaseSDNTopologyService
		implements ISDNTopologyService {
	private String controllerURL;

	public FloodlightSDNTopologyService(String tsIdentifier) {
		super(tsIdentifier);
		
		// Extract the controller URL and domain name
		this.domainName    = tsIdentifier;
		this.controllerURL = getFloodlightURL();
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.es.oscars.topoBridge.sdn.ISDNTopologyService#getTopology()
	 */
	@Override
	public Document getTopology() {
		log.debug(String.format("FloodlightSDNTopologyService\nDomain: %s\n"
				+ "Controller: %s\ntsIdentifier:%s", 
				this.domainName, this.controllerURL, this.getId()));
		List<SDNLink> links = null;
		
		try {
			links = requestFloodlightTopology();
			log.debug("Fetched "+ links.size() + " links from Floodlight.\n");
		} catch (Exception e) {
			log.error("Topology get failed: " + e.getMessage() + ".\n"
					+ "Are you sure your controller is running at " + this.controllerURL);
		}

		try {
			return createNMWGDocument(links);
		}
		catch (Exception e) {
			log.error("Error creating NMWG Document: " + e.getMessage());
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
		ClientResource cr = new ClientResource(controllerURL + "/wm/topology/links/json");
        // Retrieve a representation
        String response = cr.get(String.class);
		
		return BaseSDNTopologyService.extractSDNLinksFromJson(response);
	}
	
    private static String getFloodlightURL() {
    	return getSDNParam();
    }


}
