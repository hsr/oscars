package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

/**
 * An agent that verifies 
 * @author haniotak
 *
 */
public interface Verifier {
    /**
     *
     * verifies whether a previous (device configuration) PSSAction was successful or not
     * 
     * @param action the action that needs to be verified (typically setup or teardown)
     * @param deviceId the device on which to perform the verify
     * @return the same PSSAction 
     * @throws PSSException
     */
    public PSSAction verify(PSSAction action, String deviceId) throws PSSException;
    public void setConfig(GenericConfig config) throws PSSException;

}
