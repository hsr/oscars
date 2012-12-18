package net.es.oscars.nsibridge.ifces;

/**
 * @haniotak Date: 2012-08-08
 */
public interface TransitionHandler {
    public void process(SM_State from, SM_State to, SM_Event ev, StateMachine sm) throws StateException;
}
