package net.es.oscars.wsnbroker.utils;

import java.util.List;

import net.es.oscars.utils.notify.TopicDialect;

import org.oasis_open.docs.wsn.b_2.QueryExpressionType;

public class NotificationXPathUtil {
    
    /** 
     * Utility function for generating a ProducerProperties filter given a
     * list of producer URLs. The list is converted to an XPath OR expression.
     *
     * @param urls a list of URLs that identify producers from which the subscriber would like to receieve notifications
     * @return the generated query
     */
    public static QueryExpressionType generateProducerProperties(List<String> urls){
        boolean multiple = false;
        String xpath = "";
        for(String url : urls){
            xpath += (multiple ? " or " : "");
            xpath += "/wsa:Address='" + url + "'";
            multiple = true;
        }
        return NotificationXPathUtil.generateQueryExpression(xpath);
    }
    
    /** 
     * Utility function for generating a query expression using XPath
     *
     * @param xpath an Xpath expression used to match producers
     * @return the generated query
     */
    public static QueryExpressionType generateQueryExpression(String xpath){
        QueryExpressionType query = new QueryExpressionType();
        query.setDialect(TopicDialect.XPATH);
        query.setValue(xpath);
        
        return query;
    }
}
