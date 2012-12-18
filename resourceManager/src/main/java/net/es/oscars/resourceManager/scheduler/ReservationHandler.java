package net.es.oscars.resourceManager.scheduler;

import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ResDetails;

public class ReservationHandler {
    
    public static String PATHSETUP = "PathSetup";
    public static String TEARDOWN = "TearDown";
    public static String FINISH = "ReservationFinish";
    
    private ResDetails resDetails = null;
    private long executionTime;
    private String operationType;

    public ReservationHandler(ResDetails resDetails,
                              String operationType,
                              long executionTime) {
        
        this.resDetails = resDetails;
        this.operationType = operationType;
        this.executionTime = executionTime;
    }

    public ResDetails getResDetails () {
        return this.resDetails;
    }
    
    public String getOperationType() {
        return this.operationType;
    }
  
    public long getExecutionTime () {
        return this.executionTime;
    }
    
}
 