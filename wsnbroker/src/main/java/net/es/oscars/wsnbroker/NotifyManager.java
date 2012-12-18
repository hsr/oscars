package net.es.oscars.wsnbroker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.wsnbroker.jobs.SendNotifyJob;
import net.es.oscars.wsnbroker.policy.NotifyPEP;
import net.es.oscars.wsnbroker.utils.WSAddrParser;
import net.es.oscars.utils.notify.FilterNamespaceContext;
import net.es.oscars.utils.soap.OSCARSServiceException;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.MessageType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;

public class NotifyManager {
    Logger log = Logger.getLogger(NotifyManager.class);
    
    public void processNotify(Connection conn, NotificationMessageHolderType notification) throws OSCARSServiceException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("processNotify"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
       
        //Parse topics and start building SQL query
        if(notification.getTopic() == null){
            this.log.debug(netLog.end("processNotify", "No topic in message so no processing required"));
            return;
        }
        String[] topics = notification.getTopic().getValue().split("\\|");
        NotifyPEP matchingPep = null;
        for(NotifyPEP pep : NotificationGlobals.getInstance().getPEPList()){
            if(pep.topicMatches(Arrays.asList(topics))){
                matchingPep = pep;
                break;
            }
        }
        String topicQuery = "SELECT DISTINCT s.id, s.referenceId, s.userLogin, s.url" +
            " FROM subscriptions s INNER JOIN subscriptionFilters sf ON " +
            "s.id = sf.subscriptionId WHERE s.status=? AND s.terminationTime > ? " +
            "AND sf.type=? AND (";
        boolean firstTopic = true;
        for(int i = 0; i < topics.length; i++){
            if(firstTopic){
                firstTopic = false;
            }else{
                topicQuery += " OR ";
            }
            topicQuery += "(LENGTH(sf.value) <= ? AND SUBSTR(?, 1, LENGTH(sf.value)) = sf.value)";
        }
        topicQuery += ")";
        try{
            //Query Based on Topic
            PreparedStatement topicStmt = conn.prepareStatement(topicQuery);
            int topicStmtIndex = 0;
            topicStmt.setInt(++topicStmtIndex, SubscriptionStatus.ACTIVE_STATUS);
            topicStmt.setLong(++topicStmtIndex, System.currentTimeMillis());
            topicStmt.setString(++topicStmtIndex, FilterTypes.FILTER_TOPIC);
            for(String topic : topics){
                topicStmt.setInt(++topicStmtIndex, topic.length());
                topicStmt.setString(++topicStmtIndex, topic);
            }
            ResultSet matchingSubscriptions = topicStmt.executeQuery();
            
            //Check message and producer properties
            ArrayList<String> authZDomains = new ArrayList<String>();
            ArrayList<String> authZLogins = new ArrayList<String>();
            int topicMatchCount = 0;//subscriptions that match SQL query
            int finalMatchcount = 0;//subscriptions that match after post-processing
            PreparedStatement filterQuery = conn.prepareStatement("SELECT type, value FROM subscriptionFilters WHERE subscriptionId=?");
            while(matchingSubscriptions.next()){
                boolean matchesFilters = true;
                topicMatchCount++;
                //get all the filters
                filterQuery.setInt(1, matchingSubscriptions.getInt(1));
                ResultSet filters = filterQuery.executeQuery();
                while(filters.next()){
                    if(FilterTypes.FILTER_MSGXPATH.equals(filters.getString(1))){
                        if(!this.processMsgXpath(filters.getString(2), notification.getMessage())){
                            matchesFilters = false;
                            break;
                        }
                    }else if(FilterTypes.FILTER_PRODXPATH.equals(filters.getString(1))){
                        if(!this.processProducer(filters.getString(2), notification.getProducerReference())){
                            matchesFilters = false;
                            break;
                        }
                    }else if(FilterTypes.FILTER_AUTHZDOMAIN.equals(filters.getString(1))){
                        authZDomains.add(filters.getString(2));
                    }else if(FilterTypes.FILTER_AUTHZLOGIN.equals(filters.getString(1))){
                        authZLogins.add(filters.getString(2));
                    }
                }
                //enforce event specific policy if its been allowed so far
                if(matchesFilters && matchingPep != null){
                    matchesFilters = matchingPep.enforcePolicy(notification.getMessage(),
                            authZLogins, authZDomains);
                }
                    
                if(matchesFilters){
                    this.schedSendNotify(matchingSubscriptions.getString(2), 
                        matchingSubscriptions.getString(4), 
                        notification);
                    finalMatchcount++;
                }
            }
            netLogProps.put("topicMatchCount", topicMatchCount+"");
            netLogProps.put("finalMatchCount", finalMatchcount+"");
        }catch(Exception e){
            this.log.debug(netLog.error("processNotify", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        
        
        this.log.debug(netLog.end("processNotify", null, null, netLogProps));
    }
    
    private W3CEndpointReference producerPropsToURL(String xpathStr) {
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("producerPropsToURL"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        netLogProps.put("xpath", xpathStr);
        xpathStr = xpathStr.trim();
        String url = xpathStr.replaceFirst("^/*(.+:)?Address='", "");
        url = url.replaceAll("'$", "");
        netLogProps.put("result", url);
        this.log.debug(netLog.end("producerPropsToURL", null, null, netLogProps));
        return WSAddrParser.createAddress(url);
    }

    private boolean processMsgXpath(String xpathStr, MessageType message) {
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("processMsgXpath"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        netLogProps.put("xpath", xpathStr);
        try{
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new FilterNamespaceContext(
                    NotificationGlobals.getInstance().getFilterNamespaceMap()));
            XPathExpression xpathExpr = xpath.compile(xpathStr);
            for(Object elem : message.getAny()){
                Boolean xpathResult = (Boolean) xpathExpr.evaluate(elem, XPathConstants.BOOLEAN);
                netLogProps.put("xpathResult", xpathResult+"");
                if(!xpathResult){
                    this.log.debug(netLog.end("processMsgXpath", null, null, netLogProps));
                    return false;
                }
            }
        }catch(Exception e){
           e.printStackTrace();
           this.log.debug(netLog.error("processMsgXpath", null, null, null, netLogProps));
           return false;
        }
        this.log.debug(netLog.end("processMsgXpath", null, null, netLogProps));
        return true;
    }
    
    private boolean processProducer(String subProducer, W3CEndpointReference notifyProducerEpr){
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("processProducer"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        W3CEndpointReference subEpr = this.producerPropsToURL(subProducer);
        String subProducerAddr = WSAddrParser.getAddress(subEpr);
        String notifyProducerAddr = WSAddrParser.getAddress(notifyProducerEpr);
        netLogProps.put("subProducer", subProducerAddr);
        netLogProps.put("notifyProducer", notifyProducerAddr);
        if(!notifyProducerAddr.equals(subProducerAddr)){
            netLogProps.put("result", "false");
            this.log.debug(netLog.end("processProducer", null, null, netLogProps));
            return false;
        }
        netLogProps.put("result", "true");
        this.log.debug(netLog.end("processProducer", null, null, netLogProps));
        return true;
    }
    
    private void schedSendNotify(String refId, String url, NotificationMessageHolderType notification) throws OSCARSServiceException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.info(netLog.start("schedSendNotify"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        try{
            netLogProps.put("subscription", refId);
            Scheduler sched = NotificationGlobals.getInstance().getScheduler();
            String jobKey =  UUID.randomUUID().toString();
            String triggerName = "sendNotifyTrig-" + jobKey;
            String jobName = "sendNotify-" + jobKey;
            SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                      new Date(), null, 0, 0L);
            JobDetail jobDetail = new JobDetail(jobName, "SEND_NOTIFY",
                                                SendNotifyJob.class);
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("url", url);
            dataMap.put("topics", notification.getTopic());
            dataMap.put("producer", notification.getProducerReference());
            dataMap.put("message", notification.getMessage());
            dataMap.put("subscription", WSAddrParser.createAddress(refId));
            jobDetail.setJobDataMap(dataMap);
            sched.scheduleJob(jobDetail, trigger);
        }catch(Exception e){
            this.log.error(netLog.error("schedSendNotify", ErrSev.MAJOR, 
                    e.getMessage(), url, netLogProps));
            return;
        }
        
        this.log.info(netLog.end("schedSendNotify",null, url, netLogProps));
    }
}
