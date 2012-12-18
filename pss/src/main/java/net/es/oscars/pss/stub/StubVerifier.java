package net.es.oscars.pss.stub;

import net.es.oscars.pss.api.Verifier;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public class StubVerifier implements Verifier {

    public PSSAction verify(PSSAction action, String deviceId) throws PSSException {
        // do nothing, always succeed
        return action;
    }

    public void setConfig(GenericConfig config) throws PSSException {
        // nothing to do
        
    }


}
