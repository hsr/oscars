package net.es.oscars.logging;

import gov.lbl.netlogger.LogMessage;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;

/**
 * Generates log messages that adhere to logging practices agreed to by the OSCARS development team.
 * The messages generated follow the form defined by the NetLogger best practices document 
 * (http://www.cedps.net/index.php/LoggingBestPractices). The guidelines state that each
 * entry in the log consist of a space-delimited list of key/value pairs in the form <i>key=value</i>.At a 
 * minimum this class should be instantiated when an incoming request is received by a service. Global fields 
 * such as the <code>gri</code> and <code>user</code> can be set and displayed in each log message. Upon 
 * instantiation a <i>guid</i> can be input which can act as an identifier to link log entries from a 
 * single transaction.At this point an additional <i>localId</i> will be generated that will link the
 * log entires within a single service.
 * In order to keep these ids consistent this class should be passed to any class 
 * within an individual module that will generate log messages. 
 *
 */
public class OSCARSNetLogger {
    private String moduleName = null;
    HashMap<String, String> fieldMap;
    
    static public final String GUID_KW = "guid";        // global transaction id, one per OSCARS multi-domain request
    //static public final String LOCALID_KW = "local.id"; // per service operation id
    static public final String GRI_KW = "gri";          // globalReservationId
    static public final String PEER_IDC_KW = "peerIdc";
    static public final String STATUS_KW = "status";
    static public final String MSG_KW = "msg";
    static public final String IDC_MSG_TYPE_KW = "idcMsgType";
    static public final String ORIG_USER_KW = "origUser";
    static public final String ERROR_SRC_KW = "errorSource";
    static public final String ERROR_SEVERITY_KW = "errSeverity";
    static public final String URL_KW = "url";
    
    private static ThreadLocal<OSCARSNetLogger> LOG = new ThreadLocal<OSCARSNetLogger>() {   
        protected OSCARSNetLogger initialValue() {
            return new OSCARSNetLogger();
        }
    };
    
    public static void setTlogger(OSCARSNetLogger log){
        LOG.set(log);
    }
    public static OSCARSNetLogger getTlogger() {
        return (LOG.get());
    }

    /**
     * null constructor for use by the ThreadLocal initialValue method
     */
    public OSCARSNetLogger() {
        this.fieldMap = new HashMap<String, String>();
        //this.fieldMap.put(LOCALID_KW, UUID.randomUUID().toString());
        this.fieldMap.put(GUID_KW,"null");
    }

    /**
     * Constructor that accepts a module name used to prefix events.
     * 
     * @param moduleName a String used to identify the module generating Log 
     *                   messages. This field usually takes the form 
     *                   <i>net.es.oscars.Module</i>. The <code>ModuleName</code>
     *                   class contains constants for the standard OSCARS module names.
     */
    public OSCARSNetLogger(String moduleName){
        this.moduleName = moduleName;
        this.fieldMap = new HashMap<String, String>();
        //this.fieldMap.put(LOCALID_KW, UUID.randomUUID().toString());
        this.fieldMap.put(GUID_KW,"null");
    }
    
    /**
     * Constructor that accepts a module name used to prefix events and an ID that 
     * identifies the global transaction. It also generates an I to identify the local 
     * operation.
     * 
     * @param moduleName a String used to identify the module generating Log 
     *                   messages. This field usually takes the form 
     *                   <i>net.es.oscars.Module</i>. The <code>ModuleName</code>
     *                   class contains constants for the standard OSCARS module names.
     * @param guid a UUID that identifies the global transaction.
     */
    public OSCARSNetLogger(String moduleName, String guid){
        this.moduleName = moduleName;
        this.fieldMap = new HashMap<String, String>();
        this.fieldMap.put(GUID_KW, guid);
        //this.fieldMap.put(LOCALID_KW, UUID.randomUUID().toString());
    }
    
