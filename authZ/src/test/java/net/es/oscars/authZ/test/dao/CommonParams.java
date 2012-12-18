package net.es.oscars.authZ.test.dao;

/**
 * This class handles common parameter settings for tests.
 */
public class CommonParams {
    public static String getLogin() {
        return "testUser";
    }

    // temporary database, not a database user, just testing db ops on tables
    public static String getPassword() {
        return "testPassword";
    }

    public static String getAttributeValue() {
        return "attributeValue";
    }

    public static String getAttributeId() {
        return "role";
    }

    public static String getResourceName() {
        return "resourceName";
    }

    public static String getPermissionName() {
        return "permissionName";
    }

    public static String getConstraintName() {
        return "constraintName";
    }
}
