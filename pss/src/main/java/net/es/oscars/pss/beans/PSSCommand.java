package net.es.oscars.pss.beans;


public class PSSCommand {

    private String deviceAddress;
    private String transactionId;
    private String deviceCommand;
    
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public void setDeviceCommand(String deviceCommand) {
        this.deviceCommand = deviceCommand;
    }
    public String getDeviceCommand() {
        return deviceCommand;
    }
    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
    public String getDeviceAddress() {
        return deviceAddress;
    }
    

}
