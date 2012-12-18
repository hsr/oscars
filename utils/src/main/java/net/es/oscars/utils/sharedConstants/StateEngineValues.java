package net.es.oscars.utils.sharedConstants;

public class StateEngineValues {

    public final static String ACCEPTED = "ACCEPTED";       // createReservation is authorized, gri is assigned
    public final static String INPATHCALCULATION = "INPATHCALCULATION";   //start local path calculation
    public final static String PATHCALCULATED  = "PATHCALCULATED"; // whole path calculation done
    public final static String INCOMMIT = "INCOMMIT";       // in commit phase for calculated path
    public final static String COMMITTED = "COMMITTED";     // whole path resources committed
    public final static String RESERVED = "RESERVED";       // all domains have committed resources
    public final static String INSETUP = "INSETUP";         // circuit setup has been started
    public final static String ACTIVE = "ACTIVE";           // entire circuit has been setup
    public final static String INTEARDOWN = "INTEARDOWN";   // circuit teardown has been started
    public final static String FINISHED = "FINISHED";       // reservation endtime reached with no errors, circuit has been torndown
    public final static String CANCELLED = "CANCELLED";     // complete reservation has been canceled, no circuit
    public final static String FAILED = "FAILED";           // reservation failed at some point, no circuit
    public final static String INMODIFY = "INMODIFY";       // reservation is being modified
    public final static String MODCOMMITTED = "MODCOMMITTED";  // modifications have been committed
    public final static String INCANCEL = "INCANCEL";       // reservation is being canceled
    public final static String UNKNOWN = "UNKNOWN";         // reservation may be in an inconsistent state due to errors


}