package net.es.oscars.pss.api;

import java.util.List;

import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

/**
 * Interface for workflow agents
 *
 * @author haniotak
 *
 */
public interface Workflow {
    public void setConfig(GenericConfig config) throws PSSException;

    /**
     * Add a new action to the workflow queue
     * @param action
     * @throws PSSException
     */
    public void add(PSSAction action) throws PSSException;

    /**
     * Get the next action that needs to be handled
     * Sets it as running and advances the workflow
     * May be called multiple times by handlers, each
     * time should return the next action
     *
     * @return the request operation, null if none
     */
    public PSSAction next();

    /**
     * tell the workflow agent that an action has updated its status
     *
     * @param action
     */
    public void update(PSSAction action);

    /**
     * tell the workflow agent to process all the following actions
     *
     * @param action
     */
    public void process(List<PSSAction> actions) throws PSSException ;


    /**
     * get all completed actions
     */
    public List<PSSAction> getCompleted();

    /**
     * remove an action from the workflow
     *
     * Attempting to remove a running action must throw an exception
     *
     * @param action
     * @throws PSSException
     */
    public void remove(PSSAction action) throws PSSException;

    /**
     * return true if there are any outstanding jobs
     * @return
     */
    public boolean hasOutstanding();
    
    /**
     * return the outstanding actions 
     * @return
     */
    public List<PSSAction> getOutstanding();
}
