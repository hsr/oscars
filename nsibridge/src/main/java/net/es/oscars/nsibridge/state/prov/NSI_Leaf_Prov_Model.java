package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.ifces.Nsi_Message;
import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import net.es.oscars.nsibridge.task.LocalProvTask;
import net.es.oscars.nsibridge.task.SendNSIMessageTask;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_Leaf_Prov_Model implements NsiProvMdl {
    String connectionId = "";
    public NSI_Leaf_Prov_Model(String connId) {
        connectionId = connId;
    }
    private NSI_Leaf_Prov_Model() {}



    @Override
    public void doLocalProv() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        LocalProvTask provTask = new LocalProvTask(connectionId, NSI_Prov_Event.LOCAL_PROV_CONFIRMED);

        try {
            wf.schedule(provTask , now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendNsiProvCF() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, Nsi_Message.PROV_CF);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNsiProvFL() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, Nsi_Message.PROV_FL);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doLocalRel() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        LocalProvTask provTask = new LocalProvTask(connectionId, NSI_Prov_Event.LOCAL_REL_CONFIRMED);

        try {
            wf.schedule(provTask , now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNsiRelCF() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, Nsi_Message.REL_CF);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNsiRelFL() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, Nsi_Message.REL_FL);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }
}
