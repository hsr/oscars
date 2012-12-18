package net.es.oscars.resourceManager.common;

import java.util.*;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.beans.Reservation;
import net.es.oscars.resourceManager.dao.ReservationDAO;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.soap.OSCARSServiceException;
import org.apache.log4j.Logger;
import org.hibernate.Session;

public class StateEngine {

    /* allowed transitions
     *  initial state = ACCEPTED - in CreateReservation if the reservation request is valid, and authorized a gri is assigned 
     *     reservation is initially stored with status ACCEPTED. Otherwise nothing is stored and CreateResevation
     *     returns an error.
     * ACCEPTED -> INPATHCALCULATION, CANCELLED
     * INPATHCALCULATION -> PATHCACULATED, INCOMMIT, FAILED
     * PATHCALCULATED -> INCOMMIT, INMODIFY, INCANCEL
     * INCOMMIT -> COMMITTED, RESERVED, FAILED
     * COMMITTED -> RESERVED, FAILED
     * MODCOMMITTED -> RESERVED, ACTIVE, FAILED
     * RESERVED -> INSETUP, INMODIFY, INCANCEL  FAILED
     * INSETUP -> ACTIVE, FAILED
     * ACTIVE -> INTEARDOWN, INMODIFY, INCANCEL. FAILED
     * INTEARDOWN -> RESERVED, FINISHED, FAILED
     * INCANCEL ->  CANCELLED, FAILED
     * INMODIFY -> MODCOMMITTED, RESERVED, ACTIVE
     * final states: FAILED, FINISHED, CANCELLED
     */

 

    private String dbname;
    private static HashMap<String, String> statusMap = new HashMap<String, String>();
    private static HashMap<String, Integer> localStatusMap = new HashMap<String, Integer>();
    private Logger log;

