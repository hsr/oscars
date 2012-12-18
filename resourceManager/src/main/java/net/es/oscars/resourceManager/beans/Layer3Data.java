package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

import org.hibernate.Hibernate;
import net.es.oscars.database.hibernate.HibernateBean;

/**
 * Layer3Data is the Hibernate bean for for the rm.layer3Data table.
 */
public class Layer3Data extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

    /** persistent field */
    private String srcHost;

    /** persistent field */
    private String destHost;

    /** nullable persistent field */
    private Integer srcIpPort;

    /** nullable persistent field */
    private Integer destIpPort;

    /** nullable persistent field */
    private String protocol;

    /** nullable persistent field */
    private String dscp;


    /** default constructor */
    public Layer3Data() { }


    /**
     * @return srcHost A String with the source's IP address
     */
    public String getSrcHost() { return this.srcHost; }

    /**
     * @param srcHost A String with the source's IP address
     */
    public void setSrcHost(String srcHost) { this.srcHost = srcHost; }


    /**
     * @return destHost A String with the destination's IP address
     */
    public String getDestHost() { return this.destHost; }

    /**
     * @param destHost A String with the destination's IP address
     */
    public void setDestHost(String destHost) { this.destHost = destHost; }


    /**
     * @return srcIpPort An Integer with the reservation's source port
     */
    public Integer getSrcIpPort() { return this.srcIpPort; }

    /**
     * @param srcIpPort An Integer with the reservation's source port
     */
    public void setSrcIpPort(Integer srcIpPort) { this.srcIpPort = srcIpPort; }


    /**
     * @return destIpPort An Integer with the reservation's destination port
     */
    public Integer getDestIpPort() { return this.destIpPort; }

    /**
     * @param destIpPort An Integer with the reservation's destination port
     */
    public void setDestIpPort(Integer destIpPort) { this.destIpPort = destIpPort; }


    /**
     * @return protocol A String with the reservation's desired protocol
     */
    public String getProtocol() { return this.protocol; }

    /**
     * @param protocol A String with the reservation's desired protocol
     */
    public void setProtocol(String protocol) { this.protocol = protocol; }


    /**
     * @return dscp A String with the reservation's DSCP
     */
    public String getDscp() { return this.dscp; }

    /**
     * @param dscp A String with the reservation's DSCP
     */
    public void setDscp(String dscp) { this.dscp = dscp; }

    public Layer3Data copy() {

        Layer3Data l3DataCopy = new Layer3Data();
        l3DataCopy.setSrcHost(this.srcHost);
        l3DataCopy.setDestHost(this.destHost);
        l3DataCopy.setSrcIpPort(this.srcIpPort);
        l3DataCopy.setDestIpPort(this.destIpPort);
        l3DataCopy.setProtocol(this.protocol);
        l3DataCopy.setDscp(this.dscp);
        return l3DataCopy;
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Layer3Data castOther = (Layer3Data) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getSrcHost(), castOther.getSrcHost())
                .append(this.getDestHost(), castOther.getDestHost())
                .append(this.getSrcIpPort(), castOther.getSrcIpPort())
                .append(this.getDestIpPort(), castOther.getDestIpPort())
                .append(this.getProtocol(), castOther.getProtocol())
                .append(this.getDscp(), castOther.getDscp())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("start", this.getSrcHost())
            .append("destination", this.getDestHost())
            .toString();
    }
}
