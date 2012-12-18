package net.es.oscars.api.http;

import java.util.UUID;

import javax.xml.ws.WebServiceContext;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import net.es.oscars.api.common.OSCARSIDC;
import net.es.oscars.api.compat.DataTranslator05;
import net.es.oscars.api.compat.SubscribeJob05;
import net.es.oscars.api.compat.SubscribeManager05;
import net.es.oscars.api.soap.gen.v05.OSCARSNotifyOnly;
import net.es.oscars.api.soap.gen.v06.InterDomainEventContent;
import net.es.oscars.common.soap.gen.SubjectAttributes;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;


@javax.jws.WebService(
        serviceName = "OSCARSNotifyOnlyService",
        portName = "OSCARSNotifyOnly",
        targetNamespace = "http://oscars.es.net/OSCARS",
        endpointInterface = "net.es.oscars.api.soap.gen.v05.OSCARSNotifyOnly")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class OSCARSNotify05SoapHandler implements OSCARSNotifyOnly{
    
    @javax.annotation.Resource
    private WebServiceContext myContext;
    private Scheduler scheduler;
    
    // Implements requests
    private static final String DEFAULT_RENEW_SCHED = "0 0/2 * * * ?"; //every 2 minutes
    private static final Logger LOG = Logger.getLogger(OSCARSNotify05SoapHandler.class.getName());
    
    /**
     * Initialize the class by scheduling a job to maintain subscriptions with 
     * 0.5 NotificationBrokers
     */
    public OSCARSNotify05SoapHandler(){
        SchedulerFactory schedFactory = new StdSchedulerFactory();
        try {
            //init subscribe manager
            SubscribeManager05.getInstance();
            //schedule renew job
            this.scheduler =  schedFactory.getScheduler();
            this.scheduler.start();
            CronTrigger cronTrigger = new CronTrigger("Renew05Trigger", "RENEW05", DEFAULT_RENEW_SCHED);
            JobDetail jobDetail = new JobDetail("Renew05Job", "RENEW05", SubscribeJob05.class);
            this.scheduler.scheduleJob(jobDetail, cronTrigger);
        } catch (Exception e) {
            LOG.error("Unable to init renew job: " + e.getMessage());
            System.exit(1);
        } 
    }
    
    public void notify(Notify notify) {
        String event = "OSCARSNotify05SoapHandler.notify";
        String guid = UUID.randomUUID().toString();
        OSCARSNetLogger netLogger = new OSCARSNetLogger(ModuleName.API, guid);
        OSCARSNetLogger.setTlogger(netLogger);
        
        LOG.info(netLogger.start(event));
        try {
            //verify this is one of our subscriptions
            if(notify.getNotificationMessage().size() == 0){
                LOG.error(netLogger.error(event, ErrSev.MAJOR,
                        "Received empty 0.5 Notify message"));
                return;
            }
            String subscripDomainId = SubscribeManager05.getInstance().validateSubscription(
                    notify.getNotificationMessage().get(0).getSubscriptionReference());
            if(subscripDomainId == null){
                LOG.error(netLogger.error(event, ErrSev.MAJOR,
                    "Invalid subscription ID in message"));
                return;
            }
            
            //keep the transaction ID consistent with what we logged so far
            InterDomainEventContent interDomain06 =  DataTranslator05.translate (notify);
            interDomain06.getMessageProperties().setGlobalTransactionId(guid);
            
            //we can ignore these messages
            if("PATH_SETUP_COMPLETED".equals(interDomain06.getType()) || 
                    "PATH_TEARDOWN_COMPLETED".equals(interDomain06.getType())){
                LOG.info(netLogger.end(event));
                return;
            }
            
            //We already validated the subscription id, so we'll set a role
            //this requires a server wanting to talk to 0.5 to have the OSCARS-Service role
            SubjectAttributes subjectAttributes = new SubjectAttributes();
            AttributeType roleAttr = new AttributeType();
            roleAttr.setName(AuthZConstants.ROLE);
            roleAttr.getAttributeValue().add("OSCARS-service");
            subjectAttributes.getSubjectAttribute().add(roleAttr);
            
            //Another hack. The login-id must be the same as the domain id
            AttributeType loginAttr = new AttributeType();
            loginAttr.setName(AuthZConstants.LOGIN_ID);
            loginAttr.getAttributeValue().add(subscripDomainId);
            subjectAttributes.getSubjectAttribute().add(loginAttr);
            
           //Another hack. The institution must be the same as the domain id
            AttributeType instAttr = new AttributeType();
            instAttr.setName(AuthZConstants.INSTITUTION);
            instAttr.getAttributeValue().add(subscripDomainId);
            subjectAttributes.getSubjectAttribute().add(instAttr);
            
            CoordClient coordClient = OSCARSIDC.getInstance().getCoordClient();
            Object[] req = new Object[]{subjectAttributes,interDomain06};
            coordClient.invoke("interDomainEvent",req);
        } catch (OSCARSServiceException ex) {
            LOG.error(netLogger.error(event, ErrSev.MAJOR,
                    "Error handling 0.5 Notify: " + ex.getMessage()));
            ex.printStackTrace();
        }catch (Exception ex) {
            LOG.error(netLogger.error(event, ErrSev.MAJOR,
                                           "Error handling 0.5 Notify: " + ex.toString()));
            ex.printStackTrace();
        }
        LOG.info(netLogger.end(event));
    }
 
}
