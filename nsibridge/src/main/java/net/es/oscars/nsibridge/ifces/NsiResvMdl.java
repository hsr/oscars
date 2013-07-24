package net.es.oscars.nsibridge.ifces;


public interface NsiResvMdl {

    public void localCheck();
    public void localHold();
    public void localCommit();
    public void localAbort();

    public void sendRsvCF();
    public void sendRsvFL();

    public void sendRsvCmtCF();
    public void sendRsvCmtFL();

    public void sendRsvAbtCF();


}
