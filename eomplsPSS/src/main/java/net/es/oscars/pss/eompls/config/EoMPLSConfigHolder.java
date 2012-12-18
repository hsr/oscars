package net.es.oscars.pss.eompls.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.ho.yaml.Yaml;
import net.es.oscars.pss.eompls.beans.config.EoMPLSBaseConfig;
import net.es.oscars.utils.config.ConfigException;

public class EoMPLSConfigHolder {
    private static EoMPLSConfigHolder instance;
    private EoMPLSBaseConfig eomplsBaseConfig;

    private EoMPLSConfigHolder() {
    }
    
    public static EoMPLSConfigHolder getInstance() {
        if (instance == null) {
            instance = new EoMPLSConfigHolder();
        }
        return instance;
    }


    
    

    @SuppressWarnings("static-access")
    public static void loadConfig(String filename) throws ConfigException {
        System.out.println("ConfigHolder: "+filename);
        EoMPLSConfigHolder holder = EoMPLSConfigHolder.getInstance();

        EoMPLSBaseConfig configuration = null;
        InputStream propFile = EoMPLSConfigHolder.class.getClassLoader().getSystemResourceAsStream(filename);
        try {
            configuration = (EoMPLSBaseConfig) Yaml.loadType(propFile, EoMPLSBaseConfig.class);
        } catch (NullPointerException ex) {
            try {
                propFile = new FileInputStream(new File(filename));
                configuration = (EoMPLSBaseConfig) Yaml.loadType(propFile, EoMPLSBaseConfig.class);
            } catch (FileNotFoundException e) {
                System.out.println("EoMPLSConfigHolder: configuration file: "+ filename + " not found");
                e.printStackTrace();
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("EoMPLSConfigHolder: configuration file: "+ filename + " not found");
            e.printStackTrace();
            System.exit(1);
        }
        holder.setEomplsBaseConfig(configuration);
        try {
            propFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    

    public void setEomplsBaseConfig(EoMPLSBaseConfig eomplsBaseConfig) {
        this.eomplsBaseConfig = eomplsBaseConfig;
    }

    public EoMPLSBaseConfig getEomplsBaseConfig() {
        return eomplsBaseConfig;
    }

    

}
