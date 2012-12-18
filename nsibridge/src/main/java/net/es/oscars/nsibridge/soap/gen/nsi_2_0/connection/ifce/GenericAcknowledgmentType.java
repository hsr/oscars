
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                         Common WSDL output message for acknowledgment of an NSI CS
 *                         request (input) message.
 *                         
 *                         Elements:
 *                         We have moved the correlationId to the header so this is
 *                         now an empty response.
 *                         
 *                         Notes on acknowledgment:
 *                         Depending on NSA implementation and thread timing an
 *                         acknowledgment to a request operation may be returned
 *                         after the confirm/fail for the request has been returned
 *                         to the Requesting NSA.
 *                         
 *                         The following guidelines for acknowledgment handling are
 *                         proposed:
 *                         
 *                         1. For protocol robustness, Requesting NSA should be
 *                         able to accept confirm/fail before acknowledgment.
 *                         
 *                         2. Acknowledgment should be sent by Provider NSA before
 *                         the confirm/fail.
 *                     
 * 
 * <p>Java class for GenericAcknowledgmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericAcknowledgmentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericAcknowledgmentType")
public class GenericAcknowledgmentType {


}
