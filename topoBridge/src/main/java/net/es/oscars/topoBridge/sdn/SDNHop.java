package net.es.oscars.topoBridge.sdn;

import net.es.oscars.utils.topology.NMWGParserUtil;

/**
 * A SDNHop is a SDNConnection that takes place within devices. Unlike SDNLink,
 * they share a common SDNNode, which is the node where this hop is installed.
 * 
 * @author Henrique Rodrigues
 */
public class SDNHop extends SDNConnection implements Comparable<SDNHop> {
	private SDNNode node = null;

	public SDNHop(SDNHop hop) {
		super(hop);
		this.node = hop.getNode();
	}

	// @formatter:off
	/**
	 * Empty constructor
	 */
	public SDNHop() {}

	/**
	 * Construct link from two URNs
	 * 
	 * @param srcURN URN that describes source of the link
	 * @param dstURN URN that describes destination of the link
	 */
	public SDNHop(String srcURN, String dstURN) {
		super(srcURN, dstURN);
		this.node = new SDNNode(
			NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.NODE_TYPE)
		);
	}

	/**
	 * Checks if all the attributes are set.
	 * 
	 * @return
	 */
	@Override
	public boolean isComplete() {
		return (this.node != null) && super.isComplete();
	}

	public SDNHop reverse() {
		SDNHop hop = new SDNHop(this);
		this.srcPort = hop.getDstPort();
		this.dstPort = hop.getSrcPort();
		this.srcLink = hop.getDstLink();
		this.dstLink = hop.getSrcLink();
		return this;
	}

	public SDNHop getReverse() {
		return new SDNHop(this).reverse();
	}

	public SDNNode getNode() { return node; }
	public void setNode(SDNNode node) { this.node = node; }

	@Override
	public int compareTo(SDNHop hop) {
		if (this.equals(hop))
			return 0;
		return -1;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!o.getClass().equals(SDNHop.class))
				return false;
		
		SDNHop hop = (SDNHop) o;
		if ((this.node.equals(hop.getNode())) &&
			(this.srcPort.equals(hop.getSrcPort())) &&
			(this.dstPort.equals(hop.getDstPort())) &&
			(this.srcLink.equals(hop.getSrcLink())))
			return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.node.hashCode() +
			   super.hashCode();
	}

}
