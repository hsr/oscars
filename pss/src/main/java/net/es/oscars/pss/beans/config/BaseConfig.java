package net.es.oscars.pss.beans.config;



public class BaseConfig {
    private SoapConfig soap;
    private CircuitServiceConfig circuitService;
    private DefinitionFilesConfig definitions;

    
    private GenericConfig connectorDirectory;
    private GenericConfig deviceConnectorMap;
    private GenericConfig deviceModelMap;
    private GenericConfig deviceResolve;
    private GenericConfig notify;
    private GenericConfig verify;
    private GenericConfig validate;
    private GenericConfig workflow;
    
    public GenericConfig getNotify() {
        return notify;
    }
    public void setNotify(GenericConfig notify) {
        this.notify = notify;
    }
    public SoapConfig getSoap() {
        return soap;
    }
    public void setSoap(SoapConfig soap) {
        this.soap = soap;
    }
    public CircuitServiceConfig getCircuitService() {
        return circuitService;
    }
    public void setCircuitService(CircuitServiceConfig circuitService) {
        this.circuitService = circuitService;
    }
    public DefinitionFilesConfig getDefinitions() {
        return definitions;
    }
    public void setDefinitions(DefinitionFilesConfig definitions) {
        this.definitions = definitions;
    }
    public void setDeviceResolve(GenericConfig deviceResolve) {
        this.deviceResolve = deviceResolve;
    }
    public GenericConfig getDeviceResolve() {
        return deviceResolve;
    }
    public GenericConfig getConnectorDirectory() {
        return connectorDirectory;
    }
    public void setConnectorDirectory(GenericConfig connectorDirectory) {
        this.connectorDirectory = connectorDirectory;
    }
    public GenericConfig getDeviceConnectorMap() {
        return deviceConnectorMap;
    }
    public void setDeviceConnectorMap(GenericConfig deviceConnectorMap) {
        this.deviceConnectorMap = deviceConnectorMap;
    }
    public GenericConfig getDeviceModelMap() {
        return deviceModelMap;
    }
    public void setDeviceModelMap(GenericConfig deviceModelMap) {
        this.deviceModelMap = deviceModelMap;
    }
    public GenericConfig getValidate() {
        return validate;
    }
    public void setValidate(GenericConfig validate) {
        this.validate = validate;
    }
    public GenericConfig getWorkflow() {
        return workflow;
    }
    public void setWorkflow(GenericConfig workflow) {
        this.workflow = workflow;
    }
    public void setVerify(GenericConfig verify) {
        this.verify = verify;
    }
    public GenericConfig getVerify() {
        return verify;
    }
}
