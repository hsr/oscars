package net.es.oscars.topoBridge.sdn;

import net.es.oscars.utils.topology.NMWGParserUtil;

/**
 * A SDNHop is a SDNConnection that takes place within devices. Unlike SDNLink,
 * they share a common SDNNode, which is the node where this hop is installed.
 * 
 * There are two special cases for SDNHops that deserves some discussion: entry
 * hops and exit hops. Suppose that we're given a circuit reservation like:
 * 
 * A.port1,A.port2,...,B.port1,B.port2
 * 
 * In this case, all the hops are "complete", i.e. there is no hop with only one
 * port specified in the reservation. This means that all traffic coming from
 * A.port1 should be forwarded to B.port2 no matter what, regardless of flow
 * distinction.
 * 
 * Now supposed that we're given a reservation like:
 * A.port2,D.port1,D.port2,...,C.port1,C.port2,B.port1
 * 
 * What traffic should be forwarded through the circuit in this case? With the
 * SDN PSS implementation, the hop A is said to be an Entry Hop to the circuit,
 * and B is said to be an Exit Hop. The traffic that will be forwarded through
 * the circuit will depend on the existence of an OFMatch specified by the user.
 * If the user specifies an OFMatch, then that will be used to choose what
 * traffic will flow through the circuit. However, if no OFMatch is specified,
 * the SDN PSS leaves this decision to the switch and don't install any rules on
 * A or B.
 * 
 * @author Henrique Rodrigues
 */
public class SDNHop extends SDNConnection implements Comparable<SDNHop> {
	private SDNNode node = null;
	public static String ENTRY_HOP_URN = "urn:ogf:network:domain=domain:node=00.00.00.00.00.00.00.00:port=0:link=0";
	public static String EXIT_HOP_URN = "urn:ogf:network:domain=domain:node=FF.FF.FF.FF.FF.FF.FF.FF:port=255:link=255";
	private boolean entryHop = false;
	private boolean exitHop = false;

	public SDNHop(SDNHop hop) {
		super(hop);
		this.node = hop.getNode();
		this.entryHop = hop.isEntryHop();
		this.exitHop = hop.isExitHop();
	}

	// @formatter:off
	/**
	 * Empty constructor
	 */
	public SDNHop() {}

	/**
	 * Construct a hop from two URNs
	 * 
	 * @param srcURN URN that describes source of the link
	 * @param dstURN URN that describes destination of the link
	 * @throws Exception 
	 */
	public SDNHop(String srcURN, String dstURN) throws Exception {
		super(srcURN, dstURN);
		
		if (dstURN.equals(EXIT_HOP_URN)) {
			this.node = new SDNNode(
					NMWGParserUtil.getURNPart(srcURN, NMWGParserUtil.NODE_TYPE)
			);
			this.exitHop = true;
		}
		else if (srcURN.equals(ENTRY_HOP_URN)) {
			this.node = new SDNNode(
					NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.NODE_TYPE)
			);
			this.entryHop = true;
		}
		else if (!
			NMWGParserUtil.getURNPart(srcURN, NMWGParserUtil.NODE_TYPE).equals(
			NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.NODE_TYPE)))
			throw new Exception("SDNHop connects two ports from the *same* node.");
		else {
			this.node = new SDNNode(
					NMWGParserUtil.getURNPart(dstURN, NMWGParserUtil.NODE_TYPE)
			);
		}
		
		if (this.node.getId().matches("^00.*")) {
			this.addCapability(SDNCapability.L2);
			System.out
					.println("WARNING: Adding L2 capabitility for "
							+ this.node.getId());
		}
		else if (this.entryHop) {
			throw new Exception("Entry hops need to be L2 capable");
		}
		else if (this.exitHop) {
			throw new Exception("Exit hops need to be L2 capable");
		}
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
		
		boolean swap  = this.entryHop; 
		this.entryHop = hop.exitHop;
		this.exitHop  = swap;
		
		return this;
	}

	public SDNHop getReverse() {
		return new SDNHop(this).reverse();
	}

	public SDNNode getNode() { return node; }
	public void setNode(SDNNode node) { this.node = node; }

	public boolean isEntryHop() {
		return this.entryHop;
	}
	
	public boolean isExitHop() {
		return this.exitHop;
	}
	
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
	
	@Override
	public String toString() {
		return String.format("SDNHop(SDNNode(%s), %s:%s -> %s:%s)", 
				this.node.getId(), this.srcPort, this.srcLink,
				this.dstPort, this.dstLink);
	}
}
