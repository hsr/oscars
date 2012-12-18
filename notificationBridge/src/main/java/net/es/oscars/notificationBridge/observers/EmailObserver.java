package net.es.oscars.notificationBridge.observers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.notificationBridge.NotificationBridgeObservable;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.NMWGParserUtil;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

public class EmailObserver implements Observer{
    private Logger log = Logger.getLogger(EmailObserver.class);
    private String mailTemplateDir;
    private String localhostname;
    private String fromAddress;
    private List<String> toAddresses;
    private Session session;
    
    final private String PROP_FROM = "mail.from";
    final private String PROP_TO = "mail.to";
    final private String PROP_TO_ADDR = "address";
    
    public EmailObserver() throws ConfigException{
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_NOTIFY);
        this.mailTemplateDir = cc.getFilePath(ServiceNames.SVC_NOTIFY, "mailTemplateDir");
        
        //set session
        this.session = Session.getDefaultInstance(new Properties());
        
        //we will load these later
        this.fromAddress = null;
        this.toAddresses = null;
        
        //Get the host name
        try {
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            this.localhostname = localMachine.getHostName();
        } catch (java.net.UnknownHostException uhe) {
            this.localhostname = "host: unknown";
        }
    }
    
    /**
     * Observer method called whenever a change occurs. It accepts an 
     * Observable object and an net.es.oscars.notify.OSCARSEvent object as
     * arguments. Sends email notifications if template exists for
     * event.
     *
     * @param obj the observable object
     * @param arg the event that occurred
     */
    public void update (Observable obs, Object eventObj) {
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.info(netLog.start("EmailObserver.update"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        try{
            synchronized(this){
                this.loadAddresses(((NotificationBridgeObservable)obs).getConfig());
            }
            netLogProps.put("to", OSCARSNetLogger.serializeList(this.toAddresses));
            netLogProps.put("from", this.fromAddress);
            
            EventContent event = (EventContent) eventObj;
            String fname = this.mailTemplateDir + File.separator + event.getType() + ".xml";
            netLogProps.put("template", fname);
            if(!(new File(fname)).exists()){
                this.log.info(netLog.end("EmailObserver.update", 
                        "No template for event so no email to send.", null, netLogProps));
                return;
            }
            String contentType = "text/plain";
            String line = null;
            String template = null;
            BufferedReader reader = new BufferedReader(new FileReader(fname));
            while((line = reader.readLine()) != null){
                template += (line + "\n");
            }
            
            //replace elements
            String msg = this.applyTemplate(template, event);
            
            //split body and subject
            Pattern typePat = Pattern.compile("<contentType>(.+)</contentType>");
            Pattern subjPat = Pattern.compile("<subject>(.+)</subject>");
            Pattern bodyPat = Pattern.compile(
                "<messageBody>([\\w\\W]+)</messageBody>");
            Matcher subjMat = subjPat.matcher(msg);
            Matcher bodyMat = bodyPat.matcher(msg);
            Matcher typeMat = typePat.matcher(msg);
            
            String subject = null;
            String body = null;
    
            if(subjMat.find()){
                subject = subjMat.group(1);
            }else{
                subject = "[ALERT] no subject";
            }
    
            if(bodyMat.find()){
                body = bodyMat.group(1);
                //remove leading whitespace
                body = body.replaceFirst("^\\s+","");
                body += "\n";
            }else{
                body = "no message body\n";
            }
    
            if(typeMat.find()){
                contentType = typeMat.group(1);
            }
            
            //send mail
            this.sendMessage(subject, body, contentType);
        }catch(Exception e){
            e.printStackTrace();
            this.log.error(netLog.error("EmailObserver.update", ErrSev.CRITICAL, 
                    e.getMessage(), null, netLogProps));
        }
        this.log.info(netLog.end("EmailObserver.update", null, null, netLogProps));
    }
    
    private void loadAddresses(Map config) throws ConfigException {
        if(this.fromAddress != null && this.toAddresses != null){
            return;
        }
        
        if(config.containsKey(PROP_FROM)){
            this.fromAddress = config.get(PROP_FROM)+"";
        }else{
            throw new ConfigException("Unable to send email notification because " + PROP_FROM + " is not set.");
        }
        
        if(!config.containsKey(PROP_TO)){
            throw new ConfigException("Unable to send email notification because " + PROP_TO + " is not set.");
        }
        
        List<Map> toAddrs = (List<Map>) config.get(PROP_TO);
        this.toAddresses = new ArrayList<String>();
        for (Map addrMap : toAddrs){
            if(addrMap.containsKey(PROP_TO_ADDR)){
                this.toAddresses.add(addrMap.get(PROP_TO_ADDR)+"");
            }
        }
        if(this.toAddresses.isEmpty()){
            this.toAddresses = null;
            throw new ConfigException("Unable to send email notification because no " + 
                    PROP_TO_ADDR + " provided");
        }
    }

    /**
     * Given a template as a string this replaces all the dynamic fields with
     * values in the given event object.
     *
     * @param template String of the template containing the fields to replace
     * @param event the event containing the values to fill-in
     * @return the template with all dynamic fields replaced
     * @throws OSCARSServiceException 
     */
    private String applyTemplate(String template, EventContent event) throws OSCARSServiceException{
        String msg = template;
        
        //format event time
        String eventTime = this.formatTime(event.getTimestamp());
        
        //the next block is just extracting the originator from message properties
        // the format of the makes it look more complicated that it really is
        String originator = "";
        if(event.getMessageProperties() != null && 
                event.getMessageProperties().getOriginator() != null &&
                event.getMessageProperties().getOriginator().getSubjectAttribute() != null){
            for(AttributeType attr : event.getMessageProperties().getOriginator().getSubjectAttribute()){
                if(AuthZConstants.LOGIN_ID.equals(attr.getName()) && 
                        attr.getAttributeValue() != null &&
                        !attr.getAttributeValue().isEmpty()){                        
                    originator = attr.getAttributeValue().get(0).toString();
                }
            }
        }
        
        //NOTE: There are more efficient ways to parse and replace fields
        //but this seems to work for now.
        msg = this.replaceTemplateField("##event##", event.toString(), msg);
        msg = this.replaceTemplateField("##eventType##", event.getType(), msg);
        msg = this.replaceTemplateField("##eventTimestamp##", eventTime, msg);
        msg = this.replaceTemplateField("##eventUserLogin##", originator, msg);
        msg = this.replaceTemplateField("##eventSource##", event.getErrorSource(), msg);
        msg = this.replaceTemplateField("##errorCode##", event.getErrorCode(), msg);
        msg = this.replaceTemplateField("##errorMessage##", event.getErrorMessage(), msg);
        
        if(event.getResDetails() != null){
            
            long startTime = 0;
            long endTime = 0;
            long bandwidth = 0;
            PathInfo pathInfo = null;
            if(event.getResDetails().getReservedConstraint() != null){
                startTime = event.getResDetails().getReservedConstraint().getStartTime();
                endTime = event.getResDetails().getReservedConstraint().getStartTime();
                bandwidth = event.getResDetails().getReservedConstraint().getBandwidth();
                pathInfo = event.getResDetails().getReservedConstraint().getPathInfo();
            }else if(event.getResDetails().getUserRequestConstraint() != null){
                startTime = event.getResDetails().getUserRequestConstraint().getStartTime();
                endTime = event.getResDetails().getUserRequestConstraint().getStartTime();
                bandwidth = event.getResDetails().getUserRequestConstraint().getBandwidth();
                pathInfo = event.getResDetails().getUserRequestConstraint().getPathInfo();
            }else{
                throw new OSCARSServiceException("Event does not conatin any constraints");
            }
            
            String startTimeStr = this.formatTime(startTime);
            String endTimeStr = this.formatTime(endTime);
            String createdTimeStr = this.formatTime(event.getResDetails().getCreateTime());
            msg = this.replaceResvField("##reservation##", msg);
            msg = this.replaceTemplateField("##gri##", 
                    event.getResDetails().getGlobalReservationId(), msg);
            msg = this.replaceTemplateField("##startTime##", startTimeStr, msg);
            msg = this.replaceTemplateField("##endTime##", endTimeStr, msg);
            msg = this.replaceTemplateField("##createdTime##", createdTimeStr, msg);
            msg = this.replaceTemplateField("##bandwidth##", 
                                            bandwidth+"", msg);
            msg = this.replaceTemplateField("##resvUserLogin##", 
                                            event.getResDetails().getLogin(), msg);
            msg = this.replaceTemplateField("##status##", 
                                            event.getResDetails().getStatus(), msg);
            msg = this.replaceTemplateField("##description##", 
                                            event.getResDetails().getDescription(), msg);
            msg = this.applyUserDefinedTags(event.getResDetails().getDescription(), msg);
            msg = this.replaceTemplateField("##pathSetupMode##", 
                                            pathInfo.getPathSetupMode(), msg);
            msg = this.replaceTemplateField("##pathType##", 
                                            pathInfo.getPathType(), msg);
            if(pathInfo.getLayer2Info() != null){
                msg = this.replaceTemplateField("##source##", 
                                            pathInfo.getLayer2Info().getSrcEndpoint(), msg);
                msg = this.replaceTemplateField("##destination##", 
                                            pathInfo.getLayer2Info().getDestEndpoint(), msg);
                if(pathInfo.getLayer2Info().getSrcVtag() != null){
                    msg = this.replaceTemplateField("##srcVtag##", 
                                            pathInfo.getLayer2Info().getSrcVtag().getValue(), msg);
                    msg = this.replaceTemplateField("##tagSrcPort##", 
                        pathInfo.getLayer2Info().getSrcVtag().isTagged()+"", msg);
                }
                if(pathInfo.getLayer2Info().getDestVtag() != null){
                    msg = this.replaceTemplateField("##destVtag##", 
                                            pathInfo.getLayer2Info().getDestVtag().getValue()+"", msg);
                    msg = this.replaceTemplateField("##tagDestPort##", 
                                            pathInfo.getLayer2Info().getDestVtag().isTagged()+"", msg);
                }
            }
            if(pathInfo.getLayer3Info() != null){
                msg = this.replaceTemplateField("##source##", 
                        pathInfo.getLayer3Info().getSrcHost(), msg);
                msg = this.replaceTemplateField("##destination##", 
                        pathInfo.getLayer3Info().getDestHost(), msg);
                msg = this.replaceTemplateField("##protocol##", 
                        pathInfo.getLayer3Info().getProtocol(), msg);
                msg = this.replaceTemplateField("##dscp##", 
                        pathInfo.getLayer3Info().getDscp(), msg);
            }
            msg = this.replaceTemplateField("##path##",
                                            this.formatPath(pathInfo.getPath()), msg);
        }else{
            //need to clear out template objects so aren't in sent messages
            msg = this.replaceTemplateField("##reservation##", "", msg);
            msg = this.replaceTemplateField("##gri##", "", msg);
            msg = this.replaceTemplateField("##startTime##", "", msg);
            msg = this.replaceTemplateField("##endTime##", "", msg);
            msg = this.replaceTemplateField("##createdTime##", "", msg);
            msg = this.replaceTemplateField("##bandwidth##", "", msg);
            msg = this.replaceTemplateField("##resvUserLogin##", "", msg);
            msg = this.replaceTemplateField("##status##", "", msg);
            msg = this.replaceTemplateField("##description##", "", msg);
            msg = this.replaceTemplateField("##pathSetupMode##", "", msg);
            msg = this.replaceTemplateField("##isExplicitPath##", "", msg);
            msg = this.replaceTemplateField("##nextDomain##", "", msg);
            msg = this.replaceTemplateField("##source##", "", msg);
            msg = this.replaceTemplateField("##destination##", "", msg);
            msg = this.replaceTemplateField("##srcVtag##", "", msg);
            msg = this.replaceTemplateField("##destVtag##", "", msg);
            msg = this.replaceTemplateField("##tagSrcPort##", "", msg);
            msg = this.replaceTemplateField("##tagDestPort##", "", msg);
            msg = this.replaceTemplateField("##srcPort##", "", msg);
            msg = this.replaceTemplateField("##destPort##", "", msg);
            msg = this.replaceTemplateField("##protocol##", "", msg);
            msg = this.replaceTemplateField("##dscp##", "", msg);
            msg = this.replaceTemplateField("##burstLimit##", "", msg);
            msg = this.replaceTemplateField("##lspClass##", "", msg);
            msg = this.replaceTemplateField("##interdomainPath##", "", msg);
            msg = this.replaceTemplateField("##intradomainPath##", "", msg);
            msg = this.replaceTemplateField("##interdomainDetailPath##", "", msg);
            msg = this.replaceTemplateField("##intradomainDetailPath##", "", msg);
        }
        
        return msg;
    }
    
    private String[] formatPath(CtrlPlanePathContent path) {
        if(path == null || path.getHop() == null){
            return new String[0];
        }
        String[] detailPath = new String[path.getHop().size()];
        int i = 0;
        for(CtrlPlaneHopContent hop : path.getHop()){
            detailPath[i] = NMWGParserUtil.getURN(hop);
            if(hop.getLink() != null && hop.getLink().getSwitchingCapabilityDescriptors() != null
                    && hop.getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo() != null
                    &&  hop.getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability() != null ){
                String vlan = hop.getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
                detailPath[i] += vlan.equals("0") ? "UNTAGGED" : vlan;
            }
            i++;
        }
        return detailPath;
    }

    /**
     * Convenience method for replacing a single value in a template
     *
     * @param field the field to replace
     * @param value the String[] value with which to replace the field
     * @param template the template on which the replacement will be made
     * @return the template with replaced fields
     */
    private String replaceTemplateField(String field, String[] value, String template){
        //clear out fields if value is null
        if(value == null){
            return template.replaceAll(field, "");
        }
        
        String delim = (value.length > 1 ? "\n" : "");
        String strValue = "";
        for(int i = 0; i < value.length; i++){
            strValue += (value[i] + delim);
        }
        
        return template.replaceAll(field, strValue);
    }
    
    /**
     * Convenience method for replacing a single value in a template
     *
     * @param field the field to replace
     * @param value the value with which to replace the field
     * @param template the template on which the replacement will be made
     * @return the template with replaced fields
     */
    private String replaceTemplateField(String field, String value, String template){
        String msg = template;
        
        //clear out fields if value is null
        if(value == null){
            value = "";
        }
        msg = template.replaceAll(field, value);
        
        return msg;
    }
    
    /**
     * Convenience method for replacing a an entire reservation
     *
     * @param field the field to replace
     * @param resv the reservation fields
     * @param template the template on which the replacement will be made
     * @return the template with replaced fields
     */
    private String replaceResvField(String field, String template){       
        String resvTemplate = "";
        resvTemplate += "GRI: ##gri##\n";
        resvTemplate += "description: ##description##\n";
        resvTemplate += "login: ##resvUserLogin##\n";
        resvTemplate += "status: ##status##\n";
        resvTemplate += "start time: ##startTime##\n";
        resvTemplate += "end time: ##endTime##\n";
        resvTemplate += "bandwidth: ##bandwidth##\n";
        resvTemplate += "path setup mode: ##pathSetupMode##\n";
        resvTemplate += "source: ##source##\n";
        resvTemplate += "destination: ##destination##\n";
        resvTemplate += "source VLAN tag: ##srcVtag##\n";
        resvTemplate += "source tagged: ##tagSrcPort##\n";
        resvTemplate += "destination VLAN tag: ##destVtag##\n";
        resvTemplate += "destination tagged: ##tagDestPort##\n";
        resvTemplate += "protocol: ##protocol##\n";
        resvTemplate += "src IP port: ##srcPort##\n";
        resvTemplate += "dest IP port: ##destPort##\n";
        resvTemplate += "dscp: ##dscp##\n";
        resvTemplate += "path: \n\n ##path##\n";
        
        return template.replaceAll(field, resvTemplate);
    }
    
    /**
     * Applies user-defined tags specified in ##TAG:<i>TAG_NAME</i>## fields to
     * a given template.
     *
     * @param description the description of the reservation that may contain tags
     * @param template the template to which in which tags may be shown
     * @return the template with all user-defined tags displayed
     */
    private String applyUserDefinedTags(String description, String template){
        Pattern tagPattern = Pattern.compile("##TAG:(.+?)##");
        Matcher tagMatcher = tagPattern.matcher(template);
        
        if(description == null){
            return template;
        }
        
        while(tagMatcher.find()){
            String tag = tagMatcher.group(1);
            String printTag = "[" + tag + "]";
            if(description.contains(printTag)){
                template = template.replaceAll("##TAG:" + tag + "##", 
                    printTag + " ");
            }else{
                template = template.replaceAll("##TAG:" + tag + "##", "");
            }
        }
        
        return template;
    }
    
    /**
     * Sends an email message with the given parameters.
     *
     * @param subject the subject of the email to send
     * @param notification the body of the email to send
     * @param contentType the type of message (i.e. text/plain, text/html)
     */
    public void sendMessage(String subject, String notification, 
        String contentType) throws javax.mail.MessagingException {

        subject += " ("+this.localhostname+")";
        // Define message
        MimeMessage message = new MimeMessage(this.session);
        message.setFrom(new InternetAddress(this.fromAddress));
        for (String to: this.toAddresses) {
            message.addRecipient(Message.RecipientType.TO,
                                 new InternetAddress(to));
        }
        message.setSubject(subject);
        message.setContent(notification, contentType);
        Transport.send(message);   // Send message
    }
    
    /**
     * Returns string formatted UTC datetime
     *
     * @param timestamp a long containing the timestamp
     * @return string-formatted datetime
     */
    private String formatTime(long timestamp){
        DateFormat df = DateFormat.getInstance();
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String time = df.format(timestamp*1000L) + " UTC";
        return time;
    }

}
