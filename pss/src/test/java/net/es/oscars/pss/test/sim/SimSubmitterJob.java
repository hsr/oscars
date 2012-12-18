package net.es.oscars.pss.test.sim;

import net.es.oscars.pss.beans.PSSRequest;
import net.es.oscars.pss.soap.PSSSoapHandler;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SimSubmitterJob implements Job {

    public synchronized void execute(JobExecutionContext context)
            throws JobExecutionException {
        long startTime = System.currentTimeMillis();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        SimRequest req = (SimRequest) dataMap.get("request");

        PSSSoapHandler soap = new PSSSoapHandler();

        try {

            System.out.println(". submitting "+req.getId()+" ");

            if (req.getRequestType().equals(PSSRequest.PSSRequestTypes.SETUP)) {
                soap.setup(req.getSetupReq());
            } else if (req.getRequestType().equals(PSSRequest.PSSRequestTypes.TEARDOWN)) {
                soap.teardown(req.getTeardownReq());
            }
            long endTime = System.currentTimeMillis();
            long diff = endTime - startTime;
            System.out.println("............ "+req.getId()+" returned to client after "+diff+"ms");

        } catch (Exception ex) {
            System.out.println(req.getId()+" error:"+ex.getMessage());

        }
    }

}
