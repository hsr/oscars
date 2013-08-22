package net.es.oscars.topoBridge.sdn;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node (=network device) in the topology.
 * 
 * @author Henrique Rodrigues <hsr@cs.ucsd.edu>
 */
// Should this extend o.o.s.n.t.c.CtrlPlaneNodeContent ?
public class SDNNode extends SDNObject implements Comparable<SDNNode> {

	private List<SDNLink> links = null;
	private List<SDNLink> inLinks = null;
	private List<SDNLink> outLinks = null;

	private String id;

	public SDNNode(String id) {
		this.id = id;
		this.capabilities = new ArrayList<SDNCapability>();
		this.links = new ArrayList<SDNLink>();
		this.inLinks = new ArrayList<SDNLink>();
		this.outLinks = new ArrayList<SDNLink>();

		// By default all nodes forward based on in/out port mappings
		this.addCapability(SDNCapability.L1);
		return;
	}

	public String getId() {
		return id;
	}

	// TODO: change the design to use links from nodes
	public void setLinks(List<SDNLink> links) throws Exception {
		for (SDNLink l : links) {
			this.addLink(l);
		}
	}

	public void addLink(SDNLink link) throws Exception {
		this.links.add(link);
		if (link.getSrcNode().equals(this.id))
			this.outLinks.add(link);
		else if (link.getDstNode().equals(this.id))
			this.inLinks.add(link);
		else
			throw new Exception("Link not connected to Node");
	}

	// @formatter:off
	public List<SDNLink> getLinks() { return links; }
	public List<SDNLink> getInLinks() { return inLinks; }
	public List<SDNLink> getOutLinks() { return outLinks; }

	public int compareTo(SDNNode node) {
		// Should we compare links as well?
		return this.getId().compareTo(node.getId());
	}

}
