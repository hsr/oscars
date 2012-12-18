package net.es.oscars.pce;

import java.util.List;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.pce.soap.gen.v06.TagDataContent;

public class AggMessage {
    private MessagePropertiesType msgProps;
    private String gri;
    private String transactionId;
    private String pceName;
    private String callBackEndpoint;
    private String method;
    private List<TagDataContent> pceTagData;
    
    public AggMessage (MessagePropertiesType msgProps,
                       String gri,
                       String transactionId,
                       String pceName,
                       String callBackEndpoint,
                       String method,
                       List<TagDataContent> pceTagData) {
        this.msgProps = msgProps;
        this.gri = gri;
        this.transactionId = transactionId;
        this.pceName = pceName;
        this.callBackEndpoint = callBackEndpoint;
        this.pceTagData = pceTagData;
        this.method = method;
    }
    
    public MessagePropertiesType getMessageProperties(){
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
    
    public List<TagDataContent> getPCETagDataContent() {
        return this.pceTagData;
    }
    
    public String getMethod() {
        return this.method;
    }
}
