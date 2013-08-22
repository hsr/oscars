package net.es.oscars.topoBridge.sdn;

import net.es.oscars.utils.topology.NMWGParserUtil;

/**
 * A SDNLink is a SDNConnection that connects two network devices. Unlike
 * SDNHop, this class represents a connection between network devices. 
 * 
 * @author Henrique Rodrigues
 */
public class SDNLink extends SDNConnection implements Comparable<SDNLink> {
	private SDNNode node = null;
	private String srcNode = null;
	private String dstNode = null;

	/**
	 * Empty constructor
	 */
	public SDNLink() {
	}

	public SDNLink(SDNLink link) {
		super(link);
		this.srcNode = link.getSrcNode();
		this.dstNode = link.getDstNode();
		this.node = link.getNode();
	}

	// @formatter:off
	/**
	 * Construct link from two URNs
	 * 
	 * @param srcURN URN that describes source of the link
	 * @param dstURN URN that describes destination of the link
	 */
	public SDNLink(String srcURN, String dstURN) {
		super(srcURN, dstURN);
		this.srcNode = NMWGParserUtil.getURNPart(srcURN, NMWGParserUtil.NODE_TYPE);
		this.dstNode = NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.NODE_TYPE);
	}

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
				&& super.isComplete();
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

	public void   setSrcNode(String srcNode) {
		this.srcNode = srcNode.replaceAll("\\:", ".");
	}
	public void   setDstNode(String dstNode) { 
		this.dstNode = dstNode.replaceAll("\\:", ".");
	}	
	public String getSrcNode() { return srcNode; }
	public String getDstNode() { return dstNode; }

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
			   super.hashCode();
	}

}
