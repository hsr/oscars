package net.es.oscars.notifycmdexec;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;


/**
 * A lightweight HTTP/HTTPS listener that accepts Notify messages and passes
 * them to a NotifyHandler for processing. This class implements Thread so it 
 * can be run in the background while the calling class continues execution. 
 * SSL can be activated by setting useSSL to true on instantiation and setting
 * the javax.net.ssl.keyStore and javax.net.ssl.keyStorePassword with the -D 
 * option in the java command line to a keystore with the SSL key to use. Note
 * that setting javax.net.ssl.keyStore to a keystore with multiple keys in it
 * may produce undesired results because the server won't know which key to use.
 *
 */
public class NotifyListener extends Thread{    
    private ServerSocket socket ;
    private boolean running;
    private NotifyHandler handler;
    
    public final static Namespace SOAP_NS = Namespace.getNamespace("http://www.w3.org/2003/05/soap-envelope");
    public final static Namespace IDC_NS = Namespace.getNamespace("http://oscars.es.net/OSCARS");
    public final static Namespace WSA_NS = Namespace.getNamespace("http://www.w3.org/2005/08/addressing");
    public final static Namespace WSN_NS = Namespace.getNamespace("http://docs.oasis-open.org/wsn/b-2");
    public final static Namespace NMWG_CP_NS = Namespace.getNamespace("http://ogf.org/schema/network/topology/ctrlPlane/20080828/");
    
    /**
     * Constructor that accepts the port on which to listen, the handler to use
     * for incoming messages, and the choice of using SSL.
     *
     * @param port the port on which to accept incoming Notify messages
     * @param handler the NotifyHandler that will process all incoming requests
     * @param useSSL if true then will enable SSL, otehrwise SSL will be disabled
     */
    public NotifyListener(int port, NotifyHandler handler, boolean useSSL) throws IOException{
        ServerSocketFactory sf = null;
        if(useSSL){
            sf = SSLServerSocketFactory.getDefault();
        }else{
            sf = ServerSocketFactory.getDefault();
        }
        this.socket = sf.createServerSocket(port);
        this.running = false;
        this.handler = handler;
    }
    
    /**
     * Listens for an incoming connection. This method will block until an 
     * incoming message is received. Most applications should NOT call this 
     * method and should instead call start() which will create a new thread 
     * in which listening will occur.
     */
    @SuppressWarnings("unchecked")
    public void listen() throws IOException, JDOMException{
        Socket clientSock = this.socket.accept();
        HashMap<String,String> request = new HashMap<String,String>();
        PrintWriter out = new PrintWriter(clientSock.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
        String inputLine = "";
        String body = "";
        int i = 1;
        boolean isBody = false;
        
        /* Parse HTTP request */
        while((inputLine = in.readLine()) != null){
            String[] parts = inputLine.split(" ");
            if(i == 1 && parts.length == 3){
                request.put("method", parts[0]);
                request.put("path", parts[1]);
                request.put("version", parts[2]);
            }else if("".equals(inputLine)){
                isBody = true;
            }else if(isBody && "0".equals(inputLine)){
                break;
            }else if(isBody && inputLine.matches("^[a-fA-F0-9]+$")){
                i++;
                continue;
            }else if(isBody){
                body += inputLine;
            }else{
                String value = "";
                for(int j=1; j < parts.length; j++){
                    value += parts[j];
                }
                request.put(parts[0].replaceAll(":", ""), value);
            }
        }
        
        /* print response */
        out.write("HTTP/1.1 202 Accepted\r\n");
        out.write("Content-Type: text/xml;charset=UTF-8\r\n");
        out.write("Content-Length: 0\r\n");
        out.flush();
        
        /* Close streams */
        out.close();
        in.close();
        
        /* Build jdom object */
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(new ByteArrayInputStream(body.getBytes()));
        Element soapEnv = doc.getRootElement();
        if(soapEnv == null){
            this.handler.handleError("PARSING", new Exception("No SOAP Envelope"));
            return;
        }
        Element soapBody = soapEnv.getChild("Body", NotifyListener.SOAP_NS);
        if (soapBody == null){
            this.handler.handleError("PARSING", new Exception("No SOAP Body"));
            return;
        }
        Element notify = soapBody.getChild("Notify", NotifyListener.WSN_NS);
        if (notify == null){
            this.handler.handleError("PARSING", new Exception("No Notify element"));
            return;
        }
        List<Element> notifications = notify.getChildren("NotificationMessage", NotifyListener.WSN_NS);
        for(Element notification : notifications){
            try{
                this.handler.handleNotify(notification);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Overriden method from Thread that is called when Thread execution begins.
     * You can begin thread execution with the start() method.
     */
    public void run(){
        running = true;
        while(running){
            try{
                this.listen();
            }catch(SocketException e){
                this.handler.handleError("SOCKET", e);
            }catch(IOException e){
                this.handler.handleError("IO", e);
            }catch(JDOMException e){
                this.handler.handleError("JDOM", e);
            }catch(Exception e){
                this.handler.handleError("GENERAL", e);
            }
        }
    }
    
    /**
     * Stops thread execution and closes the listening socket.
     */
    public void shutdown(){
        this.running = false;
        try{ this.socket.close();}catch(Exception e){}
        this.interrupt();
    }
    
    /**
     * Returns the ServerSocket used for listening
     * @return the ServerSocket used for listening
     */
    public ServerSocket getSocket(){
        return this.socket;
    }
    
    /**
     * Returns whether the thread is running or not
     * @return boolean indicating if thread is running
     */
    public boolean isRunning(){
        return this.running;
    }
    
    /**
     * Returns the NotifyHandler used for processing incoming messages
     *
     * @return the NotifyHandler used for processing incoming messages
     */
    public NotifyHandler getHandler(){
        return this.handler;
    }
    
    /**
     * Sets the handler used for incoming messages
     *
     * @param handler the NotifyHandler to use for incoming messages
     */
    public void setHandler(NotifyHandler handler){
        this.handler = handler;
    }
}