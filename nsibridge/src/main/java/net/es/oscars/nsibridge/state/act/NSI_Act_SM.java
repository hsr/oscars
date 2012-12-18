package net.es.oscars.nsibridge.state.act;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;

public class NSI_Act_SM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(NSI_Act_SM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public NSI_Act_SM(String id) {
        this.state = NSI_Act_State.INACTIVE;
        this.id = id;
    }

    @Override
    public void process(SM_Event event) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        NSI_Act_State prevState = (NSI_Act_State) this.getState();
        NSI_Act_State nextState = null;
        String pre = "PRE: PSM ["+this.getId()+"] at state ["+state+"] got event ["+event+"]";
        LOG.debug(pre);
        String error = pre;

        switch (prevState) {
            case INACTIVE:
                if (event.equals(NSI_Act_Event.START_TIME)) {
                    nextState = NSI_Act_State.ACTIVATING;
                    this.setState(nextState);
                } else if (event.equals(NSI_Act_Event.CLEANUP)) {

                } else if (event.equals(NSI_Act_Event.END_TIME)) {

                } else if (event.equals(NSI_Act_Event.RECEIVED_NSI_TERM_RQ)) {

                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case ACTIVATING:
                if (event.equals(NSI_Act_Event.LOCAL_ACT_CONFIRMED)) {
                    nextState = NSI_Act_State.ACTIVE;
                    this.setState(nextState);
                } else if (event.equals(NSI_Act_Event.LOCAL_ACT_FAILED)) {
                    nextState = NSI_Act_State.INACTIVE;
                    this.setState(nextState);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case ACTIVE:
                if (event.equals(NSI_Act_Event.END_TIME)) {
                    nextState = NSI_Act_State.DEACTIVATING;
                    this.setState(nextState);
                } else if (event.equals(NSI_Act_Event.CLEANUP)) {
                    nextState = NSI_Act_State.DEACTIVATING;
                    this.setState(nextState);
                } else if (event.equals(NSI_Act_Event.END_TIME)) {
                    nextState = NSI_Act_State.DEACTIVATING;
                    this.setState(nextState);
                } else if (event.equals(NSI_Act_Event.RECEIVED_NSI_TERM_RQ)) {
                    nextState = NSI_Act_State.DEACTIVATING;
                    this.setState(nextState);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case DEACTIVATING:
                if (event.equals(NSI_Act_Event.LOCAL_DEACT_CONFIRMED)) {
                    nextState = NSI_Act_State.INACTIVE;
                    this.setState(nextState);
                } else if (event.equals(NSI_Act_Event.LOCAL_DEACT_FAILED)) {
                    nextState = NSI_Act_State.ACTIVE;
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
