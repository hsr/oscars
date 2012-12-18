package net.es.oscars.pss.stub;


import net.es.oscars.pss.api.Notifier;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public class StubNotifier implements Notifier {

    public PSSAction process(PSSAction action) throws PSSException {
        return action;
    }

    public void setConfig(GenericConfig config) {
    }

}
