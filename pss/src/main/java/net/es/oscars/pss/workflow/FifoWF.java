package net.es.oscars.pss.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import net.es.oscars.pss.api.CircuitService;
import net.es.oscars.pss.api.Notifier;
import net.es.oscars.pss.api.Workflow;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.util.ClassFactory;

/**
 * a workflow that performs actions exactly one. at. a. time in
 * strict sequential order
 *
 * @author haniotak
 *
 */
public class FifoWF implements Workflow {
    private LinkedBlockingQueue<PSSAction> outstanding;
    private PSSAction running;
    private LinkedBlockingQueue<PSSAction> completed;
    private Logger log = Logger.getLogger(FifoWF.class);
    private Integer idx = 0;

    public FifoWF() {
        outstanding = new LinkedBlockingQueue<PSSAction>();
        completed = new LinkedBlockingQueue<PSSAction>();
    }

    public synchronized List<PSSAction> getCompleted() {
        ArrayList<PSSAction> result = new ArrayList<PSSAction>();
        result.addAll(completed);
        return result;
    }

    public synchronized PSSAction next() {
        if (running == null) {
            // nothing to do
            if (outstanding.isEmpty()) {
                return null;
            }
            // get first item in the queue, set it as running, return
            running = outstanding.remove();
            return running;
        } else {
            System.out.println(running.getRequest().getRequestType());
            // If something is already running we have to wait for it to complete
            return null;
        }
    }

    public synchronized void add(PSSAction action) throws PSSException {
        if (action.getRequest().getId() == null) {
            action.getRequest().setId(idx.toString());
            idx++;
        }
        log.debug("adding to workflow: "+action.getActionType()+" for "+action.getRequest().getId());
        outstanding.add(action);
    }

    public synchronized void update(PSSAction action) {
        ActionStatus status = action.getStatus();
        if (status == null) {
            log.error("Got null status, setting to failed");
            status = ActionStatus.FAIL;
        }
        if (action.equals(running)) {
            running.setStatus(status);

            if (status.equals(ActionStatus.SUCCESS)) {
                log.info("Success!");
                completed.add(running);
            } else if (status.equals(ActionStatus.FAIL)) {
                log.equals("failure :(");
                completed.add(running);
            }
            
            Notifier not = ClassFactory.getInstance().getNotifier();
            try {
                not.process(action);
            } catch (PSSException e) {
                log.error(e);
                e.printStackTrace();
            }
            running = null;
        }

    }

    public synchronized void remove(PSSAction action) throws PSSException {
        if (action.equals(running)) {
            throw new PSSException ("Can not remove running request op");
        } else {
            if (outstanding.contains(action)) {
                outstanding.remove(action);
            }
            if (completed.contains(action)) {
                completed.remove(action);
            }
        }

    }

    public void setConfig(GenericConfig config) {
    }

    public boolean hasOutstanding() {
        if (!outstanding.isEmpty()) return true;
        if (running != null) return true;
        return false;
    }
    public List<PSSAction> getOutstanding() {
        ArrayList<PSSAction> result = new ArrayList<PSSAction>();
        result.addAll(this.outstanding);
        return result;
    }

    public synchronized void process(List<PSSAction> actions) throws PSSException {
        if (actions.isEmpty()) return;
        log.debug("processing "+actions.size()+" action(s)");
        for (PSSAction action : actions) {
            // one at a time
            ArrayList<PSSAction> oneAction = new ArrayList<PSSAction>();
            oneAction.add(action);
            CircuitService cs = ClassFactory.getInstance().getCircuitService();
            log.debug("processing a "+action.getActionType()+" action ");
            switch (action.getActionType()) {
                case SETUP :
                    cs.setup(oneAction);
                break;
                case TEARDOWN:
                    cs.teardown(oneAction);
                break;
                case MODIFY:
                    cs.modify(oneAction);
                break;
                case STATUS:
                    cs.status(oneAction);
                break;
            }
        }

    }


}
