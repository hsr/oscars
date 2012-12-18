package net.es.oscars.notificationBridge;

import java.util.Map;
import java.util.Observable;

import net.es.oscars.api.soap.gen.v06.EventContent;

public class NotificationBridgeObservable extends Observable{
    private Map config;
    
    public void setConfig(Map config){
        this.config = config;
    }
    
    public Map getConfig(){
        return config;
    }
    
    synchronized public void fireEvent(EventContent event){
        //set changed is protected, hence the special method
        this.setChanged();
        this.notifyObservers(event);
    }
}
