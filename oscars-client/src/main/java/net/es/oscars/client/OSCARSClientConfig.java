package net.es.oscars.client;

import java.util.UUID;

public class OSCARSClientConfig {
    static protected String clientKeystoreType = "JKS";
    static protected String clientKeystoreUser = null;
    static protected String clientKeystoreFile = null;
    static protected String clientKeystorePassword = null;
    static protected String clientKeyPassword = null;
    static protected String clientKeysoreSettingUUID = "";

    static protected String sslKeystoreType = "JKS";
    static protected String sslKeystoreFile = null;
    static protected String sslKeystorePassword = null;
    static protected String sslKeysoreSettingUUID = "";
    
    static public String getClientKeystoreType(){
        return clientKeystoreType;
    }
    
    static public String getClientKeystoreUser(){
        return clientKeystoreUser;
    }
    
    static public String getClientKeystoreFile(){
        return clientKeystoreFile;
    }
    
    static public String getClientKeystorePassword(){
        return clientKeystorePassword;
    }
    
    static public String getClientKeyPassword(){
        return clientKeyPassword;
    }
    
    static public String getSSLKeystoreType(){
        return sslKeystoreType;
    }
    
    static public String getSSLKeystoreFile(){
        return sslKeystoreFile;
    }
    
    static public String getSSLKeystorePassword(){
        return sslKeystorePassword;
    }
    
    static public void setClientKeystore(String keystoreUser, String keystoreFile, String keystorePassword, String keyPassword){
        clientKeystoreUser = keystoreUser;
        clientKeystoreFile = keystoreFile;
        clientKeystorePassword = keystorePassword;
        clientKeyPassword = keyPassword;
        clientKeysoreSettingUUID = UUID.randomUUID().toString();
    }
    
    static public void setClientKeystore(String keystoreUser, String keystoreFile, String keystorePassword){
        setClientKeystore(keystoreUser, keystoreFile, keystorePassword, keystorePassword);
    }
    
    static public void setSSLKeyStore(String keystoreFile, String keystorePassword) throws OSCARSClientException{
        sslKeystoreFile = keystoreFile;
        sslKeystorePassword = keystorePassword;
        sslKeysoreSettingUUID = UUID.randomUUID().toString();
    }
    
    /**
     * @return the clientKeysoreSettingUUID
     */
    public static String getClientKeysoreSettingUUID() {
        return clientKeysoreSettingUUID;
    }

    /**
     * @return the sslKeysoreSettingUUID
     */
    public static String getSSLKeysoreSettingUUID() {
        return sslKeysoreSettingUUID;
    }
}
