package net.es.oscars.utils.task;

public class TaskException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public TaskException(String msg) {
        super(msg);
    }

}
