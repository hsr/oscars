package net.es.oscars.coord.workers;

public class StoreWorker extends ModuleWorker {

    private static StoreWorker instance;
    public static StoreWorker getInstance() {
        if (instance == null) {
            instance = new StoreWorker();
        }
        return instance;
    }

}
