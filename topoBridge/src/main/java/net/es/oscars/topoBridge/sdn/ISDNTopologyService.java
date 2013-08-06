/**
 * 
 */
package net.es.oscars.topoBridge.sdn;

import org.jdom.Document;

/**
 * OSCARS Topology Bridge Interface for SDN-provided topology
 * 
 * This extension to OSCARS was developed with similar goals as OSCARS-SDNPSS
 * (http://github.com/hsr/oscars-sdnpss). The idea is to isolate OSCARS
 * reservation functionalities from network devices configurations, leaving the
 * later as a responsibility of an SDN Controller.
 * 
 * @author Henrique Rodrigues <hsr@cs.ucsd.edu>
 * 
 */
public interface ISDNTopologyService {
	/**
	 * Retrieves the topology from the SDN controller 
	 * @return A XML/NMWG formatted topology file
	 */
	public Document getTopology();	
}
