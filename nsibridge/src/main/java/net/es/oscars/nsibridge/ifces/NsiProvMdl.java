package net.es.oscars.nsibridge.ifces;


public interface NsiProvMdl {

    public void localProv();
    public void localRel();

    public void sendProvCF();
    public void notifyProvFL();

    public void sendRelCF();
    public void notifyRelFL();


}
