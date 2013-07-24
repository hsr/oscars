package net.es.oscars.nsibridge.state.actv;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;


public class NSI_Actv_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Actv_TH.class);


    private NsiActvMdl mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Actv_State from = (NSI_Actv_State) gfrom;
        NSI_Actv_State to = (NSI_Actv_State) gto;
        NSI_Actv_Event ev = (NSI_Actv_Event) gev;
        switch (from) {
            case INACTIVE:

                break;
            case ACTIVATING:
                if (to.equals(NSI_Actv_State.ACTIVATING)) {
                    mdl.doLocalAct();
                }
            case ACTIVE:

                break;
            case DEACTIVATING:
                if (to.equals(NSI_Actv_State.INACTIVE)) {
                    mdl.doLocalDeact();
                }
                break;
            default:
        }
    }


    public NsiActvMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiActvMdl mdl) {
        this.mdl = mdl;
    }

}
