package net.es.oscars.resourceManager.dao;

import java.util.*;

import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.*;

import org.hibernate.*;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.PathType;
import net.es.oscars.resourceManager.beans.Reservation;
import net.es.oscars.resourceManager.beans.ConstraintType;
import net.es.oscars.resourceManager.common.RMCore;
import net.es.oscars.resourceManager.common.RMUtils;
import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.resourceManager.common.StateEngine;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import  net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.logging.ModuleName;


/**
 * ReservationDAO is the data access object for
 * the rm.reservations table.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class ReservationDAO
    extends GenericHibernateDAO<Reservation, Integer> {

    private Logger log;
    private String dbname;
    private List<Reservation> reservations;

    public ReservationDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
        this.reservations = new ArrayList<Reservation>();
    }

    /**
     * Finds a reservation, given its global reservation id.
     *
     * @param gri a string uniquely identifying a reservation across domains
     * @return reservation found, if any
     * @throws OSCARSServiceException
     */
    public Reservation query(String gri)
        throws OSCARSServiceException {

        Reservation reservation = this.queryByParam("globalReservationId", gri);
        if (reservation == null) {
            ErrorReport errRep = new ErrorReport(ErrorCodes.RESV_QUERY_FAILED,
                                              "No reservation matches requested gri: " + gri,
                                               ErrorReport.USER,
                                               gri,
                                               null,
                                               System.currentTimeMillis()/1000,
                                               ModuleName.RM,
                                               PathTools.getLocalDomainId());
            throw new OSCARSServiceException ( errRep);
        }
        return reservation;
    }

    /**
     * Updates a reservations and nulls out the entry for the reservation
     * in the StateEngine.StateMap, so that the map will be updated from the
     * reservation on its next use.
     * @param reservation to be persisted
     */
    public void update(Reservation reservation) {
        super.update(reservation);
        RMCore core = RMCore.getInstance();
        StateEngine stateEngine = core.getStateEngine();
        try {
            // set StateEngine map value to null, so it will be reinitialized from the persistent resv
            stateEngine.updateStatus(reservation, null,false);
        } catch (OSCARSServiceException ex){
            log.error("Should  never happen");
        }
    }
    /**
     * Lists reservations
     *
     * @param numRequested int with the number of reservations to return
     * @param resOffset int with the offset into the list
     *
     * @param logins a list of user logins. If not null or empty, results will
     * only include reservations submitted by these specific users. If null / empty
     * results will include reservations by all users.
     *
     * @param statuses a list of reservation statuses. If not null or empty,
     * results will only include reservations with one of these statuses.
     * If null / empty, results will include reservations with any status.
     *
     * @param description Description field of the reservation
     * @param vlanTags a list of VLAN tags.  If not null or empty,
     * results will only include reservations where (currently) the first hop
     * in the path has a VLAN tag from the list (or ranges in the list).  If
     * null / empty, results will include reservations with any associated
     * VLAN.
     *
     * @param startTime the start of the time window to look in; null for everything before the endTime
     *
     * @param endTime the end of the time window to look in; null for everything after the startTime,
     * leave both start and endTime null to disregard time
     *
     * @return a list of reservations.
     * @throws RMException
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> list(int numRequested, int resOffset,
            List<String> logins, List<String> statuses, String description,
            List<String> vlanTags, Long startTime, Long endTime)
                throws RMException{

        //log.debug("list.start");
        this.reservations = new ArrayList<Reservation>();
        ArrayList<String> criteria = new ArrayList<String>();
        String loginQ = null;
        if (logins != null && !logins.isEmpty()) {
            loginQ = "r.login IN ("+RMUtils.join(logins, ",", "'", "'")+") ";
            criteria.add(loginQ);
        }

        String statusQ = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusQ = "r.status IN ("+RMUtils.join(statuses, ",", "'", "'")+") ";
            criteria.add(statusQ);
        }

        String descriptionQ = null;
        if (description != null) {
            descriptionQ = " (r.description LIKE '%"+description+"%') or (r.globalReservationId LIKE '%"+description+"%') ";
            criteria.add(descriptionQ);
        }

        String startQ = null;
        if (startTime != null) {
            startQ = "(r.endTime >= :startTime) ";
            criteria.add(startQ);
        }
        String endQ = null;
        if (endTime != null) {
            endQ = "(r.startTime <= :endTime) ";
            criteria.add(endQ);
        }

        String hsql = "from Reservation r";
        if (!criteria.isEmpty()) {
            hsql += " where " +RMUtils.join(criteria, " and ", "(", ")");
        }
        hsql += " order by r.startTime desc";


       // log.debug("HSQL is: ["+hsql+"]");

        Query query = this.getSession().createQuery(hsql);
        // if zero, get everything (needed for browser)
        if (numRequested > 0) {
            //log.debug("numRequested: " + numRequested + " resOffset: "+ resOffset);
            query.setMaxResults(numRequested);
            query.setFirstResult(resOffset);
        }
        if (startTime != null) {
            query.setLong("startTime", startTime);
        }
        if (endTime != null) {
            query.setLong("endTime", endTime);
        }
        log.debug("query is " + query.getQueryString());
        this.reservations = query.list();
        log.debug(reservations.size() + " reservations returned");
        //log.debug("done with Hibernate query");

        if (vlanTags != null && !vlanTags.isEmpty() &&
            !vlanTags.contains("any")) {
            ArrayList<Reservation> removeThese = new ArrayList<Reservation>();
            for (Reservation rsv : this.reservations) {
                if (!this.containsVlan(rsv, vlanTags)) {
                    removeThese.add(rsv);
                }
            }
            for (Reservation rsv : removeThese) {
                this.reservations.remove(rsv);
                // log.debug("removing reservation");
            }
        }
        //log.debug("list.finish");
        return this.reservations;
    }


    /** 
     * Retrieves the list of all pending and active reservations that
     * are within the given time interval.
     *
     * @param startTime proposed reservation start time
     * @param endTime proposed reservation end time
     * @return list of all pending and active reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation>
            overlappingReservations(Long startTime, Long endTime) {

        this.reservations = null;

        ArrayList<String> states = new ArrayList<String>();
        states.add(StateEngineValues.RESERVED);
        states.add(StateEngineValues.PATHCALCULATED);
        states.add(StateEngineValues.INCOMMIT);
        states.add(StateEngineValues.COMMITTED);
        states.add(StateEngineValues.ACTIVE);
        states.add(StateEngineValues.INMODIFY);
        states.add(StateEngineValues.INSETUP);
        states.add(StateEngineValues.INTEARDOWN);
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = states.iterator();
        if (iter.hasNext()) {
            String status = "'"+iter.next()+"'";
            sb.append(status);
            while (iter.hasNext()) {
                status = "'"+iter.next()+"'";
                sb.append(",");
                sb.append(status);
            }
        }
        String stateClause = sb.toString();

        // Get reservations with times overlapping that of the reservation
        // request.
        String hsql = "from Reservation r " +
            "where ((r.startTime <= :startTime and r.endTime >= :startTime) or " +
            "(r.startTime <= :endTime and r.endTime >= :endTime) or " +
            "(r.startTime >= :startTime and r.endTime <= :endTime)) " +
            "and (r.status IN (" + stateClause + "))";
        this.reservations = this.getSession().createQuery(hsql)
                                        .setLong("startTime", startTime)
                                        .setLong("endTime", endTime)
                                        .list();
        return this.reservations;
    }

    /**
     * Finds RESERVED OSCARS reservations which now be scheduled for a pathSetup
     *
     * @param timeInterval to add to the current time
     * @return list of pending reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> pendingReservations(int timeInterval) {

        this.reservations = null;
        long curTime = System.currentTimeMillis()/1000;
        long lookAhead = curTime + timeInterval;
        String hsql = "from Reservation where status = :status " +
                      "and startTime < :startTime " +
                      "and endTime > :now " +
                      "order by startTime ";
        this.reservations = this.getSession().createQuery(hsql)
                              .setString("status", StateEngineValues.RESERVED)
                              .setLong("startTime", lookAhead)
                              .setLong("now", curTime)
                              .list();

        return this.reservations;
    }


    /**
     * Finds RESERVED OSCARS reservations that have or will expire
     * before current time plus the look ahead time.
     *
     * reservations that are expiring and are in states ACTIVE, INSETUP or INTEARDOWN
     * are caught earlier and scheduled for a teardown. When the teardown is complete,
     * the finish code will be executed.
     * Reservations that are expiring in a transition state should eventually be failed by
     * the Coordinator.
     *
     * @param lookAhead the amount of time to add to current time
     * @return list of expired reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> unfinishedExpiredReservations(int lookAhead) {

        this.reservations = null;
        long seconds = System.currentTimeMillis()/1000 + lookAhead;


        ArrayList<String> states = new ArrayList<String>();
        states.add(StateEngineValues.RESERVED);

        String hsql = "from Reservation where " +
                      " ( (status IN (:states)) AND " +
                      "   (endTime <= :endTime) )" +
                      " order by endTime";

        this.reservations = this.getSession().createQuery(hsql)
                              .setParameterList("states", states)
                              .setLong("endTime", seconds)
                              .list();
         
        return this.reservations;
    }

     /**
     * Finds current OSCARS reservations that will expire
     * during the specified time interval and have not already sent a notify message
     *
     * @param offset start time of the interval in milliseconds since 12AM, Jan 1, 1970.
     * @param timeInterval the length of the interval in millisecs to check.
     * @param exStatus indicates whether an expiration notice has already been sent
     * @return list of expired reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> futureExpiringReservations(long offset, long timeInterval, Integer exStatus) {

        this.reservations = null;
        long intervalStart = (System.currentTimeMillis() + offset)/1000;
        long intervalEnd = intervalStart + timeInterval/1000;
        log.debug("looking for reservation where endtime >= " + new Date(intervalStart*1000));
        log.debug((" and endtime <= " + new Date(intervalEnd*1000)));

        String hsql = "from Reservation where " +
                      " ( (localStatus != :states) AND " +
                      "   (endTime >= :intervalStart) AND " +
                      "   (endTime <= :intervalEnd) )" +
                      " order by endTime";

        this.reservations = this.getSession().createQuery(hsql)
                              .setInteger("states", exStatus)
                              .setLong("intervalStart", intervalStart)
                              .setLong("intervalEnd", intervalEnd)
                              .list();

        return this.reservations;
    }
    /**
     * Finds current OSCARS reservations which now should be scheduled to be torn down.
     *
     * @param timeInterval The interval in which to look for expiring reservations
     * @return list of active reservations which will reach their end-time  within the specified interval
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> activeExpiringReservations(int timeInterval) {

        this.reservations = null;
        long seconds = 0;

        seconds = System.currentTimeMillis()/1000 + timeInterval;
        ArrayList<String> states = new ArrayList<String>();
        states.add(StateEngineValues.ACTIVE);

        String hsql = "from Reservation where " +
                      "status IN (:states) and " +
                      "endTime <= :endTime";
        this.reservations = this.getSession().createQuery(hsql)
                              .setParameterList("states", states)
                              .setLong("endTime", seconds)
                              .list();
        return this.reservations;
    }

    /**
     * Retrieves a list of all reservations with the given status.
     *
     * @param status string with reservation status
     * @return list of all reservations with the given status
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> statusReservations(String status) {

        this.reservations = null;
        // Get reservations with times overlapping that of the reservationi
        // request.
        String hsql = "from Reservation r " +
                      "where r.status = :status";
        this.reservations = this.getSession().createQuery(hsql)
                                        .setString("status", status)
                                        .list();
        return this.reservations;
    }



    /**
     * This function is meant to be called after a list() and should
     * return the number of reservations fitting the search criteria.
     *
     * @return how many reservations are in the result set
     *
     */
    public int resultsNum() {
        if (this.reservations == null) {
            return 0;
        }
        return this.reservations.size();
    }


    /**
     * Retrieves reservation given the global reservation ID (GRI) and login
     *
     * @param gri global reservation id of entry to retrieve
     * @param login user's login that made the reservation
     * @return the reservation matching the given parameters
     */
    public Reservation queryByGRIAndLogin(String gri, String login){
        String hsql = "from Reservation r " +
                      "where r.globalReservationId = ? and r.login = ?";
        return (Reservation) this.getSession().createQuery(hsql)
                                        .setString(0, gri)
                                        .setString(1, login)
                                        .uniqueResult();
    }

    /**
     * Checks to see whether first element in path contains a VLAN in the
     * list of tags or ranges specified.
     *
     * @param rsv Reservation to check
     * @param vlanTags list of tags or tag ranges to check
     * @throws RMException
     * @return whether first element in path has a matching VLAN
     */
    private boolean containsVlan(Reservation rsv, List<String> vlanTags) throws RMException  {
        int checkVtag = -1;
        int minVtag = 100000;
        int maxVtag = -1;
        List<String> tagStrs = RMUtils.getVlanTags(rsv.getPath());
        if (tagStrs.isEmpty()) {
            return false;
        }
        String tagStr = tagStrs.get(0);
        // no associated VLAN
        if (tagStr == null || tagStr.equals("any") || tagStr.equals("")) {
            log.debug ("first vlanTag is empty");
            return false;
        }
        int resvVtag = Math.abs(Integer.parseInt(tagStr));
        for (String v: vlanTags) {
            String[] range = v.split("-");
            // single number
            if (range.length == 1) {
                try {
                    checkVtag = Integer.parseInt(range[0]);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (checkVtag == resvVtag) {
                    return true;
                }
            } else if (range.length == 2) {
                try {
                    minVtag = Integer.parseInt(range[0]);
                    maxVtag = Integer.parseInt(range[1]);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if ((resvVtag >= minVtag) && (resvVtag <= maxVtag)) {
                    return true;
                }
            }
        }
        return false;
    }
}
