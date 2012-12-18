package net.es.oscars.coord.common;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.utils.config.ConfigHelper;

public class CoordConfigHolder {
    @SuppressWarnings("unchecked")
    private HashMap config;
    private static CoordConfigHolder instance;

    @SuppressWarnings("unchecked")
    private CoordConfigHolder() {
        this.config = new HashMap();
    }
    public static CoordConfigHolder getInstance() {
        if (instance == null) {
            instance = new CoordConfigHolder();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public HashMap getConfig() {
        return config;
    }

    @SuppressWarnings("unchecked")
    public void loadConfig(String filename) {
        Map tempConfig = ConfigHelper.getConfiguration(filename);
        this.config = new HashMap();
        this.config.putAll(tempConfig);

    }

}
