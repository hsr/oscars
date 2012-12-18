package net.es.oscars.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Properties;

import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;

public class OSCARSClientMerlin extends Merlin{
    
    //currently only support JKS
    
    public OSCARSClientMerlin (Properties props) throws CredentialException, IOException {
        super (props);
        this.setProps();
    }

    public OSCARSClientMerlin (Properties props, ClassLoader loader) throws CredentialException, IOException {
        super(props, loader);
        this.setProps();
    }
    
    private void setProps () throws IOException {

        if (super.properties == null) {
            super.properties = new Properties();
        }

        //this.properties.setProperty ("org.apache.ws.security.crypto.provider","org.apache.ws.security.components.crypto.DynaMerlin");
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.keystore.type", OSCARSClientConfig.getClientKeystoreType());
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.keystore.password", OSCARSClientConfig.getClientKeystorePassword());
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.file", OSCARSClientConfig.getClientKeystoreFile());

        try {
            KeyStore ks = KeyStore.getInstance(OSCARSClientConfig.getClientKeystoreType());
            FileInputStream fis = new FileInputStream(OSCARSClientConfig.getClientKeystoreFile());
            ks.load(fis, OSCARSClientConfig.getClientKeystorePassword().toCharArray());
            fis.close();
            super.setKeyStore(ks);
        } catch (Exception ee) {
            throw new RuntimeException (ee);
        }
    }
}
