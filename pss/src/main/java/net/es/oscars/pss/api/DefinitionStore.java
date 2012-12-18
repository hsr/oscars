package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.definitions.CircuitServiceDefinition;
import net.es.oscars.pss.beans.definitions.DeviceModelDefinition;

/**
 * @author haniotak
 *
 */
public interface DefinitionStore {
    public CircuitServiceDefinition getCircuitServiceDefinition(String serviceId) throws PSSException;
    public DeviceModelDefinition getDeviceModelDefinition(String modelId) throws PSSException;
}
