package net.es.oscars.topoBridge.sdn;

import java.io.IOException;
import java.io.StringReader;
import java.lang.NullPointerException;
import java.util.HashMap;
import java.util.List;

import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.topology.PathTools;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Base SDNTopology class that holds the singleton implementation of interface 
 * ISDNTopologyService. Sub-classes (e.g. FloodlightSDNTopologyService) implement
 * SDN Controller specific logic to fetch topology and transform it to OSCARS's 
 * NMWG format.
 * 
 * @author Henrique Rodrigues <hsr@cs.ucsd.edu>
 *
 */
public abstract class BaseSDNTopologyService implements ISDNTopologyService {

	protected Logger log = Logger.getLogger(ISDNTopologyService.class);
	private static ISDNTopologyService instance = null;
	
   	/* tsIdentifier (SDNTopologyIdentifier): is a String following the 
   	 * format sdn.<topologyservice>.<domain>, with each part being interpreted
   	 * as follows:
   	 *        - sdn static string indicating topo should be retrieved from a sdn contoller
   	 *        - <topologyservice> is the type of topology service (ex: floodlight)
   	 *        - <param> is a service specific param (floodlight topo service for 
	 *                  example expects the domain name and full controller url 
	 *                  separated by a semicolon. For example:
	 *                  sdn.floodlight.mydomain.http://myserver.es.net:8080).
	 *
	 * The SDNTopologyIdentifier will also be used by OSCARS as the domain name.
   	 */
	private String tsIdentifier = null;
	protected String domainName = null;
	
	public BaseSDNTopologyService(String tsIdentifier) {
		this.tsIdentifier = tsIdentifier;
	}
	
	public String getId() {
		return this.tsIdentifier;
	}
	
	public String getDomainName() {
		return this.domainName;
	}
	
	public Document getTopology() {
		throw new NotImplementedException();
	}

