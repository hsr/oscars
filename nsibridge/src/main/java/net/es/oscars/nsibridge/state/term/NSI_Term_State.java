package net.es.oscars.nsibridge.state.term;

import net.es.oscars.nsibridge.ifces.SM_State;


public enum NSI_Term_State implements SM_State {
    INITIAL,
    TERMINATING,
    TERMINATED,
    TERMINATE_FAILED,
}
