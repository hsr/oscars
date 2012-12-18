package net.es.oscars.utils.clients;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.ws.security.WSPasswordCallback;


/**
 *
 * @author hahnepeter
 */
public class ClientPasswordCallback implements CallbackHandler {

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        byte[] buffer = new byte[256];
        String username = null;
        String password = null;
        
        System.out.print("User name: ");
        int res = System.in.read (buffer);
        buffer [res - 1] = 0;
        username = new String (buffer, 0, res - 1);

        System.out.print("Password: ");
        res = System.in.read (buffer);
        buffer [res - 1] = 0;
        password = new String (buffer, 0, res - 1);      
        
        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
        pc.setIdentifier(username);
        pc.setPassword(password);
    }

}
