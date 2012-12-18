package net.es.oscars.wsnbroker.policy;

import java.util.ArrayList;
import java.util.List;

import org.oasis_open.docs.wsn.b_2.MessageType;

/**
 * Interface for developing classes that can perform context specific 
 * authorization checks on notifications
 * 
 */
public interface NotifyPEP {
    
    /**
     * Returns true if one or more of the given topics applies to this policy module
     * 
     * @param topics the list of topics in the Subscribe message to check
     * @return true if this PEP applies to one or more of the given topics
     */
    public boolean topicMatches(List<String> topics);
    
    /**
     * Returns the name of the resource that the subscriber must have permissions
     * must have access to in order to see notifications.
     * 
     * @return the name of the resource the notify represents
     */
    public String getResourceName();
    
    /**
     * Returns the name of the permission that the subscriber
     * must be able to perform on the resource in order to 
     * receive notifications.
     * 
     * @return the name of the resource the notify represents
     */
    public String getPermissionName();
    
    /**
     * Enforces policy on matching notifications. Returns true 
     * if allowed and false if denied.
     * 
     * @param notifyMsg the message to authorize
     * @param authZLogins the logins the user is allowed to see. if empty then all allowed.
     * @param authZDomains the domains the user is authorized to see. if empty then all allowed.
     * @return true if allowed, false otherwise
     */
    public boolean enforcePolicy(MessageType notifyMsg, ArrayList<String> authZLogins, ArrayList<String> authZDomains);
    
}
