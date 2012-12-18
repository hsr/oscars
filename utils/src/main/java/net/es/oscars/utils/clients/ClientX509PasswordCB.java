package net.es.oscars.utils.clients;

import java.io.IOException;
import java.util.Properties;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.ws.security.WSPasswordCallback;

import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;

public class ClientX509PasswordCB implements CallbackHandler {

    /** 
     * ClientX509PasswordCB handles a callback to returnt the password for the 
     * client keystore that contains the X509 certificate needed to sign a request message
     * 
     * @param callbacks
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        // Default password
        String keyPass = "changeit";
        ContextConfig cc = ContextConfig.getInstance();
        
        try {
            Properties props = new Properties();
            
            String pathname = null;
            if(cc.getContext() != null){
                pathname = cc.getFilePath(cc.getServiceName(), "clientKeystore.properties");
            }else{ 
                pathname = (new SharedConfig("OSCARSService")).getFilePath("clientKeystore.properties");
            }
            java.io.FileInputStream fis = new java.io.FileInputStream(pathname);
            props.load(fis);
            fis.close();
            keyPass = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");            
        } catch (Exception e) {
            System.out.println ("unable to read keystore properties, setting password to constant");
        }

        /* assume that each keyentry password is the same as the keystore password */
        
        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
            pc.setPassword(keyPass);
        }
    }
 
}
