package net.es.oscars.nsibridge.config.oscars;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.es.oscars.nsibridge.config.JsonConfigProvider;
import net.es.oscars.nsibridge.config.http.HttpConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

public class JsonOscarsConfigProvider  extends JsonConfigProvider implements OscarsConfigProvider {
    private HashMap<String, OscarsConfig> configs = new HashMap<String, OscarsConfig>();


    public void loadConfig() throws Exception {

        File configFile = new File(this.getFilename());
        String json = FileUtils.readFileToString(configFile);
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, HttpConfig>>() {}.getType();

        configs = gson.fromJson(json, type);
    }

    public OscarsConfig getConfig(String id) {
        return configs.get(id);
    }
}
