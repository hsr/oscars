package net.es.oscars.nsibridge.state.term;


import net.es.oscars.nsibridge.ifces.NSIMessage;
import net.es.oscars.nsibridge.ifces.NsiTermModel;
import net.es.oscars.nsibridge.task.OscarsTermTask;
import net.es.oscars.nsibridge.task.SendNSIMessageTask;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_Leaf_Term_Model implements NsiTermModel {
    String connectionId = "";
    public NSI_Leaf_Term_Model(String connId) {
        connectionId = connId;
    }
    private NSI_Leaf_Term_Model() {}



    @Override
    public void doLocalTerm() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task oscarsTerm = new OscarsTermTask(connectionId);

        try {
            wf.schedule(oscarsTerm, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNsiTermCF() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, NSIMessage.TERM_CF);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void sendNsiTermFL() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, NSIMessage.TERM_FL);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }
}
