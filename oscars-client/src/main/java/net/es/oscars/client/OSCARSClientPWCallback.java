package net.es.oscars.client;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

public class OSCARSClientPWCallback  implements CallbackHandler {

    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {
        
        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
        pc.setIdentifier(OSCARSClientConfig.getClientKeystoreUser());
        pc.setPassword(OSCARSClientConfig.getClientKeyPassword());
    }

}
