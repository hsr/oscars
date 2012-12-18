package net.es.oscars.resourceManager.beans;

public class PathElemParamSwcap {
    public static final String L2SC = "l2sc";
    public static final String MPLS = "psc-1";
    public static final String TDM = "tdm";
    
    static public boolean isValid(String swcap){
        if(L2SC.equals(swcap)){
            return true;
        }else if(MPLS.equals(swcap)){
            return true;
        }else if(TDM.equals(swcap)){
            return true;
        }
        return false;
    }
}
