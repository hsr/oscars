package net.es.oscars.topoBridge.sdn;

import net.es.oscars.utils.topology.NMWGParserUtil;

public abstract class SDNConnection extends SDNObject {
	protected String srcPort = null;
	protected String dstPort = null;
	protected String srcLink = null;
	protected String dstLink = null;

	/**
	 * Empty constructor
	 */
	public SDNConnection() {
	}

	public SDNConnection(SDNConnection conn) {
		this.srcPort = conn.getSrcPort();
		this.dstPort = conn.getDstPort();
		this.srcLink = conn.getSrcLink();
		this.dstLink = conn.getDstLink();
	}

	// @formatter:off
	/**
	 * Construct connection from two URNs
	 * 
	 * @param srcURN URN that describes source of the link
	 * @param dstURN URN that describes destination of the link
	 */
	public SDNConnection(String srcURN, String dstURN) {
		this.srcPort = NMWGParserUtil.getURNPart(srcURN, NMWGParserUtil.PORT_TYPE);
		this.dstPort = NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.PORT_TYPE);
		this.srcLink = NMWGParserUtil.getURNPart(srcURN, NMWGParserUtil.LINK_TYPE);
		this.dstLink = NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.LINK_TYPE);
		
		// By default all connections can forward based on in/out port mappings
		this.addCapability(SDNCapability.L1);
	}

	/**
	 * Checks if all the attributes are set.
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return (this.srcPort != null) && (this.dstPort != null) 
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

	public void   setSrcPort(String srcPort) { this.srcPort = srcPort; }
	public void   setDstPort(String dstPort) { this.dstPort = dstPort; }
	public void   setSrcLink(String srcLink) { this.srcLink = srcLink; }
	public void   setDstLink(String dstLink) { this.dstLink = dstLink; }
	public String getSrcPort() { return srcPort; }
	public String getDstPort() { return dstPort; }
	public String getSrcLink() { return srcLink; }
	public String getDstLink() { return dstLink; }
	
	@Override
	public int  hashCode() {
		return this.srcPort.hashCode() +
			   this.dstPort.hashCode() +
			   this.srcLink.hashCode() +
			   this.dstLink.hashCode();
	}
}