    /**
     *  Called when a thread starts, since threads are sometimes reused, it
     *  needs to cleanup the fieldMap
     *   
     * @param moduleName module that the thread is executing
     * @param tranId - GlobalTransaction id for this transactoin
     */
    public void init (String moduleName, String tranId){
        this.fieldMap = new HashMap<String, String>();
        //this.fieldMap.put(LOCALID_KW, UUID.randomUUID().toString());
        this.moduleName = moduleName;
        if (tranId != null ){
            this.fieldMap.put(GUID_KW, tranId);
        } else {
            this.fieldMap.put(GUID_KW,"null");
        }
    }
    /**
     * Generates an event suffixed with ".end" and status 0. This method sets the
     * <i>event</i> field.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage end(String event){
        return this.end(event, null, null);
    }
    
    /**
     * Generates an event suffixed with ".end" with status 0. This method sets the
     * <i>event</i> and <i>msg</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage end(String event, String msg){
        return this.end(event, msg, null);
    }
    
    /**
     * Generates an event suffixed with ".end". This method sets the
     * <i>event</i>, <i>msg</i>, and <i>url</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL  to a local file or the JDBC 
     *        URL that identifies a database.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage end(String event, String msg, String url){
        return this.end(event, msg, url, null);
    }
    
    /**
     * Generates an event suffixed with ".end". This method sets the
     * <i>event</i>, <i>msg</i>, and <i>url</i> fields.
     * It also accepts a HashMap that defines additional fields not 
     * specified by the other arguments.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL  to a local file or the JDBC 
     *        URL that identifies a database.
     * @param entryFieldMap a HashMap with additional fields not defined by the 
     *                      other arguments. The HashMap key and value correspond 
     *                      the key and value in the NetLogger field. If a field is 
     *                      used frequently it is recommended that this class be 
     *                      sub-classed as opposed to passing a HashMap.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage end(String event, String msg, String url, HashMap<String, String> entryFieldMap){
        if(entryFieldMap == null){
            entryFieldMap = new HashMap<String, String>();
        }
        entryFieldMap.put(STATUS_KW, "0");
        if(msg != null){
            entryFieldMap.put(MSG_KW, msg);
        }
        if(url != null){
            entryFieldMap.put(URL_KW, url);
        }
        
        return this.getMsg(event+".end", entryFieldMap);
    }
    
    /**
     * Generates an event suffixed with ".end" and status -1. This method sets the
     * <i>event</i>, <i>errSeverity</i> and <i>msg</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param errSeverity a String indicating the severity of the error.
     * @param msg a String containing additional information about the error
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage error(String event, String errSeverity, String msg){
        return this.error(event, errSeverity, msg, null, null);
    }
    
    /**
     * Generates an event suffixed with ".end" and status -1. This method sets the
     * <i>event</i>, <i>errSeverity</i>, <i>msg</i>, and <i>url</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param errSeverity a String indicating the severity of the error.
     * @param msg a String containing additional information about the error
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL to a local file or the JDBC 
     *        URL that identifies a database.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage error(String event, String errSeverity, String msg, String url){
        return this.error(event, errSeverity, msg, url, null);
    }
    
    /**
     * Generates an event suffixed with ".end" and status -1. This method sets the
     * <i>event</i>, <i>errSeverity</i>, <i>msg</i>, and <i>url</i> fields.
     * It also accepts a HashMap that defines additional fields not 
     * specified by the other arguments.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param errSeverity a String indicating the severity of the error.
     * @param msg a String containing additional information about the error
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL to a local file or the JDBC 
     *        URL that identifies a database.
     * @param entryFieldMap a HashMap with additional fields not defined by the 
     *                      other arguments. The HashMap key and value correspond 
     *                      the key and value in the NetLogger field. If a field is 
     *                      used frequently it is recommended that this class be 
     *                      sub-classed as opposed to passing a HashMap.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage error(String event, String errSeverity, String msg, String url, HashMap<String, String> entryFieldMap){
        if(entryFieldMap == null){
            entryFieldMap = new HashMap<String, String>();
        }
        
        entryFieldMap.put(STATUS_KW, "-1");
        
        if(errSeverity != null){
            entryFieldMap.put(ERROR_SEVERITY_KW, errSeverity);
        }
        
        if(msg != null){
            entryFieldMap.put(MSG_KW, msg);
        }
        if(url != null){
            entryFieldMap.put(URL_KW, url);
        }
        
        return this.getMsg(event+".end", entryFieldMap);
    }
    
    /**
     * Generates an event suffixed with ".start". This method sets the
     * <i>event</i> field.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".start".
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage start(String event){
        return this.start(event, null, null, null);
    }
    
    /**
     * Generates an event suffixed with ".start". This method sets the
     * <i>event</i> and <i>msg</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".start".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage start(String event, String msg){
        return this.start(event, msg, null, null);
    }
    
    /**
     * Generates an event suffixed with ".start". This method sets the
     * <i>event</i>, <i>msg</i>, and <i>url</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".start".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL  to a local file or the JDBC 
     *        URL that identifies a database.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage start(String event, String msg, String url){
        return this.start(event, msg, url, null);
    }
    
    /**
     * Generates an event suffixed with ".start". This method sets the
     * <i>event</i>, <i>msg</i>, and <i>url</i> fields. It also accepts
     * a HashMap that defines additional fields not specified by the other
     * arguments.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".start".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL  to a local file or the JDBC 
     *        URL that identifies a database.
     * @param entryFieldMap a HashMap with additional fields not defined by the 
     *                      other arguments. The HashMap key and value correspond 
     *                      the key and value in the NetLogger field. If a field is 
     *                      used frequently it is recommended that this class be 
     *                      sub-classed as opposed to passing a HashMap.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage start(String event, String msg, String url, HashMap<String, String> entryFieldMap){
        if(entryFieldMap == null){
            entryFieldMap = new HashMap<String, String>();
        }
        if(msg != null){
            entryFieldMap.put(MSG_KW, msg);
        }
        if(url != null){
            entryFieldMap.put(URL_KW, url);
        }
        
        return this.getMsg(event+".start", entryFieldMap);
    }
    
    /**
     * Generates a logging event automatically prefixed with the module name
     * and with fields specified in the provided HashMap. It also sets the time 
     * with the current time (in milliseconds).
     *  
     * @param event the name of the event to occur.
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage getMsg(String event, String msg){
        HashMap<String,String> entryFieldMap = new HashMap<String,String>();
        if(msg != null){
            entryFieldMap.put(MSG_KW, msg);
        }
        return this.getMsg(event, entryFieldMap);
    }
    /**
     * Generates a logging event automatically prefixed with the module name
     * and with fields specified in the provided HashMap. It also sets the time 
     * with the current time (in milliseconds).
     *  
     * @param event the name of the event to occur.
     * @param entryFieldMap a HashMap with additional fields not defined by the 
     *                      other arguments. The HashMap key and value correspond 
     *                      the key and value in the NetLogger field. If a field is 
     *                      used frequently it is recommended that this class be 
     *                      sub-classed as opposed to passing a HashMap.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage getMsg(String event, HashMap<String,String> entryFieldMap){
        LogMessage logMsg = new LogMessage(this.moduleName + "." + event);
        
        /* Set fields used in all messages */
        for(String field : fieldMap.keySet()){
            if(fieldMap.get(field) != null){
                this.addField(field, fieldMap.get(field), logMsg);
            }
        }
        
