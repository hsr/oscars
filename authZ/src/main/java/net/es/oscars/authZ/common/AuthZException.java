package net.es.oscars.authZ.common;

public class AuthZException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public AuthZException(String msg) {
        super(msg);
    }
}
