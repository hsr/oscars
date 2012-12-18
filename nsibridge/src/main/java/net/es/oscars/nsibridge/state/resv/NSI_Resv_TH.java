package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;


public class NSI_Resv_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Resv_TH.class);


    private NsiResvModel mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Resv_State from = (NSI_Resv_State) gfrom;
        NSI_Resv_State to = (NSI_Resv_State) gto;
        NSI_Resv_Event ev = (NSI_Resv_Event) gev;
        switch (from) {
            case INITIAL:
                if (to.equals(NSI_Resv_State.RESERVING)) {
                    mdl.doLocalResv();
                }
                break;
            case RESERVING:
                if (to.equals(NSI_Resv_State.RESERVED)) {
                    mdl.sendNsiResvCF();
                } else if (to.equals(NSI_Resv_State.RESERVED)) {
                    mdl.sendNSIResvFL();

                }
                break;
            case RESERVED:
                break;
            default:
        }
    }


    public NsiResvModel getMdl() {
        return mdl;
    }

    public void setMdl(NsiResvModel mdl) {
        this.mdl = mdl;
    }

}
