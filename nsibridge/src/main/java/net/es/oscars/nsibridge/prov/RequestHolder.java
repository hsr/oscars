package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.*;

import java.util.ArrayList;
import java.util.List;

public class RequestHolder {
    private List<ResvRequest>   resvRequests    = new ArrayList<ResvRequest>();
    private List<ProvRequest>   provRequests    = new ArrayList<ProvRequest>();
    private List<RelRequest>    relRequests     = new ArrayList<RelRequest>();
    private List<QueryRequest>  queryRequests   = new ArrayList<QueryRequest>();
    private List<TermRequest>   termRequests    = new ArrayList<TermRequest>();


    private static RequestHolder instance;
    private RequestHolder() {}
    public static RequestHolder getInstance() {
        if (instance == null) instance = new RequestHolder();
        return instance;
    }


    public List<ResvRequest> getResvRequests() {
        return this.resvRequests;
    }
    public List<ProvRequest> getProvRequests() {
        return provRequests;
    }

    public List<RelRequest> getRelRequests() {
        return relRequests;
    }

    public List<QueryRequest> getQueryRequests() {
        return queryRequests;
    }

    public List<TermRequest> getTermRequests() {
        return termRequests;
    }
    public ResvRequest findResvRequest(String connectionId) {
        for (ResvRequest req : this.resvRequests) {
            if (req.getConnectionId().equals(connectionId)) return req;
        }
        return null;
    }
    public TermRequest findTermRequest(String connectionId) {
        for (TermRequest req : this.termRequests) {
            if (req.getConnectionId().equals(connectionId)) return req;
        }
        return null;
    }
    public ProvRequest findProvRequest(String connectionId) {
        for (ProvRequest req : this.provRequests) {
            if (req.getConnectionId().equals(connectionId)) return req;
        }
        return null;
    }
    public RelRequest findRelRequest(String connectionId) {
        for (RelRequest req : this.relRequests) {
            if (req.getConnectionId().equals(connectionId)) return req;
        }
        return null;
    }

}
