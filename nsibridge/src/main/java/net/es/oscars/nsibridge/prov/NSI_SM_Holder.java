package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.actv.NSI_Actv_SM;
import net.es.oscars.nsibridge.state.actv.NSI_Actv_State;
import net.es.oscars.nsibridge.state.actv.NSI_Actv_TH;
import net.es.oscars.nsibridge.state.actv.NSI_UP_Actv_Impl;
import net.es.oscars.nsibridge.state.life.NSI_UP_Life_Impl;
import net.es.oscars.nsibridge.state.prov.NSI_Leaf_Prov_Model;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_TH;
import net.es.oscars.nsibridge.state.resv.NSI_UP_Resv_Impl;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_TH;
import net.es.oscars.nsibridge.state.life.NSI_Term_SM;
import net.es.oscars.nsibridge.state.life.NSI_Term_TH;

import java.util.HashMap;

public class NSI_SM_Holder {
    private HashMap<String, NSI_Actv_SM> actStateMachines= new HashMap<String, NSI_Actv_SM>();
    private HashMap<String, NSI_Prov_SM> provStateMachines= new HashMap<String, NSI_Prov_SM>();
    private HashMap<String, NSI_Resv_SM> resvStateMachines= new HashMap<String, NSI_Resv_SM>();
    private HashMap<String, NSI_Term_SM> termStateMachines= new HashMap<String, NSI_Term_SM>();

    private static NSI_SM_Holder instance;
    private NSI_SM_Holder() {}
    public static NSI_SM_Holder getInstance() {
        if (instance == null) instance = new NSI_SM_Holder();
        return instance;
    }

    public void makeStateMachines(String connId) throws ServiceException {
        NSI_Actv_SM asm = this.findNsiActSM(connId);
        NSI_Prov_SM psm = this.findNsiProvSM(connId);
        NSI_Resv_SM rsm = this.findNsiResvSM(connId);
        NSI_Term_SM tsm = this.findNsiTermSM(connId);
        boolean error = false;
        String errMsg = "";
        if (asm != null) {
            error = true;
            errMsg += "found existing actSM";
        }
        if (psm != null) {
            error = true;
            errMsg += "found existing provSM";
        }
        if (rsm != null) {
            error = true;
            errMsg += "found existing resvSM";
        }

        if (tsm != null) {
            error = true;
            errMsg += "found existing termSM";
        }
        if (error) {
            throw new ServiceException(errMsg);
        }
        asm = new NSI_Actv_SM(connId);
        asm.setState(NSI_Actv_State.INACTIVE);
        NSI_Actv_TH ath = new NSI_Actv_TH();
        asm.setTransitionHandler(ath);
        NSI_UP_Actv_Impl aml = new NSI_UP_Actv_Impl(connId);
        ath.setMdl(aml);


        psm = new NSI_Prov_SM(connId);
        psm.setState(NSI_Prov_State.SCHEDULED);
        NSI_Prov_TH pth = new NSI_Prov_TH();
        psm.setTransitionHandler(pth);
        NSI_Leaf_Prov_Model pml = new NSI_Leaf_Prov_Model(connId);
        pth.setMdl(pml);


        rsm = new NSI_Resv_SM(connId);
        NSI_Resv_TH rth = new NSI_Resv_TH();
        rsm.setTransitionHandler(rth);
        NSI_UP_Resv_Impl rml = new NSI_UP_Resv_Impl(connId);
        rth.setMdl(rml);

        tsm = new NSI_Term_SM(connId);
        NSI_Term_TH tth = new NSI_Term_TH();
        tsm.setTransitionHandler(tth);
        NSI_UP_Life_Impl tml = new NSI_UP_Life_Impl(connId);
        tth.setMdl(tml);



        this.actStateMachines.put(connId, asm);
        this.provStateMachines.put(connId, psm);
        this.resvStateMachines.put(connId, rsm);
        this.termStateMachines.put(connId, tsm);

    }


    public NSI_Actv_SM findNsiActSM(String connId) {
        return actStateMachines.get(connId);
    }

    public NSI_Prov_SM findNsiProvSM(String connId) {
        return provStateMachines.get(connId);
    }

    public NSI_Resv_SM findNsiResvSM(String connId) {
        return resvStateMachines.get(connId);
    }

    public NSI_Term_SM findNsiTermSM(String connId) {
        return termStateMachines.get(connId);
    }


    public HashMap<String, NSI_Actv_SM> getActStateMachines() {
        return actStateMachines;
    }

    public HashMap<String, NSI_Prov_SM> getProvStateMachines() {
        return provStateMachines;
    }

    public HashMap<String, NSI_Resv_SM> getResvStateMachines() {
        return resvStateMachines;
    }

    public HashMap<String, NSI_Term_SM> getTermStateMachines() {
        return termStateMachines;
    }
}
