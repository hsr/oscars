package net.es.oscars.pss.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

public class IfceAliasConfig {
    private static Logger log = Logger.getLogger(IfceAliasConfig.class);
    private static HashMap<String, HashMap<String, String>> ifceAliases = new HashMap<String, HashMap<String, String>>();

    @SuppressWarnings("unchecked")
    public static void configure() throws PSSException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        Map<String, Map<String, String>> ifceAliasConfig;
        try {
            cc.loadManifest(ServiceNames.SVC_PSS,  ConfigDefaults.MANIFEST); // manifest.yaml
            String configFilePath = cc.getFilePath("config-ifce-aliases.yaml");
            log.debug("loading ifce aliases from "+configFilePath);
            InputStream propFile = new FileInputStream(new File(configFilePath));
            ifceAliasConfig = (Map<String, Map<String, String>>) Yaml.load(propFile);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new PSSException(e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new PSSException(e);
        }
        
        for (String device : ifceAliasConfig.keySet()) {
            HashMap<String, String> deviceIfceAliases = new HashMap<String, String>();
            for (String alias : ifceAliasConfig.get(device).keySet()) {
                deviceIfceAliases.put(alias, ifceAliasConfig.get(device).get(alias));
            }
            ifceAliases.put(device, deviceIfceAliases);

        }

        String out ="";
        for (String device : ifceAliases.keySet()) {
            out  += "device: [" +device+ "] \n";
            HashMap<String, String> deviceIfceAliases = ifceAliases.get(device);
            for (String key : deviceIfceAliases.keySet()) {
                String ifce = deviceIfceAliases.get(key);
                out += "    ["+key+"] => ["+ifce+"]\n";
            }
        }
        log.debug("configured these ifce aliases: \n"+out);
    }
    public static String getIfce(String deviceId, String alias) {
        
        log.debug("looking for alias ["+alias+"] on device ["+deviceId+"]");
        HashMap<String, String> deviceIfceAliases = ifceAliases.get(deviceId);
        if (deviceIfceAliases == null) {
            log.debug("no aliases defined for "+deviceId+" trying global");
            deviceIfceAliases = ifceAliases.get("global");
            if (deviceIfceAliases == null) {
                log.debug("no global aliases defined");
                return alias;
            }
        }
        
        if (deviceIfceAliases.containsKey(alias)) {
            log.debug("alias "+alias+" for "+deviceIfceAliases.get(alias)+" found at device "+deviceId);
            return deviceIfceAliases.get(alias);
        } else {
            return alias;
        }
        
    }
}
