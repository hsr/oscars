package net.es.oscars.utils.sharedConstants;

import java.nio.channels.IllegalBlockingModeException;

/**
 * ErrorCodes definitions for OSCARS errors
 * User: mrt
 * Date: 5/20/11
 *
 */
public class ErrorCodes {
    // SYSTEM Errors
    public static String NOT_IMPLEMENTED        = "NOT_IMPLEMENTED";
    public static String UNKNOWN                = "UNKNOWN";
    public static String COULD_NOT_CONNECT      = "COULD_NOT_CONNECT";
    public static String SCHED_ERROR            = "ERROR_SCHEDULING_COORD_ACTION";
    public static String CONFIG_ERROR           = "CONFIGURATION_ERROR";
    public static String IDE_FAILED             =  "INTER_DOMAIN_EVENT_FAILED";
    public static String RESV_CREATE_FAILED     = "RESERVATION_CREATE_FAILED";
    public static String RESV_CANCEL_FAILED     = "RESERVATION_CANCEL_FAILED";
    public static String RESV_MODIFY_FAILED     = "RESERVATION_MODIFY_FAILED";
    public static String RESV_QUERY_FAILED      = "RESERVATION_QUERY_FAILED";
    public static String RESV_LIST_FAILED       = "RESERVATION_LIST_FAILED";

    public static String RESV_UPDATE_FAILED     = "RESERVATION_UPDATE_FAILED";
    public static String RESV_STATE_ERROR       = "RESERVATION_STATE_CHANGE_NOT_ALLOWED";
    public static String REQUEST_TIMEOUT        = "REQUEST_TIMED_OUT";
    public static String RESV_STORE_FAILED      = "RESERVATION_STORE_FAILED";
    public static String RESV_DATABASE_ERROR    = "RESERVATION_DATABASE_ERROR";
    public static String RESV_COMPLETE_FAILED   = "RESERVATION_COMPLETION_FAILED";
    public static String RESV_MOD_COMPLETE_FAILED = "RESERVATION_MODIFY_COMPLETION_FAILED";
    public static String PATH_REFRESH_FAILED    = "PATH_REFRESH_FAILED";
    public static String PATH_SETUP_FAILED      = "PATH_SETUP_FAILED";
    public static String PATH_SETUP_UPSTREAM_FAILED  = "PATH_SETUP_UPSTREAM_FAILED";
    public static String PATH_SETUP_DOWNSTREAM_FAILED  = "PATH_SETUP_DOWNSTREAM_FAILED";
    public static String PATH_MODIFY_FAILED      = "PATH_MODIFY_FAILED";
    public static String PATH_TEARDOWN_FAILED   = "PATH_TEARDOWN_FAILED";
    public static String PATH_TEARDOWN_UPSTREAM_FAILED  = "PATH_TEARDOWN_UPSTREAM_FAILED";
    public static String PATH_TEARDOWN_DOWNSTREAM_FAILED  = "PATH_TEARDOWN_DOWNSTREAM_FAILED";
    public static String NO_PSS_REPLY           = "NO_PSS_REPLY";
    public static String PCE_CREATE_FAILED      = "PCE_CREATE_FAILED";
    public static String PCE_COMMIT_FAILED      = "PCE_COMMIT_FAILED";
    public static String PCE_CANCEL_FAILED      = "PCE_CANCEL_FAILED";
    public static String PCE_MODIFY_FAILED      = "PCE_MODIFY_FAILED";
    public static String PCE_MODIFY_COMMIT_FAILED = "PCE_MODIFY_COMMIT_FAILED";


    // USER Errors
    public static String INVALID_PARAM          = "INVALID_PARAMETER";
    public static String ACCESS_DENIED          = "ACCESS_DENIED";
    public static String AUTHORIZATION_FAILED   = "AUTHORIZATION_FAILED";
    public static String RESV_NOT_FOUND         = "RESERVATION_NOT_FOUND";
    public static String ERR_REPORT_NOT_FOUND   = "ERR_REPORT_NOT_FOUND";


}