        /* Set fields local to this message */
        for(String field : entryFieldMap.keySet()){
            if(entryFieldMap.get(field) != null){
                this.addField(field, entryFieldMap.get(field), logMsg);
            }
        }
        
        /* Set timestamp */
        logMsg.setTimeStampMillis(System.currentTimeMillis());
        
        return logMsg;
    }
    
    /**
     * Returns the ModuleName for this netLogger
     * 
     * @return a String with the moduleName, this will be null
     *     if the netLogger object has not been initialized
     */
    public String getModuleName() {
        return this.moduleName;
    }
    
    /**
     * Returns the UUID that identifies the global transaction
     * 
     * @return a String with the UUID included in each log entry that identifies the
     *    global transaction
     */
    public String getGUID() {
        return this.fieldMap.get(GUID_KW);
    }
    /**
     * Sets the identifier included in each log entry generated by this class.
     * The GUID is automatically generated at instantiation so this class should
     * only need to be called in special situations.
     * 
     * @param guid a String with the identifier to set. 
     */
    public void setGUID(String guid) {
        this.fieldMap.put(GUID_KW, guid);
    }
    

    /**
     * Returns the local ID which is a UUID that identifies a single atomic
     * request to a module
     * 
     * @return a UUID that identifies a single atomic request to a module

    public String getLocalId() {
        return this.fieldMap.get(LOCALID_KW);
    }
    /**
     * Sets the the local ID which is a UUID that identifies a single atomic
     * request to a module
     * 
     * @param localId a UUID that identifies a single atomic request to a module

    public void setLocalId(String localId) {
        this.fieldMap.put(LOCALID_KW, localId);
    }
    */
    
    /**
     * Returns the global reservation ID (GRI) included in each log entry. 
     * Returns null if the GRI is not set.
     * @return the current global reservation ID or NULL if not set
     */
    public String getGRI() {
        return this.fieldMap.get(GRI_KW);
    }
    /**
     * Sets the global reservation ID to be displayed in log messages. This should be called 
     * as soon as possible so the GRI can be displayed. 
     * 
     * @param gri the global reservation ID to set
     */
    public void setGRI(String gri) {
        this.fieldMap.put(GRI_KW, gri);
    }
    
    /**
     * Return the name of the IDC that sent or is being sent a request.
     *  
     * @return a String indicating the IDC that sent or is being sent a request.
     */
    public String getPeerIDC() {
        return this.fieldMap.get(PEER_IDC_KW);
    }
    /**
     * Sets the IDC that sent or is being sent a request.
     * 
     * @param peerIDC a String indicating the IDC that sent or is being sent a request.
     */
    public void setPeerIDC(String peerIDC) {
        this.fieldMap.put(PEER_IDC_KW, peerIDC);
    }
    
    /**
     * Returns the name of the user that sent the original request.
     * 
     * @return the name of the user that sent the original request
     */
    public String getOrigUser(){
        return this.fieldMap.get(ORIG_USER_KW);
    }
    
    /**
     * Sets the name of the user that sent the original request.
     * 
     * @return the name of the user that sent the original request
     */
    public void setOrigUser(String origUser){
        this.fieldMap.put(ORIG_USER_KW, origUser);
    }
    
    /**
     * Return the domain that triggered a failure or error.
     * 
     * @return the domain that triggered an error or failure.
     */
    public String getErrorSrc(){
        return this.fieldMap.get(ERROR_SRC_KW);
    }
    
    /**
     * Sets the domain that triggered a failure or error.
     * 
     * @param errorSrc the domain that triggered an error or failure.
     */
    public void setErrorSrc(String errorSrc){
        this.fieldMap.put(ERROR_SRC_KW, errorSrc);
    }
    
    /**
     * Returns the type of message being sent or received
     * 
     * @return a String indicating the type of 
     *                  message being sent or received
     */
    public String getIDCMsgType() {
        return this.fieldMap.get(IDC_MSG_TYPE_KW);
    }
    /**
     * Sets the type of message being sent or received
     * 
     * @param idcMsgType a String indicating the type of 
     *                   message being sent or received
     */
    public void setIDCMsgType(String idcMsgType) {
        this.fieldMap.put(IDC_MSG_TYPE_KW, idcMsgType);
    }
    
    /**
     * Return the HashMap with all of the fields used globally across 
     * each log entry generated by this instance. You may set additional 
     * fields used across all messages by calling the <code>put</code> 
     * method on the HashMap.
     * 
     * @return the HashMap containing the fields used globally across 
     * each log entry generated by this instance.
     */
    public HashMap<String, String> getFieldMap(){
        return this.fieldMap;
    }
    
    /**
     * Chops off lines beyond 1, adds quotes around string, and replaces 
     * any double quotes with single quotes. Should be used for fields 
     * that potentially have spaces.
     * 
     * @param msg the String to be formatted
     * @return the formatted string
     */
    private String quote(String msg){
        if(msg.indexOf("\n") != -1){
            msg = msg.substring(0, msg.indexOf("\n")).trim();
        }
        msg = "\"" + msg.replaceAll("\"", "'") + "\"";
        return msg;
    }
    
    /**
     * Private method for adding a correctly quoted field to the log message.
     * 
     * @param key the name of the field to add
     * @param value the value to assign
     * @param netLogMsg the log message to add the field
     */
    private void addField(String key, String value, LogMessage netLogMsg){
        if(value != null && value.matches(".*\\s.*")){
            value = this.quote(value);
        }
        netLogMsg.add(key, value);
    }
    
    /**
     * Serializes a list for output by NetLogger. Surrounds strings with 
     * brackets and separates by commas. Example: [string1,string2,string3]
     * 
     * @param list the list to serialize
     * @return a String representation of the list
     */
    static public String serializeList(List<String> list){
        if(list == null){
            return "null";
        }
        return serializeArray(list.toArray(new String[list.size()]));
    }
    
    /**
     * Serializes an array for output by NetLogger. Surrounds strings with 
     * brackets and separates by commas.Example: [string1,string2,string3]
     * 
     * @param array the array to serialize
     * @return a String representation of the array
     */
    static public String serializeArray(String[] array){
        if(array == null){
            return "null";
        }
        String str = "[";
        for(int i = 0; i < array.length; i++){
            if(i != 0){ 
                str += ","; 
            };
            str += array[i];
        }
        str += "]";
        return str;
    }
}
