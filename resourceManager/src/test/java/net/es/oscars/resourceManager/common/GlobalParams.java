package net.es.oscars.resourceManager.common;

/**
 *  This class contains static methods returning properties that are required
 *  by all tests, but unlikely the user will want to change.  Using this also
 *  requires less set up than for properties.
 */
public class GlobalParams {

    public static String getTestDbName() {
        return "testrm";
    }
}
