package net.es.oscars.api.common;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.ws.security.WSPasswordCallback;

public class ServerPasswordCallback implements CallbackHandler{

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];

        String username = pc.getIdentifier();
        String password = pc.getPassword();
        
        // TODO: this is just a stub. Eventually, a request must be made to authN. Meanwhile 
        // returns a SecurityException if username is different than password
        if (! username.equals(password)) {
            throw new SecurityException("wrong username/password");
        }
    }

}
