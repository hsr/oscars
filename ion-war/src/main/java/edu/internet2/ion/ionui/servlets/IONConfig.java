package edu.internet2.ion.ionui.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;

import org.ho.yaml.Yaml;

//import net.es.oscars.ConfigFinder;

public class IONConfig {
    private Map configuration = null;
    private Logger log = Logger.getLogger(IONConfig.class);

    //IONConfig reads yaml file as an Object type. ConfigHelper.java reads it into a "Map" type.
    //In future, we may unify these into one file, or modify this to use ConfigHelper.getConfig itself
    public IONConfig(String filePath) {
        try {
		//ConfigFinder cf = ConfigFinder.getInstance();
		//String configFile = cf.find(ConfigFinder.PROPERTIES_DIR, filename);

		//this.log.warn("Loading config "+configFile);
		this.log.warn("Loading config "+filePath);

		InputStream yamlFile = new FileInputStream(new File(filePath));

		this.log.warn("Passed yamlFile InputStream creation");

		Object retval = Yaml.load(yamlFile);

		if (retval == null) {
			this.log.warn("YAML.load returned a null value");
		} else {
			configuration = (Map) retval;
		}

		this.log.warn("Passed Yaml.load");
        } catch (Exception ex) {
		this.log.error("Failed to load config: "+ex);
        }
    }

    public String getString(String path, boolean recursive) {
	Object obj = this.getObject(path, recursive);
	if (obj == null) {
		return null;
	}

	if ((obj instanceof String) == false) {
		return null;
	}

	return (String) obj;
    }

    public Map getGroup(String path, boolean recursive) {
	if ("".equals(path)) {
		return this.configuration;
	}

	Object obj = this.getObject(path, recursive);
	if (obj == null) {
		return null;
	}

	if ((obj instanceof Map) == false) {
		return null;
	}

	return (Map) obj;
    }

    public Object getObject(String path, boolean recursive) {
	String [] brokenOutPath = path.split("/");
	Map current = configuration;

	this.log.warn("Path == "+path);
	if (configuration == null)  {
		this.log.warn("Configuration == null");
		return null;
	}

	Object lastFound = null;

	for(int i = 0; i < brokenOutPath.length; i++) {
		if (current.get(brokenOutPath[brokenOutPath.length - 1]) != null) {
			this.log.warn("Found what we're looking for in current location");
			lastFound = current.get(brokenOutPath[brokenOutPath.length - 1]);
		}

		Object result = current.get(brokenOutPath[i]);
		if (result == null) {
			if (lastFound != null && recursive) {
				this.log.warn("Didn't find the element "+brokenOutPath[i]+" but the property was defined earlier");
				return lastFound;
			}

			this.log.warn("Didn't find the element "+brokenOutPath[i]);
			return null;
		}

		if (i == brokenOutPath.length - 1) {
			if (result != null || !recursive) {
				this.log.warn("Returning current since it's either not-null or its not a recursive variable");
				return result;
			} else {
				this.log.warn("Current is null, returning lastFound");
				return lastFound;
			}
		}

		if ((result instanceof Map) == false) {
			this.log.warn("Found non-Map group: "+brokenOutPath[i]);
			return null;
		}

		current = (Map) result;
	}

	return null;
    }
}

