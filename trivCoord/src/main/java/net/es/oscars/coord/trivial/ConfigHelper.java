package net.es.oscars.coord.trivial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.ho.yaml.Yaml;


public class ConfigHelper {
    @SuppressWarnings("unchecked")
    private Map configuration = null;

    private static ConfigHelper instance;

    public static ConfigHelper getInstance() {
        if (instance == null) {
            instance = new ConfigHelper();
        }
        return instance;
    }

    private ConfigHelper() {
    }

    @SuppressWarnings({ "static-access", "unchecked" })
    public Map getConfiguration(String filename) {
        if (configuration == null) {
            InputStream propFile = this.getClass().getClassLoader().getSystemResourceAsStream(filename);
            try {
                configuration = (Map) Yaml.load(propFile);
            } catch (NullPointerException ex) {
                try {
                    propFile = new FileInputStream(new File("config/"+filename));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                configuration = (Map) Yaml.load(propFile);
            }
        }
        return configuration;
    }
}

