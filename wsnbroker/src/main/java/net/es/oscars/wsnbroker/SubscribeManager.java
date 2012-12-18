package net.es.oscars.wsnbroker;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import org.apache.log4j.Logger;

import net.es.oscars.authZ.soap.gen.CheckAccessParams;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.wsnbroker.policy.NotifyPEP;
import net.es.oscars.wsnbroker.utils.WSAddrParser;
import net.es.oscars.utils.notify.TopicDialect;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;

public class SubscribeManager {
    private Logger log = Logger.getLogger(SubscribeManager.class);
    private long defaultExpiration;
    
    public SubscribeManager(long defaultExpiration){
        this.defaultExpiration = defaultExpiration;
    }
    
    public SubscribeResponse create(Connection conn, Subscribe subscribe, 
            SubjectAttributes subjAttrs) throws OSCARSServiceException{
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start("SubscribeManager.create"));
        SubscribeResponse subscribeResponse = new SubscribeResponse();

        //Parse consumer url
        String consumerUrl = WSAddrParser.getAddress(subscribe.getConsumerReference());
        if(consumerUrl == null){
            throw new OSCARSServiceException("No consumer address provided");
        }
        //validate URL
        try{
            new URL(consumerUrl);
        }catch(Exception e){
            throw new OSCARSServiceException("Consumer address is not a valid URL");
        }
        
        //get topics 
        List<String> topicList = this.parseTopics(subscribe.getFilter().getTopicExpression());
        NotifyPEP matchingPep = null;
        for(NotifyPEP pep : NotificationGlobals.getInstance().getPEPList()){
            if(pep.topicMatches(topicList)){
                matchingPep = pep;
                break;
            }
        }
        
        //use matching pep to get authConditions
        AuthConditions authConds = this.getAuthZPerms(subjAttrs, matchingPep);
        
        //get the user that sent the subscribe message
        String loginName = "";
        if(subjAttrs != null){
            for(AttributeType attr : subjAttrs.getSubjectAttribute()){
                if(AuthZConstants.LOGIN_ID.equals(attr.getName()) && 
                        attr.getAttributeValue() != null && 
                       !attr.getAttributeValue().isEmpty()){
                    loginName = attr.getAttributeValue().get(0) + "";
                }
            }
        }
        //parse filters 
        ArrayList<String[]> filters = new ArrayList<String[]>();
        
        //parse authConditions
        if(authConds != null && authConds.getAuthCondition() != null){
            for(AuthConditionType cond : authConds.getAuthCondition()){
                String authzFilterType = "";
                if(cond.getName() == null){
                    continue;
                }else if(cond.getName().equals(AuthZConstants.PERMITTED_DOMAINS)){
                    authzFilterType = FilterTypes.FILTER_AUTHZDOMAIN;
                }else if(cond.getName().equals(AuthZConstants.PERMITTED_LOGIN)){
                    authzFilterType = FilterTypes.FILTER_AUTHZLOGIN;
                }
                for(String condValue : cond.getConditionValue()){
                    String[] tmpTuple = new String[2];
                    tmpTuple[0] = authzFilterType;
                    tmpTuple[1] = condValue;
                    filters.add(tmpTuple);
                }
            }
        }

        //parse producer filters
        for(QueryExpressionType prodFilter : subscribe.getFilter().getProducerProperties()){
            if(this.validateQueryExpression(prodFilter)){
                String[] tmpTuple = new String[2];
                tmpTuple[0] = FilterTypes.FILTER_PRODXPATH;
                tmpTuple[1] = prodFilter.getValue();
                filters.add(tmpTuple);
            }
        }
        
        //parse message filters
        for(QueryExpressionType msgFilter : subscribe.getFilter().getMessageContent()){
            if(this.validateQueryExpression(msgFilter)){
                String[] tmpTuple = new String[2];
                tmpTuple[0] = FilterTypes.FILTER_MSGXPATH;
                tmpTuple[1] = msgFilter.getValue();
                filters.add(tmpTuple);
            }
        }
        
        //parse topic filters
        for(String topicFilter : topicList){
            String[] tmpTuple = new String[2];
            tmpTuple[0] = FilterTypes.FILTER_TOPIC;
            tmpTuple[1] = topicFilter;
            filters.add(tmpTuple);
        }
        
