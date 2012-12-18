package net.es.oscars.nsibridge.state.term;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_State;
import org.apache.log4j.Logger;

public class NSI_Term_SM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(NSI_Term_SM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public NSI_Term_SM(String id) {
        this.state = NSI_Term_State.INITIAL;
        this.id = id;
    }

    @Override
    public void process(SM_Event event) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        NSI_Term_State prevState = (NSI_Term_State) this.getState();
        NSI_Term_State nextState = null;
        String pre = "PRE: PSM ["+this.getId()+"] at state ["+state+"] got event ["+event+"]";
        LOG.debug(pre);
        String error = pre;



        switch (prevState) {
            case INITIAL:
                if (event.equals(NSI_Term_Event.CLEANUP)) {
                    nextState = NSI_Term_State.TERMINATING;
                    this.setState(nextState);
                } else if (event.equals(NSI_Term_Event.END_TIME)) {
                    nextState = NSI_Term_State.TERMINATING;
                    this.setState(nextState);
                } else if (event.equals(NSI_Term_Event.RECEIVED_NSI_TERM_RQ)) {
                    nextState = NSI_Term_State.TERMINATING;
                    this.setState(nextState);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case TERMINATING:
                if (event.equals(NSI_Term_Event.LOCAL_TERM_CONFIRMED)) {
                    nextState = NSI_Term_State.TERMINATED;
                    this.setState(nextState);
                } else if (event.equals(NSI_Term_Event.LOCAL_TERM_FAILED)) {
                    nextState = NSI_Term_State.TERMINATE_FAILED;
                    this.setState(nextState);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case TERMINATED:
                error = pre + " : error : event ["+event+"] not allowed";
                LOG.error(error);
                throw new StateException(error);
            case TERMINATE_FAILED:
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
