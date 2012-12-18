package net.es.oscars.pss.test.sim;


import org.apache.log4j.Logger;

import net.es.oscars.pss.api.Notifier;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public class SimNotifyAgent implements Notifier {
    private Logger log = Logger.getLogger(SimNotifyAgent.class);

    public PSSAction process(PSSAction action) throws PSSException {
        log.info("simulating notification for: "+action.getActionType());       
        return action;
    }

    public void setConfig(GenericConfig config) {
        // TODO Auto-generated method stub
        
    }

}
