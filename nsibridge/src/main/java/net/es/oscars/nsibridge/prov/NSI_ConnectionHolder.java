package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.NSIConnection;

import java.util.ArrayList;
import java.util.List;

public class NSI_ConnectionHolder {
    private ArrayList<NSIConnection> connections = new ArrayList<NSIConnection>();

    private static NSI_ConnectionHolder instance;
    private NSI_ConnectionHolder() {}
    public static NSI_ConnectionHolder getInstance() {
        if (instance == null) instance = new NSI_ConnectionHolder();
        return instance;
    }


    public List<NSIConnection> getConnections() {
        return this.connections;
    }

    public NSIConnection findConnection(String connectionId) {
        for (NSIConnection conn : connections) {
            if (conn.getConnectionId().equals(connectionId)) {
                return conn;
            }
        }
        return null;
    }



}