	/**
	 * 
	 * @param SDNTopologyIdentifier
	 *        String following the format sdn.<topologyservice>.<param> where
	 *        - sdn is a static string indicating that the topology
	 *        - <topologyservice> is the type of topology service (ex: floodlight)
	 *        - <param> is a service specific param (floodlight topo service for 
	 *                  example expects the domain name and full controller url 
	 *                  separated by a semicolon. For example:
	 *                  sdn.floodlight.mydomain.http://myserver.es.net:8080).
	 *                  
	 * @return instance a singleton that implements ISDNTopologyService
	 */
	public static ISDNTopologyService getInstance (String tsIdentifier) {
		if (instance == null) {
			if (tsIdentifier.matches("^sdn\\.floodlight\\..*")) {
				instance = new FloodlightSDNTopologyService(tsIdentifier);
			}
			//else if (tsIdentifier.matches("^sdn\\.ryu\\..*"))
			//	instance = new RyuSDNTopologyService(tsIdentifier);
			else
				throw new NotImplementedException(
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
	
	private String createNMWGNode(SDNNode node) throws JDOMException, IOException {
		String xmlNodeOpen = ""
				+ "<CtrlPlane:node id=\"urn:ogf:network:domain=%s:node=%s\">"
				+ "	<CtrlPlane:address>%s</CtrlPlane:address>";
		String xmlPortOpen = ""
				+ "<CtrlPlane:port id=\"urn:ogf:network:domain=%s:node=%s:port=%s\">"
				+ "<CtrlPlane:capacity>1000000000</CtrlPlane:capacity>"
				+ "<CtrlPlane:maximumReservableCapacity>1000000000</CtrlPlane:maximumReservableCapacity>"
				+ "<CtrlPlane:minimumReservableCapacity>1000000</CtrlPlane:minimumReservableCapacity>"
				+ "<CtrlPlane:granularity>1000000</CtrlPlane:granularity>";
		
		String xmlLink = ""
				+ "<CtrlPlane:link id=\"urn:ogf:network:domain=%s:node=%s:port=%s:link=link*\">"
				+ "	<CtrlPlane:remoteLinkId>urn:ogf:network:domain=%s:node=%s:port=%s:link=link*</CtrlPlane:remoteLinkId>"
				+ "	<CtrlPlane:trafficEngineeringMetric>100</CtrlPlane:trafficEngineeringMetric>"
				+ "	<CtrlPlane:SwitchingCapabilityDescriptors>"
				+ "		<CtrlPlane:switchingcapType/>"
				+ "		<CtrlPlane:encodingType>packet</CtrlPlane:encodingType>"
				+ "		<CtrlPlane:switchingCapabilitySpecificInfo>"
				+ "			<CtrlPlane:capability/>"
				+ "			<CtrlPlane:interfaceMTU>9000</CtrlPlane:interfaceMTU>"
				+ "			<CtrlPlane:vlanRangeAvailability>2-4094</CtrlPlane:vlanRangeAvailability>"
				+ "		</CtrlPlane:switchingCapabilitySpecificInfo>"
				+ "	</CtrlPlane:SwitchingCapabilityDescriptors>"
				+ "</CtrlPlane:link>";
		
		String xmlPortClose = ""
				+ "</CtrlPlane:port>";
		
		String xmlNodeClose = ""
				+ "</CtrlPlane:node>";
		
		String xml;

		xml = String.format(xmlNodeOpen, this.getId(), node.getId(), "127.0.0.1");
		for (SDNLink link : node.getOutLinks()) {
			// @formatter:off
			xml += String.format(xmlPortOpen, this.getId(), node.getId(), link.getSrcPort());
			xml += String.format(xmlLink, 
					this.getId(), node.getId(), link.getSrcPort(), // src link args
					this.getId(), link.getDstNode(), link.getDstPort()); // dst link args
			xml += xmlPortClose;
			// @formatter:on
		}
		xml += xmlNodeClose;
		
		return xml;
	}
	
	protected Document createNMWGDocument(List<SDNLink> links) throws Exception {
		List<SDNNode> nodes = null;
		String xml = null;
		
		try {
			nodes = SDNLink.getSDNNodeMapFromSDNLinks(links);
		}
		catch (Exception e) {
			log.warn("Couldn't get node map: " + e.getMessage());
			return null;
		}
		
		String xmlDomainOpen = ""
				+ "<CtrlPlane:topology xmlns:CtrlPlane=\"http://ogf.org/schema/network/topology/ctrlPlane/20080828/\" id=\"%s\">"
				+ "	<xsd:documentation xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" lang=\"en\">"
				+ "		Auto generated based on Floodlight's Topology Service"
				+ "	</xsd:documentation>"
				+ "	<CtrlPlane:idcId>%s</CtrlPlane:idcId>"
				+ "	<CtrlPlane:domain id=\"%s\">";
		String xmlDomainClode = ""
				+ "</CtrlPlane:domain></CtrlPlane:topology>";
		
		xml = String.format(xmlDomainOpen, this.getId(), this.getId(), this.getId());
		for (SDNNode node : nodes) {
			xml += createNMWGNode(node);
		}
		xml += xmlDomainClode;

		//System.out.println(xml);
		
		Document nmwgTopo = null;
		try {
			nmwgTopo = new SAXBuilder().build(new StringReader(xml));
			if (nmwgTopo == null) {
				log.warn("Couldn't convert generated topology to NMWG. "
						+ "Please check SDN Controller output.");
			}
		}
		catch (Exception e) {
			log.warn("Error parsing generated NMWG xml.");

		}

		return nmwgTopo;

	}
	
    /**
     * Retrieves the contents of parameter "sdn" from config.yaml
     * 
     * @return the contents of sdn parameter 
     * @throws ConfigException 
     */
	protected static String getSDNParam() {
        HashMap<String,Object> localDomainMap = PathTools.getLocalDomainSettings();
        if (localDomainMap == null || !localDomainMap.containsKey("sdn")){
            return null;
        }
        String sdnParam = localDomainMap.get("sdn") + "";
        return sdnParam;

	}
	
}
