package net.es.oscars.pss.util;

public class URNParserResult {
    String domainId;
    String nodeId;
    String portId;
    String linkId;
    String fqti;
    String concise;
    URNType type;
    
    public enum URNType {
        UNKNOWN,
        DOMAIN,
        NODE,
        PORT,
        LINK,
        IPV4ADDR,
        IPV6ADDR,
        
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getFqti() {
        return fqti;
    }

    public void setFqti(String fqti) {
        this.fqti = fqti;
    }

    public String getConcise() {
        return concise;
    }

    public void setConcise(String concise) {
        this.concise = concise;
    }

    public URNType getType() {
        return type;
    }

    public void setType(URNType type) {
        this.type = type;
    }
    
    
    public boolean equals(Object b) {
        URNParserResult other;
        
        if (!(b instanceof URNParserResult)) {
            return false;
        } else {
            other = (URNParserResult) b;
        }
        if (this.type == null || other.getType() == null) {
            return false;
        } else if (!this.type.equals(other.getType())) {
            return false;
            
        }
        if (this.type.equals(URNType.DOMAIN)) {
            if (this.domainId == null) {
                return false;
            } else return this.domainId.equals(other.getDomainId());
        } else if (this.type.equals(URNType.NODE)) {
            if (this.domainId == null || this.nodeId == null ) {
                return false;
            } else {
                return ( this.domainId.equals(other.getDomainId()) && 
                            this.nodeId.equals(other.getNodeId())  
                          );
            }
        } else if (this.type.equals(URNType.PORT)) {
            if (this.domainId == null || this.nodeId == null || this.portId == null) {
                return false;
            } else {
                return ( this.domainId.equals(other.getDomainId()) && 
                            this.nodeId.equals(other.getNodeId())  &&
                            this.portId.equals(other.getPortId())  
                          );
            }
        } else if (this.type.equals(URNType.LINK)) {
            if (this.domainId == null || this.nodeId == null || this.portId == null || this.linkId == null) {
                return false;
            } else {
                return ( this.domainId.equals(other.getDomainId()) && 
                            this.nodeId.equals(other.getNodeId())  &&
                            this.portId.equals(other.getPortId())  && 
                            this.linkId.equals(other.getLinkId())  
                          );
            }
        } else {
            return false;
        } 
        
    }
    public int hashCode() {
        if (this.getFqti() == null) return 0;
        int hash = this.getFqti().hashCode();
        return hash;
    }

    public String toString() {
        String result = "";
        if (type != null) {
            result = "type = ["+type+"]";
        } else {
            result = "type: [NULL]";
        }
        result += " fqti: ["+fqti+"] hash:"+this.hashCode();
        return result;
    }

}
