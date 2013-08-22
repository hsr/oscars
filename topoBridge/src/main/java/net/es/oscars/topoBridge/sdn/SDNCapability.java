package net.es.oscars.topoBridge.sdn;

// @formatter:off
/**
 * Enumeration of all capabilities of a SDN Object
 * 
 * VLAN: can differentiate traffic using VLAN
 * MPLS: can differentiate traffic using MPLS labels
 * L3: 	 can differentiate traffic based on packet IP addresses
 * L2:   can differentiate traffic based on MAC addresses
 * L1:   can take forwarding decisions based on in/out port mappings
 * 
 */
// @formatter:on
public enum SDNCapability {
	VLAN, MPLS, L3, L2, L1
}