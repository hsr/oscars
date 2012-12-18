
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

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
 *                 state.
 *                 
 *                 activationState - Models the current connection data plane
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
 *         &lt;element name="reservationState" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ReservationStateType"/>
 *         &lt;element name="provisionState" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ProvisionStateType"/>
 *         &lt;element name="activationState" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ActivationStateType"/>
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
    "activationState"
})
public class ConnectionStatesType {

    @XmlElement(required = true)
    protected ReservationStateType reservationState;
    @XmlElement(required = true)
    protected ProvisionStateType provisionState;
    @XmlElement(required = true)
    protected ActivationStateType activationState;

    /**
     * Gets the value of the reservationState property.
     * 
     * @return
     *     possible object is
     *     {@link ReservationStateType }
     *     
     */
    public ReservationStateType getReservationState() {
        return reservationState;
    }

    /**
     * Sets the value of the reservationState property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReservationStateType }
     *     
     */
    public void setReservationState(ReservationStateType value) {
        this.reservationState = value;
    }

    /**
     * Gets the value of the provisionState property.
     * 
     * @return
     *     possible object is
     *     {@link ProvisionStateType }
     *     
     */
    public ProvisionStateType getProvisionState() {
        return provisionState;
    }

    /**
     * Sets the value of the provisionState property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProvisionStateType }
     *     
     */
    public void setProvisionState(ProvisionStateType value) {
        this.provisionState = value;
    }

    /**
     * Gets the value of the activationState property.
     * 
     * @return
     *     possible object is
     *     {@link ActivationStateType }
     *     
     */
    public ActivationStateType getActivationState() {
        return activationState;
    }

    /**
     * Sets the value of the activationState property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivationStateType }
     *     
     */
    public void setActivationState(ActivationStateType value) {
        this.activationState = value;
    }

}
