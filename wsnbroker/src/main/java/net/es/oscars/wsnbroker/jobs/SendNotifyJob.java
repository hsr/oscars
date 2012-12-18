package net.es.oscars.wsnbroker.jobs;

import java.util.UUID;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.WSNBrokerClient;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.wsnbroker.soap.gen.WSNBrokerPortType;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.MessageType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendNotifyJob implements Job{

    public void execute(JobExecutionContext context) throws JobExecutionException {
        Logger log = Logger.getLogger(this.getClass());
        OSCARSNetLogger netLogger = new OSCARSNetLogger(ModuleName.WSNBROKER);
        netLogger.setGUID(UUID.randomUUID()+"");
        log.info(netLogger.start("SendNotifyJob.execute"));
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_WSNBROKER);
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String url = dataMap.getString("url");
        try {
            WSNBrokerPortType client = WSNBrokerClient.getClient(url, 
                    cc.getWSDLPath(null)+"").getPortType();
            ClientProxy.getClient(client).getRequestContext().put(
                    "org.apache.cxf.message.Message.ENDPOINT_ADDRESS", url);
            Notify notifyRequest = new Notify();
            NotificationMessageHolderType notifyMsg = new NotificationMessageHolderType();
            notifyMsg.setProducerReference((W3CEndpointReference) dataMap.get("producer"));
            notifyMsg.setSubscriptionReference((W3CEndpointReference) dataMap.get("subscription"));
            notifyMsg.setTopic((TopicExpressionType) dataMap.get("topics"));
            notifyMsg.setMessage((MessageType) dataMap.get("message"));
            notifyRequest.getNotificationMessage().add(notifyMsg);
            client.notify(notifyRequest);
        } catch (Exception e) {
            log.info(netLogger.error("SendNotifyJob.execute", ErrSev.MAJOR, 
                    e.getMessage(), url));
            return;
        }
        log.info(netLogger.end("SendNotifyJob.execute", null, url));
    }

}