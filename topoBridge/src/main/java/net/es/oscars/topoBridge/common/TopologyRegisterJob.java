package net.es.oscars.topoBridge.common;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.soap.OSCARSServiceException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.perfsonar.PSTopologyClient;

public class TopologyRegisterJob implements Job{
    private Logger log = Logger.getLogger(TopologyRegisterJob.class);
    static private String topoHash = "";
    
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        
        try {
            this.log.debug(netLog.start("TopologyRegisterJob.execute"));
            TopoBridgeCore topoBridgeCore = TopoBridgeCore.getInstance();
            Document domain = topoBridgeCore.getLocalTopology();
            if(domain == null){
                return;
            }
            XMLOutputter outputter = new XMLOutputter();
            Format topoFormat = Format.getRawFormat();
            topoFormat.setOmitEncoding(true);
            topoFormat.setOmitDeclaration(true);
            outputter.setFormat(topoFormat);
            String domainString = outputter.outputString(domain);
            String newTopoHash = DigestUtils.md5Hex(domainString);
            //if topology has not changed then do not re-register
            if(newTopoHash.equals(topoHash)){
                this.log.debug(netLog.end("TopologyRegisterJob.execute"));
                return;
            }
            this.registerTopology(topoBridgeCore.getRegisterURL(), domainString);
            topoHash = newTopoHash;
            this.log.debug(netLog.end("TopologyRegisterJob.execute"));
        } catch (OSCARSServiceException e) {
            this.log.error(netLog.error("TopologyRegisterJob.execute", ErrSev.MAJOR, e.getMessage()));
        }
        
    }
    
    public void registerTopology(String url, String domainString){
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.info(netLog.start("registerTopology", null, url));
        PSTopologyClient psClient = new PSTopologyClient(url);
        psClient.addReplaceDomain(domainString);
        this.log.info(netLog.end("registerTopology", null, url));
    }

}
