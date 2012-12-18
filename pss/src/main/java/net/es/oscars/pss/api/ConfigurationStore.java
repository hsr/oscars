package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.BaseConfig;

/**
 * @author haniotak
 *
 */
public interface ConfigurationStore {
    public BaseConfig getBaseConfig() throws PSSException;
}
