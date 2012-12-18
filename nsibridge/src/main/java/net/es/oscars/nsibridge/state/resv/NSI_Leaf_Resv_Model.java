package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.ifces.NSIMessage;
import net.es.oscars.nsibridge.ifces.NsiResvModel;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionServiceProvider;
import net.es.oscars.nsibridge.task.OscarsResvTask;
import net.es.oscars.nsibridge.task.SendNSIMessageTask;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_Leaf_Resv_Model implements NsiResvModel {
    String connectionId = "";
    public NSI_Leaf_Resv_Model(String connId) {
        connectionId = connId;
    }



    @Override
    public void doLocalResv() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task oscarsResv = new OscarsResvTask(connectionId);

        try {
            wf.schedule(oscarsResv, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNsiResvCF() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, NSIMessage.RESV_CF);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNSIResvFL() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, NSIMessage.RESV_FL);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }
}
