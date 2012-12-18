package net.es.oscars.resourceManager.common;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.es.oscars.resourceManager.beans.*;
import net.es.oscars.utils.soap.ErrorReport;
import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.OptionalConstraintType;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.dao.IdSequenceDAO;
import net.es.oscars.resourceManager.dao.ErrorReportDAO;
import net.es.oscars.resourceManager.dao.ReservationDAO;
import net.es.oscars.resourceManager.http.WSDLTypeConverter;
import net.es.oscars.resourceManager.scheduler.RMReservationScheduler;
import net.es.oscars.resourceManager.scheduler.ReservationScheduler;
import org.hibernate.Session;

import static net.es.oscars.resourceManager.common.RMUtils.res2resDetails;


/*
*  This the methods in this class are called by RMSoapHandler which begins and commits
*  the hibernate transactions. The object is stored in RMCore and used as a singleton
*/
public class ResourceManager {
    private Logger log;
    private StateEngine stateEngine;
    private String dbname;
    private RMCore core;
    private String localDomainName;
    private String moduleName = ModuleName.RM;

    /**
     * Normal constructor
     * Only called by RMCore.getResourceManager which saves the object 
     * thus making this a singleton class
     * RMCore initializes dbname from resourceManager.yaml
     */
    public ResourceManager() {
        this.log = Logger.getLogger(this.getClass());
        this.core = RMCore.getInstance();
        this.dbname = core.getDbname();
        this.localDomainName = core.getLocalDomainId();
        this.stateEngine = core.getStateEngine();
    }
    /**
     * Constructor used by unit testing to override dbname to testrm
     * May not be necessary since tests are now configured to use in-memory sql
     * @param dbname  Data base bane
    */
    
