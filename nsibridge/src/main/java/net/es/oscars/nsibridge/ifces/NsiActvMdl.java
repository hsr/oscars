package net.es.oscars.nsibridge.ifces;

/**
 * @haniotak Date: 2012-08-08
 */
public interface NsiActvMdl {


    public void localAct();
    public void localDeact();

    public void notifyAct();
    public void notifyDeact();
    public void notifyError();


}
