/**
 * 
 */
package net.es.oscars.resourceManager.common;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.beans.ConstraintType;
import net.es.oscars.resourceManager.beans.Reservation;
import net.es.oscars.resourceManager.dao.ReservationDAO;
import net.es.oscars.resourceManager.scheduler.RMReservationScheduler;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.resourceManager.beans.Path;

import org.apache.log4j.Logger;

/**
 * @author mrt, lomax
 *
 */
public class ScanReservations {
     private RMCore core;
     private static ScanReservations instance = null;
     private int scanInterval;
     private int lookAhead;
     private long lastScanned;
     private long lastDailyScan = 0;
     private long lastWeeklyScan = 0;
     private long lastMonthlyScan = 0;
     private static Logger LOG = Logger.getLogger(ScanReservations.class.getName());
     private String dbname;
     private static final long day = 24*3600*1000;  // milliseconds
     private static final long week = 7*day;
     private static final long month = 30*day;

     /**
      * Constructor
      * @param dbname if not null is the test database name
      *        if null use the dbname in the RMCore
      */
     private ScanReservations(String dbname){
         //LOG.debug("constructor called with dbname " + dbname);
         this.core = RMCore.getInstance();
         if (dbname == null) {
             this.core = RMCore.getInstance();
             this.dbname = core.getDbname();
         } else {
             //LOG.debug("setting this.dbname to " + dbname);
             this.dbname = dbname;
         }
         this.scanInterval = core.getScanInterval();
         this.lookAhead = core.getLookAhead();
         // avoid an immediate scan on startup  - delay 1/10 of the scanInterval
         this.lastScanned = System.currentTimeMillis() - this.scanInterval * 100;
     }
     
     /**
      * return singleton ScanReservations
      * @param dbname if not null is the test database name
      *        if null use the dbname in the RMCore
      * @return
      */
     public static ScanReservations getInstance(String dbname) {
         //LOG.debug("getInstance called with dbname " + dbname);
         if (ScanReservations.instance == null) {
             ScanReservations.instance = new ScanReservations(dbname);
         }
         return instance;
     }
     