        String subscriptionId = "urn:uuid:" + UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        long terminationTime = currentTime + this.defaultExpiration;
        if(subscribe.getInitialTerminationTime() != null){
            terminationTime = this.parseTermTime(subscribe.getInitialTerminationTime().getValue());
        }
        
        //insert into database
        try {
            //insert subscriptions
            PreparedStatement subscripInsert = conn.prepareStatement("INSERT INTO " +
                    "subscriptions VALUES(DEFAULT, ?, ?, ?, ?, ?, ?)", 
                    Statement.RETURN_GENERATED_KEYS);
            subscripInsert.setString(1, subscriptionId);
            subscripInsert.setString(2, loginName);
            subscripInsert.setString(3, consumerUrl);
            subscripInsert.setLong(4, currentTime);
            subscripInsert.setLong(5, terminationTime);
            subscripInsert.setInt(6, SubscriptionStatus.ACTIVE_STATUS);
            subscripInsert.executeUpdate();
            ResultSet genKeys = subscripInsert.getGeneratedKeys();
            if(!genKeys.next()){
                throw new OSCARSServiceException("No auto-generated keys from subscription");
            }
            int subscripId = genKeys.getInt(1);
            
            //insert filters
            PreparedStatement filterInsert = conn.prepareStatement("INSERT INTO " +
            "subscriptionFilters VALUES(DEFAULT, ?, ?, ?)");
            filterInsert.setInt(1, subscripId);
            for(String[] filter : filters){
                filterInsert.setString(2, filter[0]);
                filterInsert.setString(3, filter[1]);
                filterInsert.executeUpdate();
            }
        } catch (SQLException e) {
            this.log.debug(netLogger.error("SubscribeManager.create", ErrSev.MAJOR, e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException("Error saving subscription to database");
        }
        
        W3CEndpointReference subscriptionRef = (new W3CEndpointReferenceBuilder()).address(subscriptionId).build();
        subscribeResponse.setSubscriptionReference(subscriptionRef);
        subscribeResponse.setCurrentTime(this.createXMLTime(currentTime));
        subscribeResponse.setTerminationTime(this.createXMLTime(terminationTime));

        
        this.log.debug(netLogger.end("SubscribeManager.create"));
        
        return subscribeResponse;
    }
    
/*    public OscarsListSubscriptionsResponse list(OscarsListSubscriptions request) throws OSCARSServiceException{
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        this.log.debug(netLogger.start("SubscribeManager.list"));
        OscarsListSubscriptionsResponse response = new OscarsListSubscriptionsResponse();
        String subscripSQL = "SELECT id, referenceId, userLogin, url, " +
            "createdTime, terminationTime, status FROM subscriptions";
        
        //parse statuses
        ArrayList<Integer> subscripSQLStatusParams = new ArrayList<Integer>();
        boolean hasWhere = false;
        String tmpWhere = "";
        for(String status : request.getStatus()){
            if(SubscriptionStatus.ALL_STRING.equals(status)){
                tmpWhere = "";
                hasWhere = false;
                break;
            }
            if(!hasWhere){
                tmpWhere += " WHERE (";
                hasWhere = true;
            }else{
                tmpWhere += " OR";
            }
            
            if(SubscriptionStatus.ACTIVE_STRING.equals(status)){
                tmpWhere += " status=?";
                subscripSQLStatusParams.add(SubscriptionStatus.ACTIVE_STATUS);
            }else if(SubscriptionStatus.INACTIVE_STRING.equals(status)){
                tmpWhere += " status=?";
                subscripSQLStatusParams.add(SubscriptionStatus.INACTIVE_STATUS);
            }else if(SubscriptionStatus.PAUSED_STRING.equals(status)){
                tmpWhere += " status=?";
                subscripSQLStatusParams.add(SubscriptionStatus.PAUSED_STATUS);
            }else{
                String errMsg = "Invalid status " + status + " provided";
                this.log.debug(netLogger.error("SubscribeManager.create", ErrSev.MAJOR, errMsg));
                throw new OSCARSServiceException(errMsg);
            }
        }
        tmpWhere += (!"".equals(tmpWhere)) ? ")" : "";
        subscripSQL += tmpWhere;
        
        //parse users
        tmpWhere = "";
        for(String user : request.getUser()){
            if(!hasWhere){
                tmpWhere += " WHERE (";
                hasWhere = true;
            }else if(hasWhere && "".equals(tmpWhere)){
                tmpWhere += " AND (";
            }else{
                tmpWhere += " OR";
            }
            tmpWhere += " userLogin=?";
        }
        tmpWhere += (!"".equals(tmpWhere)) ? ")" : "";
        subscripSQL += tmpWhere;
        
        //parse ids
        tmpWhere = "";
        for(String id : request.getSubscriptionId()){
            if(!hasWhere){
                tmpWhere += " WHERE (";
                hasWhere = true;
            }else if(hasWhere && "".equals(tmpWhere)){
                tmpWhere += " AND (";
            }else{
                tmpWhere += " OR";
            }
            tmpWhere += " referenceId=?";
        }
        tmpWhere += (!"".equals(tmpWhere)) ? ")" : "";
        subscripSQL += tmpWhere;
        netLogProps.put("sql", subscripSQL);
        
        //insert into database
        try {
            Connection conn = NotificationGlobals.getInstance().getConnection();
            
            //get subscriptions
            PreparedStatement subscripQuery = conn.prepareStatement(subscripSQL);
            PreparedStatement filterQuery = conn.prepareStatement("SELECT type, value " +
                "FROM subscriptionFilters WHERE subscriptionId=?");
            int i = 1;
            for(Integer status : subscripSQLStatusParams){
                subscripQuery.setInt(i, status);
                i++;
            }
            for(String user : request.getUser()){
                subscripQuery.setString(i, user);
                i++;
            }
            for(String id : request.getSubscriptionId()){
                subscripQuery.setString(i, id);
                i++;
            }
            ResultSet subscripRows = subscripQuery.executeQuery();
            while(subscripRows.next()){
                OscarsSubscription subscription = new OscarsSubscription();
                subscription.setSubscriptionReference(WSAddrParser.createAddress(subscripRows.getString(2)));
                subscription.setUser(subscripRows.getString(3));
                subscription.setConsumerReference(WSAddrParser.createAddress(subscripRows.getString(4)));
                subscription.setCreatedTime(this.createXMLTime(subscripRows.getLong(5)));
                subscription.setTerminationTime(this.createXMLTime(subscripRows.getLong(6)));
                subscription.setStatus(SubscriptionStatus.statusToString(subscripRows.getInt(7)));
                
                //get filter
                filterQuery.setInt(1, subscripRows.getInt(1));
                ResultSet filterRows = filterQuery.executeQuery();
                FilterType filter = new FilterType();
                while(filterRows.next()){
                    String type = filterRows.getString(1);
                    String val = filterRows.getString(2);
                    if(FilterTypes.FILTER_AUTHZDOMAIN.equals(type)){
                        subscription.getAuthorizedDomain().add(val);
                    }else if(FilterTypes.FILTER_AUTHZLOGIN.equals(type)){
                        subscription.getAuthorizedLogin().add(val);
                    }else if(FilterTypes.FILTER_MSGXPATH.equals(type)){
                        QueryExpressionType msgFilter = new QueryExpressionType();
                        msgFilter.setDialect(TopicDialect.XPATH);
                        msgFilter.setValue(val);
                        filter.getMessageContent().add(msgFilter );
                    }else if(FilterTypes.FILTER_PRODXPATH.equals(type)){
                        QueryExpressionType prodFilter = new QueryExpressionType();
                        prodFilter.setDialect(TopicDialect.XPATH);
                        prodFilter.setValue(val);
                        filter.getProducerProperties().add(prodFilter);
                    }else if(FilterTypes.FILTER_TOPIC.equals(type)){
                        TopicExpressionType topicExpr = new TopicExpressionType();
                        topicExpr.setDialect(TopicDialect.SIMPLE);
                        topicExpr.setValue(val);
                        filter.getTopicExpression().add(topicExpr );
                    }
                    
                }
                subscription.setFilter(filter);
                response.getSubscription().add(subscription);
            }
        } catch (SQLException e) {
            this.log.debug(netLogger.error("SubscribeManager.list", ErrSev.MAJOR, e.getMessage(), null, netLogProps));
            e.printStackTrace();
            throw new OSCARSServiceException("Error getting subscriptions from database");
        }
        
        
        this.log.debug(netLogger.end("SubscribeManager.list", null, null, netLogProps));
        return response;
    }*/
    
    private XMLGregorianCalendar createXMLTime(long timestamp) throws OSCARSServiceException {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp);
        
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException e) {
            throw new OSCARSServiceException(e.getMessage());
        }
    }

    /**
     *  Utility function that extracts an xsd:datetime or xsd:duration from a string
     *
     * @param termTime the string to parse
     * @return a timestamp in seconds equivalent to the given string
     * @throws UnacceptableInitialTerminationTimeFault
     */
    private long parseTermTime(String termTime) throws OSCARSServiceException{
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("SubscribeManager.parseTermTime"));
        HashMap<String,String> netLogProps = new HashMap<String,String>();
        
        /* Parsing initial termination time since Axis2 does not like unions */
        long timestamp = 0L;
        if(termTime.startsWith("P")){
            //duration
            netLogProps.put("timeType", "xsd:duration");
            try{
                DatatypeFactory dtFactory = DatatypeFactory.newInstance();
                Duration dur = dtFactory.newDuration(termTime);
                GregorianCalendar cal = new GregorianCalendar();
                dur.addTo(cal);
                timestamp = (cal.getTimeInMillis());
            }catch(Exception e){
                String errMsg = "InitialTerminationTime appears to be an invalid xsd:duration value.";
                this.log.debug(netLog.error("SubscribeManager.parseTermTime", ErrSev.MINOR, errMsg, null, netLogProps));
                throw new OSCARSServiceException(errMsg);
            }
        }else{
            //datetime or invalid
            netLogProps.put("timeType", "xsd:datetime");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            try{
                Date date = df.parse(termTime);
                timestamp = (date.getTime());
            }catch(Exception e){
                String errMsg = "InitialTerminationTime must be of type xsd:datetime or xsd:duration.";
                this.log.debug(netLog.error("SubscribeManager.parseTermTime", ErrSev.MINOR, errMsg, null, netLogProps));
                throw new OSCARSServiceException(errMsg);
            }
        }
        
        this.log.debug(netLog.end("SubscribeManager.parseTermTime", null, null, netLogProps));
        return timestamp;
    }
    
    /**
     * Validates a QueryExpression as those used in the ProducerProperties and
     * MessageContent sections of a Subscribe Filter. Checks to see it is in the 
     * XPath dialect and that a valid XPath expression was provided.
     *
     * @param query the QueryExpression to validate
     * @return true if valid, false if no query exists. Throws an exception otherwise.
     * @throws OSCARSServiceException
     */
    private boolean validateQueryExpression(QueryExpressionType query) throws OSCARSServiceException{
        if(query == null){
            return false;
        }
        
        String dialect = query.getDialect().toString();
        if(!TopicDialect.XPATH.equals(dialect)){
            throw new OSCARSServiceException("Filter dialect '" + dialect +
                                         "'is not supported by this service.");
        }
        
        String xpathStr = query.getValue();
        try{
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            xpath.compile(xpathStr);
        }catch(Exception e){
            String err = "Invalid expression: " + e.getMessage();
            throw new OSCARSServiceException(err);
        }
        
        return true;
    }

    /**
     * Parse a TopicExpression to make sure it is in a known Dialect.
     * It also splits topics into multiple topics. Currently this method
     * supports the SimpleTopic, ConcreteTopic, and FullTopic(partially)
     * specifications. 
     *
     * @param list the TopicExpression to parse
     * @return an array of strings containing each Topic
     * @throws InvalidTopicExpressionFault
     * @throws TopicExpressionDialectUnknownFault
     */
    public ArrayList<String> parseTopics(List<TopicExpressionType> list) 
            throws OSCARSServiceException{
        if(list == null || list.size() < 1){
            return new ArrayList<String>(0);
        }
        
        ArrayList<String> topics = new ArrayList<String>();
        for(TopicExpressionType topicFilter : list){
            if(topicFilter == null){
                continue;
            }
            String dialect = topicFilter.getDialect().toString();
            String topicString = topicFilter.getValue();
            
            //check dialect
            if(TopicDialect.XPATH.equals(dialect)){
                 throw new OSCARSServiceException("The XPath Topic " +
                            "Expression dialect is not supported at this time.");
            }else if(!(TopicDialect.SIMPLE.equals(dialect) || 
                    TopicDialect.CONCRETE.equals(dialect) || 
                    TopicDialect.FULL.equals(dialect))){
                throw new OSCARSServiceException("Unknown Topic dialect '" + dialect + "'");
            }
            
            if(topicString == null || "".equals(topicString)){
                throw new OSCARSServiceException("Empty topic expression given.");
            }
            String[] topicTokens = topicString.split("\\|");
            for(String topicToken : topicTokens){
                topics.add(topicToken);
            }
            /* NOTE: Currently the notification broker is neutral as to the 
               type of topics is sends/receives so there is no check to see 
               if a topic is supported. This provides the greatest flexibility
               but doesn't allow the broker to return an error if it knows it
               can never send a notification for a particular topic. As we gain
               more experience we can revisit this fact */
        }
        
        return topics;
    }
    
    public void changeStatus(Connection conn, W3CEndpointReference epr, int newStatus, 
            SubjectAttributes subjAttrs, AuthConditions authConds) throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start("SubscribeManager.changeStatus"));
        
        //Legacy support for 0.5 subscription ID format
        String subscriptionId = WSAddrParser.get05SubscriptionId(epr);
        if(subscriptionId == null){
            subscriptionId = WSAddrParser.getAddress(epr);
        }
        try{
            PreparedStatement updateSQL = null;
            //ALL is special case that modifies all subscriptions belonging to a user
            if("ALL".equals(subscriptionId)){
                String userLogin = null;
                for(AttributeType attr : subjAttrs.getSubjectAttribute()){
                    if(AuthZConstants.LOGIN_ID.equals(attr.getName()) &&
                            !attr.getAttributeValue().isEmpty()){
                        userLogin = (String) attr.getAttributeValue().get(0);
                    }
                }
                if(userLogin == null){
                    throw new OSCARSServiceException("Cannot determine user that made request");
                }
                updateSQL = conn.prepareStatement("UPDATE subscriptions SET status=? WHERE userLogin=? AND status != ?");
                updateSQL.setInt(1, newStatus);
                updateSQL.setString(2, userLogin);
                updateSQL.setInt(3, SubscriptionStatus.INACTIVE_STATUS);
            }else{
                
                PreparedStatement querySQL = conn.prepareStatement("SELECT status, userLogin FROM subscriptions WHERE referenceId=?");
                querySQL.setString(1, subscriptionId);
                ResultSet queryResult = querySQL.executeQuery();
                if(!queryResult.next()){
                    throw new OSCARSServiceException("Unable to find subscription with id " + subscriptionId);
                }
                
                //check permissions
                this.checkAuthConditions(queryResult.getString(2), authConds);
                
                int currentStatus = queryResult.getInt(1);
                if(newStatus == SubscriptionStatus.PAUSED_STATUS && 
                        currentStatus == SubscriptionStatus.INACTIVE_STATUS){
                    throw new OSCARSServiceException("Cannot pause a subscription that is in state INACTIVE");
                }else if(newStatus == SubscriptionStatus.ACTIVE_STATUS && 
                        currentStatus == SubscriptionStatus.INACTIVE_STATUS){
                    throw new OSCARSServiceException("Cannot resume a subscription that is in state INACTIVE");
                }
                
                updateSQL = conn.prepareStatement("UPDATE subscriptions SET status=? WHERE referenceId=?");
                updateSQL.setInt(1, newStatus);
                updateSQL.setString(2, subscriptionId);
            }
            
            updateSQL.executeUpdate();
        }catch(SQLException e){
            this.log.debug(netLogger.error("SubscribeManager.changeStatus", ErrSev.MINOR, e.getMessage()));
            throw new OSCARSServiceException(e.getMessage());
        }
        this.log.debug(netLogger.end("SubscribeManager.changeStatus"));
        
    }

    public RenewResponse renew(Connection conn, String subscriptionId, String reqTermTime, 
            AuthConditions authConds) throws OSCARSServiceException{
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.debug(netLogger.start("SubscribeManager.renew"));
        RenewResponse renewResponse = new RenewResponse();
        renewResponse.setSubscriptionReference(WSAddrParser.createAddress(subscriptionId));
        try{
            PreparedStatement querySQL = conn.prepareStatement("SELECT id, userLogin FROM subscriptions WHERE referenceId=?");
            querySQL.setString(1, subscriptionId);
            ResultSet queryResult = querySQL.executeQuery();
            if(!queryResult.next()){
                throw new OSCARSServiceException("Unable to find subscription with id " + subscriptionId);
            }
            
            //check permissions
            this.checkAuthConditions(queryResult.getString(2), authConds);
            
            //calc new subscription time
            long terminationTime = System.currentTimeMillis() + this.defaultExpiration;
            if(reqTermTime != null){
                long tmpTermTime = this.parseTermTime(reqTermTime);
                //only allow specified expiration if its less than the default
                if(tmpTermTime <= terminationTime){
                    terminationTime = tmpTermTime;
                }else{
                    String errMsg = "Requested termination time is too far in the future. " +
                        "This server only allows expirations " + this.defaultExpiration/1000 + 
                        " seconds in the future.";
                    this.log.debug(netLogger.start("SubscribeManager.renew", ErrSev.MINOR, errMsg));
                    throw new OSCARSServiceException(errMsg);
                }
            }
            renewResponse.setCurrentTime(this.createXMLTime(System.currentTimeMillis()));
            renewResponse.setTerminationTime(this.createXMLTime(terminationTime));
            
            PreparedStatement updateSQL = conn.prepareStatement("UPDATE subscriptions SET terminationTime=?, status=? WHERE referenceId=?");
            updateSQL.setLong(1, terminationTime);
            updateSQL.setInt(2, SubscriptionStatus.ACTIVE_STATUS);
            updateSQL.setString(3, subscriptionId);
            updateSQL.executeUpdate();
        }catch(SQLException e){
            this.log.debug(netLogger.error("SubscribeManager.renew", ErrSev.MINOR, e.getMessage()));
            throw new OSCARSServiceException(e.getMessage());
        }
        this.log.debug(netLogger.end("SubscribeManager.renew"));
        
        return renewResponse;
    }

    private AuthConditions getAuthZPerms(SubjectAttributes attrs, 
            NotifyPEP pep) throws OSCARSServiceException{
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        HashMap<String,String> netLogProps = new HashMap<String, String>();
        this.log.info(netLogger.start("getAuthZPerms"));
        //if no attributes just return
        if(attrs == null){
            this.log.info(netLogger.end("getAuthZPerms"));
            return null;
        }
        if(pep == null){
            this.log.info(netLogger.end("getAuthZPerms", 
                    "Did not contact authZ service because " +
                    "no permission needed based on PEPs configured."));
            return null;
        }
        AuthZClient authZClient = NotificationGlobals.getInstance().getAuthZClient();
        if(authZClient == null){
            this.log.info(netLogger.end("getAuthZPerms", 
                    "Did not contact authZ service because not configured."));
            return null;
        }
        
        CheckAccessParams checkAccessReq = new CheckAccessParams();
        checkAccessReq.setTransactionId(netLogger.getGUID());
        checkAccessReq.setSubjectAttrs(attrs);
        checkAccessReq.setResourceName(pep.getResourceName());
        checkAccessReq.setPermissionName(pep.getPermissionName());
        CheckAccessReply checkAccessReply = null;
        try{
            Object[] req = new Object[]{checkAccessReq};
            Object[] res = authZClient.invoke("checkAccess", req);
            checkAccessReply = (CheckAccessReply)res[0];
        }catch(Exception e){
            this.log.error(netLogger.error("getAuthZPerms",ErrSev.MAJOR, e.getMessage(), 
                    NotificationGlobals.getInstance().getAuthZUrl(), netLogProps));
            throw new OSCARSServiceException(e);
        }
        this.log.info(netLogger.end("getAuthZPerms", null, 
                NotificationGlobals.getInstance().getAuthZUrl(), netLogProps));
     
        return checkAccessReply.getConditions();
    }
    
    private void checkAuthConditions(String userLogin, AuthConditions authConds) throws OSCARSServiceException{
        
        if(authConds == null || authConds.getAuthCondition() == null || userLogin == null){
            //if no conditions then assume they can do all
            return;
        }
        
        //compare userLogin to permitted logins
        for(AuthConditionType cond : authConds.getAuthCondition()){
            if(cond.getName().equals(AuthZConstants.PERMITTED_LOGIN)){
                for(String condValue : cond.getConditionValue()){
                    if(AuthZConstants.ALL_USERS.equals(condValue)){
                        //if all then allowed
                        return;
                    }else if(userLogin.toLowerCase().equals(condValue.toLowerCase())){
                        //if subscription creator then allowed
                        return;
                    }
                }
            }
        }
        throw new OSCARSServiceException("You do not have permissions to change specified subscription");
    }
}