    public StateEngine(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /**
     *  Check to see if the state transition is allowed and if changes it to the new status value
     * @param resv
     * @param newStatus
     * @param isFailure If this is a failure case, any transition is allowed
     *        e.g inmodify -> previous status if modify failed, also used by forceUpdateStatus
     * @return The new status. It will be newStatus or FINISHED if the end time has been reached and
     *         newStatus is RESERVED
     * @throws RMException from canModifyStatus if the state transition is not allowed
     */

    public synchronized String updateStatus(Reservation resv, String newStatus, Boolean isFailure) throws OSCARSServiceException {
        
        String gri = resv.getGlobalReservationId();
        String event = "updateStatus";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        
        // set value to null to cause a re-initialization on next call.
        // Used when status is reset by a RMStore action
        if (newStatus == null)  {
            StateEngine.statusMap.put(gri,null);
            return null;
        }

        String status = this.getStatus(resv);
        
        log.debug(netLogger.start(event,"request state change from " + status + " to newStatus " + newStatus));
        // at the end of a teardown see if endtime has been reached
        if (newStatus.equals(StateEngineValues.RESERVED)){
            // check to see if end time has been reached
            if (resv.getEndTime() <= System.currentTimeMillis()/1000) {
                newStatus = StateEngineValues.FINISHED;
            }
        }

        if (newStatus == StateEngineValues.FINISHED && ! isFailure) {
            this.doFinish(resv, status);
            newStatus = resv.getStatus();  //may now be FAILED
        }
        else {
            // throws an OSCARSServiceException if transition is not allowed
            if (! isFailure) { // accept anything
                newStatus = StateEngine.canModifyStatus(status, newStatus);
            }
        }
        
        log.debug(netLogger.end(event, "changing state from " + status + " to newStatus " + newStatus));
        status = newStatus;
        resv.setStatus(status);
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        resvDAO.update(resv);
        StateEngine.statusMap.put(gri, status);
        return status;
    }

/* This is intentionally not synchronized, we do not want to block when reading the status
    public static void canUpdateStatus(Reservation resv, String newStatus) throws RMException {
        String status = StateEngine.getStatus(resv);
        StateEngine.canModifyStatus(status, newStatus);
    }
*/
    
//  Business / state diagram logic goes here
    public static String canModifyStatus(String status, String newStatus) throws  OSCARSServiceException {
        boolean allowed = false;
        String retStatus = newStatus;
        if (newStatus.equals(status)) {
            // no-ops always allowed
            allowed = true;
        } else if (newStatus.equals("SUBMITTED")) {
            throw new OSCARSServiceException(ErrorCodes.INVALID_PARAM,
                                             "SUBMITTED is no longer a valid state value. It is replaced by " +
                                              StateEngineValues.ACCEPTED,
                                              ErrorReport.SYSTEM);
        } else if (newStatus.equals(StateEngineValues.ACCEPTED)) {
            // always allowed, must not abuse..
             allowed = true;
        } else if (newStatus.equals(StateEngineValues.FAILED)) {
            // always allowed, must not abuse..
             allowed = true;
        } else if (newStatus.equals(StateEngineValues.INPATHCALCULATION )) {
            if (status.equals(StateEngineValues.ACCEPTED)) {
                allowed = true;
            }
        } else if (newStatus.equals(StateEngineValues.PATHCALCULATED)) {
            if (//status.equals(StateEngineValues.INMODIFY)  ||
                    status.equals(StateEngineValues.INPATHCALCULATION )) {
                allowed = true;
            }
        } else if (newStatus.equals(StateEngineValues.INCOMMIT)) {
            if (status.equals(StateEngineValues.PATHCALCULATED) ||
                    status.equals(StateEngineValues.INPATHCALCULATION) ||
                    status.equals(StateEngineValues.INMODIFY)){
                allowed = true;
            } 
        } else if (newStatus.equals(StateEngineValues.COMMITTED)) { 
            if (status.equals(StateEngineValues.INCOMMIT )) {
                allowed = true;
            }
        } else if (newStatus.equals(StateEngineValues.RESERVED)) {
            if (status.equals(StateEngineValues.COMMITTED ) ||
                    status.equals(StateEngineValues.INCOMMIT) ||
                    status.equals(StateEngineValues.INMODIFY) ||
                    status.equals(StateEngineValues.MODCOMMITTED) ||
                    status.equals(StateEngineValues.INTEARDOWN)) {
                allowed = true;
            }
        } else if (newStatus.equals(StateEngineValues.INMODIFY)) {
            if (status.equals(StateEngineValues.RESERVED) ||
                    status.equals(StateEngineValues.ACTIVE) ||
                    status.equals(StateEngineValues.MODCOMMITTED)) {
                allowed = true;
            }
        } else if ((newStatus.equals(StateEngineValues.MODCOMMITTED))) {
            if (status.equals(StateEngineValues.INMODIFY)){
                allowed=true;
            }
        } else if (newStatus.equals(StateEngineValues.INSETUP)) {
            if (status.equals(StateEngineValues.RESERVED)) {
                allowed = true;
            }
        } else if (newStatus.equals(StateEngineValues.ACTIVE)) {
            if (status.equals(StateEngineValues.INSETUP) ||
                    status.equals(StateEngineValues.INMODIFY)||
                    status.equals(StateEngineValues.MODCOMMITTED)) {
                allowed = true;
            }
        } else if (newStatus.equals(StateEngineValues.INTEARDOWN)) {
            if (status.equals(StateEngineValues.ACTIVE) ||
                    status.equals(StateEngineValues.FAILED)) { // may have timed out while in a state that requires a teardown
              allowed = true;
            }
        } else if (newStatus.equals(StateEngineValues.INCANCEL)) {
            if (status.equals(StateEngineValues.ACTIVE) ||
                    status.endsWith(StateEngineValues.ACCEPTED) ||
                    status.equals(StateEngineValues.RESERVED)) {
                allowed = true;
            }
        } else if (newStatus.equals(StateEngineValues.CANCELLED)) {
            if (status.equals(StateEngineValues.ACCEPTED) ||
                    status.equals(StateEngineValues.INCANCEL)) {
                allowed = true;
            }
        }
        if (!allowed) {
            throw new OSCARSServiceException(ErrorCodes.RESV_STATE_ERROR,
                                             "Current status is "+status+"; cannot change to "+newStatus,
                                               ErrorReport.SYSTEM);
        }
        return retStatus;
    }
    /**
     * set the state for an expired reservation
     * @param resv The reservtion that is expiring
     * @param currentStatus the current status of the reservation
     */
    private void doFinish(Reservation resv, String currentStatus){
        if (currentStatus.equals(StateEngineValues.CANCELLED) ||
             currentStatus.equals(StateEngineValues.FAILED) ||
             currentStatus.equals(StateEngineValues.FINISHED)){
            // do nothing
        } else if (currentStatus.equals(StateEngineValues.RESERVED) ||
                currentStatus.equals(StateEngineValues.INTEARDOWN)) {
            resv.setStatus(StateEngineValues.FINISHED);
        } else if (currentStatus.equals(StateEngineValues.ACTIVE)) {
            /* teardown should have been scheduled, maybe we need a new statevalue 
             * for teardown scheduled, as INTEARDOWN is only set when the coord responds
             * For now just ignore this case.
             */
        } else {
            resv.setStatus(StateEngineValues.FAILED);
            String description = resv.getDescription();
            String newDescription = description + 
                   " *** FAILED when endtime was reached when the reservation was in state: " + currentStatus;
            resv.setDescription(newDescription);
        }
    }
    /**
     * @return the status
     */
    public static String getStatus(Reservation resv) {
        String gri = resv.getGlobalReservationId();
        String status = null;
        // if the state engine has not been initialized, return what is in the Reservation object
        if (StateEngine.statusMap.get(gri) != null) {
            status = StateEngine.statusMap.get(gri);
        } else {
            status = resv.getStatus();
            StateEngine.statusMap.put(gri,status);
        }

        return status;
    }

    /**
     * @return the localStatus
     */
    public static Integer getLocalStatus(Reservation resv) {
        String gri = resv.getGlobalReservationId();
        Integer status = null;
        // if the state engine has not been initialized, return what is in the Reservation object
        if (StateEngine.localStatusMap.get(gri) != null) {
            status = StateEngine.localStatusMap.get(gri);
        } else {
            status = resv.getLocalStatus();
        }

        return status;
    }

    /**
     * Utility function for verifying the status is up-to-date before committing a 
     * Hibernate session. The statusMap is synchronized so will always be the most 
     * accurate representation of the status. During interdomain path setup this call should 
     * ALWAYS be used to commit the transaction. There is a time between when a path 
     * receives a notification and makes a change where a race condition exists without this.
     * 
     * @param reservations the reservations to save
     * @param session the Hibernate session to commit
    */
public synchronized void safeHibernateCommit(List<Reservation> reservations,
        Session session) {
    for(Reservation resv : reservations){
        //sets the reservation status to the value of the status map
        resv.setStatus(StateEngine.getStatus(resv));
        //sets the reservation local status to the value of the local status map
        resv.setLocalStatus(StateEngine.getLocalStatus(resv));
    }
    session.getTransaction().commit();
}

/**
     * Convenience method for calling safeHibernateCommit(List&lt;Reservation&gt; reservations,
     * Session bss) with only one reservation.
     * 
     * @param resv the reservations to save
     * @param session the Hibernate session to commit
     */
public synchronized void safeHibernateCommit(Reservation resv, Session session){
    List<Reservation> reservations = new ArrayList<Reservation>();
    reservations.add(resv);
    this.safeHibernateCommit(reservations, session);
}
 
}
