package net.es.oscars.topoBridge.sdn;

import java.lang.NullPointerException;

import org.jdom.Document;


/**
 * Base SDNTopology class that holds the singleton implementation of interface 
 * ISDNTopologyService.	  
 * 
 * @author Henrique Rodrigues <hsr@cs.ucsd.edu>
 *
 */
public abstract class BaseSDNTopologyService implements ISDNTopologyService {

	private static ISDNTopologyService instance = null; 
	
	@Override
	public Document getTopology() {
		return null;
	}

	/**
	 * 
	 * @param SDNTopologyIdentifier
	 *        String following the format sdn:<topologyservice>:<param> where
	 *        - sdn is a static string indicating that the topology
	 *        - <topologyservice> is the type of topology service (ex: floodlight)
	 *        - <param> is a service specific param (floodlight topo service for 
	 *                  example expects the controller url).
	 *                  
	 * @return instance a singleton that implements ISDNTopologyService
	 */
	public static ISDNTopologyService getInstance (String tsIdentifier) {
		if (instance == null) {
			if (tsIdentifier.matches("^sdn:floodlight"))
				instance = new FloodlightSDNTopologyService(tsIdentifier);
			//else if (tsIdentifier.matches("^sdn:ryu"))
			//	instance = new RyuSDNTopologyService(tsIdentifier);
			else
				throw new NullPointerException(
						"No SDN TopologyService implementation for given identifier"
				);
				
		}
		return instance;
	}
	
	public static ISDNTopologyService getInstance () {
		if (instance == null)
			throw new NullPointerException(
					"TopologyService SDN Singleton need to be instantiated first"
			); // Use getInstance (String SDNTopologyIdentifier) before getInstance;
		return instance;
	}
	
	public static ISDNTopologyService getType(String SDNTopologyIdentifier) {
		return FloodlightSDNTopologyService.getInstance(SDNTopologyIdentifier);
	}

}
