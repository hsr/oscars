package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsibridge.common.JettyContainer;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType;
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

        ActivationStateType at = new ActivationStateType();
        at.setVersion(1);
        cst.setActivationState(at);

        ReservationStateType rt = new ReservationStateType();
        rt.setVersion(1);
        cst.setReservationState(rt);

        ProvisionStateType pt = new ProvisionStateType();
        pt.setVersion(1);
        cst.setProvisionState(pt);

        NSI_Act_State as = (NSI_Act_State)   asm.getState();
        NSI_Prov_State ps = (NSI_Prov_State) psm.getState();
        NSI_Resv_State rs = (NSI_Resv_State) rsm.getState();
        NSI_Term_State ts = (NSI_Term_State) tsm.getState();

        switch (as) {
            case INACTIVE:
                at.setState(ActivationStateEnumType.INACTIVE);
                break;
            case ACTIVE:
                at.setState(ActivationStateEnumType.ACTIVE);
                break;
            case ACTIVATING:
                at.setState(ActivationStateEnumType.ACTIVATING);
                break;
            case DEACTIVATING:
                at.setState(ActivationStateEnumType.DEACTIVATING);
                break;
            default:
                at.setState(ActivationStateEnumType.UNKNOWN);
        }

        switch (ps) {
            case INITIAL:
                pt.setState(ProvisionStateEnumType.INITIAL);
                break;
            case SCHEDULED:
                pt.setState(ProvisionStateEnumType.SCHEDULED);
                break;
            case PROVISIONED:
                pt.setState(ProvisionStateEnumType.PROVISIONED);
                break;
            case PROVISIONING:
                pt.setState(ProvisionStateEnumType.PROVISIONING);
                break;
            case PROVISION_FAILED:
                pt.setState(ProvisionStateEnumType.PROVISION_FAILED);
                break;
            case RELEASING:
                pt.setState(ProvisionStateEnumType.RELEASING);
                break;
            case RELEASE_FAILED:
                pt.setState(ProvisionStateEnumType.RELEASE_FAILED);
                break;
            default:
                pt.setState(ProvisionStateEnumType.UNKNOWN);
        }

        switch (rs) {
            case INITIAL:
                rt.setState(ReservationStateEnumType.INITIAL);
                break;
            case RESERVING:
                rt.setState(ReservationStateEnumType.RESERVING);
                break;
            case RESERVED:
                rt.setState(ReservationStateEnumType.RESERVED);
                break;
            case RESERVE_FAILED:
                rt.setState(ReservationStateEnumType.RESERVE_FAILED);
                break;
            case MODIFYING:
                rt.setState(ReservationStateEnumType.MODIFYING);
                break;
            case MODIFY_FAILED:
                rt.setState(ReservationStateEnumType.MODIFY_FAILED);
                break;
            case MODIFY_CHECKING:
                rt.setState(ReservationStateEnumType.MODIFY_CHECKING);
                break;
            case MODIFY_CHECKED:
                rt.setState(ReservationStateEnumType.MODIFY_CHECKED);
                break;
            case MODIFY_CANCELING:
                rt.setState(ReservationStateEnumType.MODIFY_CANCELING);
                break;
            case MODIFY_CANCEL_FAILED:
                rt.setState(ReservationStateEnumType.MODIFY_CANCEL_FAILED);
                break;
            default:
                rt.setState(ReservationStateEnumType.UNKNOWN);
        }
        switch (ts) {
            case INITIAL:
                break;
            case TERMINATING:
                rt.setState(ReservationStateEnumType.TERMINATING);
                pt.setState(ProvisionStateEnumType.TERMINATING);
                break;
            case TERMINATED:
                rt.setState(ReservationStateEnumType.TERMINATED_REQUEST);
                pt.setState(ProvisionStateEnumType.TERMINATED_REQUEST);
                break;
            case TERMINATE_FAILED:
                rt.setState(ReservationStateEnumType.TERMINATE_FAILED);
                pt.setState(ProvisionStateEnumType.TERMINATE_FAILED);
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

        ht.setReplyTo("http://jupiter.es.net:8288/ConnectionService");
        return ht;

    }
    public static Holder makeHolder(CommonHeaderType hd) {
        Holder h = new Holder();
        h.value = hd;
        return h;
    }

}
