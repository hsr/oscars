package net.es.oscars.coord.common;

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



}
