package net.es.oscars.topoBridge.sdn;

import java.io.IOException;
import java.lang.NullPointerException;
import java.util.List;

import net.es.oscars.pss.sdn.common.SDNLink;
import net.es.oscars.pss.sdn.common.SDNNode;

import org.apache.commons.lang.NotImplementedException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Base SDNTopology class that holds the singleton implementation of interface 
 * ISDNTopologyService.	  
 * 
 * @author Henrique Rodrigues <hsr@cs.ucsd.edu>
 *
 */
public abstract class BaseSDNTopologyService implements ISDNTopologyService {

	private static ISDNTopologyService instance = null;
	private String tsIdentifier = null;
	
	public BaseSDNTopologyService(String tsIdentifier) {
		this.tsIdentifier = tsIdentifier;
	}
	
	public String getId() {
		return this.tsIdentifier;
	}
	
	public Document getTopology() {
		throw new NotImplementedException();
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
			if (tsIdentifier.matches("^sdn\\:floodlight\\:.*")) {
				instance = new FloodlightSDNTopologyService(tsIdentifier);
			}
			
			//else if (tsIdentifier.matches("^sdn\\:ryu\\:.*"))
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
	
	public static ISDNTopologyService getType(String SDNTopologyIdentifier) {
		return FloodlightSDNTopologyService.getInstance(SDNTopologyIdentifier);
	}

	
	private String createNMWGNode(SDNNode node) throws JDOMException, IOException {
		String xmlNodeOpen= ""
				+ "<CtrlPlane:node id=\"urn:ogf:network:domain=%s:node=%s\""
				+ "	<CtrlPlane:address>%s</CtrlPlane:address>";
		String xmlPortOpen = ""
				+ "<CtrlPlane:port id=\"urn:ogf:network:domain=%s:node=%s:port=%s\">"
				+ "<CtrlPlane:capacity>1000000000</CtrlPlane:capacity>"
				+ "<CtrlPlane:maximumReservableCapacity>1000000000</CtrlPlane:maximumReservableCapacity>"
				+ "<CtrlPlane:minimumReservableCapacity>1000000</CtrlPlane:minimumReservableCapacity>"
				+ "<CtrlPlane:granularity>1000000</CtrlPlane:granularity>";
		
		String xmlLink = ""
				+ "<CtrlPlane:link id=\"urn:ogf:network:domain=%s:node=%s:port=%s:link=link*\">"
				+ "	<CtrlPlane:remoteLinkId>urn:ogf:network:domain=%s:node=%s:port=%s:link*</CtrlPlane:remoteLinkId>"
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
		
		return xml; //new SAXBuilder().build(xml).getRootElement();
	}
	
	protected Document createNMWGDocument(List<SDNLink> links) throws Exception {
		List<SDNNode> nodes = SDNLink.getSDNNodeMapFromSDNLinks(links);
		String xml = null;
		
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

		return new SAXBuilder().build(xml);

	}
	
	
}
