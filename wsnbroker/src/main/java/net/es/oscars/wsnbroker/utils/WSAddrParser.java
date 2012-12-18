package net.es.oscars.wsnbroker.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class WSAddrParser {

    static public String getAddress(W3CEndpointReference epr){
       //Create instance of DocumentBuilderFactory
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        //Get the DocumentBuilder
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            return null;
        }
        Document doc = docBuilder.newDocument();
        DOMResult domResult = new DOMResult(doc);
        epr.writeTo(domResult);
        if(doc == null){
            return null;
        }
        if(doc.getElementsByTagName("Address") == null || 
                doc.getElementsByTagName("Address").getLength() != 1){
            return null;
        }
        
        return doc.getElementsByTagName("Address").item(0).getTextContent();
    }
    
    static public String get05SubscriptionId(W3CEndpointReference epr){
        //Create instance of DocumentBuilderFactory
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         //Get the DocumentBuilder
         DocumentBuilder docBuilder = null;
         try {
             docBuilder = docBuilderFactory.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             return null;
         }
         Document doc = docBuilder.newDocument();
         DOMResult domResult = new DOMResult(doc);
         epr.writeTo(domResult);
         if(doc == null){
             return null;
         }
         NodeList subscriptionId = doc.getElementsByTagNameNS("http://oscars.es.net/OSCARS", "subscriptionId");
         if(subscriptionId == null || subscriptionId.getLength() < 1){
             return null;
         }
         
         return subscriptionId.item(0).getTextContent();
     }
    
    static public W3CEndpointReference createAddress(String uri){
        return (new W3CEndpointReferenceBuilder()).address(uri).build();
     }
}
