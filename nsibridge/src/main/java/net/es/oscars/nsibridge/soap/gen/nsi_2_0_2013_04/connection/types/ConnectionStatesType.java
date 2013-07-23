
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A holder element containing the state machines associated with
 *                 a connection reservation.
 *                 
 *                 Elements:
 *                 
 *                 reservationState - Models the current connection reservation
 *                 state.
 *                 
 *                 provisionState - Models the current connection provisioning
 *                 state.  The provisionState is created for a reservation once
 *                 the reservation is committed.
 *                 
 *                 lifecycleState - Models the current connection lifecycle state.
 *                 
 *                 dataPlaneStatus - Models the current connection data plane
 *                 activation state.
 *             
 * 
 * <p>Java class for ConnectionStatesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConnectionStatesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reservationState" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}ReservationStateEnumType"/>
 *         &lt;element name="provisionState" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}ProvisionStateEnumType" minOccurs="0"/>
 *         &lt;element name="lifecycleState" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}LifecycleStateEnumType"/>
 *         &lt;element name="dataPlaneStatus" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}DataPlaneStatusType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConnectionStatesType", propOrder = {
    "reservationState",
    "provisionState",
    "lifecycleState",
    "dataPlaneStatus"
})
public class ConnectionStatesType {

    @XmlElement(required = true)
    protected ReservationStateEnumType reservationState;
    protected ProvisionStateEnumType provisionState;
    @XmlElement(required = true)
    protected LifecycleStateEnumType lifecycleState;
    @XmlElement(required = true)
    protected DataPlaneStatusType dataPlaneStatus;

    /**
     * Gets the value of the reservationState property.
     * 
     * @return
     *     possible object is
     *     {@link ReservationStateEnumType }
     *     
     */
    public ReservationStateEnumType getReservationState() {
        return reservationState;
    }

    /**
     * Sets the value of the reservationState property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReservationStateEnumType }
     *     
     */
    public void setReservationState(ReservationStateEnumType value) {
        this.reservationState = value;
    }

    /**
     * Gets the value of the provisionState property.
     * 
     * @return
     *     possible object is
     *     {@link ProvisionStateEnumType }
     *     
     */
    public ProvisionStateEnumType getProvisionState() {
        return provisionState;
    }

    /**
     * Sets the value of the provisionState property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProvisionStateEnumType }
     *     
     */
    public void setProvisionState(ProvisionStateEnumType value) {
        this.provisionState = value;
    }

    /**
     * Gets the value of the lifecycleState property.
     * 
     * @return
     *     possible object is
     *     {@link LifecycleStateEnumType }
     *     
     */
    public LifecycleStateEnumType getLifecycleState() {
        return lifecycleState;
    }

    /**
     * Sets the value of the lifecycleState property.
     * 
     * @param value
     *     allowed object is
     *     {@link LifecycleStateEnumType }
     *     
     */
    public void setLifecycleState(LifecycleStateEnumType value) {
        this.lifecycleState = value;
    }

    /**
     * Gets the value of the dataPlaneStatus property.
     * 
     * @return
     *     possible object is
     *     {@link DataPlaneStatusType }
     *     
     */
    public DataPlaneStatusType getDataPlaneStatus() {
        return dataPlaneStatus;
    }

    /**
     * Sets the value of the dataPlaneStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataPlaneStatusType }
     *     
     */
    public void setDataPlaneStatus(DataPlaneStatusType value) {
        this.dataPlaneStatus = value;
    }

}
