package net.es.oscars.coord.events;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.ListIterator;
import java.lang.ref.WeakReference;

import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.coord.jobs.CoordJob;
import net.es.oscars.coord.jobs.EventJob;
import net.es.oscars.coord.jobs.EventListenerJob;
import net.es.oscars.utils.soap.OSCARSServiceException;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.apache.log4j.Logger;


public class CoordEvent <P> {
    
    private static long EVENTCLEANER_REPEAT = (5 * 60 * 1000); // 5 minutes

    @SuppressWarnings("unchecked")
    private static HashMap<String, WeakReference<CoordEvent>> events = new HashMap<String, WeakReference<CoordEvent>>();
    private ArrayList<WeakReference<CoordEventListener>> listeners = new ArrayList<WeakReference<CoordEventListener>>();
    
    private String name = null;
    private P data = null;
    
    // Start the background thread that will prune empty entries in the events table.
    static {
        SimpleTrigger jobTrigger = new SimpleTrigger("CoordEvent.EventCleaner",
                                                     null,
                                                     SimpleTrigger.REPEAT_INDEFINITELY,
                                                     CoordEvent.EVENTCLEANER_REPEAT);
        
        JobDetail     jobDetail  = new JobDetail("CoordEvent.EventCleaner", null, EventJob.class);
        jobDetail.setVolatility(false);
        
        try {
            Coordinator.getInstance().getScheduler().scheduleJob(jobDetail, jobTrigger);
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OSCARSServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }     
    }
    
    /**
     * Retrieve the shared named CoordEvent object. 
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public static CoordEvent getCoordEvent (String name) {
        synchronized (CoordEvent.events) {
            WeakReference<CoordEvent> eventReference = CoordEvent.events.get(name);
            CoordEvent event = null;
            if (eventReference != null) {
                event = eventReference.get();
                if (event != null) {
                    // An event object of the specified name already exists. Return it.
                    return event;
                } else {
                    // The reference to the coordinator was to an event that has been gc'ed.
                    // Delete the current weak reference from the table. A new one will be re-created
                    CoordEvent.events.remove(eventReference);
                }
            }
            // No event of the specified name has been yet created. Create it and return it.
            event = new CoordEvent (name);
            
            // Create a WeakReference of the event before storing it
            eventReference = new WeakReference<CoordEvent>(event);
            CoordEvent.events.put (name, eventReference);
            return event;
        }
    }
    
    public CoordEvent (String name) {
       this.name = name;
    }

    public String getName() {
        return this.name;
    }
 
    /**
     * Adds a listener to the event. 
     * Note that this method also cleans up the table of listeners, removing those that have been GC'ed (non longer exist)
     * The cleanup is done here as an optimization, instead of creating a thread per event to perform the task in background.
     * It is assumed that adding a listener to an event is a rare event and an event will only have a few listeners.
     * 
     * @param listener
     */
    public void addListener (CoordEventListener listener) {
        synchronized (this.listeners) {
            boolean alreadyAdded = false;
            ListIterator<WeakReference<CoordEventListener>> iterator = this.listeners.listIterator();
            for (;iterator.hasNext();) {
                WeakReference<CoordEventListener> eventListenerReference = iterator.next();
                CoordEventListener eventListener = eventListenerReference.get();
                if (eventListener == null) {
                    // The listener is has been GC'ed already. Cleanup this entry. Then entry can be safely removed
                    // within the iterator loop because this is a ListIterator (vs Iterator)
                    this.listeners.remove(eventListenerReference);
                } else if (eventListener == listener) {
                    // The caller is trying to add the same listener more than once. Just ignore the operation
                    alreadyAdded = true;
                }
            }
            if (! alreadyAdded) {
                // Need to add the new listener.
                WeakReference<CoordEventListener> eventListenerReference = new WeakReference<CoordEventListener> (listener);
                this.listeners.add(eventListenerReference);
            }
        }
    }
    
    /**
     * Removes a listener from the event. 
     * Note that this method also cleans up the table of listeners, removing those that have been GC'ed (non longer exist)
     * The cleanup is done here as an optimization, instead of creating a thread per event to perform the task in background.
     * It is assumed that adding a listener to an event is a rare event and an event will only have a few listeners.
     * 
     * @param listener
     * @return true if the listener was found and removed, false otherwise
     */
    public boolean removeListener (CoordEventListener listener) {
        boolean ret = false;
        synchronized (this.listeners) {
            ListIterator<WeakReference<CoordEventListener>> iterator = this.listeners.listIterator();
            for (;iterator.hasNext();) {
                WeakReference<CoordEventListener> eventListenerReference = iterator.next();
                CoordEventListener eventListener = eventListenerReference.get();
                if (eventListener == null) {
                    // The listener is has been GC'ed already. Cleanup this entry. Then entry can be safely removed
                    // within the iterator loop because this is a ListIterator (vs Iterator)
                    this.listeners.remove(eventListenerReference);
                } else if (eventListener == listener) {
                    // This is the listener to remove
                    ret = this.listeners.remove(eventListenerReference);
                }
            }
        }
        return ret;
    }
        
    /**
     * This method removes the weak references from the table that are no longer referring to existing events.
     * It is assumed that this method is invoked on a regular basis by a background thread so there is no memory leak.
     * 
     */
    @SuppressWarnings("unchecked")
    public static void cleanupEvents () {
        synchronized (CoordEvent.events) {
            for (WeakReference <CoordEvent> eventReference : CoordEvent.events.values()) {
                CoordEvent event = eventReference.get();
                if (event == null) {
                    // This is a null entry. remove 
                    CoordEvent.events.remove(eventReference);
                }
            }
        }
    }

    /**
     * Create jobs to execute listeners
     */
    public final void trigger () {
        synchronized (this.listeners) {
            ListIterator<WeakReference<CoordEventListener>> iterator = this.listeners.listIterator();
            for (;iterator.hasNext();) {
                WeakReference<CoordEventListener> eventListenerReference = iterator.next();
                CoordEventListener eventListener = eventListenerReference.get();

                if (eventListener == null) {         
                    // The listener is has been GC'ed already. Cleanup this entry. Then entry can be safely removed
                    // within the iterator loop because this is a ListIterator (vs Iterator)
                    this.listeners.remove(eventListenerReference);
                } else {

                    String nameTag = CoordJob.createId();
                    SimpleTrigger jobTrigger = new SimpleTrigger("CoordEvent." + this.getName() + nameTag,null);
                    JobDetail     jobDetail  = new JobDetail("CoordEvent." + this.getName() + nameTag, null, EventListenerJob.class);
                    jobDetail.setVolatility(false);
                    EventListenerJob.setEventListener(jobDetail, this, eventListener);
                    try {
                        Coordinator.getInstance().getScheduler().scheduleJob(jobDetail, jobTrigger);
                    } catch (SchedulerException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (OSCARSServiceException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }                    
                }
            }            
        }     
    }
    
    public void setData (P data) {
        this.data = data;    
    }
    
    public P getData () {
        return this.data;
    }
} 
