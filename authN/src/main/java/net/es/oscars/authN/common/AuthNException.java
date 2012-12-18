package net.es.oscars.authN.common;

public class AuthNException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public AuthNException(String msg) {
        super(msg);
    }
}
