package net.es.oscars.nsibridge.state.actv;


import net.es.oscars.nsibridge.ifces.NsiActvMdl;
import net.es.oscars.nsibridge.task.LocalActvTask;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;


import java.util.Date;


public class NSI_UP_Actv_Impl implements NsiActvMdl {
    String connectionId = "";
    public NSI_UP_Actv_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_UP_Actv_Impl() {}


    @Override
    public void localAct() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        LocalActvTask actvTask = new LocalActvTask(connectionId, NSI_Actv_Event.LOCAL_ACT_CONFIRMED);

        try {
            wf.schedule(actvTask, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void localDeact() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        LocalActvTask actvTask = new LocalActvTask(connectionId, NSI_Actv_Event.LOCAL_DEACT_CONFIRMED);

        try {
            wf.schedule(actvTask, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }
}
