package net.es.oscars.api.common;

import java.io.IOException;
import java.util.Properties;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.log4j.Logger;
import org.apache.ws.security.WSPasswordCallback;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

public class ServerX509PasswordCB implements CallbackHandler {
     private static Logger LOG = Logger.getLogger(ServerX509PasswordCB.class);

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        // Default password
        String keyPass = "changeit";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "ServerX509PasswordCB";

        try {
            Properties props = new Properties();
            String pathname = null;
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_API);
            LOG = Logger.getLogger(ServerX509PasswordCB.class);
            LOG.debug(netLogger.start(event));
            try {
                if (cc.getContext() != null ) {  // use new configuration method
                    pathname = cc.getFilePath(cc.getServiceName(), "serverKeystore.properties");
                } else { 
                    LOG.error(netLogger.error(event, ErrSev.MAJOR,"ContextConfig not set"));
                    System.exit(-1);
                }
            } catch  (ConfigException e) {
                LOG.error(netLogger.error(event,ErrSev.MAJOR,"ServerX509PasswordCB caught ConfigException"));
                throw new OSCARSServiceException(e.getMessage());
            }
            //LOG.debug(netLogger.getMsg(event, "keystore pathname is " + pathname));
            java.io.FileInputStream fis = new java.io.FileInputStream(pathname);
            props.load(fis);
            fis.close();
            keyPass = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");
            //LOG.debug(netLogger.getMsg(event, "keystore password is " + keyPass));
        } catch (Exception e) {
            LOG.warn (netLogger.error(event, ErrSev.MINOR,
                                ">>>>>>>>>>>>>>> unable to read keystore properties, setting password to constant"));
            LOG.warn(netLogger.error(event, ErrSev.MINOR, "exception is " + e.toString()));
        }

        /* assume that each keyentry password is the same as the keystore password */
        
        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
            pc.setPassword(keyPass);
        }
    }
 
}
