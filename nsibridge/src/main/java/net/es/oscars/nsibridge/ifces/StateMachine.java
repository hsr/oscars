package net.es.oscars.nsibridge.ifces;

/**
 * @haniotak Date: 2012-08-08
 */
public interface StateMachine {

    public void process(SM_Event ev) throws StateException;

    public void setTransitionHandler(TransitionHandler th);
    public TransitionHandler getTransitionHandler();
    public SM_State getState();


}

