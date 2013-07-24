package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.SM_Event;

public enum NSI_Resv_Event implements SM_Event {
    RECEIVED_NSI_RESV_RQ,
    LOCAL_RESV_CONFIRMED,
    LOCAL_RESV_FAILED,
    LOCAL_RESV_HELD,
    LOCAL_RESV_ABORTED,
    LOCAL_RESV_COMMITTED,
    LOCAL_RESV_FAILED_TO_COMMIT,


}
