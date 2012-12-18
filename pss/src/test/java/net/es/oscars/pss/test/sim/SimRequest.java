package net.es.oscars.pss.test.sim;

import net.es.oscars.pss.beans.PSSRequest;


public class SimRequest extends PSSRequest {
    int srcSetupTime;
    int dstSetupTime;
    int startTime;
    public int getSrcSetupTime() {
        return srcSetupTime;
    }
    public void setSrcSetupTime(int srcSetupTime) {
        this.srcSetupTime = srcSetupTime;
    }
    public int getDstSetupTime() {
        return dstSetupTime;
    }
    public void setDstSetupTime(int dstSetupTime) {
        this.dstSetupTime = dstSetupTime;
    }
    public int getStartTime() {
        return startTime;
    }
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }


}
