package net.es.oscars.notifycmdexec;

import org.jdom.Element;

/**
 * Callback interface used by NotifyListener when a Notify message is received.
 * This callback interface should be implemented by users of the NotifyListener
 * class and filled with application specific business logic.
 */
public interface NotifyHandler{
    /**
     * Called when a Notify message is received
     *
     * @param notification a JDOM element representing the NotificationMessage element
     */
    public void handleNotify(Element notification);
    
    /**
     * Called when an error, such as a parsing error, occurs in NotifyListener
     *
     * @param type the type of error (SOCKET, IO, PARSING, JDOM, GENERAL)
     * @param e the exception to report
     */
    public void handleError(String type, Exception e);
}