package net.es.oscars.topoBridge.sdn;

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
	public void fillLinkAttributes() {
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
