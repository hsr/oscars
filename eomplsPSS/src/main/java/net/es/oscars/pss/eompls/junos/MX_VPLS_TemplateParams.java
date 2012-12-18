package net.es.oscars.pss.eompls.junos;

import java.util.ArrayList;
import java.util.HashMap;

public class MX_VPLS_TemplateParams {

    /*
    setup:
    1. policy (string)
    2. community: name, id
    3. filters: stats, policing
    4. policer: name, bandwidth_limit, burst_size_limit
    5. vpls: name, id

    6. ifces: list_of <name, vlan, description>
    7. paths: list_of <name, hops>
                             hops: list of string >
    8. lsps: list_of <name, from, to, path, neighbor, bandwidth>
    */

    /*
    teardown:
    1. policy (string)
    2. community: name
    3. filters: stats, policing
    4. policer: name
    5. vpls: name

    6. ifces: list_of <name, vlan>
    7. paths: list_of <name>
    8. lsps: list_of <name>
    */

    protected String policy = "";
    protected HashMap community = new HashMap();
    protected HashMap filters = new HashMap();
    protected HashMap policer = new HashMap();
    protected HashMap vpls = new HashMap();
    protected ArrayList ifces = new ArrayList();
    protected ArrayList paths = new ArrayList();
    protected ArrayList lsps = new ArrayList();

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public HashMap getCommunity() {
        return community;
    }

    public void setCommunity(HashMap community) {
        this.community = community;
    }

    public HashMap getFilters() {
        return filters;
    }

    public void setFilters(HashMap filters) {
        this.filters = filters;
    }

    public HashMap getPolicer() {
        return policer;
    }

    public void setPolicer(HashMap policer) {
        this.policer = policer;
    }

    public HashMap getVpls() {
        return vpls;
    }

    public void setVpls(HashMap vpls) {
        this.vpls = vpls;
    }

    public ArrayList getIfces() {
        return ifces;
    }

    public void setIfces(ArrayList ifces) {
        this.ifces = ifces;
    }

    public ArrayList getPaths() {
        return paths;
    }

    public void setPaths(ArrayList paths) {
        this.paths = paths;
    }

    public ArrayList getLsps() {
        return lsps;
    }

    public void setLsps(ArrayList lsps) {
        this.lsps = lsps;
    }
}
