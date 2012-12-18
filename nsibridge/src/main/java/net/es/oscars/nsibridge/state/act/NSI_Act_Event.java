package net.es.oscars.nsibridge.state.act;

import net.es.oscars.nsibridge.ifces.SM_Event;

/**
 * @haniotak Date: 2012-08-07
 */
public enum NSI_Act_Event implements SM_Event {


    START_TIME,
    END_TIME,
    CLEANUP,
    RECEIVED_NSI_TERM_RQ,

    LOCAL_ACT_CONFIRMED,
    LOCAL_ACT_FAILED,


    LOCAL_DEACT_CONFIRMED,
    LOCAL_DEACT_FAILED,

}
