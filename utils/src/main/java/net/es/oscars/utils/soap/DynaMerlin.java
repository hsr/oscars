package net.es.oscars.utils.soap;

import java.lang.ClassLoader;
import java.util.Properties;
import java.io.IOException;

import java.security.KeyStore;

import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.components.crypto.CredentialException;

import net.es.oscars.utils.config.ContextConfig;

public class DynaMerlin extends Merlin {

    private Properties deployProps;  // properties from site specific property file
    
    /**
     * Constructor sets static properties in parent class 
     *    and site modified properteries in deployProps
     * @param props  static properties from jar classpath
     * @throws CredentialException
     * @throws IOException
     */
    public DynaMerlin (Properties props) throws CredentialException, IOException {
        super (props);
        this.setProps();
    }
    
    /**
     * Constructor sets static properties in parent class 
     *    and site modified properteries in deployProps
     * @param props  static properties from jar classpath
     * @throws CredentialException
     * @throws IOException
     */
    public DynaMerlin (Properties props, ClassLoader loader) throws CredentialException, IOException {
        super(props);
        this.setProps();
    }
    /**
     * Uses the unqualified service and keystore.properties names that have
     * come from the properties file in the jar/classes directory to 
     * load the properties from the property file.
     * deployProps has properties that can be modified by a deployment site.
     * @param file unqualified property file name
     */
    private void readProps (String file) {

        this.deployProps = new Properties();
        try {
            ContextConfig cc = ContextConfig.getInstance();
            String pathname = cc.getFilePath(file);
            java.io.FileInputStream fis = new java.io.FileInputStream(pathname);
            this.deployProps.load(fis);
            fis.close();
            
        } catch (Exception ee) {
            throw new RuntimeException (ee);
        }
    }
    
    /**
     * Adds the properties defined in the site specific properties file to the
     * Merlin.properties.
     * NOTE: has only been tested with JKS keystores
     */
    private void setProps () throws IOException {

        if (super.properties == null) {
            super.properties = new Properties();
        }
        // Read deployment property file
        String propFile = this.properties.getProperty ("net.es.oscars.utils.soap.DynaMerlin.propfile");
        this.readProps (propFile);

        // Retrieve the remaining properties
        String password = this.deployProps.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");
        String keystore = this.deployProps.getProperty("org.apache.ws.security.crypto.merlin.file");
        String keystoreType = this.deployProps.getProperty("org.apache.ws.security.crypto.merlin.keystore.type");
        
        //this.properties.setProperty ("org.apache.ws.security.crypto.provider","org.apache.ws.security.components.crypto.DynaMerlin");
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.keystore.type", keystoreType);
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.keystore.password",password);
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.file",keystore);
       //this.properties.list(System.out);

        try {
            KeyStore ks = KeyStore.getInstance(keystoreType);
            java.io.FileInputStream fis = new java.io.FileInputStream(keystore);
            ks.load(fis, password.toCharArray());
            fis.close();
            super.setKeyStore(ks);
        } catch (Exception ee) {
            throw new RuntimeException (ee);
        }
    }
}
