package net.es.oscars.wsnbroker;

import net.es.oscars.utils.soap.OSCARSServiceException;

public class SubscriptionStatus {
    final public static int INACTIVE_STATUS = 0;
    final public static int ACTIVE_STATUS = 1;
    final public static int PAUSED_STATUS = 2;
    
    final public static String INACTIVE_STRING = "INACTIVE";
    final public static String ACTIVE_STRING = "ACTIVE";
    final public static String PAUSED_STRING = "PAUSED";
    final public static String ALL_STRING = "ALL";
    
    static public String statusToString(int statusInt) throws OSCARSServiceException{
        if(statusInt == INACTIVE_STATUS){
            return INACTIVE_STRING;
        }else if(statusInt == ACTIVE_STATUS){
            return ACTIVE_STRING;
        }else if(statusInt == PAUSED_STATUS){
            return PAUSED_STRING;
        }
        
        throw new OSCARSServiceException("Invalid status " + statusInt);
    }
}
