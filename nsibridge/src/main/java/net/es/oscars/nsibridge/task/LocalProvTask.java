package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;

public class LocalProvTask extends Task  {
    private NSI_Prov_Event event;
    private String connectionId;
    public LocalProvTask(String connectionId, NSI_Prov_Event event) {
        this.scope = "oscars";
        this.connectionId = connectionId;
        this.event = event;
    }


    public void onRun() throws TaskException {
        super.onRun();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Prov_SM sm = smh.getProvStateMachines().get(connectionId);
        try {
            sm.process(event);
        } catch (StateException e) {
            e.printStackTrace();
        }
        System.out.println(this.id+" ran!");
        this.onSuccess();
    }

}
