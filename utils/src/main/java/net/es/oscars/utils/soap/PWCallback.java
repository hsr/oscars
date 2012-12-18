package net.es.oscars.utils.soap;

import java.io.IOException;
import java.util.Properties;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.ws.security.WSPasswordCallback;

import org.apache.xml.utils.*;


public class PWCallback implements CallbackHandler {

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        System.out.println (">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ClientX509Handler");
        String keyPass = "changeit";
/**
        try {
            Properties props = PropertyLoader.loadProperties("keystore.properties","",false);
            keyPass = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");
            System.out.println("keystore password is " + keyPass);
        } catch (Exception e) {
            System.out.println ("unable to read keystore properties, setting password to constant");
        }
**/
        /* assume that each keyentry password is the same as the keystore password */
        
        for (int i = 0; i < callbacks.length; i++) {
            System.out.println(">>>>>>>>>>> SETTING PASSWORD");
            WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
            pc.setPassword(keyPass);
        }
    }
 
}
