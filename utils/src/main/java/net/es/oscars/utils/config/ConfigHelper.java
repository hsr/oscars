package net.es.oscars.utils.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.ho.yaml.Yaml;


public class ConfigHelper {

    @SuppressWarnings({ "static-access", "unchecked" })
    static public Map getConfiguration(String filename) {
    	Map configuration = null;
    	
        if (configuration == null) {
            InputStream propFile = ConfigHelper.class.getClassLoader().getSystemResourceAsStream(filename);
            try {
                configuration = (Map) Yaml.load(propFile);
            } catch (NullPointerException ex) {
                try {
                    propFile = new FileInputStream(new File(filename));
                } catch (FileNotFoundException e) {
                    System.out.println("ConfigHelper: configuration file: "+ filename + " not found");
                    e.printStackTrace();
                    System.exit(1);
                }
                configuration = (Map) Yaml.load(propFile);
            }
        }
        return configuration;
    }

    static public <T> T getConfiguration(String filename, java.lang.Class<T> clazz) {
        T configuration = null;

        if (configuration == null) {
            InputStream propFile = ConfigHelper.class.getClassLoader().getSystemResourceAsStream(filename);
            if (propFile == null) {
                try {
                    propFile = new FileInputStream(new File(filename));
                } catch (FileNotFoundException e) {
                    System.out.println("ConfigHelper: configuration file: "+ filename + " not found");
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            try {
                configuration =  Yaml.loadType(propFile, clazz);
            } catch (FileNotFoundException e) {
                System.out.println("ConfigHelper: configuration file: "+ filename + " not found");
                e.printStackTrace();
                System.exit(1);

            }
        }
        return configuration;
    }


}

