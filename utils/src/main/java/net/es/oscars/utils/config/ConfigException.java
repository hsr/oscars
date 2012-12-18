package net.es.oscars.utils.config;

public class ConfigException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public ConfigException(String msg) {
        super(msg);
    }

}
