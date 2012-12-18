package net.es.oscars.resourceManager.common;

import java.util.*;

import net.es.oscars.api.soap.gen.v06.OptionalConstraintType;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.resourceManager.http.WSDLTypeConverter;
import net.es.oscars.utils.notify.NotifySender;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.log4j.*;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.beans.*;

/**
 * @author David Robertson (dwrobertson@lbl.gov)
 *
 * This class contains utility methods for use by the reservation manager.
 */
public class RMUtils {
    private static Logger log = Logger.getLogger(RMUtils.class);
    public final static int LOCAL_INIT = 0;
    public final static int expiredSent = 1;
    public final static int notice1DaySent = 2;  // Notification of expiration in one day has been sent
    public final static int notice7DaySent = 3;  // Notification of expiration in seven days has been sent
    public final static int notice30DaySent = 4; // Notification of expiration in thirty days has been sent

    // utils class, do not instantiate
    private RMUtils() {
    }
         /**
     *  Convert a reservation to a ResDetails for the soap message reply
     * @param res input reservation
     * @return ResDetails structure
     */
    public static ResDetails res2resDetails(Reservation res, Boolean internalPathAuthorized)
        throws RMException {

        ResDetails resDetails = new ResDetails();
        String localDomainName  = RMCore.getInstance().getLocalDomainId();
        StdConstraint constraint = null;
        Map <String,StdConstraint> conMap = new HashMap<String,StdConstraint>();
        String gri = res.getGlobalReservationId();
        resDetails.setGlobalReservationId(gri);
        resDetails.setLogin(res.getLogin());
        resDetails.setCreateTime(res.getCreatedTime());
        resDetails.setDescription(res.getDescription());
        resDetails.setStatus(res.getStatus());
        conMap = res.getConstraintMap();
        constraint = conMap.get(ConstraintType.USER);
        if (constraint == null) {
            throw  new RMException("no USER constraint for reservation: " + gri);
        }
        UserRequestConstraintType uc = WSDLTypeConverter.stdConstraint2UserRequest(constraint,
                internalPathAuthorized, localDomainName);
        resDetails.setUserRequestConstraint(uc);
        StdConstraint resvConstraint = conMap.get(ConstraintType.RESERVED);
        if (resvConstraint != null) {
            ReservedConstraintType rt = WSDLTypeConverter.stdConstraint2ReservedConstraint(resvConstraint,
                    internalPathAuthorized,localDomainName);
            resDetails.setReservedConstraint(rt);
        }
       /*@S  bhr*/ 
       	List <OptConstraint> optConsList = res.getOptConstraintList();
    	
        if (optConsList != null && !optConsList.isEmpty()) {
            for (OptConstraint oc: optConsList) {
            	OptionalConstraintType oct = WSDLTypeConverter.OptConstraint2OptionalConstraintType(oc);
            	resDetails.getOptionalConstraint().add(oct);     	
            }  
        }
        /*@E bhr*/

        
        
        return resDetails;
    }

     /**
      *  creates the MessageProperties that are needed for
      *     messages to the coordinator and notificationBridge
      *
      *    The owner of the reservation is added to the Orginator as LoginId
      *    The OSCARS-service role is added to grant access for the requested operation
      * @param resDetails
      * @return
      */
    public static MessagePropertiesType getMsgProps(ResDetails resDetails){
        MessagePropertiesType msgProps = new MessagePropertiesType();
        SubjectAttributes originator = new SubjectAttributes();
        AttributeType att = new AttributeType();

        msgProps.setGlobalTransactionId(PathTools.getLocalDomainId() + "-RM-" +UUID.randomUUID().toString());
        att.setName(AuthZConstants.LOGIN_ID);
        att.getAttributeValue().add(resDetails.getLogin());
        originator.getSubjectAttribute().add(att);
        att.setName(AuthZConstants.ROLE);
        att.getAttributeValue().add("OSCARS-service");
        originator.getSubjectAttribute().add(att);
        msgProps.setOriginator(originator);
        return msgProps;
    }
    /**
     * calls notify for expiring reservations
     *
     * @param notifyType the value of the NotifyRequestType to send
     * @param resv the details for the reservation that will expire
     * @throws OSCARSServiceException RESV_DATABASE_ERROR if res2Details fails
     *           or Connection error from NotifySender
     */
    public static void notify (String notifyType, Reservation resv )
            throws  OSCARSServiceException {
        try {
             ResDetails resDetails = RMUtils.res2resDetails(resv,true);
             MessagePropertiesType msgProps = RMUtils.getMsgProps(resDetails);
             NotifySender.send(notifyType, msgProps, resDetails);
        } catch (RMException rmEx) {
            throw new OSCARSServiceException(ErrorCodes.RESV_DATABASE_ERROR, rmEx.getMessage(), ErrorReport.SYSTEM);
        }
    }
    /**
     * Gets path depending on reservation state and set of paths
     * available.  Used where need to choose just one of the paths,
     * for example in list and query.
     *
     * @param resv Reservation with set of paths
     * @return path Path chosen
     */
    public static Path getPath(Reservation resv) throws RMException {
        StdConstraint resvConstraint = resv.getConstraint(ConstraintType.RESERVED);
        if (resvConstraint != null) {
           Path reservedPath = resvConstraint.getPath();
           if (reservedPath != null) {
                return reservedPath;
           }
        }
        Path requestedPath = resv.getConstraint(ConstraintType.USER).getPath();
        if (requestedPath != null) {
            return requestedPath;
        }
        throw new RMException("No path found");
    }

