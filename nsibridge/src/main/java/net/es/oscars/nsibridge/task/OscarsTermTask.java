package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.nsibridge.beans.NSIConnection;
import net.es.oscars.nsibridge.beans.TermRequest;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsibridge.state.life.NSI_Term_Event;
import net.es.oscars.nsibridge.state.life.NSI_Term_SM;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

public class OscarsTermTask extends Task  {

    private String connId = "";
    private static final Logger log = Logger.getLogger(OscarsTermTask.class);
    public OscarsTermTask(String connId) {
        this.connId = connId;

        this.scope = "oscars";
    }
    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        try {
            super.onRun();

            RequestHolder rh = RequestHolder.getInstance();
            NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();


            TermRequest req = rh.findTermRequest(connId);
            NSI_Term_SM tsm = smh.getTermStateMachines().get(connId);
            NSIConnection conn = ch.findConnection(connId);
            String oscarsGri = conn.getOscarsGri();


            if (conn != null) {
                log.debug("found connection entry for connId: "+connId);
            }

            if (req != null) {
                log.debug("found request for connId: "+connId);
            }

            if (tsm != null) {
                log.debug("found state machine for connId: "+connId);
            }


            CancelResContent rc = NSI_OSCARS_Translation.makeOscarsCancel(oscarsGri);

            try {
                CancelResReply reply = OscarsProxy.getInstance().sendCancel(rc);
                if (reply.getStatus().equals("FAILED")) {
                    tsm.process(NSI_Term_Event.LOCAL_TERM_FAILED);
                } else {
                    tsm.process(NSI_Term_Event.LOCAL_TERM_CONFIRMED);
                }
            } catch (OSCARSServiceException e) {
                try {
                    tsm.process(NSI_Term_Event.LOCAL_TERM_FAILED);
                } catch (StateException e1) {
                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
