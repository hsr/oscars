package net.es.oscars.resourceManager.common;

/**
 * RMException is the top-level exception thrown by resource manager methods.
 */
public class RMException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public RMException(String msg) {
        super(msg);
    }
    public RMException(Exception ex) {
    	super(ex);
    }
}
