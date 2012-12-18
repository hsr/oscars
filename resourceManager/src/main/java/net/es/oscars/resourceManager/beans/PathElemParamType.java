package net.es.oscars.resourceManager.beans;

public class PathElemParamType {
    
    public static final String ENCODING_TYPE = "encodingType";
    public static final String L2SC_VLAN_RANGE = "vlanRangeAvailability";
    public static final String L2SC_SUGGESTED_VLAN = "suggestedVlan";
    public static final String MPLS_VLAN_RANGE = "vlanRangeAvailability";
    public static final String MPLS_SUGGESTED_VLAN = "suggestedVlan";
    
    static public boolean isValid(String type){
        if(L2SC_VLAN_RANGE.equals(type)){
            return true;
        }else if(L2SC_SUGGESTED_VLAN.equals(type)){
            return true;
        }else if(MPLS_VLAN_RANGE.equals(type)){
            return true;
        }else if(MPLS_SUGGESTED_VLAN.equals(type)){
            return true;
        }else if(ENCODING_TYPE.equals(type)){
            return true;
        }
        return false;
    }
}
