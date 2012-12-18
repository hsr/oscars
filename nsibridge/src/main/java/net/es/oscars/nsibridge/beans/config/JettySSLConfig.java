package net.es.oscars.nsibridge.beans.config;



public class JettySSLConfig {

    private boolean useSSL = false;
    private String sslKeystorePath = "";
    private String sslKeystorePass = "";
    private String sslKeyPass = "";
    private String sslTruststorePath = "";
    private String sslTruststorePass = "";

    public JettySSLConfig(){

    };

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }


    public String getSslKeystorePath() {
        return sslKeystorePath;
    }

    public void setSslKeystorePath(String sslKeystorePath) {
        this.sslKeystorePath = sslKeystorePath;
    }

    public String getSslKeystorePass() {
        return sslKeystorePass;
    }

    public void setSslKeystorePass(String sslKeystorePass) {
        this.sslKeystorePass = sslKeystorePass;
    }

    public String getSslKeyPass() {
        return sslKeyPass;
    }

    public void setSslKeyPass(String sslKeyPass) {
        this.sslKeyPass = sslKeyPass;
    }

    public String getSslTruststorePath() {
        return sslTruststorePath;
    }

    public void setSslTruststorePath(String sslTruststorePath) {
        this.sslTruststorePath = sslTruststorePath;
    }

    public String getSslTruststorePass() {
        return sslTruststorePass;
    }

    public void setSslTruststorePass(String sslTruststorePass) {
        this.sslTruststorePass = sslTruststorePass;
    }

}
