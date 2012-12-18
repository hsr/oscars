package net.es.oscars.pss.beans;

public class PSSException extends Exception {

	private static final long serialVersionUID = 1L;
	public PSSException(Throwable ex) {
	    super(ex);
	}

	public PSSException(String msg){
        super(msg);
    }
}
