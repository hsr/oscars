package net.es.oscars.coord.workers;

import net.es.oscars.utils.soap.OSCARSServiceException;


public abstract class ModuleWorker {
    
    /**
     * For the client to re-connect to the module implementing the service.
     */
    public void reconnect() throws OSCARSServiceException {
        throw new OSCARSServiceException ("reconnect is not implemented");
    }

}
