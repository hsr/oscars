package net.es.oscars.nsibridge.state.term;

import net.es.oscars.nsibridge.ifces.SM_Event;

public enum NSI_Term_Event implements SM_Event {
    END_TIME,
    CLEANUP,
    RECEIVED_NSI_TERM_RQ,

    LOCAL_TERM_CONFIRMED,
    LOCAL_TERM_FAILED,




}
