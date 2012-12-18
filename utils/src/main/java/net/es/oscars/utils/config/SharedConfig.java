package net.es.oscars.utils.config;

import java.io.File;
import java.io.IOException;
import java.lang.RuntimeException;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * The SharedConfig class hides the actual layout of OSCARS configuration file to the services.
 * Each configuration file is owned by a service which provides its name space. A null service represents 
 * the top level (i.e. OSCARS).
 * 
 * The root directory of the storage area for the configuration files is defined at runtime by the 
 * shell variable OSCARS_HOME.
 *
 */
public class SharedConfig {
    public static String OSCARS_HOME_PROPERTY      = "OSCARS_HOME";
    
    private static String SERVICE_CONF_DIR         = "conf";
    private static String oscarsHome               = null;
    
    private String service                         = null;

    static {
        SharedConfig.setOscarsHome();
    }
    
    /**
     * Checks to see that oscarsHome has been set to an existing directory
     * 
     * @param service unqualified name of the service which is being configured
     */
    public SharedConfig (String service) {
        // First check if the OSCARS_HOME property is defined as a system property.
        
        this.service = service;
        
        File oscarsDirFile = new File (SharedConfig.oscarsHome);
        if ( ! oscarsDirFile.exists()) {
            throw new RuntimeException ("Can't access OSCARS home: " + SharedConfig.oscarsHome);
        }
        try {
            SharedConfig.oscarsHome = oscarsDirFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
     }
    
    /**
     * sets the static oscarsHome variable from either a system property or and
     * environment variable named "OSCARS_HOME"
     * called statically at startup
     */
    public static synchronized void setOscarsHome() {
        if (SharedConfig.oscarsHome != null) {
            // Already initialized
            return;
        }
        SharedConfig.oscarsHome = System.getProperty(SharedConfig.OSCARS_HOME_PROPERTY);
        if (SharedConfig.oscarsHome == null) {
            // No system property has been defined. Try shell environment variable
            SharedConfig.oscarsHome = System.getenv(OSCARS_HOME_PROPERTY);
            if (SharedConfig.oscarsHome == null) {
                throw new RuntimeException (SharedConfig.OSCARS_HOME_PROPERTY + " must be defined (Shell env. or System property.");
            }
        }
    }
    
    /**
     * Retrieve the pathname of a configuration file
     * @param filename: name of the configuration file
     * @return pathname of the configuration file
     */
    public String getFilePath (String filename) {
        if (service != null) {
            return SharedConfig.oscarsHome + File.separatorChar + service + File.separatorChar + SharedConfig.SERVICE_CONF_DIR + File.separatorChar + filename ;
        } else {
            return SharedConfig.oscarsHome + File.separatorChar + SharedConfig.SERVICE_CONF_DIR + File.separatorChar + filename ;
        }
    }

   public String getFilePath (String category, String filename) {
        if (service != null) {
            return SharedConfig.oscarsHome + File.separatorChar + category + File.separatorChar + service + File.separatorChar + filename ;
        } else {
            return SharedConfig.oscarsHome + File.separatorChar + category + File.separatorChar + filename ;
        }
    }
   /**
    * Retrieve the URL for the wsdl file for this service
    * 
    * @param version version number of the wsdl file
    *   null if there is no version 
    * @return URL of the wsdl file
    */
    public URL getWSDLPath (String version) throws MalformedURLException {
        if (version != null) {
            return new URL ("file:" +SharedConfig.oscarsHome + File.separatorChar + "wsdl" + File.separatorChar + service + File.separatorChar + service + "-" + version + ".wsdl");
        } else {
            return new URL ("file:" + SharedConfig.oscarsHome + File.separatorChar + "wsdl" + File.separatorChar + service + File.separatorChar + service + ".wsdl");
        }
    }

    public URL getNamedWSDLPath (String wsdlName) throws MalformedURLException {
        if (wsdlName != null) {
            return new URL ("file:" +SharedConfig.oscarsHome + File.separatorChar + "wsdl" + File.separatorChar + service + File.separatorChar + wsdlName);
        } 
        return null;
    }
    /**
     * Sets the absolute path name of the log4j.properties file for this service
     * Called from service Invoker classes and test initTest classes
     */
    public void setLog4j () {
        String path = this.getFilePath ("log4j.properties");
        System.setProperty("log4j.configuration", "file:" + path);
        //System.out.println("setLog4j setting log4j.configuration to file:" + path);
    }
}

