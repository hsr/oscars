package net.es.oscars.resourceManager.beans;

public class PathDirection {
    public static final String FORWARD = "forward";
    public static final String REVERSE = "reverse";
    public static final String BIDIRECTIONAL = "bidirectional";

    public static boolean isValid(String pathDirection) {
        if (pathDirection.equals(FORWARD)) {
            return true;
        } else if (pathDirection.equals(REVERSE)) {
            return true;
        } else if (pathDirection.equals(BIDIRECTIONAL)) {
            return true;
        }
        return false;
    }


}
