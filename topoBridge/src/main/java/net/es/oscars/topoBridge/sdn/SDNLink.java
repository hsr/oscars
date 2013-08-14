package net.es.oscars.topoBridge.sdn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import net.es.oscars.utils.topology.NMWGParserUtil;

public class SDNLink implements Comparable<SDNLink> {
	private SDNNode node = null;
	private String srcNode = null;
	private String dstNode = null;
	private String srcPort = null;
	private String dstPort = null;
	private String srcLink = null;
	private String dstLink = null;

	public SDNNode getNode() {
		return node;
	}

	public void setNode(SDNNode node) {
		this.node = node;
	}

	/**
	 * Checks if all the attributes are set.
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return (this.srcNode != null) && (this.dstNode != null)
				&& (this.srcPort != null) && (this.dstPort != null)
				&& (this.srcLink != null) && (this.dstLink != null);
	}

	/**
	 * Fill (src & dst)Link attributes if they are still set to null. This is
	 * useful when the application using this class don't consider link
	 * attributes on links as OSCARS do
	 */
	private void fillLinkAttributes() {
		if (this.srcLink == null)
			this.srcLink = "1";
		if (this.dstLink == null)
			this.dstLink = "1";
	}

	/**
	 * Empty constructor
	 */
	public SDNLink() {
	}

	public SDNLink(SDNLink link) {
		this.srcNode = link.getSrcNode();
		this.dstNode = link.getDstNode();
		this.srcPort = link.getSrcPort();
		this.dstPort = link.getDstPort();
		this.srcLink = link.getSrcLink();
		this.dstLink = link.getDstLink();
		this.node = link.getNode();
	}

	public SDNLink reverse() {
		SDNLink link = new SDNLink(this);
		this.srcNode = link.getDstNode();
		this.dstNode = link.getSrcNode();
		this.srcPort = link.getDstPort();
		this.dstPort = link.getSrcPort();
		this.srcLink = link.getDstLink();
		this.dstLink = link.getSrcLink();
		this.node = new SDNNode(link.getDstNode());
		return this;
	}

	public SDNLink getReverse() {
		return new SDNLink(this).reverse();
	}

