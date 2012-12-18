package net.es.oscars.pss.eompls.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;

public class EoMPLSUtils {
    private static Logger log = Logger.getLogger(EoMPLSUtils.class);

    public static String getDeviceId(ResDetails res, boolean reverse) throws PSSException {
        ReservedConstraintType rc = res.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();
        List<CtrlPlaneHopContent> localHops;
        try {
            localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            throw new PSSException(e);
        }
       
        CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
        CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();
        
        String srcLinkId = ingressLink.getId();
        URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
        String dstLinkId = egressLink.getId();
        URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
        String srcDeviceId = srcRes.getNodeId();
        String dstDeviceId = dstRes.getNodeId();
        
        if (reverse) return dstDeviceId;
        return srcDeviceId;
    }

    public static String genJunosVCId(String portId, String vlanId) throws PSSException {

        Pattern pattern =  Pattern.compile(".*(\\d).(\\d).(\\d).*");
        Matcher matcher =  pattern.matcher(portId);
        String x = null;
        String y = null;
        String z = null;
        
        while (matcher.find()){
            x = matcher.group(1);
            y = matcher.group(2);
            z = matcher.group(3); 
        }
        if (x == null || y == null || z == null) {
            pattern =  Pattern.compile("ae(\\d+)");
            matcher =  pattern.matcher(portId);
            while (matcher.find()){
                z = matcher.group(1);
            }
            if (z == null) {
                throw new PSSException("could not decide a l2circuit vcid!");
            }
            x = "9"; y = "9"; 
        }
        // can't lead with zeros, junos thinks it's an octal
        if (x.equals("0")) {
            x = "10";
        }
        return(x+y+z+vlanId);
    }

    public static String genIOSVCId(String portId, String vlanId) throws PSSException {

        Pattern pattern =  Pattern.compile(".*(\\d).(\\d).*");
        Matcher matcher =  pattern.matcher(portId);
        String x = null;
        String y = null;
        
        while (matcher.find()){
            x = matcher.group(1);
            y = matcher.group(2);
        }
        if (x == null || y == null ) {
            throw new PSSException("could not decide a l2circuit vcid!");
        }
        // can't lead with zeros, junos thinks it's an octal
        if (x.equals("0")) {
            x = "10";
        }
        return(x+y+vlanId);
    }
    
    @SuppressWarnings("rawtypes")
    public static String generateConfig(Map root, String templateFile) throws PSSException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        String templateDir;
        try {
            String context = cc.getContext();
            templateDir = cc.getFilePath("templateDir");
            log.debug("templates for context "+context+" at:"+templateDir);
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
