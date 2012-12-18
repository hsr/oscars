package net.es.oscars.nsibridge.state.act;


import net.es.oscars.nsibridge.ifces.NsiActModel;
import net.es.oscars.nsibridge.task.LocalActTask;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;


import java.util.Date;


public class NSI_Leaf_Act_Model implements NsiActModel {
    String connectionId = "";
    public NSI_Leaf_Act_Model(String connId) {
        connectionId = connId;
    }
    private NSI_Leaf_Act_Model() {}


    @Override
    public void doLocalAct() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        LocalActTask actTask = new LocalActTask(connectionId, NSI_Act_Event.LOCAL_ACT_CONFIRMED);

        try {
            wf.schedule(actTask , now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doLocalDeact() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        LocalActTask actTask = new LocalActTask(connectionId, NSI_Act_Event.LOCAL_DEACT_CONFIRMED);

        try {
            wf.schedule(actTask , now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }
}
