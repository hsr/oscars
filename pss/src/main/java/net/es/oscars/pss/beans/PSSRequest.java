package net.es.oscars.pss.beans;

import net.es.oscars.coord.soap.gen.PSSReplyContent;
import net.es.oscars.pss.soap.gen.ModifyReqContent;
import net.es.oscars.pss.soap.gen.SetupReqContent;
import net.es.oscars.pss.soap.gen.StatusReqContent;
import net.es.oscars.pss.soap.gen.TeardownReqContent;

/**
 * A class to abstract / encapsulate PSS SOAP requests
 *
 * @author haniotak
 *
 */
public class PSSRequest {
    public enum PSSRequestTypes {
        SETUP,
        TEARDOWN,
        MODIFY,
        STATUS,
    }
    private String id;

    private PSSRequestTypes requestType;

    private SetupReqContent setupReq;
    private TeardownReqContent teardownReq;
    private StatusReqContent statusReq;
    private ModifyReqContent modifyReq;
    private PSSReplyContent reply;

    public PSSRequest() {
    }

    public boolean equals(PSSRequest other) {
        return (other.getId().equals(id));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public SetupReqContent getSetupReq() {
        return setupReq;
    }

    public void setSetupReq(SetupReqContent setupReq) {
        this.setupReq = setupReq;
    }

    public void setReply(PSSReplyContent reply) {
        this.reply = reply;
    }

    public PSSReplyContent getReply() {
        return this.reply;
    }


    public TeardownReqContent getTeardownReq() {
        return teardownReq;
    }

    public void setTeardownReq(TeardownReqContent teardownReq) {
        this.teardownReq = teardownReq;
    }

    public void setStatusReq(StatusReqContent statusReq) {
        this.statusReq = statusReq;
    }

    public StatusReqContent getStatusReq() {
        return statusReq;
    }

    public void setModifyReq(ModifyReqContent modifyReq) {
        this.modifyReq = modifyReq;
    }

    public ModifyReqContent getModifyReq() {
        return modifyReq;
    }

    public void setRequestType(PSSRequestTypes requestType) {
        this.requestType = requestType;
    }

    public PSSRequestTypes getRequestType() {
        return requestType;
    }
}
