package net.es.oscars.nsibridge.config;

public interface ConfigProvider {
    public String getFilename();

    public void setFilename(String filename);

    public void loadConfig() throws Exception;
}
