package net.es.oscars.utils.validator.wrappers.org.ogf.schema.network.topology.ctrlplane;

import net.es.oscars.utils.validator.EndpointValidator;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

public class CtrlPlanePathContentValidator {
    /**
     * Validate the content of a ResCreateContent object. Note that object will never be null.
     * 
     * @param obj to validate
     * @throws RuntimeException
     */
    public static void validator (CtrlPlanePathContent obj) throws RuntimeException {
        
        for(CtrlPlaneHopContent hop : obj.getHop()){
            //if a link then check if we need to lookup
            if(hop.getLink() != null && hop.getLink().getId() != null && 
                    (!hop.getLink().getId().startsWith("urn:ogf:network"))){
                hop.getLink().setId(EndpointValidator.lookupHostUrn(hop.getLink().getId()));
            }else if(hop.getLinkIdRef() != null && 
                    (!hop.getLinkIdRef().startsWith("urn:ogf:network"))){
                hop.setLinkIdRef(EndpointValidator.lookupHostUrn(hop.getLinkIdRef()));
            }
        }
    }
}
