package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

/**
 * An agent that transforms PSS requests
 * @author haniotak
 *
 */
public interface Notifier {
    /**
     *
     * processes an action and returns a transformed action as the result
     *
     * this is an interface meant to be used by pre- or post- processing agents
     *
     * @param req the request to be processed
     * @throws PSSException
     * @return the transformed request
     */
    public PSSAction process(PSSAction action) throws PSSException;
    public void setConfig(GenericConfig config) throws PSSException;

}
