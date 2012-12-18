package net.es.oscars.pce;

public class SimplePCEJob {
    private PCEMessage message;
    private int jobType;
    
    final static public int CREATE_TYPE = 1;
    final static public int CREATE_COMMIT_TYPE = 2;
    final static public int MODIFY_COMMIT_TYPE = 3;
    final static public int CANCEL_TYPE = 4;
    final static public int MODIFY_TYPE = 5;
    
    public SimplePCEJob(PCEMessage message, int jobType){
        this.message = message;
        this.jobType = jobType;
    }
    
    
    /**
     * @return the message
     */
    public PCEMessage getMessage() {
        return this.message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(PCEMessage message) {
        this.message = message;
    }
    /**
     * @return the jobType
     */
    public int getJobType() {
        return this.jobType;
    }
    /**
     * @param jobType the jobType to set
     */
    public void setJobType(int jobType) {
        this.jobType = jobType;
    }
    
}
