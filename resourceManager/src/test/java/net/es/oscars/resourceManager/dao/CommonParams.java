package net.es.oscars.resourceManager.dao;

import net.es.oscars.resourceManager.beans.Reservation;
import net.es.oscars.resourceManager.beans.StdConstraint;
import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;

/**
 * This class handles common parameter settings for tests.
 */
public class CommonParams {
    private static final Long BANDWIDTH = 25000000L;  // 25 Mbps
    private static final int DURATION = 240;
    // for layer 3
    private static final int BURST_LIMIT = 10000000; // 10 Mbps
    private static final String PROTOCOL = "UDP";
    private static final String LSP_CLASS = "4";

    public static void setParameters(Reservation resv, String description) {
        Long seconds = System.currentTimeMillis()/1000;
        resv.setStartTime(seconds);
        resv.setCreatedTime(seconds);
        seconds += DURATION;
        resv.setEndTime(seconds);

        resv.setBandwidth(BANDWIDTH);
        resv.setDescription(description);
        resv.setStatus("TEST");
        resv.setLogin(getLogin());
    }
    public static void setParameters(ResDetails resDetails, String description) {
        Long seconds = System.currentTimeMillis()/1000;
        resDetails.setCreateTime(seconds);
        resDetails.setStatus("COMMITTED");
        resDetails.setDescription(description);
        resDetails.setLogin(getLogin());
    }
    
    public static void setParameters(StdConstraint constraint, String constraintType)  throws RMException {
        Long seconds = System.currentTimeMillis()/1000;
        constraint.setStartTime(seconds);
        seconds += DURATION;
        constraint.setEndTime(seconds);
        constraint.setBandwidth(BANDWIDTH);
        constraint.setConstraintType(constraintType);
    }

    public static void setParameters(UserRequestConstraintType constraint)  throws RMException {
        Long seconds = System.currentTimeMillis()/1000;
        constraint.setStartTime(seconds);
        seconds += DURATION;
        constraint.setEndTime(seconds);
        Long mbps = BANDWIDTH / 1000000L; 
        int bandwidth = mbps.intValue();
        constraint.setBandwidth(bandwidth);
    }
    public static void setParameters(ReservedConstraintType constraint)  throws RMException {
        Long seconds = System.currentTimeMillis()/1000 +480;
        constraint.setStartTime(seconds);
        seconds += DURATION;
        constraint.setEndTime(seconds);
        Long mbps = BANDWIDTH / 1000000L; 
        int bandwidth = mbps.intValue();
        constraint.setBandwidth(bandwidth);
    }
    public static String getLogin() {
        return "testUser";
    }

    public static String getIdentifier() {
        return "test suite";
    }

    public static String getPathIdentifier() {
        return "path test suite";
    }

    public static String getResvIdentifier() {
        return "reservation test";
    }

    public static String getReservationDescription() {
        return "Test reservation unique description 123aaa456zzz";
    }

    public static Long getBandwidth() {
        return BANDWIDTH;
    }
    
    public static Long getMPLSBurstLimit() {
        return 1000000L;
    }

    public static String getSrcEndpoint() {
        return "urn:ogf:network:domainIdent:nodeIdent1:portIdent";
    }

    public static String getDestEndpoint() {
        return "urn:ogf:network:domainIdent:nodeIdent2:portIdent";
    }

    public static String getSrcHost() {
        return "test.src.domain";
    }

    public static String getDestHost() {
        return "test.dest.domain";
    }

    public static String getConstraintCategory() {
        return "TEST CONSTRAINT";
    }
    
    public static String getIpAddress() {
        return "127.0.0.1";
    }
}
