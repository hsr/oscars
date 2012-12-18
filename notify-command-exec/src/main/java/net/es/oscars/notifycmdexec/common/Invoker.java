package net.es.oscars.notifycmdexec.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.ho.yaml.Yaml;
import org.mortbay.jetty.Server;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.notifycmdexec.NotifyExecHandler;
import net.es.oscars.notifycmdexec.jobs.SubscribeJob05;

public class Invoker {
    private static final String DEFAULT_RENEW_SCHED = "0 * * * * ?"; //every minute
    
    public static void main(String[] args){
        
        //Read command line options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("c", "config"), "configuration file directory").withRequiredArg().ofType(String.class);
            }
        };
        
        OptionSet opts = parser.parse(args);
        if(opts.has("h")){
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e){}
            System.exit(0);
        }
        
        String configFile = "./config/config.yaml";
        if(opts.has("c")){
            configFile = (String) opts.valueOf("c");
        }
        System.out.println("Using config file " + configFile);
        
        //Read config file
        NotifyExecHandler handler = new NotifyExecHandler();
        int port = 8080;
        String consumerUrl = null;
        String clientKey = null;
        String clientKeyPassword = null;
        String clientKeystore = null;
        String clientKeystorePassword = null;
        String sslKeystore = null;
        String sslKeystorePassword = null;
        
        Map config = null;
        try {
            config = (Map) Yaml.load(new File(configFile));
        } catch (FileNotFoundException e) {
            System.err.println("Unable to open config file: " + e.getMessage());
            System.exit(1);
        }
        if(config.containsKey("port") && config.get("port") != null){
            port = (Integer) config.get("port");
        }
        if(config.containsKey("setupCommand") && config.get("setupCommand") != null){
            handler.setSetupCommand((String) config.get("setupCommand"));
        }
        if(config.containsKey("teardownCommand") && config.get("teardownCommand") != null){
            handler.setTeardownCommand((String) config.get("teardownCommand"));
        }
        if(config.containsKey("host") && config.get("host") != null){
            consumerUrl = "http://" + (String) config.get("host") + ":" + port;
        }else{
            System.err.println("No host property provided");
            System.exit(1);
        }
        if(config.containsKey("clientKey") && config.get("clientKey") != null){
            clientKey = (String) config.get("clientKey");
        }else{
            System.err.println("No clientKey property provided");
            System.exit(1);
        }
        if(config.containsKey("clientKeystore") && config.get("clientKeystore") != null){
            clientKeystore = (String) config.get("clientKeystore");
        }else{
            System.err.println("No clientKeystore property provided");
            System.exit(1);
        }
        if(config.containsKey("clientKeystorePassword") && config.get("clientKeystorePassword") != null){
            clientKeystorePassword = (String) config.get("clientKeystorePassword");
        }
        if(config.containsKey("sslKeystore") && config.get("sslKeystore") != null){
            sslKeystore = (String) config.get("sslKeystore");
        }
        if(config.containsKey("sslKeystorePassword") && config.get("sslKeystorePassword") != null){
            sslKeystorePassword = (String) config.get("sslKeystorePassword");
        }
        if(config.containsKey("clientKeyPassword") && config.get("clientKeyPassword") != null){
            clientKeyPassword = (String) config.get("clientKeyPassword");
        }else{
            clientKeyPassword = clientKeystorePassword;
        }
        
        //get NB URL
        if(!config.containsKey("notificationBroker") || config.get("notificationBroker") == null){
            System.err.println("notificationBroker property not set");
            System.exit(1);
        }
        if(!config.containsKey("idc") || config.get("idc") == null){
            System.err.println("idc property not set");
            System.exit(1);
        }
        
      //schedule subscription
        try {
            OSCARSClientConfig.setClientKeystore(clientKey, clientKeystore, clientKeystorePassword, clientKeyPassword);
            if(sslKeystore != null){
                OSCARSClientConfig.setSSLKeyStore(sslKeystore, sslKeystorePassword);
            }
            SubscribeJob05.init((String)config.get("notificationBroker"), (String)config.get("idc"), consumerUrl);
            SchedulerFactory schedFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedFactory.getScheduler();
            scheduler.start();
            CronTrigger cronTrigger = new CronTrigger("Renew05Trigger", "RENEW05", DEFAULT_RENEW_SCHED);
            JobDetail jobDetail = new JobDetail("Renew05Job", "RENEW05", SubscribeJob05.class);
            scheduler.scheduleJob(jobDetail, cronTrigger);
        } catch (Exception e) {
            System.err.println("Error scheduling subscription: " + e.getMessage());
            System.exit(1);
        }
        
        //start server
        Server server = new Server(port);
        server.setHandler(handler);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
