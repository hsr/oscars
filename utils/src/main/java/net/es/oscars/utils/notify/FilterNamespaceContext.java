package net.es.oscars.utils.notify;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class FilterNamespaceContext implements NamespaceContext{
    private Map<String,String> namespaceMap;
    
    public FilterNamespaceContext(){
        this.namespaceMap = new HashMap<String,String>();
    }
    
    public FilterNamespaceContext(Map<String,String> namespaceMap){
        this.namespaceMap = namespaceMap;
    }
    
    public String getNamespaceURI(String prefix) {
        if(namespaceMap.containsKey(prefix)){
            return namespaceMap.get(prefix);
        }
        return XMLConstants.NULL_NS_URI;
    }

    //don't need this
    public String getPrefix(String arg0) {
        throw new UnsupportedOperationException();
    }

   //don't need this
    public Iterator getPrefixes(String arg0) {
        throw new UnsupportedOperationException();
    }
    
    public Map<String, String> getNamespaceMap(){
        return this.namespaceMap;
    }

}
