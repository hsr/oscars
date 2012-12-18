package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.SM_State;


public enum NSI_Resv_State implements SM_State {
    INITIAL,
    RESERVING,
    RESERVED,
    RESERVE_FAILED,

    MODIFYING,
    MODIFY_CHECKING,
    MODIFY_CHECKED,
    MODIFY_CANCELING,
    MODIFY_CANCEL_FAILED,
    MODIFY_FAILED
}
