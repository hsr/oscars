#!/usr/bin/python

# TODO FIXME: Write some OO code instead of repeatedly replace string contents

# Three placeholders/arguments for this string:
#  1 - domain id
#  2 - domain id
#  3 - domain id
NMWGXMLHeaderOpen = """
<CtrlPlane:topology xmlns:CtrlPlane="http://ogf.org/schema/network/topology/ctrlPlane/20080828/" id="%s">
<xsd:documentation xmlns:xsd="http://www.w3.org/2001/XMLSchema" lang="en">Auto generated based on Floodlight's Topology Service</xsd:documentation>
<CtrlPlane:idcId>%s</CtrlPlane:idcId>
<CtrlPlane:domain id="%s">
"""

# Three placeholders/arguments for this string:
#  1 - domain id
#  2 - node   id
#  3 - node   address
#
# Note that node = network device
NMWGXMLNodeOpen = """
<CtrlPlane:node id="urn:ogf:network:domain=%s:node=%s">
    <CtrlPlane:address>%s</CtrlPlane:address>
"""

# Three placeholders/arguments for this string:
#  1 - domain id
#  2 - node   id
#  3 - port   id
#
#  4 - capacity
#  5 - maximumReservableCapacity
#  6 - minimumReservableCapacity
#  7 - granularity
#
# Note that node = network device
#
# TODO: add placeholders for network capacity/reservation and granularity
NMWGXMLPortOpen = """
<CtrlPlane:port id="urn:ogf:network:domain=%s:node=%s:port=%s">
<CtrlPlane:capacity>%d</CtrlPlane:capacity>
<CtrlPlane:maximumReservableCapacity>%d</CtrlPlane:maximumReservableCapacity>
<CtrlPlane:minimumReservableCapacity>%d</CtrlPlane:minimumReservableCapacity>
<CtrlPlane:granularity>%d</CtrlPlane:granularity>
"""

# Six placeholders/arguments for this string:
#  1 - domain id
#  2 - node   id
#  3 - port   id
#  4 - domain id
#  5 - node   id
#  6 - port   id
#
# Note that node = network device
#
# TODO: add placeholders for network metrics below (trafficEngineeringMetric,interfaceMTU,...)
# TODO: Add new placeholders to links. Currently each port has a single link
NMWGXMLLink = """
<CtrlPlane:link id="urn:ogf:network:domain=%s:node=%s:port=%s:link=%s">
	<CtrlPlane:remoteLinkId>urn:ogf:network:domain=%s:node=%s:port=%s:link=%s</CtrlPlane:remoteLinkId>
	<CtrlPlane:trafficEngineeringMetric>100</CtrlPlane:trafficEngineeringMetric>
	<CtrlPlane:SwitchingCapabilityDescriptors>
		<CtrlPlane:switchingcapType/>
		<CtrlPlane:encodingType>packet</CtrlPlane:encodingType>
		<CtrlPlane:switchingCapabilitySpecificInfo>
			<CtrlPlane:capability/>
			<CtrlPlane:interfaceMTU>9000</CtrlPlane:interfaceMTU>
			<CtrlPlane:vlanRangeAvailability>0-4094</CtrlPlane:vlanRangeAvailability>
		</CtrlPlane:switchingCapabilitySpecificInfo>
	</CtrlPlane:SwitchingCapabilityDescriptors>
</CtrlPlane:link>
"""

NMWGXMLPortClose = """
</CtrlPlane:port>
"""

NMWGXMLNodeClose = """
</CtrlPlane:node>
"""

NMWGXMLHeaderClose = """
</CtrlPlane:domain></CtrlPlane:topology>
"""

def NMWGXML_Domain (domain, nodes):
    DomainOpen = NMWGXMLHeaderOpen % (domain, domain, domain);
    return '%s%s%s' % (DomainOpen, nodes, NMWGXMLHeaderClose)

# Given source and destination description of a pair of links (each composed 
# domain, node and port), this function returns a XML representation of a NMWG Link.
def NMWGXML_Link (srcDomain, srcNode, srcPort, dstDomain, dstNode, dstPort, srcLink='1', dstLink='1'):
    return NMWGXMLLink % (srcDomain, srcNode, srcPort, srcLink, dstDomain, dstNode, dstPort, dstLink);
    
# Given a domain, a node, port and set of links (obtained using NMWGXML_Link) in the
# format of an XML string, this function returns a XML representation of a NMWG port.
#
# Optional arguments are port capacity, maximumReservableCapacity, 
# minimumReservableCapacity and granularity Default values are 1G, 
# 1G, 1M, 1M respectively.
def NMWGXML_Port (domain, node, port, links, 
                  capacity=1E9, maxReservable=1E9, minReservable=1E6, granularity=1E6):
    PortOpen = NMWGXMLPortOpen % (domain, node, port, capacity, 
                                  maxReservable, minReservable, granularity);
    return '%s%s%s' % (PortOpen, links, NMWGXMLPortClose);
    
# Given a domain, a node id, and a set of ports (obtained using NMWGXML_Port) in the
# format of an XML string, this function returns a XML representation of a NMWG Node.
def NMWGXML_Node (domain, node, ports, address='0.0.0.0'):
    NodeOpen = NMWGXMLNodeOpen % (domain, node, address);
    return '%s%s%s' % (NodeOpen, ports, NMWGXMLNodeClose);
