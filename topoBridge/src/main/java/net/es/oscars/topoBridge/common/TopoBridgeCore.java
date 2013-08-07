package net.es.oscars.topoBridge.common;

import java.util.Map;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.topoBridge.sdn.BaseSDNTopologyService;
import net.es.oscars.topoBridge.sdn.ISDNTopologyService;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;


public class TopoBridgeCore {
    private static Logger log = Logger.getLogger(TopoBridgeCore.class);
    private static TopoBridgeCore instance = null;
    private static String localDomainId = null;
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_TOPO);
    private String registerURL = null;
    private Scheduler scheduler;
    /**
     * Constructor - private because this is a Singleton
     */
    private TopoBridgeCore() throws ConfigException {
        String configFile = cc.getFilePath(ConfigDefaults.CONFIG);
        Map config = ConfigHelper.getConfiguration(configFile);
        assert config != null : "No configuration";
        Map topoBridgeCfg = (Map) config.get("topoBridge");
        assert topoBridgeCfg != null : "No topoBridge stanza in configuration";

        localDomainId = PathTools.getLocalDomainId();
        assert localDomainId != null : "No localDomainId in configuration";
        
        //determine if we need to register any topology
        if(topoBridgeCfg.containsKey("registerUrl") && topoBridgeCfg.get("registerUrl") != null){
            String DEFAULT_REG_SCHED = "0 * * * * ?"; //every 1 minute
            this.registerURL = (String) topoBridgeCfg.get("registerUrl");
            this.log.debug("register URL=" + this.registerURL);
            SchedulerFactory schedFactory = new StdSchedulerFactory();
            
            try {
                scheduler = schedFactory.getScheduler();
                scheduler.start();
                CronTrigger cronTrigger = new CronTrigger("TopoRegTrigger", "TOPOREG", DEFAULT_REG_SCHED);
                JobDetail jobDetail = new JobDetail("TopoRegJob", "TOPOREG", TopologyRegisterJob.class);
                scheduler.scheduleJob(jobDetail, cronTrigger);
            } catch (Exception e) {
                throw new ConfigException("Error scheduling registration: " + e.getMessage());
            }
            
        }
    }

    /**
     * @return the TopoBridgeCore singleton instance
     */
    public static TopoBridgeCore getInstance() throws OSCARSServiceException {
        try {
            if (TopoBridgeCore.instance == null) {
                TopoBridgeCore.instance = new TopoBridgeCore();
            }
        } catch (ConfigException e) {
            throw new OSCARSServiceException(e.getMessage());
        }
        return TopoBridgeCore.instance;
    }

    public Document getLocalTopology() throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        log.debug(netLogger.start("getLocalTopology"));

		if (localDomainId.matches("^sdn\\:.*")) {
			return getSDNTopology();
		}
        
        try {
            TopologyCache tc = TopologyCache.getInstance();

            Document domain = tc.getDomain(localDomainId, netLogger);
            // XMLOutputter outputter = new XMLOutputter();
            // PSTopoConverter.convertTopology(domain, tc.getNsUri());
            // outputter.output(domain, System.out);

            // domain = tc.getDomain("testdomain-1");
            // PSTopoConverter.convertTopology(domain, tc.getNsUri());
            // outputter.output(domain, System.out);
            log.debug(netLogger.end("getLocalTopology"));
            return domain;
        } catch (OSCARSServiceException e) {
            log.debug(netLogger.error("getLocalTopology", ErrSev.MAJOR, e.getMessage()));
            throw e;
        } catch (Exception e) {
            log.debug(netLogger.error("getLocalTopology", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
    }
    
	/**
	 * This method tries to get the network topology from an SDN controller
	 * specified by in the localDomainId string
	 * 
	 * @return Document domain
	 * @throws OSCARSServiceException 
	 */
	private Document getSDNTopology() throws OSCARSServiceException {
		OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
		log.debug(netLogger.start("getSDNTopology"));
		
		try {
			ISDNTopologyService ts = BaseSDNTopologyService.getInstance(localDomainId);

			log.debug(netLogger.end("getSDNTopology"));
			return ts.getTopology();
		} catch (Exception e) {
			log.debug(netLogger.error("getSDNTopology", ErrSev.MAJOR,
					e.getMessage()));
			e.printStackTrace();
			throw new OSCARSServiceException(e.getMessage());
		}
	}
    public void shutdown() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        log.info(netLogger.end("shutdown","not implemented"));
    }

    public String getRegisterURL() {
        return this.registerURL ;
    }

}
