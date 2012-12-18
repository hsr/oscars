package net.es.oscars.pss.test.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import net.es.oscars.pss.api.CircuitService;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.util.ClassFactory;

public class SimCircuitService implements CircuitService {
    private CircuitServiceConfig config;
    private Logger log = Logger.getLogger(SimCircuitService.class);

    public List<PSSAction> setup(List<PSSAction> actions) {

        log.debug("setup.start");
        int totalTime = 2;

        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            try {
                Thread.sleep(1000*totalTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        log.debug("setup.end");
        return results;
    }
    public List<PSSAction> status(List<PSSAction> actions) {
        log.debug("status.start");
        Random rand = new Random();
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            int timeToStatus = 1 + rand.nextInt(3);
            try {
                Thread.sleep(1000*timeToStatus);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        log.debug("status.end");
        return results;
    }
    
    public List<PSSAction> teardown(List<PSSAction> actions) {
        int totalTime = 2;
        log.debug("teardown.start");

        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            try {
                Thread.sleep(1000*totalTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        log.debug("teardown.end");
        return results;
    }
    
    public List<PSSAction> modify(List<PSSAction> actions) throws PSSException {
        throw new PSSException("Unsupported operation");
    }
    public void setConfig(CircuitServiceConfig config) {
        this.config = config;
        
    }

}
