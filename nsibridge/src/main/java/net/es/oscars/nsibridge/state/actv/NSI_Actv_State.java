package net.es.oscars.nsibridge.state.actv;

import net.es.oscars.nsibridge.ifces.SM_State;


public class NSI_Actv_State implements SM_State {
    private NSI_Actv_StateEnum enumType;
    public String value() {
        return enumType.value();

    }
    public void setValue(String value) {
        enumType = NSI_Actv_StateEnum.fromValue(value);

    }
    public Object state() {
        return enumType;
    }
    public void setState(Object state) {
        if (state instanceof NSI_Actv_StateEnum) {
            enumType = (NSI_Actv_StateEnum) state;
        }
    }

}
