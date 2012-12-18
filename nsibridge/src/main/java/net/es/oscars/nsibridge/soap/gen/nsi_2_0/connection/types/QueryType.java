
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type definition for the queryRequest message providing a mechanism
 *                 for either Requester or Provider NSA to query the other NSA for a
 *                 set of connection service reservation instances between the RA-PA
 *                 pair. This message can also be used as a status polling mechanism.
 * 
 *                 Elements:
 * 
 *                 operation - Parameter specifying the type of query operation to
 *                 perform.
 *                 
 *                 queryFilter - Parameter specifying the query criteria to match
 *                 against reserved connections. Any matching connections must be
 *                 returned.
 *             
 * 
 * <p>Java class for QueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="operation" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}QueryOperationType"/>
 *         &lt;element name="queryFilter" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}QueryFilterType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryType", propOrder = {
    "operation",
    "queryFilter"
})
public class QueryType {

    @XmlElement(required = true, defaultValue = "Summary")
    protected QueryOperationType operation;
    @XmlElement(required = true)
    protected QueryFilterType queryFilter;

    /**
     * Gets the value of the operation property.
     * 
     * @return
     *     possible object is
     *     {@link QueryOperationType }
     *     
     */
    public QueryOperationType getOperation() {
        return operation;
    }

    /**
     * Sets the value of the operation property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryOperationType }
     *     
     */
    public void setOperation(QueryOperationType value) {
        this.operation = value;
    }

    /**
     * Gets the value of the queryFilter property.
     * 
     * @return
     *     possible object is
     *     {@link QueryFilterType }
     *     
     */
    public QueryFilterType getQueryFilter() {
        return queryFilter;
    }

    /**
     * Sets the value of the queryFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryFilterType }
     *     
     */
    public void setQueryFilter(QueryFilterType value) {
        this.queryFilter = value;
    }

}
