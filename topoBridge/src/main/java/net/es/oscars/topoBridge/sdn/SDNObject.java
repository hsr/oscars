/**
 * 
 */
package net.es.oscars.topoBridge.sdn;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for SDN* Objects.
 * 
 * @author Henrique Rodrigues
 *
 */
public class SDNObject {
	protected List<SDNCapability> capabilities = null;
	
	public SDNObject() {
		this.capabilities = new ArrayList<SDNCapability>();
	}
	
	public List<SDNCapability> getCapabilities() {
		return capabilities;
	}

	public void addCapability(SDNCapability c) {
		this.capabilities.add(c);
	}
	
	public boolean hasCapability(SDNCapability c) {
		return capabilities.contains(c);
	}
	
}
