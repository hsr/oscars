package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;

public class NSI_Resv_SM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(NSI_Resv_SM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public NSI_Resv_SM(String id) {
        this.state = NSI_Resv_State.INITIAL;
        this.id = id;
    }

    @Override
    public void process(SM_Event event) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        NSI_Resv_State prevState = (NSI_Resv_State) this.getState();
        NSI_Resv_State nextState = null;
        String pre = "PRE: PSM ["+this.getId()+"] at state ["+state+"] got event ["+event+"]";
        LOG.debug(pre);
        String error = pre;


        switch (prevState) {
            case INITIAL:
                if (!event.equals(NSI_Resv_Event.RECEIVED_NSI_RESV_RQ)) {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                } else {
                    nextState = NSI_Resv_State.RESERVING;
                    this.setState(nextState);
                }
                break;
            case RESERVING:
                if (event.equals(NSI_Resv_Event.LOCAL_RESV_CONFIRMED)) {
                    nextState = NSI_Resv_State.RESERVED;
                    this.setState(nextState);
                } else if (event.equals(NSI_Resv_Event.LOCAL_RESV_FAILED)) {
                    nextState = NSI_Resv_State.RESERVE_FAILED;
                    this.setState(nextState);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case RESERVED:
                error = pre + " : error : event ["+event+"] not allowed";
                LOG.error(error);
                throw new StateException(error);

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
