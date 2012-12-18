package net.es.oscars.nsibridge.beans;


import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;

public class GenericRequest {
    protected CommonHeaderType inHeader;
    protected CommonHeaderType outHeader;
    public GenericRequest() {

    }

    public CommonHeaderType getInHeader() {
        return inHeader;
    }

    public void setInHeader(CommonHeaderType inHeader) {
        this.inHeader = inHeader;
    }

    public CommonHeaderType getOutHeader() {
        return outHeader;
    }

    public void setOutHeader(CommonHeaderType outHeader) {
        this.outHeader = outHeader;
    }
}
