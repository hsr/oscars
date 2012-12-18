package net.es.oscars.pce;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.clients.PCERuntimeClient;
import net.es.oscars.utils.soap.OSCARSServiceException;

public abstract class SimplePCEServer extends PCEProtocolServer implements Runnable {
    private        Thread          processingThread;
    private ArrayList<SimplePCEJob> pceJobs = new ArrayList<SimplePCEJob>();
    private Logger log = Logger.getLogger(this.getClass());
    private String netLogModName = "SimplePCEServer";  // should get reset to specific PCE
    private static HashMap <String, SimplePCEServer> pces = new HashMap <String, SimplePCEServer>();
    
    public static SimplePCEServer getInstance(String service) {
        return SimplePCEServer.pces.get(service);
    }

    public SimplePCEServer(String service) throws OSCARSServiceException {
        super(service);
        SimplePCEServer.pces.put(service, this);
        if (this.getClass().isAnnotationPresent(OSCARSNetLoggerize.class)){
            OSCARSNetLoggerize anno = this.getClass().getAnnotation(OSCARSNetLoggerize.class);
            this.netLogModName = anno.moduleName();
        }
        // Create and start the background thread
        this.processingThread = new Thread (this);
        this.processingThread.start();
    }
    
    public void addPceJob(PCEMessage message, int jobType) {
        synchronized (this.pceJobs) {
            this.pceJobs.add(new SimplePCEJob(message, jobType));
            this.pceJobs.notify();
        }
    }
    
    public SimplePCEJob getNextPceJob () {
        synchronized (this.pceJobs) {
            if (this.pceJobs.size() > 0) {
                return this.pceJobs.remove(0);
            } else {
                try {
                    // no query in the list: wait for a query to be inserted into the list
                    this.pceJobs.wait();
                    return this.pceJobs.remove(0);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
    
    protected void processJob (SimplePCEJob job) throws OSCARSServiceException {
        // The NullPCE simply returns the same set of constraints (pass-thru PCE)
        // Use the PCERuntime client to send the PCEResponse back
        String callback = job.getMessage().getCallBackEndpoint();

        // Get the client
        PCERuntimeClient client = null;
        try {
            client = PCERuntimeClient.getPCERuntimeClient(callback);
            PCEDataContent pceData = null;
            OSCARSServiceException ex = null;
            if (job.getJobType() == SimplePCEJob.CREATE_TYPE){
                try {
                    pceData = this.calculatePath(job.getMessage());
                } catch ( OSCARSServiceException e) {
                    // exception has already been logged, return error to PCERuntime
                    ex = e;
                }
                // need the transactionId
                client.sendPceReply(job.getMessage().getMessageProperties(),
                                    job.getMessage().getGri(),
                                    job.getMessage().getTransactionId(),
                                    job.getMessage().getPceName(), pceData, ex);
            } else if(job.getJobType() == SimplePCEJob.CREATE_COMMIT_TYPE){
                try {
                    pceData = this.commitPath(job.getMessage());
                } catch ( OSCARSServiceException e) {
                    // exception has already been logged, return error to PCERuntime
                    ex =e;
                }
                client.sendCreateCommitReply(job.getMessage().getMessageProperties(),
                                             job.getMessage().getGri(),
                                             job.getMessage().getTransactionId(),
                                             job.getMessage().getPceName(),
                                             pceData, ex);

            }  else if(job.getJobType() == SimplePCEJob.MODIFY_COMMIT_TYPE){
                try {
                    pceData = this.modifyCommitPath(job.getMessage());
                } catch ( OSCARSServiceException e) {
                    // exception has already been logged, return error to PCERuntime

                    ex =e;
                }
                client.sendModifyCommitReply(job.getMessage().getMessageProperties(),
                                             job.getMessage().getGri(),
                                             job.getMessage().getTransactionId(),
                                             job.getMessage().getPceName(),
                                             pceData, ex);

            } else if(job.getJobType() == SimplePCEJob.MODIFY_TYPE){
                try {
                    pceData = this.modifyPath(job.getMessage());
                } catch ( OSCARSServiceException e) {
                    // exception has already been logged, return error to PCERuntime
                    ex =e;
                }
                client.sendModifyReply(job.getMessage().getMessageProperties(),
                                       job.getMessage().getGri(), 
                                       job.getMessage().getTransactionId(),
                                       job.getMessage().getPceName(),  
                                       pceData, ex);
            } else if(job.getJobType() == SimplePCEJob.CANCEL_TYPE){
                try {
                    pceData = this.cancelPath(job.getMessage());
                } catch ( OSCARSServiceException e) {
                    // exception has already been logged, return error to PCERuntime
                    ex =e;
                }
                client.sendCancelReply(job.getMessage().getMessageProperties(),
                                       job.getMessage().getGri(),
                                       job.getMessage().getTransactionId(),
                                       job.getMessage().getPceName(), 
                                       pceData, ex);
            } else {
                throw new OSCARSServiceException ("Invalid PCE job type: " + job.getJobType());
            }
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException (e);
        }
    }
    
    abstract public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException;
    
    public PCEDataContent commitPath(PCEMessage msg) throws OSCARSServiceException{
        return msg.getPCEDataContent();
    }
    
    public PCEDataContent modifyPath(PCEMessage msg) throws OSCARSServiceException{
        //by default, modify and create are handled the same
        return this.calculatePath(msg);
    }
    
    public PCEDataContent modifyCommitPath(PCEMessage msg) throws OSCARSServiceException{
        //by default, modify commit and create commit are handled the same
        return this.commitPath(msg);
    }
    
    public PCEDataContent cancelPath(PCEMessage msg) throws OSCARSServiceException{
        return msg.getPCEDataContent();
    }
    
    public void run() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        HashMap<String, String> netLogProps = new HashMap<String, String>();
        // Process queries
        for (;;) {
            SimplePCEJob job = this.getNextPceJob();
            if (job == null) {
                // This should not happen unless this thread has been interrupted
                break;
            }
            // Process query
            try {
                // The following netlogger code assumes we are only processing 1 job at a time -mrt
                String transactionId = job.getMessage().getTransactionId();
                netLogger.init(this.netLogModName,transactionId);
                this.processJob (job);
            } catch (OSCARSServiceException e) {
                netLogProps.put("exception",e.getMessage());
                netLogProps.put("GRI",job.getMessage().getGri());
                String  jobType = ( job.getJobType() == SimplePCEJob.CREATE_TYPE ? "create" : "commit"); 
                netLogProps.put("jobType", jobType);
                this.log.error(netLogger.error("processJob",null,"exception caught in processJob",null,netLogProps));
                 e.printStackTrace();
            }
        }
        
    }   
    
}