    /**
     * Copies fields that for now are in common to various types of paths.
     * Currently pathSetupMode and layer specific information is in common
     *
     * @param Path Path with information to copy
     * @param updatePath Path with information to update

    public static void copyPathFields(Path path, Path updatePath)
            throws RMException {

        updatePath.setPathSetupMode(path.getPathSetupMode());
        if (path.getLayer2Data() != null) {
            Layer2Data layer2DataCopy = path.getLayer2Data().copy();
            updatePath.setLayer2Data(layer2DataCopy);
        }
        if (path.getLayer3Data() != null) {
            Layer3Data layer3DataCopy = path.getLayer3Data().copy();
            updatePath.setLayer3Data(layer3DataCopy);
        }
        if (path.getMplsData() != null) {
            MPLSData mplsDataCopy = path.getMplsData().copy();
            updatePath.setMplsData(mplsDataCopy);
        }
    }
    
    /**
     * Copies path field and path elems to new path
     * @throws RMException 

    public static void copyPath(Path path, Path updatePath) throws RMException{
        updatePath.setPathType(path.getPathType());
        copyPathFields(path, updatePath);
        for(PathElem elem : path.getPathElems()){
            updatePath.addPathElem(PathElem.copyPathElem(elem));
        }
    }
    */

    /**
     * Converts data associated with a Hibernate path to a series of strings.
     *
     * @param path path to convert to string
     * @return pathDataStr path data in string format
     * @throws RMException
     */
    public static String pathDataToString(Path path) throws RMException {
        StringBuilder sb =  new StringBuilder();
        if (path.getPathSetupMode() != null) {
            sb.append("path setup mode: " + path.getPathSetupMode() + "\n");
        }


        Layer2Data layer2Data = path.getLayer2Data();


        if (path.isLayer2()) {
            sb.append("layer: 2\n");
            if (layer2Data.getSrcEndpoint() != null) {
                sb.append("source endpoint: " +
                      layer2Data.getSrcEndpoint() + "\n");
            }
            if (layer2Data.getDestEndpoint() != null) {
                sb.append("dest endpoint: " +
                          layer2Data.getDestEndpoint() + "\n");
            }
            List<PathElem> pathElems = path.getPathElems();
            if (!pathElems.isEmpty()) {
                PathElem pathElem = pathElems.get(0);
                PathElemParam pep =
                    pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                                         PathElemParamType.L2SC_VLAN_RANGE);
                String linkDescr = pep.getValue();
                if (linkDescr != null) {
                    sb.append("VLAN tag: " + linkDescr + "\n");
                }
            }
        }
        Layer3Data layer3Data = path.getLayer3Data();
        if (path.isLayer3()) {
            sb.append("layer: 3\n");
            if (layer3Data.getSrcHost() != null) {
                sb.append("source host: " + layer3Data.getSrcHost() + "\n");
            }
            if (layer3Data.getDestHost() != null) {
                sb.append("dest host: " + layer3Data.getDestHost() + "\n");
            }
            if (layer3Data.getProtocol() != null) {
                sb.append("protocol: " + layer3Data.getProtocol() + "\n");
            }
            if ((layer3Data.getSrcIpPort() != null) &&
                (layer3Data.getSrcIpPort() != 0)) {
                sb.append("src IP port: " + layer3Data.getSrcIpPort() + "\n");
            }
            if ((layer3Data.getDestIpPort() != null) &&
                (layer3Data.getDestIpPort() != 0)) {
                sb.append("dest IP port: " +
                          layer3Data.getDestIpPort() + "\n");
            }
            if (layer3Data.getDscp() != null) {
                sb.append("dscp: " +  layer3Data.getDscp() + "\n");
            }
        }
        MPLSData mplsData = path.getMplsData();
        if (mplsData != null) {
            if (mplsData.getBurstLimit() != null) {
                sb.append("burst limit: " + mplsData.getBurstLimit() + "\n");
            }
            if (mplsData.getLspClass() != null) {
                sb.append("LSP class: " + mplsData.getLspClass() + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * Converts Hibernate path to a series of identifier strings.
     *
     * @param path path to convert to string
     * @param interDomain boolean for intra or interdomain path
     * @return pathStr converted path
     */
    public static String pathToString(Path path, boolean interDomain) {

        String param = null;

        // FIXME:  more null checks may be necessary; return this or null
        if (path == null) {
            return "";
        }
        List<PathElem> pathElems = path.getPathElems();
        if (pathElems == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int sz = pathElems.size();
        int i = 0;
        for (PathElem pathElem: pathElems) {
            if(i != 0){
                sb.append("\n");
            }
            //  send back topology identifier in both layer 2 and layer 3 case
            sb.append(pathElem.getUrn());
            i++;
        }
        // in this case, all hops are local
        if (interDomain && (sz == 2)) {
            return "";
        // internal path has not been set up
        // NOTE:  this depends on the current implementation sometimes having
        //        one hop in the path from when the reservation has been in
        //        the ACCEPTED state, but the path has not been or may never
        //        be set up.
        } else if (!interDomain && (sz == 1)) {
            return "";
        }
        String pathStr = sb.toString();
        return pathStr;
    }
    
    /**
     * Gets VLAN tags for a given path. 
     *
     * @param path Path with reservation's page
     * @return vlanTags list of strings with VLAN tag for each hop, if any
     * @throws RMException
     */
    public static List<String> getVlanTags(Path path) throws RMException {
        List<String> vlanTags = new ArrayList<String>();
        String event = "getVlanTags";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        if (path == null) {
            log.debug(netLogger.getMsg(event,"path is null"));
            return vlanTags;
        }
        List<PathElem> pathElems = path.getPathElems();
        if ((pathElems == null) || pathElems.isEmpty()) {
            log.debug(netLogger.getMsg(event,"pathElems null or empty"));
            return vlanTags;
        }
        for (PathElem pathElem: pathElems) {
            pathElem.initializePathElemParams();
            PathElemParam pep =
                pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                                          PathElemParamType.L2SC_SUGGESTED_VLAN);
            if (pep == null ) {
                pep = pathElem.getPathElemParam(PathElemParamSwcap.MPLS,
                                                PathElemParamType.MPLS_SUGGESTED_VLAN);
            }
            if (pep == null ) {
                pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                                          PathElemParamType.L2SC_VLAN_RANGE);
            }
            if (pep == null) {
                pep = pathElem.getPathElemParam(PathElemParamSwcap.MPLS,
                                                PathElemParamType.MPLS_VLAN_RANGE);
            } if (pep == null) {
                log.debug(netLogger.getMsg(event,"no suggested_vlan or vlan range"));
                //vlanTags.add("");
            } else {
                String vlanTag = pep.getValue();
                vlanTags.add(vlanTag);
                // log.debug(netLogger.getMsg(event,"adding vlanTag " + vlanTag));
            }
        }
        return vlanTags;
    }

    /**
     * String joiner
     * @param s a Collection of objects to join (uses toString())
     * @param delimiter the delimiter
     * @param quote a string to prefix each object with (null for none)
     * @param unquote a string to postfix each object with (null for none)
     * @return joined the objects, quoted & unquoted, joined by the delimiter
     */
    public static String join(Collection s, String delimiter, String quote, String unquote) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(quote);
            buffer.append(iter.next());
            buffer.append(unquote);
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

}
