package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.PSSRequest;
import net.es.oscars.pss.beans.config.GenericConfig;

/**
 * A validator that checks PSS requests for errors
 *
 * @author haniotak
 *
 */
public interface Validator {
    /**
     *
     * Validates request for errors
     *
     * Just return if everything is OK
     * Throw an exception if an error is detected
     *
     * @param req the request to be validated
     * @throws PSSException
     */
    public void validate(PSSRequest req) throws PSSException;
    public void setConfig(GenericConfig config) throws PSSException;

}
