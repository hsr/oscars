package net.es.oscars.resourceManager.beans;

public class ConstraintType {
    public static final String USER = "user";
    public static final String RESERVED = "reserved";
    public static final String OPTIONAL = "optional";
    
    // never construct this object
    private ConstraintType() {
    }

    public static boolean isValid(String constraintType) {
        if (constraintType.equals(USER)) {
            return true;
        } else if (constraintType.equals(RESERVED)) {
            return true;
        } else if (constraintType.equals(OPTIONAL)) {
            return true;
        }
        return false;
    }
    public static boolean isStandard(String constraintType)  {
        if (constraintType.equals(USER)) {
            return true;
        } else if (constraintType.equals(RESERVED)) {
            return true;
        }
        return false;
    }
    public static boolean isOptional(String constraintType) {
         if (constraintType.equals(OPTIONAL)) {
            return true;
        }
        return false;
    }

}
