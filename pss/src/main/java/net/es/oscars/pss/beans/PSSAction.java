package net.es.oscars.pss.beans;

import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;

public class PSSAction {

    private ActionType actionType;
    private PSSRequest request;
    private ActionStatus status;
    private OSCARSFaultReport faultReport;

    public boolean equals(PSSAction other) {
        if (other == null) return false;
        if (other.getRequest().equals(request)) {
            return other.getActionType().equals(actionType);
        } else {
            return false;
        }
    }

    public void setActionType(ActionType opType) {
        this.actionType = opType;
    }
    public ActionType getActionType() {
        return actionType;
    }
    public void setRequest(PSSRequest request) {
        this.request = request;
    }
    public PSSRequest getRequest() {
        return request;
    }
    public void setStatus(ActionStatus status) {
        this.status = status;
    }
    public ActionStatus getStatus() {
        return status;
    }

    public void setFaultReport(OSCARSFaultReport faultReport) {
        this.faultReport = faultReport;
    }

    public OSCARSFaultReport getFaultReport() {
        return faultReport;
    }


}
