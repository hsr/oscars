package net.es.oscars.lookup;

/**
 *  Interface independent exception for general errors in the lookup module 
 *
 */
public class LookupException extends Exception{
    public LookupException(String msg){
        super(msg);
    }
}
