package net.es.oscars.pss.bridge.beans;

public class DeviceBridge {
    private String deviceId;
    private String portA;
    private String portZ;
    private String vlanA;
    private String vlanZ;
    
    public DeviceBridge() {
        
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public void setPortA(String portA) {
        this.portA = portA;
    }
    public String getPortA() {
        return portA;
    }
    public void setPortZ(String portZ) {
        this.portZ = portZ;
    }
    public String getPortZ() {
        return portZ;
    }
    public void setVlanA(String vlanA) {
        this.vlanA = vlanA;
    }
    public String getVlanA() {
        return vlanA;
    }
    public void setVlanZ(String vlanZ) {
        this.vlanZ = vlanZ;
    }
    public String getVlanZ() {
        return vlanZ;
    }
    
}