    public ResourceManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.core = RMCore.getInstance(dbname);
        this.dbname = dbname;
        this.localDomainName = core.getLocalDomainId();
        this.stateEngine = core.getStateEngine();
    }

    /**
     * Generates the next Global Resource Identifier, created from the local
     *  Domain's topology identifier and the next unused index in the IdSequence table.
     *  
     * @return a new GlobalReservationId
     * @throws OSCARSServiceException
     */
    public String generateGRI() throws OSCARSServiceException {
        
        String gri = null;
        IdSequenceDAO idDAO = new IdSequenceDAO(this.dbname);

        if (this.localDomainName == null) {
            throw new OSCARSServiceException (ErrorCodes.RESV_CREATE_FAILED, "no domain name configured ",
                                              ErrorReport.SYSTEM);
        }
        try {
            int id = idDAO.getNewId();
            gri = this.localDomainName + "-" + id;
        } catch (RMException rmEx) {
            throw new OSCARSServiceException( ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
        }
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.end("mgrGenerateGRI",gri));

        return gri;
    }
    
    /**
     * updateStatus
     *
     * @param gri GlobalReservationId of the reservation to update
     * @param  inStatus new status string.
     * @param isFailure true if reservation failed in coordinator
     * @param errorReport if inStatus is FAILED, should be the reason for the failure
     * @return the newStatus of the reservation, might be different from inStatus, e.g. FINISHED vs RESERVED
     * @throws OSCARSServiceException if reservation is not found or state transition is not allowed
     */
    public String updateStatus(String gri, String inStatus, Boolean isFailure, ErrorReport errorReport)
            throws OSCARSServiceException {
        String outStatus = inStatus;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start("updateStatus"));
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Reservation res = resvDAO.query(gri);
        //this.log.debug(netLogger.getMsg("updateStatus", "found reservation"));
        if (isFailure){
            if (errorReport != null) {
                this.log.debug(netLogger.getMsg("updateStatusFailed", errorReport.toString()));
                // add the error Report to the reservation
                ErrorReportData errData = new ErrorReportData(errorReport.getErrorCode(),
                                                              errorReport.getErrorMsg(),
                                                              errorReport.getErrorType(),
                                                              errorReport.getGRI(),
                                                              errorReport.getTransId(),
                                                              errorReport.getTimestamp(),
                                                              errorReport.getModuleName(),
                                                              errorReport.getDomainId() );
                res.addErrorReport(errData);
            } else {
                this.log.debug(netLogger.getMsg("updateFailureStatus", "missing errorReportData"));
            }
        }

        outStatus = this.stateEngine.updateStatus(res, inStatus, isFailure);

        if (outStatus.equals(StateEngineValues.FINISHED) &&
                res.getLocalStatus() != RMUtils.expiredSent )   {
            RMUtils.notify(NotifyRequestTypes.RESERVATION_PERIOD_FINISHED, res);
        }

        // see if any setup or teardown actions should be scheduled
        try {
            ResDetails resDetails = res2resDetails(res,true);
            this.schedulePathAction(resDetails,outStatus,
                                    res.getConstraint(ConstraintType.USER).getPath().getPathSetupMode());
         } catch (RMException RMex) {
                // shouldn't happen
                throw new OSCARSServiceException(ErrorCodes.RESV_DATABASE_ERROR, RMex.getMessage(),
                                                 ErrorReport.SYSTEM );
         }
         this.log.debug(netLogger.end("updateStatus"));
         return outStatus;
    }

    /**
     * Check to see if any pathActions should be scheduled or removed and if so, calls RMScheduler
     * to doit.
     * @param resDetails  reservation details
     * @param outStatus   new status for reservation
     * @param pathSetupMode  Path.MODE_AUTO or Path MODE.SIGNAL
     */
    private void schedulePathAction(ResDetails resDetails, String outStatus, String pathSetupMode) {
        RMReservationScheduler scheduler = RMReservationScheduler.getInstance();
        if (scheduler != null) {
            // get data updated to database before RMSchedule looks for it.
            Session session = this.core.getSession();
            session.flush();
            if (outStatus.equals(StateEngineValues.RESERVED)) {
                if (pathSetupMode.equals(Path.MODE_AUTO)) {
                    // see if setup should be scheduled
                    scheduler.scheduleSetup(resDetails);
                } else {
                     // see if a teardown has been scheduled for MODE_SIGNAL and should be deleted
                    scheduler.forget(resDetails);
                }
            }
            if (outStatus.equals(StateEngineValues.ACTIVE)){
                    scheduler.scheduleTeardown(resDetails);
            }
        }
    }
    /**
     * getStatus
     *
     * @param gri GlobalReservationId of the reservation for which to return status
     * @return the status of the reservation
     * @throws OSCARSServiceException if reservation is not found
     */
    public String getStatus(String gri) throws OSCARSServiceException {

        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Reservation res = resvDAO.query(gri);
        return res.getStatus();
    }
    /**
     * store Stores (new) or updates (existing) a reservation
     * 
     *
     * @param resDetails  Reservation details to be stored
     * @throws OSCARSServiceException
     */
    public void store(ResDetails resDetails) throws OSCARSServiceException {
        String event = "mgrStoreReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start(event));
        String gri = resDetails.getGlobalReservationId();
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Reservation res = null;
        boolean newRes = false;
        try {
            res = resvDAO.query(gri);
        } catch (OSCARSServiceException ex ) {
            res = new Reservation();
            newRes = true;
        }
        try {
            if (newRes == false) {
                this.log.debug(netLogger.getMsg(event,"Request to change status from " + res.getStatus() + 
                              " to " + resDetails.getStatus()));
                try {
                    String newStatus = StateEngine.canModifyStatus(res.getStatus(),resDetails.getStatus());
                    this.log.debug(netLogger.getMsg(event,"Changing to status to " + newStatus));
                    resDetails.setStatus(newStatus);
                    update(res, resDetails);
                } catch (OSCARSServiceException rmEx){ // Change error code from STATE_FAILED
                    throw new OSCARSServiceException(ErrorCodes.RESV_STORE_FAILED,
                                                     rmEx.getMessage(), ErrorReport.SYSTEM);
                }
            } else { // storing reservation for first time
                StdConstraint constraint = null;
                Map <String,StdConstraint> conMap = res.getConstraintMap();

                res.setGlobalReservationId(resDetails.getGlobalReservationId());
                res.setLogin(resDetails.getLogin());
                res.setCreatedTime(resDetails.getCreateTime());
                res.setDescription(resDetails.getDescription());
                res.setStatus(resDetails.getStatus());
                res.setLocalStatus(0);

                UserRequestConstraintType userConstraint = resDetails.getUserRequestConstraint();
                if (userConstraint == null){
                    throw new OSCARSServiceException(ErrorCodes.RESV_STORE_FAILED,
                                                     "null UserConstraint not allowed", ErrorReport.USER);
                }
                constraint = WSDLTypeConverter.userRequest2StdConstraint(userConstraint);
                conMap.put(ConstraintType.USER,constraint);
                Long mbps = userConstraint.getBandwidth() * 1000000L; 
                res.setBandwidth(mbps);
                res.setStartTime(userConstraint.getStartTime());
                res.setEndTime(userConstraint.getEndTime());

                ReservedConstraintType reservedConstraint = resDetails.getReservedConstraint();
                if (reservedConstraint != null){
                    constraint = WSDLTypeConverter.reserved2StdConstraint(reservedConstraint);
                    conMap.put(ConstraintType.RESERVED,constraint);
                }
                res.setConstraintMap(conMap);
                /* Assume that on the initial store, a reservedConstraint will not have different
                 * times or bandwidth than the userConstraint
                 */

                List<OptConstraint> optConstraintList = new ArrayList<OptConstraint>();

                List <OptionalConstraintType> oclist = resDetails.getOptionalConstraint();
                if (oclist != null && !oclist.isEmpty()) {
                    for (OptionalConstraintType oc: oclist) {
                    	OptConstraint optionalCons = WSDLTypeConverter.OptionalConstraintType2OptConstraint(oc);

                    	//optionalCons.setReservationId(Integer.parseInt(resDetails.getGlobalReservationId()));
                    	this.log.debug(netLogger.getMsg(event,
                                                        "entering optional constraint category: " +
                                                         optionalCons.getKeyName() +
                                                         "value" + optionalCons.getValue()));
                    	optConstraintList.add(optionalCons);

                    }

                    res.setOptConstraintList(optConstraintList);

                }
            }
            resvDAO.update(res);

            // get data updated to database before RMScheduler looks for it.
            Session session = this.core.getSession();
            session.flush();
            // check to see if setup should be scheduled
            RMReservationScheduler scheduler = RMReservationScheduler.getInstance();
            if (scheduler != null) {
               this.schedulePathAction(resDetails, res.getStatus(),
                                        res.getConstraint(ConstraintType.USER).getPath().getPathSetupMode());
            }
        } catch (RMException rmEx) {
            this.log.error(netLogger.error(event, ErrSev.MAJOR, rmEx.getMessage()));
            throw new OSCARSServiceException( ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
        }
        this.log.debug(netLogger.end(event));
    }
    /**
     * updates an existing reservation. If a field or element in resDetails is
     *      empty or null, does not update that element in the existing reservation
     * @param res Reservation object that will be updated
     * @param resDetails Contains elements of the reservation to be modified
     * @throws OSCARSServiceException
     */
    private void update(Reservation res, ResDetails resDetails) throws OSCARSServiceException {
        StdConstraint constraint = null;
        Map <String,StdConstraint> conMap = res.getConstraintMap();
        String event = "mgrUpdateReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();

        this.log.debug(netLogger.start(event, "gri = " + res.getGlobalReservationId()));
        String input = null;
        long longInput = 0;

        if ((input=resDetails.getLogin()) != null) {
            res.setLogin(input);
        }
        if ((longInput=resDetails.getCreateTime()) != 0){
            res.setCreatedTime(longInput);
        }
        if ((input=resDetails.getDescription()) != null){
            res.setDescription(input);
        }
        if ((input=resDetails.getStatus()) != null) {
            res.setStatus(input);
        }
        try {
            UserRequestConstraintType userConstraint = resDetails.getUserRequestConstraint();
            if (userConstraint != null){
                constraint = conMap.get(ConstraintType.USER);
                if (constraint == null) {
                    throw new OSCARSServiceException(ErrorCodes.RESV_UPDATE_FAILED,
                                                    "stored reservation has null user constraint",
                                                     ErrorReport.SYSTEM);
                }
                WSDLTypeConverter.updateStdConstraint (constraint, userConstraint, this.dbname);
                // update duplicated values in reservation in case  they were changed
                Long mbps = userConstraint.getBandwidth() * 1000000L;
                if (mbps != null && mbps != 0) {
                    res.setBandwidth(mbps);
                }
                if ((longInput=userConstraint.getStartTime()) != 0 ){
                    res.setStartTime(longInput);
                }
                if ((longInput=userConstraint.getEndTime()) !=0){
                    res.setEndTime(longInput);
                }
            }
            ReservedConstraintType reservedConstraint = resDetails.getReservedConstraint();
            if (reservedConstraint != null){
                constraint = conMap.get(ConstraintType.RESERVED);
                if (constraint == null) {
                    constraint = WSDLTypeConverter.reserved2StdConstraint(reservedConstraint);
                    
                	if(constraint.getBandwidth() == 0 || constraint.getBandwidth() == null)
                	{                		                			
                		constraint.setBandwidth(res.getBandwidth());
                	}
                	
                    conMap.put(ConstraintType.RESERVED,constraint);
                } else {
                    WSDLTypeConverter.updateStdConstraint(constraint, reservedConstraint, this.dbname);
                }
                // update duplicated values in reservation in case  they were changed
                Long mbps = reservedConstraint.getBandwidth() * 1000000L;
                if (mbps !=  null && mbps != 0) {
                    res.setBandwidth(mbps);
                }
                if ((longInput=reservedConstraint.getStartTime()) != 0 ){
                    res.setStartTime(longInput);
                }
                if ((longInput=reservedConstraint.getEndTime()) !=0){
                    res.setEndTime(longInput);
                }
            }
        } catch (RMException rmEx){
            throw new OSCARSServiceException( ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
        }
        res.setConstraintMap(conMap);

        List <OptionalConstraintType> oclist = resDetails.getOptionalConstraint();
        if (oclist != null && !oclist.isEmpty()) {
            for (OptionalConstraintType oc: oclist) {
                OptConstraint optionalCons = WSDLTypeConverter.OptionalConstraintType2OptConstraint(oc);
             	//optionalCons.setReservationId(Integer.parseInt(resDetails.getGlobalReservationId()));
                 res.addOptConstraint(optionalCons);
            }
        }
        this.log.debug(netLogger.end(event));
    }

    /**
     * query returns details about the reservation specified by the gri
     * @param authConditions AuthConditions that may include restrictions on
     *     which reservations may be queied.
     * @param gri GlobalReservationId of reservation to be queried
     * @return ResDetails structure for the reservation
     * @throws OSCARSServiceException
     */
    public ResDetails query(AuthConditions authConditions,String gri) throws OSCARSServiceException{

        String event = "mgrQueryReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start(event));
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Reservation res = null;
        Boolean internalPathAuthorized;
        // throws OSCARSServiceException if no reservation is found
        res = resvDAO.query(gri);
        String loginId = this.getPermittedLogin(authConditions);
        List<String> domains = getPermittedDomains(authConditions);
        if (loginId != null) {
            if (!res.getLogin().equals(loginId)){
                throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED,  "not owner", ErrorReport.USER);
            }
        }
        try {
            if (domains != null ){
                if (!checkDomains(res,domains,netLogger,event)){
                    throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED,"not own domain", ErrorReport.USER);
                }
            }
            internalPathAuthorized = internalHopsAllowed(authConditions);
            this.log.debug(netLogger.getMsg(event,"internal PathAuthorized is " + internalPathAuthorized));
            ResDetails resDetails = RMUtils.res2resDetails(res, internalPathAuthorized);
            this.log.debug(netLogger.end(event));
            return resDetails;
        } catch (RMException rmEx) {
            throw new OSCARSServiceException( ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
        }
    }

    /**
     * Returns any errorReports associated with the reservation,
     * @param authConditions AuthConditions that may include restrictions on
     *     which reservations may be queried.
     * @param gri GlobalReservationId of reservation to be queried
     * @return ErrorReport which may be empty
     * @throws OSCARSServiceException
     */
    public ErrorReport getErrReportByGRI(AuthConditions authConditions,String gri) {

        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Reservation res = null;
        ErrorReport errRep = new ErrorReport();
        try{
         // throws OSCARSServiceException if no reservation is found
            res = resvDAO.query(gri);
            List <ErrorReportData> errData = res.getErrorReports();
            errRep = returnErrorReport(authConditions,res, errData);
        } catch (OSCARSServiceException ex) {
             // just return empty ErrorReport
        }
        return errRep;
    }

    /**
     * Returns any errorReports associated with the reservation,
     * @param authConditions AuthConditions that may include restrictions on
     *     which reservations may be queried.
     * @param transId transactionId of the transactionId to be queried
     * @return ErrorReport may be empty
     * @throws OSCARSServiceException if errror Reports are found for which this user does not have access
     */
    public ErrorReport getErrReportByTransId(AuthConditions authConditions,String transId) throws OSCARSServiceException{

        ErrorReportDAO erDAO = new ErrorReportDAO(this.dbname);
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Reservation res = null;
        List<ErrorReportData> errData = null;

        errData = erDAO.listByTransId(transId);
        if (errData.isEmpty()) {
            throw new OSCARSServiceException(ErrorCodes.ERR_REPORT_NOT_FOUND,
                                             "no error report for transaction" + transId,
                                              ErrorReport.USER);
        }
        res = resvDAO.query(errData.get(0).getGRI());

        return returnErrorReport(authConditions, res, errData);
      }

    private ErrorReport returnErrorReport( AuthConditions authConditions, Reservation res,
                                           List<ErrorReportData> errData)  throws OSCARSServiceException {
        ErrorReport errReport = new ErrorReport();
        String event = "mgrReturnErrorReport";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start(event));
        if (!errData.isEmpty()) {
            String loginId = this.getPermittedLogin(authConditions);
            List<String> domains = getPermittedDomains(authConditions);
            if (loginId != null) {
                if (!res.getLogin().equals(loginId)){
                   throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED,  "not owner", ErrorReport.USER);
                }
             }
             try {
                 if (domains != null ){
                     if (!checkDomains(res,domains,netLogger,event)){
                         throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED,"not own domain", ErrorReport.USER);
                     }
                 }
             } catch (RMException rmEx) {
                 throw new OSCARSServiceException(ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
             }

             for (ErrorReportData erd : errData){
                 ErrorReport nextErrReport = new ErrorReport(erd.getErrorCode(), erd.getErrorMsg(),
                                                             erd.getErrorType(), erd.getGRI(),
                                                             erd.getTransId(), erd.getTimestamp(),
                                                             erd.getModuleName(), erd.getDomainId()
                                                    );

                 errReport.add(nextErrReport);
                 //this.log.debug(netLogger.getMsg(event,"adding errorReport"));
             }
         }
         this.log.debug(netLogger.end(event));
         return errReport;
    }
    /**
     * cancel called by Coordinator via RMSoapHandler. Checks that user is authorized to cancel 
     * this reservation and that the reservation is either RESERVED , ACTIVE or ACCEPTED.
     * Returns details about the reservation to the coordinator. Any scheduled pathSetup or
     * teardown will be removed from the RMReservationScheduler.
     *  
     * @param authConditions AuthConditions that may include restrictions on
     *     which reservations may be canceled. 
     * @param gri GlobalReservationId of reservation to be canceled
     * @return ResDetails structure for the reservation
     * @throws OSCARSServiceException
     */
    public ResDetails cancel(AuthConditions authConditions,String gri) throws OSCARSServiceException{

        String event = "mgrCancelReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start(event));
        ResDetails resDetails = null;
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Reservation res = null;
        StdConstraint constraint = null;
        Map <String,StdConstraint> conMap = new HashMap<String,StdConstraint>();
        Boolean internalPathAuthorized = true; // Coord needs complete path to send to PCECancel

        // throws RMException if no reservation is found
        res = resvDAO.query(gri);
        String loginId = this.getPermittedLogin(authConditions);
        List<String> domains = getPermittedDomains(authConditions);
        if (loginId != null) {
            if (!res.getLogin().equals(loginId)){
                throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED, "not owner", ErrorReport.USER);
            }
        }
        try {
            if (domains != null ){
                if (!checkDomains(res,domains,netLogger, event)){
                    this.log.info(netLogger.getMsg(event,
                                                   "user " +loginId+ " not allowed to cancel this reservation: " +gri));
                    throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED, "not owm domain", ErrorReport.USER);
                }
            }
            boolean accepted = false;
            String status = res.getStatus();
            try {
                if (status.equals(StateEngineValues.ACCEPTED)) {
                    stateEngine.updateStatus(res, StateEngineValues.CANCELLED, true);
                    accepted = true;
                } else {
                    String newStatus = StateEngine.canModifyStatus(status,StateEngineValues.INCANCEL);
                }
            } catch( OSCARSServiceException oEx) {
                throw new OSCARSServiceException( ErrorCodes.RESV_STATE_ERROR, oEx.getMessage(), ErrorReport.USER);
            }
            resDetails = res2resDetails(res, internalPathAuthorized);
            if (!accepted) {
                RMReservationScheduler.getInstance().forget(resDetails);
            }
        } catch (RMException rmEx) {
            throw new OSCARSServiceException( ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
        }

        this.log.debug(netLogger.end(event));
        return resDetails;
    }
    
    /**
     * modifyReservation called by Coordinator. Checks that user is authorized to modify this reservation and
     * that the reservation is in a state that allows modification.. 
     *  returns the current status of the reservation to the coordinator
     * @param authConditions AuthConditions that may include restrictions on
     *     which reservations may be modified. 
     * @param gri GlobalReservationId of reservation to be queried
     * @return status
     * @throws OSCARSServiceException
     */
    public String modify(AuthConditions authConditions,String gri) throws OSCARSServiceException{

        String event = "mgrModifyReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start(event));
        ResDetails resDetails = null;
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Reservation res = null;
        Boolean internalPathAuthorized = true; // Coord needs complete path to send to PCEModify
        synchronized (RMReservationScheduler.schedLock){
            try {
                RMReservationScheduler.schedLock = "modifyReservation";
                // throws RMException if no reservation is found
                res = resvDAO.query(gri);
                String loginId = this.getPermittedLogin(authConditions);
                List<String> domains = getPermittedDomains(authConditions);
                if (loginId != null) {
                    if (!res.getLogin().equals(loginId)){
                        throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED, "not owner", ErrorReport.USER);
                    }
                }
                if (domains != null ){
                    if (!checkDomains(res,domains,netLogger, event)){
                        this.log.info(netLogger.getMsg(event,
                                                       "user "+ loginId +" not allowed to modify this reservation: "+ gri));
                        throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED, "not own domain", ErrorReport.USER);
                    }
                }
                try {
                    String newStatus = StateEngine.canModifyStatus(res.getStatus(),StateEngineValues.INMODIFY);
                } catch (OSCARSServiceException rmEx){
                    throw new OSCARSServiceException(ErrorCodes.RESV_MODIFY_FAILED, rmEx.getMessage(), ErrorReport.USER);
                }
                resDetails = RMUtils.res2resDetails(res, internalPathAuthorized);
                ReservationScheduler scheduler = RMReservationScheduler.getInstance();
                scheduler.forget(resDetails);
                RMReservationScheduler.schedLock = "unlocked";
            } catch (RMException rmEx)  {
                throw new OSCARSServiceException( ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
            }
        }// end synchronized block
        this.log.debug(netLogger.end(event));
        return res.getStatus();
    }
    /**
     * list returns details about the reservations that match the input constraints
     * @param authConditions AuthConditions that may include restrictions on
     *     which reservations may be listed. 
     * @param numRequested int with the number of reservations to return
     * @param resOffset int with the offset into the list
     *
     * @param statuses a list of reservation statuses. If not null or empty,
     * results will only include reservations with one of these statuses.
     * If null / empty, results will include reservations with any status.
     *
     * @param description the reservation description field
     *
     * @param userName if not null, return only reservation owned by this user
     *
     * @param linkIds a list of link id's. If not null / empty, results will
     * only include reservations whose path includes at least one of the links.
     * If null / empty, results will include reservations with any path.
     *
     * @param vlanTags a list of VLAN tags.  If not null or empty,
     * results will only include reservations where (currently) the first link
     * in the path has a VLAN tag from the list (or ranges in the list).  If
     * null / empty, results will include reservations with any associated
     * VLAN.
     *
     * @param startTime the start of the time window to look in; null for
     * everything before the endTime
     *
     * @param endTime the end of the time window to look in; null for everything after the startTime,
     * leave both start and endTime null to disregard time
     * 
     * @return reservations list of reservations that user is allowed to see. List
     *     may be empty
     * @throws OSCARSServiceException
     */
    
    public List<ResDetails> list(AuthConditions authConditions, int numRequested, int resOffset,
            List<String> statuses, String description, String userName, List<String> linkIds,
            List<String> vlanTags,  Long startTime, Long endTime)
            throws OSCARSServiceException {

        String event = "mgrListReservations";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String reqLogin = null;
        List<String> reqDomains = null;
        List<Reservation> reservations = null;
        Boolean internalPathAuthorized = false;
        internalPathAuthorized = internalHopsAllowed(authConditions);

        List<String> loginIds = new ArrayList<String>();
        List<ResDetails> resDetailsList = new ArrayList<ResDetails>();
        this.log.debug(netLogger.start(event));

        reqLogin = getPermittedLogin(authConditions);
        if (reqLogin != null){
            if ((userName == null) || (userName.equals(reqLogin))) {
             // get only reservations that belong to this user
                loginIds.add(reqLogin);
            } else {
                throw new OSCARSServiceException(ErrorCodes.ACCESS_DENIED,
                                                 "no permission to get reservations for " + userName,
                                                  ErrorReport.USER);
            }
        }
        if (userName != null && reqLogin == null ) {
            loginIds.add(userName);
        }
        try {
            ReservationDAO dao = new ReservationDAO(this.dbname);
            reservations = dao.list(numRequested, resOffset, loginIds, statuses,
                    description, vlanTags, startTime, endTime);
            if (linkIds != null && !linkIds.isEmpty()) {
                Map<String, Pattern> patterns = new HashMap<String,Pattern>();
                for (String id: linkIds) {
                    patterns.put(id, Pattern.compile(".*" + id + ".*"));
                }
                ArrayList<Reservation> removeThese = new ArrayList<Reservation>();
                for (Reservation rsv : reservations) {
                    Path path = this.getPath(rsv);

                    boolean matches =this.matches(path, patterns);
                    if (!matches) {
                        /* this.log.debug(netLogger.getMsg(event,"not returning: " +
                                       rsv.getGlobalReservationId())); */
                        removeThese.add(rsv);
                    }
                }
                for (Reservation rsv : removeThese) {
                    reservations.remove(rsv);
                }
            }
            reqDomains = getPermittedDomains(authConditions);
            if (reqDomains != null) {
                ArrayList<Reservation> removeThese = new ArrayList<Reservation>();
                // keep reservations that start or terminate at institution
                // or belong to this user
                this.log.debug(netLogger.getMsg(event,"Checking " + reservations.size() +
                                " reservations for site"));
                for (Reservation resv : reservations) {
                    if (!checkDomains(resv,reqDomains,netLogger, event)) {
                        removeThese.add(resv);
                    }
                }
                for (Reservation rsv : removeThese) {
                    reservations.remove(rsv);
                }
            }
            for ( Reservation resv : reservations ){
                resDetailsList.add(RMUtils.res2resDetails(resv, internalPathAuthorized));
            }
        } catch (RMException rmEx ) {
            throw new OSCARSServiceException( ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
        }
        this.log.debug(netLogger.end(event));
        return resDetailsList;
    }

    /**
     * Returns the reserved path for a reservation if there is a reservedConstraint
     * otherwise returns the requested path.
     * @param resv  reservation
     * @return Either the reserved path if one exists or else the requested path
     * @throws RMException
     */
    public Path getPath(Reservation resv) throws RMException {

        StdConstraint constraint = resv.getConstraint(ConstraintType.RESERVED);
        if (constraint != null) {
            return constraint.getPath();
        }
        constraint = resv.getConstraint(ConstraintType.USER);
        if (constraint != null) {
            return constraint.getPath();
        }
        return null;
    }
    /**
     * Matches if any hop in the path has a topology identifier that at
     * least partially matches a link id.
     *
     * @param path path to check
     * @param patterns map from linkIds to compiled Patterns
     */
    public boolean matches(Path path, Map<String, Pattern> patterns) {
     // TODO probably needs work
        List<PathElem> pathElems = path.getPathElems();
        StringBuilder sb = new StringBuilder();
        for (PathElem pathElem: pathElems) {
            String topologyIdent = pathElem.getUrn();
            sb.append(topologyIdent);
            String localIdent = URNParser.abbreviate(topologyIdent);
            sb.append(localIdent);
        }
        for (Pattern pattern: patterns.values()) {
            Matcher matcher = pattern.matcher(sb.toString());
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
    /**
     *  Check to see if either the src or destination of the reservation is in an allowed domain
     * @param res reservation 
     * @param domains String array containing all the domains allowed for this request
     * @return true, if this reservation is allowed for this request, false otherwise
     * @throws  RMException
     */
    public boolean checkDomains(Reservation res, List<String> domains,OSCARSNetLogger netLogger,String event) throws RMException
    {
        Path reqPath = this.getPath(res);
        List<PathElem> pathElems = reqPath.getPathElems();
        String srcURN = pathElems.get(0).getUrn();
        String src = parseURN(srcURN,"domain");
        String destURN = pathElems.get(pathElems.size()-1).getUrn();
        String dest = parseURN(destURN,"domain");
        if (dest == null || src == null ) {
            log.debug( netLogger.error(event,ErrSev.MINOR,
                        "incorrect path in database for reservation res.getGlobalReservationId()"));
        }
        for (String dom : domains) {
            if (dom.equals(src) ||
                    dom.equals(dest)) {
                return true;
            }
        }
        return false;
    }
    private String parseURN(String urn, String value){
        String parts[] = urn.split(":");
        for (int i= 0; i < parts.length; i++){
            if (parts[i].startsWith( value + "=")) {
                return parts[i].substring(value.length() +1);
            }
        }
        return null;
    }

    
    /**
     *  Check to see if there is an authConditions requiring a specific loginId
     * @param authConditions
     * @return required Login Id, or null if there is not one
     */
    public String getPermittedLogin(AuthConditions authConditions) {
        String reqLoginId = null;
        if (authConditions == null) {
            return reqLoginId;
        }
        for (AuthConditionType authCond: authConditions.getAuthCondition()){
            if (authCond.getName().equals(AuthZConstants.PERMITTED_LOGIN)) {
                reqLoginId = authCond.getConditionValue().get(0);
            }
        }
        return reqLoginId;
    }
    /**
     *  check to see if there is a authConditions requiring specific domains
     * @param authConditions
     * @return required domains, or null if there is not one
     */
    public List<String> getPermittedDomains(AuthConditions authConditions) {
        List<String> reqDomains = null;
        if (authConditions == null ){
            return reqDomains;
        }
        for (AuthConditionType authCond: authConditions.getAuthCondition()){
            if (authCond.getName().equals(AuthZConstants.PERMITTED_DOMAINS)) {
                reqDomains = authCond.getConditionValue();
                break;
            }
        }
        return reqDomains;
    }
    
    /**
     *  Check to see if there is an authConditions that allows user to see internal hops
     * @param authConditions
     * @return true if such a condition exists, false otherwise
     */
    public Boolean internalHopsAllowed(AuthConditions authConditions) {
        Boolean hopsAllowed = false;
        if (authConditions == null ){
            return hopsAllowed;
        }
        for (AuthConditionType authCond: authConditions.getAuthCondition()){
            if (authCond.getName().equals(AuthZConstants.INT_HOPS_ALLOWED)) {
                if ( authCond.getConditionValue().get(0).equals("true") )
                    hopsAllowed = true;
                break;
            }
        }
        return hopsAllowed;
    }
}
