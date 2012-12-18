package net.es.oscars.notificationBridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;

public class NotificationBridgeGlobals {
    private Logger log = Logger.getLogger(NotificationBridgeGlobals.class);
    static private NotificationBridgeGlobals instance = null;
    static private ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_NOTIFY);
    
    private NotificationBridgeObservable observable;
    
    final private String PROP_OBSERVERS = "observers";
    final private String PROP_OBSERVER_CLASS = "class";
    
    static public NotificationBridgeGlobals getInstance() throws OSCARSServiceException {
        if(instance == null){
           instance = new NotificationBridgeGlobals();
        }
        return instance;
    }
    
    public NotificationBridgeGlobals() throws OSCARSServiceException {
        //load config file
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        String configFile = null;
        Map config = null;
        this.log.debug(netLog.start("loadConfig"));
        try {
            configFile = cc.getFilePath(ConfigDefaults.CONFIG);
            config = ConfigHelper.getConfiguration(configFile);
        } catch (ConfigException e) {
            this.log.debug(netLog.error("loadConfig", ErrSev.FATAL, e.getMessage()));
            throw new OSCARSServiceException(e.getMessage());
        }
        
        //init Observable object
        this.observable = new NotificationBridgeObservable();
        this.observable.setConfig(config);
        try{
            this.log.debug(netLog.start("initObservers"));
            HashMap<String,String> netLogProps = new HashMap<String,String>();
            List<Map> observers = (List<Map>) config.get(PROP_OBSERVERS);
            ArrayList<String> obsClasses = new ArrayList<String>();
            if(observers != null){
                ClassLoader classLoader = this.getClass().getClassLoader();
                for(Map pep : observers){
                    if(pep.containsKey(PROP_OBSERVER_CLASS) && pep.get(PROP_OBSERVER_CLASS) != null){
                        Class obsClass = classLoader.loadClass((String)pep.get(PROP_OBSERVER_CLASS));
                        this.observable.addObserver((Observer)obsClass.newInstance());
                        obsClasses.add(pep.get(PROP_OBSERVER_CLASS)+"");
                    }
                }
            }
            netLogProps.put("classes", OSCARSNetLogger.serializeList(obsClasses));
            this.log.debug(netLog.end("initObservers", null, null, netLogProps));
        }catch(Exception e){
            this.log.debug(netLog.error("initObservers", ErrSev.FATAL, e.getMessage()));
            throw new OSCARSServiceException("Error initializing notification bridge: " +
                        "Failed to initialize observers");
        }
    }
    
    public NotificationBridgeObservable getObservable(){
        return this.observable;
    }
}
