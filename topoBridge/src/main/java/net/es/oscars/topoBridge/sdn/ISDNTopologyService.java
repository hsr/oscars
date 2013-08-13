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
 * reservation functionalities from network devices configurations/management,
 * leaving the later as a responsibility of an SDN Controller.
 * 
 * This interface describes a service that fetches topology information from 
 * an SDN Controller. This service is used by OSCARS's Topology Bridge module
 * to retrieve and convert the topology maintained by an SDN Controller to a
 * format expected by OSCARS.
 * 
 * Note: at this time it is assumed that a SDN Controller is responsible for
 * a single domain. 
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
	
	/**
	 * Retrieves the domain name from the SDN topology service
	 * @return String domain Name
	 */
	public String getDomainName();
}
