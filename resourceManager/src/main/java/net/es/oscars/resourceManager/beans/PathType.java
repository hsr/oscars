package net.es.oscars.resourceManager.beans;

public class PathType {
    public static final String STRICT = "strict";
    public static final String LOOSE = "loose";
    public static final String UNDEFINED = "undefined";
    // never construct this object
    private PathType() {
    }

    public static boolean isValid(String pathType) {
       if (pathType== null) {
            return true;
        }
        if (pathType.equals(STRICT)) {
            return true;
        } else if (pathType.equals(LOOSE)) {
            return true;
        }

        return false;
    }


}