     public long getLastScanned() {
         return lastScanned;
     }
     /** 
      * scheduled to run at RMCore.scanInterval to look for timer-automatic path setup and teardown
      * and expired reservations that need to have their status updated. It will only be called from the
      * scheduler if currentTime > lastScanned + scanInterval.
      * Also called by ResourceManager.store when a RESERVED reservation is stored
      * TODO looks like modifyReservation is not handled yet.
      */
     public void scan () {
         String event = "scanReservations";
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         LOG.debug(netLogger.start(event));
         synchronized (RMReservationScheduler.schedLock) {
             this.lastScanned = System.currentTimeMillis();
             RMReservationScheduler.schedLock =  "scanning";
             ReservationDAO resDAO = new ReservationDAO(this.dbname);
             ResourceManager manager = new ResourceManager();
             Session session = this.core.getSession();

             session.beginTransaction();
             RMReservationScheduler rmSched = RMReservationScheduler.getInstance();
             List<String> griList = new ArrayList<String>();
             try {
                 /* scan for timer-automatic reservations that should start in the lookAhead interval
                  * and schedule a setup call
                  */
                 List<Reservation> setUpResList = resDAO.pendingReservations(lookAhead);

                 for (Reservation res: setUpResList) {
                     if (res.getConstraint(ConstraintType.USER).getPath().getPathSetupMode().equals(Path.MODE_AUTO)) {
                         ResDetails resDetails = RMUtils.res2resDetails(res,true);  // internalHopAllowed
                         if (resDetails.getReservedConstraint() == null) {
                             LOG.warn(netLogger.error(event,ErrSev.MINOR,"skipping reservation " + 
                                                      resDetails.getGlobalReservationId()  +
                                                      ": status is RESERVED but no reservedConstraint"));
                             continue;
                         }
                         LOG.debug(netLogger.getMsg(event, "scheduling setup for " + resDetails.getGlobalReservationId()));
                         griList.add(resDetails.getGlobalReservationId());
                         rmSched.scheduleSetup(resDetails);
                     }
                 }

                 /* scan for reservations that are ACTIVE  and will expire in the lookAhead interval
                  * and schedule a teardown call
                  */
                 List<Reservation> tearDownRes = resDAO.activeExpiringReservations(lookAhead);
                 if ( tearDownRes.size() > 0) {
                    //LOG.debug(netLogger.getMsg(event,"found "  + tearDownRes.size() + " tear down reservations"));
                 }
                 for ( Reservation res: tearDownRes) {
                     // tear it down even if it is signal-xml
                     ResDetails resDetails = RMUtils.res2resDetails(res,true);  // internalHopAllowed
                     if (resDetails.getReservedConstraint() == null) {
                         LOG.warn(netLogger.getMsg(event,"" +
                                                   " skipping reservation " + resDetails.getGlobalReservationId()  +
                                                   ": status is RESERVED but no reservedConstraint")); 
                         continue;
                     }
                     LOG.debug("scheduling teardown for " + resDetails.getGlobalReservationId());
                     rmSched.scheduleTeardown(resDetails);
                     griList.add(resDetails.getGlobalReservationId());
                 }

                 /* scan for all reservations that are expired where STATE is RESERVED
                  * and schedule a finish operation
                  */
                 List<Reservation> finishRes = resDAO.unfinishedExpiredReservations(scanInterval);
                 if (finishRes.size() > 0){
                    //LOG.debug(netLogger.getMsg(event, "found "  + finishRes.size() + " unfinished expired reservations"));
                 }
                 for ( Reservation res: finishRes) {
                   //don't schedule same reservation for both setup or teardown and finish
                     if (!griList.contains(res.getGlobalReservationId())) {
                         ResDetails resDetails = RMUtils.res2resDetails(res,true);  // internalHopAllowed
                         LOG.debug("scheduling finish for " + resDetails.getGlobalReservationId());
                         rmSched.scheduleFinish(resDetails);
                     }
                 }
                 if (this.lastDailyScan < System.currentTimeMillis() - day) {
                     /* scan for reservations expiring in the next 24hrs. */
                     List<Reservation> expiringRes = resDAO.futureExpiringReservations(day, day, RMUtils.notice1DaySent);
                     if (expiringRes.size() > 0 ){
                        LOG.debug(netLogger.getMsg(event, "found " + expiringRes.size() + " reservations expiring in 1 day"));
                     }
                     for (Reservation res : expiringRes) {
                         try {
                            RMUtils.notify(NotifyRequestTypes.RESERVATION_EXPIRES_1DAY, res);
                             res.setLocalStatus(RMUtils.notice1DaySent);
                             resDAO.update(res);
                         } catch (Exception ex) {
                             LOG.error(netLogger.error(event,ErrSev.MINOR,"Failed to send 1 day expiration notice"));
                         }
                     }
                     this.lastDailyScan = System.currentTimeMillis();
                 }

                 if (this.lastWeeklyScan < System.currentTimeMillis() - week) {
                     /* scan for any reservations expiring in the next 7 days. */
                     List<Reservation> expiringRes = resDAO.futureExpiringReservations(week, day, RMUtils.notice7DaySent);
                     if (expiringRes.size() > 0 ){
                        LOG.debug(netLogger.getMsg(event, "found " + expiringRes.size() + " reservations expiring in 7 days"));
                     }
                     for (Reservation res : expiringRes) {
                         try {
                            RMUtils.notify(NotifyRequestTypes.RESERVATION_EXPIRES_7DAYS, res);
                            res.setLocalStatus(RMUtils.notice7DaySent);
                            resDAO.update(res);
                         }catch (Exception ex) {
                             LOG.error(netLogger.error(event,ErrSev.MINOR,"Failed to send 7 day expiration notice"));
                         }
                     }
                     this.lastWeeklyScan = System.currentTimeMillis();
                 }

                 if (this.lastMonthlyScan < System.currentTimeMillis() - month) {
                     /* scan for reservations expiring in the next 30 days. */
                     List<Reservation> expiringRes = resDAO.futureExpiringReservations(month, day, RMUtils.notice30DaySent);
                     if (expiringRes.size() > 0 ){
                        LOG.debug(netLogger.getMsg(event, "found " + expiringRes.size() + " reservations expiring in 30 days"));
                     }
                     for (Reservation res : expiringRes) {
                         try {
                            RMUtils.notify(NotifyRequestTypes.RESERVATION_EXPIRES_30DAYS, res);
                            res.setLocalStatus(RMUtils.notice30DaySent);
                            resDAO.update(res);
                         } catch (Exception ex) {
                              LOG.error(netLogger.error(event,ErrSev.MINOR,"Failed to send 30 day expiration notice"));
                         }
                     }
                     this.lastMonthlyScan = System.currentTimeMillis();
                 }
             } catch (RMException ex) {
                 // Found an invalid reservation in table: no user constraint or no reserved constraint
                 LOG.warn(netLogger.error(event, ErrSev.MINOR,"Exception: " + ex.getMessage()));
             }
             session.getTransaction().commit();
             //RMReservationScheduler.schedLock.unlock();
             RMReservationScheduler.schedLock = "unlocked";
             LOG.debug(netLogger.end(event));
         }
     }
}
