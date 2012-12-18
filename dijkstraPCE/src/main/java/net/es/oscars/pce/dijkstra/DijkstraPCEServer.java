package net.es.oscars.pce.dijkstra;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.SimplePCEServer;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

/**
 * Standalone server that reads in configuration file and publishes a 
 * Dijkstra PCE service.
 * 
 * @author Andy Lake <andy@es.net>
 */
@OSCARSService (
        serviceName = ServiceNames.SVC_PCE_DIJ,
        config = ConfigDefaults.CONFIG,
        implementor = "net.es.oscars.pce.dijkstra.DijkstraPCEProtocolHandler"
)
@OSCARSNetLoggerize(moduleName = "net.es.oscars.pce.Dijkstra", serviceName = ServiceNames.SVC_PCE_DIJ)
public class DijkstraPCEServer extends SimplePCEServer{
    static private ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PCE_DIJ);
    
    final private String PROP_LINKEVAL = "link-evaluators";
    final private String PROP_LINKEVAL_CLASS = "class";
    
    final private Logger log = Logger.getLogger(this.getClass());
    private DijkstraPCE pce;
    private String moduleName;
    
    public DijkstraPCEServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_PCE_DIJ);
        //get the name of the module from netLogger.Use annotation to be 
        //consistent with protocol handler even though variable would probably do
        this.moduleName = 
            this.getClass().getAnnotation(OSCARSNetLoggerize.class).moduleName();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.info(netLogger.start("init"));
        try{
            //get config file
            String configAlias=
                this.getClass().getAnnotation(OSCARSService.class).config();
            String configFilename = cc.getFilePath(configAlias);
            Map config = ConfigHelper.getConfiguration(configFilename);
            
            //initialize pce
            this.pce = new DijkstraPCE();
            List<Map> evaluators = (List<Map>) config.get(PROP_LINKEVAL);
            ArrayList<String> linkEvalClasses = new ArrayList<String>();
            if(evaluators != null){
                ClassLoader classLoader = this.getClass().getClassLoader();
                for(Map evaluator : evaluators){
                    if(evaluator.containsKey(PROP_LINKEVAL_CLASS) && evaluator.get(PROP_LINKEVAL_CLASS) != null){
                        Class linkEvalClass = classLoader.loadClass((String)evaluator.get(PROP_LINKEVAL_CLASS));
                        this.pce.getLinkEvaluators().add((LinkEvaluator)linkEvalClass.newInstance());
                        linkEvalClasses.add((String)evaluator.get(PROP_LINKEVAL_CLASS));
                    }
                }
            }
            HashMap<String,String> netLoggerProps = new HashMap<String,String>();
            netLoggerProps.put("configFile", configFilename);
            netLoggerProps.put("context", cc.getContext());
            netLoggerProps.put("linkEvals", OSCARSNetLogger.serializeList(linkEvalClasses));
            this.log.info(netLogger.end("init", null, null, netLoggerProps));
        }catch(Exception e){
            this.log.error(netLogger.error("init", ErrSev.CRITICAL, e.getMessage()));
            System.exit(1);
        }
    }
    
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException{
 
        PCEDataContent pceData = null;
        OSCARSNetLogger netLogger = new OSCARSNetLogger(this.moduleName,query.getTransactionId());
        netLogger.setGRI(query.getGri());
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.debug(netLogger.start("calculatePath"));
        try{
            pceData = this.pce.calculatePath(query);
            this.log.debug(netLogger.end("calculatePath"));
        }catch(OSCARSServiceException e){
            this.log.debug(netLogger.error("calculatePath caught OSCARSServiceException", ErrSev.MAJOR, e.getMessage(), " exception is " + e.toString()));
            throw e;
        }catch(Exception e){
            this.log.debug(netLogger.error("calculatePath caught exception", ErrSev.MAJOR, e.toString()));
            e.printStackTrace();
            throw new OSCARSServiceException(e);
        }

        return pceData;
    }
    
    public PCEDataContent commitPath(PCEMessage query) throws OSCARSServiceException{
        PCEDataContent pceData = null;
        OSCARSNetLogger netLogger = new OSCARSNetLogger(this.moduleName,query.getTransactionId());
        netLogger.setGRI(query.getGri());
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.debug(netLogger.start("commitPath"));
        try{
            pceData = this.pce.commitPath(query);
            this.log.debug(netLogger.end("commitPath"));
        }catch(OSCARSServiceException e){
            this.log.debug(netLogger.error("commitPath", ErrSev.MAJOR, e.getMessage()));
            throw e;
        }catch(Exception e){
            this.log.debug(netLogger.error("commitPath", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException(e);
        }
        
        return pceData;
    }
}
