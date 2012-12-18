package net.es.oscars.pss.eompls.beans.config;

import net.es.oscars.pss.beans.config.GenericConfig;



public class EoMPLSBaseConfig {

    
    private GenericConfig eomplsDeviceAddressResolve;
    private GenericConfig eomplsIfceAddressResolve;
    private DatabaseConfig database;
    
    public GenericConfig getEomplsDeviceAddressResolve() {
        return eomplsDeviceAddressResolve;
    }
    public void setEomplsDeviceAddressResolve(
            GenericConfig eomplsDeviceAddressResolve) {
        this.eomplsDeviceAddressResolve = eomplsDeviceAddressResolve;
    }
    public GenericConfig getEomplsIfceAddressResolve() {
        return eomplsIfceAddressResolve;
    }
    public void setEomplsIfceAddressResolve(GenericConfig eomplsIfceAddressResolve) {
        this.eomplsIfceAddressResolve = eomplsIfceAddressResolve;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

}
