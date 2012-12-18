package net.es.oscars.nsibridge.state.term;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;

public class NSI_Term_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Term_TH.class);


    private NsiTermModel mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Term_State from = (NSI_Term_State) gfrom;
        NSI_Term_State to = (NSI_Term_State) gto;
        NSI_Term_Event ev = (NSI_Term_Event) gev;

        switch (from) {
            case INITIAL:
                if (to.equals(NSI_Term_State.TERMINATING)) {
                    mdl.doLocalTerm();
                }
                break;
            case TERMINATING:
                if (to.equals(NSI_Term_State.TERMINATED)) {
                    mdl.sendNsiTermCF();
                } else if (to.equals(NSI_Term_State.TERMINATE_FAILED)) {
                    mdl.sendNsiTermFL();
                }
                break;
            case TERMINATE_FAILED:
                break;
            case TERMINATED:
                break;
            default:
        }
    }


    public NsiTermModel getMdl() {
        return mdl;
    }

    public void setMdl(NsiTermModel mdl) {
        this.mdl = mdl;
    }

}
