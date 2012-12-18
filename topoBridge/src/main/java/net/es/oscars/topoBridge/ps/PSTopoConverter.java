package net.es.oscars.topoBridge.ps;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;


import org.jdom.Document;
import org.jdom.transform.JDOMSource;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;

public class PSTopoConverter {

    /**
     * Converts a JDOM topology document at the domain level to
     * a CXF type generated from SOAP.
     *
     * @param domain a JDOM document that holds the domain XML info
     * @param nsUri the XML namespace
     * @return the CXF type
     */
    public static CtrlPlaneDomainContent convert(Document domain, String nsUri) {
        try {
            Source src = new JDOMSource(domain);
            JAXBContext context = JAXBContext.newInstance("org.ogf.schema.network.topology.ctrlplane");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement jaxbObj = (JAXBElement) unmarshaller.unmarshal(src);
            CtrlPlaneDomainContent topoContent = (CtrlPlaneDomainContent) (jaxbObj.getValue());
            return topoContent;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Object", e);
        }
    }

}
