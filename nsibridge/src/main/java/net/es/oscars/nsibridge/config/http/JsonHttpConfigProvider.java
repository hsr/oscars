package net.es.oscars.nsibridge.config.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.es.oscars.nsibridge.config.JsonConfigProvider;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

public class JsonHttpConfigProvider  extends JsonConfigProvider implements HttpConfigProvider {


    private HashMap<String, HttpConfig> configs = new HashMap<String, HttpConfig>();


    public void loadConfig() throws Exception {

        File configFile = new File(this.getFilename());
        String json = FileUtils.readFileToString(configFile);
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, HttpConfig>>() {}.getType();

        configs = gson.fromJson(json, type);
    }

    public HttpConfig getConfig(String id) {
        return configs.get(id);
    }

}