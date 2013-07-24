package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.ReservationStateEnumType;


public class NSI_Resv_State implements SM_State {
    private ReservationStateEnumType enumType;
    public String value() {
        return enumType.value();

    }
    public void setValue(String value) {
        enumType = ReservationStateEnumType.fromValue(value);

    }
    public Object state() {
        return enumType;
    }

    public void setState(Object state) {
        if (state instanceof ReservationStateEnumType) {
            enumType = (ReservationStateEnumType) state;
        }
    }


}
