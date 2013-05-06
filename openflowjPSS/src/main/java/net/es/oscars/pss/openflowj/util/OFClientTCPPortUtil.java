package net.es.oscars.pss.openflowj.util;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.openflowj.io.OpenFlowClient;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

/**
 * Utility class for loading configuration information related to ports.
 *
 */
public class OFClientTCPPortUtil {

    static private HashMap<String,Integer> mgmtPortMap;

    private static final String CONFIG_FILE_PORTS = "config-openflow-mgmtports.yaml";

    static public int lookupMgmtPort(String deviceAddr){
        synchronized(OFClientTCPPortUtil.class) {
            if(mgmtPortMap == null){
                OFClientTCPPortUtil.loadConfig();
            }
        }

        if(mgmtPortMap.containsKey(deviceAddr) && mgmtPortMap.get(deviceAddr) != null){
            return mgmtPortMap.get(deviceAddr);
        }else if(mgmtPortMap.containsKey("default") && mgmtPortMap.get("default") != null){
            return mgmtPortMap.get("default");
        }

        return  OpenFlowClient.DEFAULT_PORT;
    }

    static private void loadConfig(){
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        Map config = null;
        try {
            String configFile = cc.getFilePath(CONFIG_FILE_PORTS);
            config = ConfigHelper.getConfiguration(configFile);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        mgmtPortMap = new HashMap<String,Integer>();
        for(Object nodeAddrObj : config.keySet()){
            mgmtPortMap.put(nodeAddrObj+"", Integer.parseInt(config.get(nodeAddrObj)+""));
        }
    }


}