	// @formatter:off
	/**
	 * Construct link from two URNs
	 * 
	 * @param srcURN URN that describes source of the link
	 * @param dstURN URN that describes destination of the link
	 */
	public SDNLink(String srcURN, String dstURN) {
		this.srcNode = NMWGParserUtil.getURNPart(srcURN, NMWGParserUtil.NODE_TYPE);
		this.dstNode = NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.NODE_TYPE);
		this.srcPort = NMWGParserUtil.getURNPart(srcURN, NMWGParserUtil.PORT_TYPE);
		this.dstPort = NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.PORT_TYPE);
		this.srcLink = NMWGParserUtil.getURNPart(srcURN, NMWGParserUtil.LINK_TYPE);
		this.dstLink = NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.LINK_TYPE);
	}
	// @formatter:on

	/**
	 * Extract links from a CtrlPlaneHopContent object and returns them as a
	 * list of SDNLinks
	 * 
	 * @param hops
	 * @return list of SDNLinks
	 */
	public static List<SDNLink> extractSDNLinks(List<CtrlPlaneHopContent> hops) {
		List<SDNLink> links = new ArrayList<SDNLink>();
		String src = null;

		try {
			for (CtrlPlaneHopContent hop : hops) {
				String dst = hop.getLink().getId();

				if (src == null) {
					src = dst;
					continue;
				}

				if (NMWGParserUtil.compareURNPart(src, dst,
						NMWGParserUtil.NODE_TYPE)) {
					SDNLink l = new SDNLink(src, dst);

					// TODO: check for capabilities
					// TODO: change the design to avoid having multiple objects
					// representing the same node
					l.setNode(new SDNNode(l.srcNode.replaceAll("\\.", ":")));
					links.add(l);
				}
				src = dst;
			}
		} catch (Exception e) {
			return null;
		}

		return links;
	}

	/**
	 * Extract links from a serialized JSON array object and returns them as a
	 * list of SDNLinks
	 * 
	 * @param fmJson
	 *            an serialized JSON array object with link descriptions
	 * @return list of SDNLinks
	 * @throws IOException
	 */
	public static List<SDNLink> extractSDNLinksFromJson(String fmJson)
			throws IOException {
		List<SDNLink> links = new ArrayList<SDNLink>();
		SDNLink link = null;

		MappingJsonFactory f = new MappingJsonFactory();
		JsonParser jp;

		try {
			jp = f.createParser(fmJson);
		} catch (JsonParseException e) {
			throw new IOException(e);
		}

		jp.nextToken();
		if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
			throw new IOException("Expected START_ARRAY");
		}

		while (jp.nextToken() != JsonToken.END_ARRAY) {
			if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
				link = new SDNLink();
				continue;
			}

			if (jp.getCurrentToken() == JsonToken.END_OBJECT) {
				if (link != null) {
					link.fillLinkAttributes();
					if (link.isComplete()) {
						links.add(link);

						/* Adding bi-directional links by hand */
						links.add(link.getReverse());
					} else {
						throw new IOException("Link is incomplete");
					}
				}
				continue;
			}

			if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
				throw new IOException("Expected FIELD_NAME");
			}

			String n = jp.getCurrentName();
			jp.nextToken();

			if (n == "src-switch")
				link.setSrcNode(jp.getText());
			else if (n == "dst-switch")
				link.setDstNode(jp.getText());
			else if (n == "src-port")
				link.setSrcPort(jp.getText());
			else if (n == "dst-port")
				link.setDstPort(jp.getText());
			else if (n == "direction" || n == "type") // ignore direction and
														// type for now
				jp.getText(); // do we have to call this to advance the head?
		}

		return links;
	}

	/**
	 * Build a list with SDNNode objects from a list of SDNLink objects. Nodes
	 * created in this method contain an updated list of links connecting them.
	 * 
	 * @param fmJson
	 *            an serialized JSON array object with link descriptions
	 * @return list of SDNLinks
	 * @throws Exception
	 * @throws IOException
	 */
	public static List<SDNNode> getSDNNodeMapFromSDNLinks(List<SDNLink> links)
			throws Exception {
		Map<String, SDNNode> nodes = new HashMap<String, SDNNode>();
		SDNNode node;

		for (SDNLink link : links) {
			if (nodes.containsKey(link.getSrcNode())) {
				node = nodes.get(link.getSrcNode());
			} else {
				node = new SDNNode(link.getSrcNode());
				nodes.put(node.getId(), node);
			}
			link.setNode(node);
			node.addLink(link);
		}
		return new ArrayList<SDNNode>(nodes.values());
	}

	// @formatter:off
	public void   setSrcNode(String srcNode) {
		this.srcNode = srcNode.replaceAll("\\:", ".");
	}
	public void   setDstNode(String dstNode) { 
		this.dstNode = dstNode.replaceAll("\\:", ".");
	}
	public void   setSrcPort(String srcPort) { this.srcPort = srcPort; }
	public void   setDstPort(String dstPort) { this.dstPort = dstPort; }
	public void   setSrcLink(String srcLink) { this.srcLink = srcLink; }
	public void   setDstLink(String dstLink) { this.dstLink = dstLink; }
	public String getSrcNode() { return srcNode; }
	public String getDstNode() { return dstNode; }
	public String getSrcPort() { return srcPort; }
	public String getDstPort() { return dstPort; }
	public String getSrcLink() { return srcLink; }
	public String getDstLink() { return dstLink; }

	@Override
	public int compareTo(SDNLink link) {
		if (this.equals(link))
			return 0;
		return -1;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!o.getClass().equals(SDNLink.class))
				return false;
		
		SDNLink link = (SDNLink) o;
		if ((this.srcNode.equals(link.getSrcNode())) &&
			(this.dstNode.equals(link.getDstNode())) &&
			(this.srcPort.equals(link.getSrcPort())) &&
			(this.dstPort.equals(link.getDstPort())) &&
			(this.srcLink.equals(link.getSrcLink())))
			return true;
		return false;
	}
	
	@Override
	public int  hashCode() {
		return this.srcNode.hashCode() +
			   this.dstNode.hashCode() +
			   this.srcPort.hashCode() +
			   this.dstPort.hashCode() +
			   this.srcLink.hashCode() +
			   this.dstLink.hashCode();
	}

}
