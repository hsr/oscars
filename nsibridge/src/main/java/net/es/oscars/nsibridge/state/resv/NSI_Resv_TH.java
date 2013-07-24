package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.ReservationStateEnumType;


public class NSI_Resv_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Resv_TH.class);


    private NsiResvMdl mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Resv_State from = (NSI_Resv_State) gfrom;
        NSI_Resv_State to = (NSI_Resv_State) gto;
        NSI_Resv_Event ev = (NSI_Resv_Event) gev;

        ReservationStateEnumType fromState = (ReservationStateEnumType) from.state();
        ReservationStateEnumType toState = (ReservationStateEnumType) to.state();
        String transitionStr = fromState+" -> "+toState;

        switch (fromState) {
            case RESERVE_START:
                if (toState == ReservationStateEnumType.RESERVE_CHECKING) {
                    mdl.localCheck();
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_CHECKING:
                if (toState == ReservationStateEnumType.RESERVE_HELD) {
                    mdl.localHold();
                    mdl.sendRsvCF();
                } else if (toState == ReservationStateEnumType.RESERVE_FAILED) {
                    mdl.localAbort();
                    mdl.sendRsvFL();
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_HELD:
                if (toState == ReservationStateEnumType.RESERVE_COMMITTING) {
                    mdl.localCommit();
                } else if (toState == ReservationStateEnumType.RESERVE_TIMEOUT) {
                    mdl.localAbort();
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_COMMITTING:
                if (toState == ReservationStateEnumType.RESERVE_START) {
                    if (ev == NSI_Resv_Event.LOCAL_RESV_COMMITTED) {
                       mdl.sendRsvCmtCF();
                    } else {
                        mdl.sendRsvCmtFL();
                    }

                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_FAILED:
                if (toState == ReservationStateEnumType.RESERVE_ABORTING) {
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_ABORTING:
                if (toState == ReservationStateEnumType.RESERVE_START) {
                    mdl.sendRsvAbtCF();
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_TIMEOUT:
                if (toState == ReservationStateEnumType.RESERVE_ABORTING) {

                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;
            default:

        }
    }


    public NsiResvMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiResvMdl mdl) {
        this.mdl = mdl;
    }

}
