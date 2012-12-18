package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.SM_State;


public enum NSI_Prov_State implements SM_State {
    INITIAL,
    SCHEDULED,

    PROVISIONING,
    PROVISION_FAILED,
    PROVISIONED,

    RELEASING,
    RELEASE_FAILED,


}
