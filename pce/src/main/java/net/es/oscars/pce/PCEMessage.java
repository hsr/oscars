package net.es.oscars.pce;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;

public class PCEMessage {
    private MessagePropertiesType msgProps;
    private String gri;
    private String transactionId;
    private String pceName;
    private String callBackEndpoint;
    private String method;
    private PCEDataContent pceData;
    
    public PCEMessage (MessagePropertiesType msgProps,
                       String gri,
                       String transactionId,
                       String pceName,
                       String callBackEndpoint,
                       String method,
                       PCEDataContent pceData) {
        this.msgProps= msgProps;
        this.gri = gri;
        this.transactionId = transactionId;
        this.pceName = pceName;
        this.callBackEndpoint = callBackEndpoint;
        this.pceData = pceData;
        this.method = method;
    }
    
    public MessagePropertiesType getMessageProperties() {
        return this.msgProps;
    }
    
    public String getGri() {
        return this.gri;
    }
    
    public String getTransactionId() {
        return this.transactionId;
    }
    
    public String getPceName() {
        return this.pceName;
    }
    
    public String getCallBackEndpoint() {
        return this.callBackEndpoint;
    }
    
    public PCEDataContent getPCEDataContent() {
        return this.pceData;
    }
    
    public String getMethod() {
        return this.method;
    }
}
