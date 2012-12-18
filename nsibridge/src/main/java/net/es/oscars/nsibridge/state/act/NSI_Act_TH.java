package net.es.oscars.nsibridge.state.act;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;


public class NSI_Act_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Act_TH.class);


    private NsiActModel mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Act_State from = (NSI_Act_State) gfrom;
        NSI_Act_State to = (NSI_Act_State) gto;
        NSI_Act_Event ev = (NSI_Act_Event) gev;
        switch (from) {
            case INACTIVE:

                break;
            case ACTIVATING:
                if (to.equals(NSI_Act_State.ACTIVATING)) {
                    mdl.doLocalAct();
                }
            case ACTIVE:

                break;
            case DEACTIVATING:
                if (to.equals(NSI_Act_State.INACTIVE)) {
                    mdl.doLocalDeact();
                }
                break;
            default:
        }
    }


    public NsiActModel getMdl() {
        return mdl;
    }

    public void setMdl(NsiActModel mdl) {
        this.mdl = mdl;
    }

}
