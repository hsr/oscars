package net.es.oscars.nsibridge.ifces;

/**
 * @haniotak Date: 2012-08-08
 */
public interface SM_State {
    public Object state();
    public String value();
    public void setValue(String value);
    public void setState(Object state);

}
