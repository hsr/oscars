package net.es.oscars.pss.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateUtils {
    private static Logger log = Logger.getLogger(TemplateUtils.class);
    
    @SuppressWarnings("rawtypes")
    public static String generateConfig(Map root, String templateFile) throws PSSException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        String templateDir;
        try {
            templateDir = cc.getFilePath("templateDir");
        } catch (ConfigException e) {
            throw new PSSException(e);
        }
        String config = "";
        Template temp = null;
        Configuration cfg = new Configuration();
        try {
            cfg.setDirectoryForTemplateLoading(new File(templateDir));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Writer out = new StringWriter();
            temp = cfg.getTemplate(templateFile);
            temp.process(root, out);
            out.flush();
            config = out.toString();
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        } catch (TemplateException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        } catch (Exception e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        }
        return config;
    }
}
