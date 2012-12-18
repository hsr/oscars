package net.es.oscars.notifycmdexec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mortbay.jetty.handler.AbstractHandler;

public class NotifyExecHandler extends AbstractHandler{
    private String setupCommand = null;
    private String teardownCommand = null;
    
    final private String SETUP_EVENT = "PATH_SETUP_COMPLETED";
    final private String TEARDOWN_EVENT = "PATH_TEARDOWN_COMPLETED";
    final private String CANCEL_EVENT = "RESERVATION_CANCEL_COMPLETED";
    
    public void handleNotify(Element notification) {
        XMLOutputter outputter = new XMLOutputter();
        try {
            outputter.output(notification, System.out);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public void handleError(String type, Exception e) {
        System.err.println("An error occurred!");
        System.err.println("    Error Type: " + type);
        System.err.println("    Error Msg: " + e.getMessage());
    }

    public void handle(String target, HttpServletRequest request,
            HttpServletResponse response, int dispatch) throws IOException,
            ServletException {
        
        HashMap<String, String> circuitParams = new HashMap<String, String>(); 
        String msgString = "";
        String line = null;
        while((line = request.getReader().readLine()) != null){
            //System.out.println(line);
            msgString += line;
        }
        System.out.println();
        
        SAXBuilder builder = new SAXBuilder(false);
        Element eventTypeElem = null;
        try {
            Document doc = builder.build(new ByteArrayInputStream(msgString.getBytes()));
            XPath xpath = XPath.newInstance("//*[local-name() = 'event']/*[local-name() = 'type']"); 
            eventTypeElem = (Element) xpath.selectSingleNode(doc);
            
            XPath vlanXpath = XPath.newInstance("//*[local-name() = 'event']" +
                    "/*[local-name() = 'resDetails']" +
                    "/*[local-name() = 'pathInfo']/*[local-name() = 'path']" +
                    "/*[local-name() = 'hop']/*[local-name() = 'link']" +
                    "/*[local-name() = 'SwitchingCapabilityDescriptors']" +
                    "/*[local-name() = 'switchingCapabilitySpecificInfo']" +
                    "/*[local-name() = 'vlanRangeAvailability']");
           List<Element> vlanList = vlanXpath.selectNodes(doc);
           if(!vlanList.isEmpty()){
               circuitParams.put("source.vlan", vlanList.get(0).getText());
               circuitParams.put("dest.vlan", vlanList.get(vlanList.size() - 1).getText());
               System.out.println("source.vlan= " + vlanList.get(0).getText());
               System.out.println("dest.vlan= " + vlanList.get(vlanList.size() - 1).getText());
           }
        } catch (JDOMException e) {
            System.err.println("Parsing error: " + e.getMessage());
        }
        
        if(eventTypeElem != null && eventTypeElem.getText() != null){
            System.out.println("EVENT TYPE: " + eventTypeElem.getText());
            this.runCommand(eventTypeElem.getText(), circuitParams);
        }

        response.setStatus(202);
        response.setContentType("Content-Type: text/xml;charset=UTF-8");
        response.setContentLength(0);
        
    }

    private void runCommand(String eventType, HashMap<String, String> circuitParams) {
        String command = null;
        if(SETUP_EVENT.equals(eventType) && setupCommand != null){
            command = this.parseCmd(setupCommand, circuitParams);
        }else if((TEARDOWN_EVENT.equals(eventType) || CANCEL_EVENT.equals(eventType)) 
                && teardownCommand != null){
            command = this.parseCmd(teardownCommand, circuitParams);
        }else{
            System.out.println("No command to run\n");
            return;
        }
        
        //run command
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }
    
    private String parseCmd(String cmd,
            HashMap<String, String> circuitParams) {
        for(String param : circuitParams.keySet()){
            cmd = cmd.replaceAll("\\%\\{" + param + "\\}", circuitParams.get(param));
        }
        System.out.println("Running command: " + cmd);
        System.out.println();
        return cmd;
    }

    /**
     * @return the setupCommand
     */
    public String getSetupCommand() {
        return this.setupCommand;
    }

    /**
     * @param setupCommand the setupCommand to set
     */
    public void setSetupCommand(String setupCommand) {
        this.setupCommand = setupCommand;
    }

    /**
     * @return the teardownCommand
     */
    public String getTeardownCommand() {
        return this.teardownCommand;
    }

    /**
     * @param teardownCommand the teardownCommand to set
     */
    public void setTeardownCommand(String teardownCommand) {
        this.teardownCommand = teardownCommand;
    }
}
