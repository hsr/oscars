package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.state.act.NSI_Act_Event;
import net.es.oscars.nsibridge.state.act.NSI_Act_SM;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;

public class LocalActTask extends Task  {
    private NSI_Act_Event event;
    private String connectionId;
    public LocalActTask(String connectionId, NSI_Act_Event event) {
        this.scope = "oscars";
        this.connectionId = connectionId;
        this.event = event;
    }


    public void onRun() throws TaskException {
        super.onRun();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Act_SM sm = smh.getActStateMachines().get(connectionId);
        try {
            sm.process(event);
        } catch (StateException e) {
            e.printStackTrace();
        }
        System.out.println(this.id+" ran!");
        this.onSuccess();
    }

}
