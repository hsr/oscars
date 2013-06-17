package net.es.oscars.pss.beans.config;


import java.util.HashMap;

public class TemplateConfig {
    private HashMap<String, String> templates;
    public HashMap<String, String> getTemplates() {
        if (templates == null) {
            templates = new HashMap<String, String>();
        }
        return templates;
    }

    public void setTemplates(HashMap<String, String> templates) {
        this.templates = templates;
    }
}
