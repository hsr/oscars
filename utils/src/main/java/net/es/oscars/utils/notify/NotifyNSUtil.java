package net.es.oscars.utils.notify;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class NotifyNSUtil {
    final static private String PROP_FILTER_NAMESPACES = "notifyNamespaces";
    
    public static Map<String,String> getNamespaceMap(){
        Map<String,String> filterNamespaceMap = new HashMap<String,String>();
        ContextConfig cc = ContextConfig.getInstance();
        String configFilename = null;
        try {
            configFilename = cc.getFilePath(ServiceNames.SVC_UTILS,cc.getContext(),
                    ConfigDefaults.CONFIG);
        } catch (ConfigException e) {
            return filterNamespaceMap;
        }
        HashMap<String,Object> utilConfig = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
        if(utilConfig.containsKey(PROP_FILTER_NAMESPACES) && utilConfig.get(PROP_FILTER_NAMESPACES) != null){
            filterNamespaceMap =(Map<String,String>) utilConfig.get(PROP_FILTER_NAMESPACES);
        }
        
        return filterNamespaceMap;
    }
    
    public static Map<String,String> getPrefixMap(){
        Map<String,String> filterNamespaceMap = NotifyNSUtil.getNamespaceMap();
        Map<String,String> filterPrefixMap = new HashMap<String,String>();
        for(String prefix : filterNamespaceMap.keySet()){
            filterPrefixMap.put(filterNamespaceMap.get(prefix), prefix);
        }
        
        return filterPrefixMap;
    }
}
