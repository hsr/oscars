package net.es.oscars.pss.stub;


import net.es.oscars.pss.api.Validator;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.PSSRequest;
import net.es.oscars.pss.beans.config.GenericConfig;

public class StubValidator implements Validator {

    /**
     * the stub validator accepts all requests
     */
    public void validate(PSSRequest req) throws PSSException {
        return;
    }

    public void setConfig(GenericConfig config) {
        // TODO Auto-generated method stub
        
    }

}
