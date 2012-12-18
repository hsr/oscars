package net.es.oscars.pss.api;

import java.util.List;

import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;

/**
 * @author haniotak
 *
 */
public interface CircuitService {
    public void setConfig(CircuitServiceConfig config) throws PSSException;
    
    public List<PSSAction> setup(List<PSSAction> actions) throws PSSException;
    public List<PSSAction> teardown(List<PSSAction> actions) throws PSSException;
    public List<PSSAction> status(List<PSSAction> actions) throws PSSException;
    public List<PSSAction> modify(List<PSSAction> actions) throws PSSException;
}
