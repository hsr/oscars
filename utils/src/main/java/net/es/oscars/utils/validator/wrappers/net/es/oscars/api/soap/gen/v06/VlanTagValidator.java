package net.es.oscars.utils.validator.wrappers.net.es.oscars.api.soap.gen.v06;

import net.es.oscars.api.soap.gen.v06.VlanTag;
import net.es.oscars.utils.topology.VlanRange;

public class VlanTagValidator {
    /**
     * Validate the content of a VlanTag object.
     * @param obj to validate
     * @throws RuntimeException
     */
    public static void validator (VlanTag obj) throws RuntimeException {
        if(obj == null){
            return;
        }
        
        //check for empty tag
        if(obj.getValue() == null){
            throw new RuntimeException("Vlan tag does not contain a value");
        }
        
        //check that it is in the correct range
        new VlanRange(obj.getValue());
    }
}
