package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;

public class NSI_Prov_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Prov_TH.class);


    private NsiProvMdl mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Prov_State from = (NSI_Prov_State) gfrom;
        NSI_Prov_State to = (NSI_Prov_State) gto;
        NSI_Prov_Event ev = (NSI_Prov_Event) gev;

        switch (from) {
            case INITIAL:

                break;
            case SCHEDULED:
                if (to.equals(NSI_Prov_State.PROVISIONING)) {
                    mdl.doLocalProv();
                }
                break;
            case PROVISIONING:
                if (to.equals(NSI_Prov_State.PROVISIONED)) {
                    mdl.sendNsiProvCF();
                } else if (to.equals(NSI_Prov_State.SCHEDULED)) {
                    mdl.sendNsiProvFL();
                }
                break;
            case PROVISIONED:
                if (to.equals(NSI_Prov_State.PROVISIONING)) {
                    mdl.doLocalProv();
                } else if (to.equals(NSI_Prov_State.RELEASING)) {
                    mdl.doLocalRel();
                }
                break;

            case RELEASING:
                if (to.equals(NSI_Prov_State.SCHEDULED)) {
                    mdl.sendNsiRelCF();
                } else if (to.equals(NSI_Prov_State.PROVISIONED)) {
                    mdl.sendNsiRelFL();
                }
                break;
            default:
        }
    }


    public NsiProvMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiProvMdl mdl) {
        this.mdl = mdl;
    }

}
