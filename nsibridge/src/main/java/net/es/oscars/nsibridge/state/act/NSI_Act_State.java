package net.es.oscars.nsibridge.state.act;

import net.es.oscars.nsibridge.ifces.SM_State;


public enum NSI_Act_State implements SM_State {
    INACTIVE,
    ACTIVATING,
    ACTIVE,
    DEACTIVATING,
}
