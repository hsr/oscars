package net.es.oscars.pss.test.sim;

import java.util.ArrayList;

import net.es.oscars.pss.api.Workflow;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.util.ClassFactory;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SimWorkflowInspectorJob implements Job {
    private Logger log = Logger.getLogger(SimWorkflowInspectorJob.class);

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        Workflow wfAgent = ClassFactory.getInstance().getWorkflow();
        PSSAction next = wfAgent.next();
        if (next != null) {
            ArrayList<PSSAction> actions = new ArrayList<PSSAction>();
            actions.add(next);
            try {
                wfAgent.process(actions);
            } catch (PSSException e) {
                log.error(e);
                e.printStackTrace();
            }
            wfAgent.update(next);
        }
    }

}
