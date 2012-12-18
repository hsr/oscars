package net.es.oscars.pss.util;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.enums.ActionType;

public class ActionUtils {
    public static ResDetails getReservation(PSSAction action) throws PSSException {
        ResDetails res = null;
        if (action == null) {
            throw new PSSException("null action");
        }
        if (action.getActionType() == null) {
            throw new PSSException("null action type");
        } else if (action.getActionType().equals(ActionType.MODIFY)) {
            res = action.getRequest().getModifyReq().getReservation();
        } else if (action.getActionType().equals(ActionType.SETUP)) {
            res = action.getRequest().getSetupReq().getReservation();
        } else if (action.getActionType().equals(ActionType.TEARDOWN)) {
            res = action.getRequest().getTeardownReq().getReservation();
        } else if (action.getActionType().equals(ActionType.STATUS)) {
            res = action.getRequest().getStatusReq().getReservation();
        } else {
            throw new PSSException("unknown action: "+action.getActionType());
        }
        return res;
    }

}
