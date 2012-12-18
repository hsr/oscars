package net.es.oscars.nsibridge.beans.config;


import java.util.HashMap;

public class JettyAuthConfig {
    private boolean useBasicAuth = false;
    private String passwdFileName = "passwd.yaml";
    private HashMap<String, String> userPasswords = new HashMap<String, String>();

    public JettyAuthConfig(){

    };

    public String getPasswdFileName() {
        return passwdFileName;
    }

    public void setPasswdFileName(String passwdFileName) {
        this.passwdFileName = passwdFileName;
    }

    public HashMap<String, String> getUserPasswords() {
        return userPasswords;
    }

    public void setUserPasswords(HashMap<String, String> userPasswords) {
        this.userPasswords = userPasswords;
    }

    public boolean isUseBasicAuth() {
        return useBasicAuth;
    }

    public void setUseBasicAuth(boolean useBasicAuth) {
        this.useBasicAuth = useBasicAuth;
    }


}
