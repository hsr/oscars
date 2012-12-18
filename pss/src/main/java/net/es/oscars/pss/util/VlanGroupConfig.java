package net.es.oscars.pss.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

public class VlanGroupConfig {
    private static Logger log = Logger.getLogger(VlanGroupConfig.class);
    private static HashMap<String, HashMap<String, ArrayList<String>>> vlanGroupsByTopo = new HashMap<String, HashMap<String, ArrayList<String>>>();

    @SuppressWarnings("unchecked")
    public static void configure() throws PSSException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        Map<String, Map<String, String>> vlanGroupConfig;
        try {
            cc.loadManifest(ServiceNames.SVC_PSS,  ConfigDefaults.MANIFEST); // manifest.yaml
            String configFilePath = cc.getFilePath("config-vlan-groups.yaml");
            log.debug("loading vlan groups from "+configFilePath);

            InputStream propFile = new FileInputStream(new File(configFilePath));
            vlanGroupConfig = (Map<String, Map<String, String>>) Yaml.load(propFile);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new PSSException(e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new PSSException(e);
        }
        for (String topoId : vlanGroupConfig.keySet()) {
            HashMap<String, ArrayList<String>> vlanGroups = new HashMap<String, ArrayList<String>>();
            vlanGroupsByTopo.put(topoId, vlanGroups);
            
            for (String group : vlanGroupConfig.get(topoId).keySet()) {
                String groupConfig  = vlanGroupConfig.get(topoId).get(group);
                vlanGroups.put(group, new ArrayList<String>());

                String[] ranges = groupConfig.split("\\,");
                for (String range : ranges) {
                    range = range.trim();
                    if (range.contains("-")) {
                        String[] parts = range.split("-");
                        String start = parts[0];
                        String end = parts[1];
                        for (int i = Integer.valueOf(start); i <= Integer.valueOf(end); i++) {
                            vlanGroups.get(group).add(i+"");
                        }
                    } else {
                        String vlan = range.trim();
                        vlanGroups.get(group).add(vlan);
                    }
                }

            }
        }
        String out = "vlan groups config:\n";
        for (String topoId : vlanGroupsByTopo.keySet()) {
            HashMap<String, ArrayList<String>> vlanGroups =vlanGroupsByTopo.get(topoId);
            out += "  "+topoId+ ":\n";
            for (String group : vlanGroups.keySet()) {
                out += "    "+group+ ": [ ";
                for (String vlan : vlanGroups.get(group)) {
                    out += vlan+" ";
                }
                out += "]\n";
            }
        }
        log.debug(out);
    }
    public static ArrayList<String> getVlans(String deviceId, String portId, String vlan) {
        ArrayList<String> vlans = new ArrayList<String>();
        HashMap<String, ArrayList<String>> vlanGroups;
        if (vlanGroupsByTopo.containsKey(deviceId+":"+portId)) {
            vlanGroups = vlanGroupsByTopo.get(deviceId+":"+portId);
        } else if (vlanGroupsByTopo.containsKey(deviceId)) {
            vlanGroups = vlanGroupsByTopo.get(deviceId);
        } else if (vlanGroupsByTopo.containsKey("global")) {
            vlanGroups = vlanGroupsByTopo.get("global");
        } else {
            vlans.add(vlan);
            return vlans;
        }
        if (vlanGroups == null) {
            vlans.add(vlan);
            return vlans;
        }
        if (vlanGroups.containsKey(vlan)) {
            return vlanGroups.get(vlan);
        } else {
            vlans.add(vlan);
            return vlans;
            
        }
            
    }
    
}
