package net.es.oscars.utils.topology;

import net.es.oscars.utils.soap.OSCARSServiceException;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

/**
 * Utility class for parsing NMWG elements and identifiers 
 * in topology used by the PCE
 * 
 * @author Andy Lake <andy@es.net>
 */
public class NMWGParserUtil {
    final static public String TOPO_ID_PREFIX    = "urn:ogf:network";
    final static public String[] TOPO_ID_PARTS   = {"domain", "node", "port", "link"};
    final static public String TOPO_ID_SEPARATOR = ":";
    final static public String TOPO_ID_LABEL_SEPARATOR = "=";
    final static public String TOPO_ID_WILDCARD = "*";
    final static public int DOMAIN_TYPE = 1;
    final static public int NODE_TYPE = 2;
    final static public int PORT_TYPE = 3;
    final static public int LINK_TYPE = 4;
    
    /**
     * Extracts a URN of the specified type from a hop
     * 
     * @param hop the hop with the ID to extract
     * @param type the type of id (domain, node, port, or link) to extract.
     *   Use the constants suffixed with _TYPE in the class.
     * @return the extracted ID of the specified type
     * @throws OSCARSServiceException
     */
    static public String getURN(CtrlPlaneHopContent hop, int type) throws OSCARSServiceException {
        return NMWGParserUtil.getURN(NMWGParserUtil.getURN(hop), type);
    }
    
    /**
     * Extracts a URN of the specified type from another URN
     * 
     * @param urn the urn to parse
     * @param type the type of id (domain, node, port, or link) to extract.
     *   Use the constants suffixed with _TYPE in the class.
     * @return the extracted ID of the specified type
     * @throws OSCARSServiceException
     */
    static public String getURN(String urn, int type) throws OSCARSServiceException{
        urn = NMWGParserUtil.normalizeURN(urn);
        
        //break urn into parts
        String[] urnParts = urn.split(TOPO_ID_SEPARATOR);
        if(urnParts.length < type){
            throw new OSCARSServiceException("URN " + urn + " only has " + 
                    urnParts.length + " but asked for " + type);
        }
        
        //build new urn
        String newURN = TOPO_ID_PREFIX;
        for(int i = 0; i < type; i++){
            newURN += TOPO_ID_SEPARATOR + TOPO_ID_PARTS[i] + TOPO_ID_LABEL_SEPARATOR + urnParts[i];
        }
        
        return newURN;
    }
    
    /**
     * Normalizes a URN by trimming whitespace, converting to lowercase, removing the prefix, 
     * and all labels such as <i>partType=</i>.
     * 
     * @param urn the URN to normalize
     * @return the normalized URN
     */
    static public String normalizeURN(String urn){
        urn = urn.trim();
        urn = urn.toLowerCase();
        urn = urn.replaceAll(TOPO_ID_PREFIX+TOPO_ID_SEPARATOR, "");
        for(String part : TOPO_ID_PARTS){
            urn = urn.replaceAll(part+TOPO_ID_LABEL_SEPARATOR, "");
        }
        
        return urn;
    }
    
    /**
     * Extracts a URN from a hop
     * 
     * @param hop the hop with the ID to extract
     * @return the extracted ID
     * @throws OSCARSServiceException
     */
    public static String getURN(CtrlPlaneHopContent hop) {
        String urn = "";
        if(hop.getLink() != null){
            urn = hop.getLink().getId();
        }else if(hop.getLinkIdRef() != null){
            urn = hop.getLinkIdRef();
        }else if(hop.getPort() != null){
            urn = hop.getPort().getId();
        }else if(hop.getPortIdRef() != null){
            urn = hop.getPortIdRef();
        }else if(hop.getNode() != null){
            urn = hop.getNode().getId();
        }else if(hop.getNodeIdRef() != null){
            urn = hop.getNodeIdRef();
        }else if(hop.getDomain() != null){
            urn = hop.getDomain().getId();
        }else if(hop.getDomainIdRef() != null){
            urn = hop.getDomainIdRef();
        }
        
        return urn;
    }
    
    /**
     * Determines whether a URN is of domain, node, port, or link type.
     * 
     * @param urn the URN with the type to extract
     * @return an int indicating the type. use the constants in this class to identify.
     */
    public static int getURNType(String urn){
        urn = NMWGParserUtil.normalizeURN(urn);
        return urn.split(TOPO_ID_SEPARATOR).length;
    }
    
    /**
     * Given an URN, return its parts
     * 
     * @param urn the URN
     * @return a String [] with URN's parts or null if URN is not well formed (or don't have parts)
     */
    private static String[] getURNParts(String urn) {
    	urn = normalizeURN(urn);
        
        //break urn into parts
    	if (urn.length() > 0)
    		return urn.split(TOPO_ID_SEPARATOR);
    	return null;
    }
    
    /**
     * Given an URN, retrieve one of its parts
     * 
     * @param urn the URN with the type to extract
     * @param type one of: DOMAIN_TYPE, NODE_TYPE, PORT_TYPE, LINK_TYPE 
     * @return a String with the extracted part. null case URN doesn't have the specified part
     */
    public static String getURNPart(String urn, int type){
    	String [] urnParts = getURNParts(urn);
    	if (urnParts.length >= type)
    		return urnParts[type-1];
    	return null;
    }
    
    /**
     * Given two URNs, compare one of their parts
     * 
     * @param urn1 the first URN
     * @param urn2 the second URN
     * @param type the part of the URN to compare. One of: DOMAIN_TYPE, NODE_TYPE, PORT_TYPE, LINK_TYPE 
     * @return 0 if equal, positive int if first > second, negative int otherwise.
     */
    public static int compareURNPart(String urn1, String urn2, int type){
       	return getURNPart(urn1,type).compareTo(getURNPart(urn2,type));
    }
}
