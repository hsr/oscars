package net.es.oscars.notifycmdexec.jobs;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import net.es.oscars.notifycmdexec.SubscribeClient05;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SubscribeJob05 implements Job{
    
    private static W3CEndpointReference subscriptionId = null;
    private static Long termTime = null;
    private static String nbUrl = null;
    private static String idcUrl= null; 
    private static String consumerUrl = null; 
    
    final private String WS_TOPIC_FULL = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full";
    final private String XPATH_URI = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    final private double DEFAULT_TERM_TIME_WINDOW = .2;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            if(subscriptionId == null){
                this.sendSubscribe();
            }else if(termTime != null && termTime <= System.currentTimeMillis()){
                this.sendRenew();
            }
        } catch (Exception e) {
            System.err.println("Error subscribing: " + e.getMessage());
        }
    }
    
    static public void init(String nbUrlParam, String idcUrlParam, String consumerUrlParam){
        nbUrl = nbUrlParam;
        idcUrl = idcUrlParam;
        consumerUrl = consumerUrlParam;
    }
    
    private void sendRenew() {
        try {
            SubscribeClient05 nbClient = new SubscribeClient05(nbUrl);
            Renew renewRequest = new Renew();
            renewRequest.setSubscriptionReference(subscriptionId);
            RenewResponse renewResp = nbClient.renew(renewRequest);
            termTime = this.calcNextRenewTime(renewResp.getTerminationTime().toGregorianCalendar().getTimeInMillis());
        } catch (Exception e) {
            termTime = null;
            subscriptionId = null;
            System.out.println("Error sending renew: " + e.getMessage());
        } 
    }

    private void sendSubscribe() {
      //subscribe
        try {
            SubscribeClient05 nbClient = new SubscribeClient05(nbUrl);
            //clear old subscriptions
            try{
                Unsubscribe unsubscribe = new Unsubscribe();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document domDoc = db.newDocument();                
                Element subscrIdElem = domDoc.createElementNS("http://oscars.es.net/OSCARS", "subscriptionId");
                subscrIdElem.setTextContent("ALL");
                W3CEndpointReference subRef = (new W3CEndpointReferenceBuilder())
                            .address(nbUrl)
                            .referenceParameter(subscrIdElem)
                            .build();
                unsubscribe.setSubscriptionReference(subRef);
                nbClient.unsubscribe(unsubscribe);
            }catch(Exception e){}
            
            //create new subscription
            Subscribe subscribeRequest = new Subscribe();
            subscribeRequest.setConsumerReference((new W3CEndpointReferenceBuilder()).address(consumerUrl).build());
            FilterType filterType = new FilterType();
            TopicExpressionType topic = new TopicExpressionType();
            topic.setDialect(WS_TOPIC_FULL);
            topic.setValue("idc:IDC");
            filterType.getTopicExpression().add(topic);
            QueryExpressionType producer = new QueryExpressionType();
            producer.setDialect(XPATH_URI);
            producer.setValue("/wsa:Address='" + idcUrl + "'");
            filterType.getProducerProperties().add(producer);
            subscribeRequest.setFilter(filterType);
            SubscribeResponse subResp = nbClient.subscribe(subscribeRequest);
            termTime = this.calcNextRenewTime(subResp.getTerminationTime().toGregorianCalendar().getTimeInMillis());
            subscriptionId = subResp.getSubscriptionReference();
        } catch (Exception e) {
            System.out.println("Error sending subscribe: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Determine the next time a subscription needs to be renewed. Its 
     * based on the returned expiration time, minus some window so subscription 
     * does not expire before it can be renewed.
     * 
     * @param termTime the expiration of the subscription
     * @return
     */
    private Long calcNextRenewTime(long termTime){
        long currentTime = System.currentTimeMillis();
        return currentTime + (long)((1.0-DEFAULT_TERM_TIME_WINDOW) * 
                (double)(termTime - currentTime)); 
    }
}
 