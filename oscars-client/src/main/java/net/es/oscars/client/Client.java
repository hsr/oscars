package net.es.oscars.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;

public abstract class Client<P> {
    protected P portType;
    protected String lastClientKeystoreUUID;
    protected String lastSSLKeystoreUUID;
    
    final protected String SIG_PROP_FILE = "clientKeystore.properties";
    
    //disable extraneous cxf logging
    static{
        java.util.logging.Logger.getLogger("org.springframework.beans.factory").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.cxf").setLevel(java.util.logging.Level.OFF);
    }
    
    private void prepareClientKeystore(){
        if(OSCARSClientConfig.getClientKeystoreUser() == null){
            return;
        }
        org.apache.cxf.endpoint.Client client = ClientProxy.getClient(this.portType);
        client.getRequestContext().put("ws-security.signature.username", OSCARSClientConfig.getClientKeystoreUser());
        client.getRequestContext().put("ws-security.callback-handler", OSCARSClientPWCallback.class.getName());
        client.getRequestContext().put("ws-security.signature.properties", SIG_PROP_FILE);
    }
    
    protected void prepareSSLForWSDL(){
        //init ssl so we can grab the wsdl
        if(OSCARSClientConfig.getSSLKeystoreFile() != null){
            System.setProperty("javax.net.ssl.trustStore", OSCARSClientConfig.getSSLKeystoreFile());
        }
        if(OSCARSClientConfig.getSSLKeystorePassword() != null){
            System.setProperty("javax.net.ssl.trustStorePassword", OSCARSClientConfig.getSSLKeystorePassword());
        }
    }
    
    protected void setServiceEndpoint(String serviceUrl){
        //override the service URL from the WSDL if explicitly provided by caller
        if(serviceUrl != null){
            ClientProxy.getClient(this.portType).getRequestContext().put("org.apache.cxf.message.Message.ENDPOINT_ADDRESS", serviceUrl);
        }
    }
    
    private void prepareSSLKeyStore() throws OSCARSClientException{
        if(OSCARSClientConfig.getSSLKeystoreFile() == null){
            return;
        }
        
        //Configure SSL
        org.apache.cxf.endpoint.Client client = ClientProxy.getClient(this.portType);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        TLSClientParameters tlsParams = new TLSClientParameters();
        
        /* Configure trust manager */
        try {
            KeyStore keyStore = KeyStore.getInstance(OSCARSClientConfig.getSSLKeystoreType());
            keyStore.load(new FileInputStream(OSCARSClientConfig.getSSLKeystoreFile()), OSCARSClientConfig.getSSLKeystorePassword().toCharArray());
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(keyStore);
            tlsParams.setTrustManagers(trustFactory.getTrustManagers());
        } catch (FileNotFoundException e) {
            throw new OSCARSClientException("Unable to find file " + OSCARSClientConfig.getSSLKeystoreFile());
        } catch (IOException e) {
            throw new OSCARSClientException("Unable to read file " + OSCARSClientConfig.getSSLKeystoreFile() + ": " + e.getMessage());
        } catch (Exception e) {
            throw new OSCARSClientException("Keystore error: " + e.getMessage());
        }
        
        /* Configure ciper suite filers - taken from client config file */
        FiltersType ciperSuiteFilter = new FiltersType();
        ciperSuiteFilter.getInclude().add(".*_EXPORT_.*");
        ciperSuiteFilter.getInclude().add(".*_EXPORT1024_.*");
        ciperSuiteFilter.getInclude().add(".*_WITH_DES_.*");
        ciperSuiteFilter.getInclude().add(".*_WITH_NULL_.*");
        ciperSuiteFilter.getInclude().add(".*_DH_anon_.*");
        tlsParams.setCipherSuitesFilter(ciperSuiteFilter);
        
        conduit.setTlsClientParameters(tlsParams);
    }
    
    synchronized protected void prepareClient() throws OSCARSClientException{
        if(!OSCARSClientConfig.getClientKeysoreSettingUUID().equals(this.lastClientKeystoreUUID)){
            this.lastClientKeystoreUUID = OSCARSClientConfig.getClientKeysoreSettingUUID();
            this.prepareClientKeystore();
        }
        if(!OSCARSClientConfig.getSSLKeysoreSettingUUID().equals(this.lastSSLKeystoreUUID)){
            this.lastSSLKeystoreUUID = OSCARSClientConfig.getSSLKeysoreSettingUUID();
            this.prepareSSLKeyStore();
        }
    }
}
