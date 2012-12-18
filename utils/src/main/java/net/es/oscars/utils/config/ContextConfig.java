package net.es.oscars.utils.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.ho.yaml.Yaml;

import net.es.oscars.utils.bootstrap.BootClassLoader;

/**
 * This class is used to provide the facility to IDC service components
 * to access configuration files without going into the details of the
 * file system.
 *
 * A context and service name must be set, and a manifest mapping
 * populated through yaml or programmatically before the main functions
 * can be used.
 *
 * @author haniotak
 *
 */
public class ContextConfig {
    private static ContextConfig defaultInstance;
    private static HashMap<String,ContextConfig> instances = new HashMap<String,ContextConfig>();
    private HashMap<String, HashMap<String, HashMap<String, String>>> manifest = new HashMap<String, HashMap<String, HashMap<String, String>>>();


    /**
     * Singleton private constructor
     */
    private ContextConfig() {
    }

    private String context = null;
    private String serviceName = null;

    /**
     * Singleton access
     * @return the ContextConfig instance
     */
    public static ContextConfig getInstance() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl instanceof BootClassLoader) {
            return getInstance(((BootClassLoader) cl).getModuleName());
        } else if (ContextConfig.defaultInstance == null) {
            return getInstance("DEFAULT_CONFIG_CONTEXT");
        }
        return ContextConfig.defaultInstance;
    }

    public static ContextConfig getInstance(String module) {

        ContextConfig config = ContextConfig.instances.get (module);
        if (config == null) {
            config = new ContextConfig();
            ContextConfig.instances.put (module, config);
        }
        ContextConfig.defaultInstance = config;
        return config;
    }

    /**
     * Gets the full path to a file given the "canonical name"
     * @param fileAlias the canonical name
     * @return the full path to the file
     * @throws ConfigException
     */
    public String getFilePath(String fileAlias) throws ConfigException {
        return this.getFilePath(serviceName, context, fileAlias);
    }

    /**
     * Gets the full path to a file given the "canonical name" and service name
     * @param serviceName the service name
     * @param fileAlias the canonical name
     * @return the full path to the file
     * @throws ConfigException
     */
    public String getFilePath(String serviceName, String fileAlias) throws ConfigException {
        return this.getFilePath(serviceName, context, fileAlias);
    }

    /**
     * Gets the full path to a file given the "canonical name", service name, and
     * configuration context
     *
     * @param serviceName the service name
     * @param context the context
     * @param fileAlias the canonical name
     * @return the full path to the file
     * @throws ConfigException
     */
    public String getFilePath(String serviceName, String context, String fileAlias) throws ConfigException {

        String oscarsHomeProp = ConfigDefaults.OSCARS_HOME_PROPERTY;
        String oscarsDistProp = ConfigDefaults.OSCARS_DIST_PROPERTY;

        String oscarsHome = System.getenv(oscarsHomeProp);
        String oscarsDist = System.getenv(oscarsDistProp);

        if (context == null) {
            throw new ConfigException("No configuration context set");
        }

        if (serviceName == null) {
            throw new ConfigException("No serviceName set");
        }

        if (fileAlias == null) {
            throw new ConfigException("No fileAlias provided");
        }

        if (manifest == null) {
            throw new ConfigException("No config manifest!");
        }
        HashMap<String, HashMap<String, String>> svcManifest = manifest.get(serviceName);
        if (svcManifest == null) {
            throw new ConfigException("No manifest for service: ["+serviceName+"]");
        }
        HashMap<String, String> contextManifest = svcManifest.get(context);
        if (contextManifest == null) {
            throw new ConfigException("No manifest for context: "+context);
        }
        String filePath = contextManifest.get(fileAlias);
        if (filePath == null) {
            throw new ConfigException("No filename for alias: "+fileAlias);
        }

        if (oscarsHomeProp != null && filePath.contains(oscarsHomeProp)) {
            if (oscarsHome != null ) {
                filePath = filePath.replace(oscarsHomeProp, oscarsHome);
            } else {
                throw new ConfigException("Environment variable not set: "+oscarsHomeProp);
            }
        }


        if (oscarsDistProp != null && filePath.contains(oscarsDistProp)) {
            if (oscarsDist != null ) {
                filePath = filePath.replace(oscarsDistProp, oscarsDist);
            } else {
                throw new ConfigException("Environment variable not set: "+oscarsDistProp);
            }
        }

        return filePath;
    }

    /**
     * Utility function to populate the manifest map with a YAML
     * file.
     *
     * @param service name of the directory where the service's configuration files are deployed
     * @param fileName name of the manifest.yaml file (normally "manifest.yaml"
     * @throws ConfigException
     */
    @SuppressWarnings({ "unchecked", "static-access" })
    public void loadManifest(String service, String fileName) throws ConfigException {
        String filePath = new SharedConfig (service).getFilePath(fileName);

        loadManifest(new File(filePath));
    }

    public void loadManifest(File manifestFile) throws ConfigException {
        Map yaml = null;
        String filePath = manifestFile.getAbsolutePath();
        System.out.println("Loading manifest from " + manifestFile);
        InputStream manifestIs = null;
        if (manifestFile.exists()) {
            try {
                manifestIs = new FileInputStream(manifestFile);
            } catch (FileNotFoundException e) {
                // never happen
                e.printStackTrace();
                throw new ConfigException(e.getMessage());
            }
        } else {
            ClassLoader cl = ContextConfig.class.getClassLoader();
            manifestIs = cl.getResourceAsStream(filePath);
            if (manifestIs == null) {
                manifestIs = cl.getSystemResourceAsStream(filePath);
            }
            if (manifestIs == null) {
                throw new ConfigException("Could not locate manifest file: "+filePath);
            }
        }
        yaml = (Map<String, Object>) Yaml.load(manifestIs);

        Set<String> serviceNames = yaml.keySet();

        HashMap<String, HashMap<String, HashMap<String, String>>> tmpManifest = new HashMap<String, HashMap<String, HashMap<String, String>>>();
        for (String serviceName : serviceNames) {
            //System.out.println("service name is " + serviceName);
            Map<String, Object> ySvcConfig = (Map<String, Object>) yaml.get(serviceName);
            Set<String> contexts = ySvcConfig.keySet();

            HashMap<String, HashMap<String, String>> svcManifest = new HashMap<String, HashMap<String, String>>();

            for (String context : contexts) {
                //System.out.println("context is " + context);
                Map<String, String> yAliases = (Map<String, String>) ySvcConfig.get(context);

                HashMap<String, String> aliases = new HashMap<String, String>();
                aliases.putAll(yAliases);
                svcManifest.put(context, aliases);
            }
            tmpManifest.put(serviceName, svcManifest);
        }
        manifest = tmpManifest;
    }

    /**
     * returns the configuration manifest as a human-readable string
     * @return the readable manifest
     */
    public String getManifestString() {
        String out = "";
        for (String serviceName : manifest.keySet()) {
            Set<String> contexts = manifest.get(serviceName).keySet();
            for (String context : contexts) {
                HashMap<String, String> aliases = manifest.get(serviceName).get(context);
                for (String alias: aliases.keySet()) {
                    String path = aliases.get(alias);
                    out += serviceName+" : "+context+" : "+alias+" : "+path+"\n";
                }
            }
        }
        return out;
    }

    /**
     * returns the path to the WSDL file for the currently set serviceName
     * context and service name must be set before calling
     *
     * @param version the desired version of the WSDL, may be null
     * @return the URL to the WSDL
     * @throws MalformedURLException
     */
    public URL getWSDLPath (String version) throws MalformedURLException {
        if (serviceName == null) {
            throw new MalformedURLException("Service name not set");
        }
        return getWSDLPath(serviceName, version);
    }


    /**
     * returns the path to the WSDL file for the input serviceName
     * context must be set before calling
     *
     * @param version the desired version of the WSDL, may be null
     * @return the URL to the WSDL
     * @throws MalformedURLException
     */
    public URL getWSDLPath (String serviceName,String version) throws MalformedURLException {

        String wsdlName = ConfigDefaults.WSDL;
        return getNamedWSDLPath(serviceName, wsdlName, version);
    }

    public URL getNamedWSDLPath (String serviceName, String wsdlName, String version) throws MalformedURLException {
        if (version != null) {
            wsdlName = wsdlName +"-"+version;
        }
        String wsdlPath = null;
        try {
            wsdlPath = getFilePath(serviceName, wsdlName);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new MalformedURLException(e.getMessage());
        }
        return new URL ("file:" +wsdlPath);

    }
    /**
     * returns the path to the WSDL file for the canonical filename argument
     * context and service name must be set before calling
     *
     * @param wsdlName the canonical name of the WSDL
     * @param version the desired version of the WSDL, may be null
     * @return the URL to the WSDL
     * @throws MalformedURLException
     */
    public URL getNamedWSDLPath (String wsdlName, String version) throws MalformedURLException {
        if (version != null) {
            wsdlName = wsdlName +"-"+version;
        }
        wsdlName = wsdlName+".wsdl";
        String wsdlPath = null;
        try {
            wsdlPath = getFilePath(wsdlName);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new MalformedURLException(e.getMessage());
        }
        return new URL ("file:" +wsdlPath);

    }

    public HashMap<String, HashMap<String, HashMap<String, String>>> getManifest() {
        return manifest;
    }

    public void setManifest(HashMap<String, HashMap<String, HashMap<String, String>>> manifest) {
        this.manifest = manifest;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
    /**
     * Sets the absolute path name of the log4j.properties file for this service
     * Called from service Invoker classes and test initTest classes
     */
    public void setLog4j () throws ConfigException {
        String path = this.getFilePath ("log4j.properties");
        System.setProperty("log4j.configuration", "file:" + path);
        System.out.println("setLog4j setting log4j.configuration to file:" + path);
    }

}
