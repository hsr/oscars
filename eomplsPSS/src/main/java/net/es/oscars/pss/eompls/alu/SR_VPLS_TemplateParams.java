package net.es.oscars.pss.eompls.alu;

import java.util.ArrayList;
import java.util.HashMap;

public class SR_VPLS_TemplateParams {
    protected ArrayList ifces = new ArrayList();
    protected ArrayList paths = new ArrayList();
    protected ArrayList lsps = new ArrayList();
    protected ArrayList sdps = new ArrayList();
    protected HashMap vpls = new HashMap();
    protected HashMap ingqos = new HashMap();

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

    public ArrayList getSdps() {
        return sdps;
    }

    public void setSdps(ArrayList sdps) {
        this.sdps = sdps;
    }

    public HashMap getVpls() {
        return vpls;
    }

    public void setVpls(HashMap vpls) {
        this.vpls = vpls;
    }

    public HashMap getIngqos() {
        return ingqos;
    }

    public void setIngqos(HashMap ingqos) {
        this.ingqos = ingqos;
    }
}
