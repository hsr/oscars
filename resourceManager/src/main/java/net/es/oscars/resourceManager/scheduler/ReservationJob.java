package net.es.oscars.resourceManager.scheduler;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.MessagePropertiesType;

public class ReservationJob implements Job {

    public static String RESERVATION = "ResDetails";
    public static String MSGPROPS = "MsgProperties";
    
    public ReservationJob() {
    }
    
    static public void setResDetails (JobDetail jobDetail, ResDetails resDetails, 
                                      MessagePropertiesType msgProps) {
        
        if (jobDetail == null) {
            Thread.dumpStack();
            throw new RuntimeException ("no JobDetail was provided");
        }
        JobDataMap map = new JobDataMap();
        map.put(ReservationJob.RESERVATION, resDetails);
        map.put(ReservationJob.MSGPROPS, msgProps);
        jobDetail.setJobDataMap(map);
    }
    
    static public ResDetails getResDetails (JobExecutionContext context) {
        if (context == null) {
            // No context
            return null;
        }
        JobDataMap map = context.getMergedJobDataMap();
        if (map == null) {
            // No map
            return null;
        }
        ResDetails resDetails = (ResDetails) map.get(ReservationJob.RESERVATION);  
        return resDetails;
    }
    
    static public MessagePropertiesType getMessageProperties (JobExecutionContext context) {
        if (context == null) {
            // No context
            return null;
        }
        JobDataMap map = context.getMergedJobDataMap();
        if (map == null) {
            // No map
            return null;
        }
        MessagePropertiesType msgProps = (MessagePropertiesType) map.get(ReservationJob.MSGPROPS);  
        return msgProps;
    }
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Should not be executed here.
        throw new JobExecutionException ("Based class execute is invoked.");
    }  
    
}
 