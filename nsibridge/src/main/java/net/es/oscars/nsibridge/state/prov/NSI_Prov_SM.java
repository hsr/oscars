package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_State;
import org.apache.log4j.Logger;

public class NSI_Prov_SM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(NSI_Prov_SM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public NSI_Prov_SM(String id) {
        this.state = NSI_Prov_State.INITIAL;
        this.id = id;
    }

    @Override
    public void process(SM_Event event) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        NSI_Prov_State prevState = (NSI_Prov_State) this.getState();
        NSI_Prov_State nextState = null;
        String pre = "PRE: PSM ["+this.getId()+"] at state ["+state+"] got event ["+event+"]";
        LOG.debug(pre);
        String error = pre;

        switch (prevState) {
            case INITIAL:
                nextState = NSI_Prov_State.SCHEDULED;
                this.setState(nextState);
                break;

            case SCHEDULED:
                if (!event.equals(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ)) {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                } else {
                    nextState = NSI_Prov_State.PROVISIONING;
                    this.setState(nextState);
                }
                break;

            case PROVISIONING:
                if (event.equals(NSI_Prov_Event.LOCAL_PROV_CONFIRMED)) {
                    nextState = NSI_Prov_State.PROVISIONED;
                    this.setState(nextState);
                } else if (event.equals(NSI_Prov_Event.LOCAL_PROV_FAILED)) {
                    nextState = NSI_Prov_State.SCHEDULED;
                    this.setState(nextState);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case PROVISIONED:
                if (event.equals(NSI_Prov_Event.RECEIVED_NSI_REL_RQ)) {
                    nextState = NSI_Prov_State.RELEASING;
                    this.setState(nextState);
                } else if (event.equals(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ)) {
                    nextState = NSI_Prov_State.PROVISIONING;
                    this.setState(nextState);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;


            case RELEASING:
                if (event.equals(NSI_Prov_Event.LOCAL_REL_CONFIRMED)) {
                    nextState = NSI_Prov_State.SCHEDULED;
                    this.setState(nextState);
                } else if (event.equals(NSI_Prov_Event.LOCAL_REL_FAILED)) {
                    nextState = NSI_Prov_State.PROVISIONED;
                    this.setState(nextState);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
        }

        String post = "PST: PSM ["+this.getId()+"] now at state ["+this.getState()+"] after event ["+event+"]";
        LOG.debug(post);
        this.transitionHandler.process(prevState, nextState, event, this);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public SM_State getState() {
        return state;
    }

    public void setState(SM_State state) {
        this.state = state;
    }

    @Override
    public TransitionHandler getTransitionHandler() {
        return transitionHandler;
    }

    @Override
    public void setTransitionHandler(TransitionHandler transitionHandler) {
        this.transitionHandler = transitionHandler;
    }


}
