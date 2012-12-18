package net.es.oscars.pss.test.sim;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

public class SimSubmitterCreatorJob implements Job {
    /**
     * Creates new jobs that each will create a new simulated request,
     * on a simulated schedule.
     * The list of the requests to make is in the job context at
     * dataMap.get("requests") . Each simulated request contains scheduling
     * information.
     */
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        System.out.println("starting request creator");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        List<SimRequest> requests = (List<SimRequest>) dataMap.get("requests");
        boolean moreRequests = !requests.isEmpty();

        int time = 0;
        while (moreRequests) {
            List<SimRequest> reqsToSubmit = new ArrayList<SimRequest>();
            for (SimRequest req : requests) {
                if (req.getStartTime() < time) {
                    reqsToSubmit.add(req);
                    // System.out.println("CLIENT: added: "+req.getId()+" time: "+time);
                }
            }
            int i = 0;
            for (SimRequest req : reqsToSubmit) {
                Simulation.getInstance().setReady(true);
                i++;
                Date date = new Date();

                SimpleTrigger trigger = new SimpleTrigger("SimSubmitter"+req.getId(), null, date, null, 0, 0L);
                JobDetail jobDetail = new JobDetail("SimSubmitter"+req.getId(), "SimSubmitter", SimSubmitterJob.class);
                JobDataMap clientDataMap = new JobDataMap();
                clientDataMap.put("request", req);
                jobDetail.setJobDataMap(clientDataMap);
                try {
                    SimScheduler.getInstance().getScheduler().scheduleJob(jobDetail, trigger);
                } catch (SchedulerException e) {
                    System.out.println("Could not schedule client job for request"+req.getId());
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                requests.remove(req);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            moreRequests = !requests.isEmpty();
            time++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Simulation.getInstance().setDoneSubmitting(true);
    }

}
