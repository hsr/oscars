package net.es.oscars.utils.validator.wrappers.net.es.oscars.api.soap.gen.v06;


import net.es.oscars.api.soap.gen.v06.Layer3Info;

public class Layer3InfoValidator {
    /**
     * Validate the content of a Layer3Info object. Note that object will never be null.
     * @param obj to validate
     * @throws RuntimeException
     */
    public static void validator (Layer3Info obj) throws RuntimeException {
        
        obj.setSrcHost(normalizeIPList(obj.getSrcHost()));
        obj.setDestHost(normalizeIPList(obj.getDestHost()));
       
        
    }

    private static String normalizeIPList(String hostList) {
        if(hostList == null){
            return hostList;
        }
        
        //normalize
        hostList = hostList.trim();
        hostList = hostList.replaceAll(",", " ");
        hostList = hostList.replaceAll("\\s+", ",");
        
        return hostList;
    }
}
