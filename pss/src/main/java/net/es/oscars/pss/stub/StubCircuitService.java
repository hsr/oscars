package net.es.oscars.pss.stub;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.pss.api.CircuitService;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.util.ClassFactory;

public class StubCircuitService implements CircuitService {
    
    /**
     * Always fails (for now)
     */
    public List<PSSAction> modify(List<PSSAction> actions) {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            action.setStatus(ActionStatus.FAIL);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        return results;
    }
    public List<PSSAction> teardown(List<PSSAction> actions) {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        return results;
    }
    public List<PSSAction> setup(List<PSSAction> actions) {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        return results;
    }
    public List<PSSAction> status(List<PSSAction> actions) {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        return results;
    }
    
    public void setConfig(CircuitServiceConfig config) {
    }

}
