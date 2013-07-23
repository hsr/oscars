package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsibridge.beans.config.JettyConfig;
import net.es.oscars.nsibridge.common.JettyContainer;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.types.ServiceExceptionType;
import net.es.oscars.nsibridge.state.act.NSI_Act_SM;
import net.es.oscars.nsibridge.state.act.NSI_Act_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_State;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_State;
import net.es.oscars.nsibridge.state.term.NSI_Term_SM;
import net.es.oscars.nsibridge.state.term.NSI_Term_State;

import javax.xml.ws.Holder;

public class NSI_Util {
    public static ConnectionStatesType makeConnectionStates(String connId) throws Exception {

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);
        NSI_Act_SM asm = smh.getActStateMachines().get(connId);
        NSI_Prov_SM psm = smh.getProvStateMachines().get(connId);
        NSI_Term_SM tsm = smh.getTermStateMachines().get(connId);
        if (rsm == null) {

        }
        if (asm == null) {

        }
        if (psm == null) {

        }
        if (tsm == null) {

        }


        ConnectionStatesType cst = new ConnectionStatesType();

        LifecycleStateEnumType lt = null;
        cst.setLifecycleState(lt);

        ReservationStateEnumType rt = null;
        cst.setReservationState(rt);

        DataPlaneStatusType dt = new DataPlaneStatusType();
        dt.setVersion(1);
        dt.setActive(false);
        cst.setDataPlaneStatus(dt);

        ProvisionStateEnumType pt = null;
        cst.setProvisionState(pt);

        NSI_Act_State as = (NSI_Act_State)   asm.getState();
        NSI_Prov_State ps = (NSI_Prov_State) psm.getState();
        NSI_Resv_State rs = (NSI_Resv_State) rsm.getState();
        NSI_Term_State ts = (NSI_Term_State) tsm.getState();

        switch (as) {
            case INACTIVE:
                dt.setActive(false);
                break;
            case ACTIVE:
                dt.setActive(true);
                break;
            case ACTIVATING:
                dt.setActive(false);
                break;
            case DEACTIVATING:
                dt.setActive(true);
                break;
            default:
                dt.setActive(false);
        }

        switch (ps) {
            case INITIAL:
                pt = null;
                break;
            case SCHEDULED:
                pt = ProvisionStateEnumType.PROVISIONING;
                break;
            case PROVISIONED:
                pt = ProvisionStateEnumType.PROVISIONED;
                break;
            case PROVISIONING:
                pt = ProvisionStateEnumType.PROVISIONING;
                break;
            case PROVISION_FAILED:
                pt = null;
                break;
            case RELEASING:
                pt = ProvisionStateEnumType.RELEASING;
                break;
            case RELEASE_FAILED:
                pt = null;
                break;
            default:
                pt = null;
        }

        switch (rs) {
            case INITIAL:
                rt = null;
                break;
            case RESERVING:
                rt = ReservationStateEnumType.RESERVE_START;
                break;
            case RESERVED:
                rt = ReservationStateEnumType.RESERVE_HELD;
                break;
            case RESERVE_FAILED:
                rt = ReservationStateEnumType.RESERVE_FAILED;
                break;
            default:
                rt = null;
        }
        switch (ts) {
            case INITIAL:
                break;
            case TERMINATING:
                lt = LifecycleStateEnumType.TERMINATING;
                break;
            case TERMINATED:
                lt = LifecycleStateEnumType.TERMINATED;
                break;
            case TERMINATE_FAILED:
                lt = LifecycleStateEnumType.FAILED;
                break;
            default:
        }

        return cst;

    }

    public static ServiceExceptionType makeServiceException(String error) {
        ServiceExceptionType st = new ServiceExceptionType();
        // st.setNsaId();

        return st;
    }

    public static CommonHeaderType makeNsiOutgoingHeader(CommonHeaderType ph) {


        CommonHeaderType ht = new CommonHeaderType();
        ht.setCorrelationId(ph.getCorrelationId());
        ht.setProtocolVersion(ph.getProtocolVersion());
        ht.setProviderNSA(ph.getProviderNSA());
        ht.setRequesterNSA(ph.getRequesterNSA());


        JettyConfig jc = JettyContainer.getInstance().getConfig();
        String hostname = jc.getHttp().getHostname();
        Integer port = jc.getHttp().getPort();

        ht.setReplyTo("http://"+hostname+":"+port+"/ConnectionService");
        return ht;

    }
    public static Holder makeHolder(CommonHeaderType hd) {
        Holder h = new Holder();
        h.value = hd;
        return h;
    }

}
