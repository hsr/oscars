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
# Note that node = network device
#
# TODO: add placeholders for network capacity/reservation and granularity
NMWGXMLPortOpen = """
<CtrlPlane:port id="urn:ogf:network:domain=%s:node=%s:port=%s">
<CtrlPlane:capacity>1000000000</CtrlPlane:capacity>
<CtrlPlane:maximumReservableCapacity>1000000000</CtrlPlane:maximumReservableCapacity>
<CtrlPlane:minimumReservableCapacity>1000000</CtrlPlane:minimumReservableCapacity>
<CtrlPlane:granularity>1000000</CtrlPlane:granularity>
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
<CtrlPlane:link id="urn:ogf:network:domain=%s:node=%s:port=%s:link=1">
	<CtrlPlane:remoteLinkId>urn:ogf:network:domain=%s:node=%s:port=%s:link=1</CtrlPlane:remoteLinkId>
	<CtrlPlane:trafficEngineeringMetric>100</CtrlPlane:trafficEngineeringMetric>
	<CtrlPlane:SwitchingCapabilityDescriptors>
		<CtrlPlane:switchingcapType/>
		<CtrlPlane:encodingType>packet</CtrlPlane:encodingType>
		<CtrlPlane:switchingCapabilitySpecificInfo>
			<CtrlPlane:capability/>
			<CtrlPlane:interfaceMTU>9000</CtrlPlane:interfaceMTU>
			<CtrlPlane:vlanRangeAvailability>2-4094</CtrlPlane:vlanRangeAvailability>
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
def NMWGXML_Link (srcDomain, srcNode, srcPort, dstDomain, dstNode, dstPort):
    return NMWGXMLLink % (srcDomain, srcNode, srcPort, dstDomain, dstNode, dstPort);
    
# Given a domain, a node, port and set of links (obtained using NMWGXML_Link) in the
# format of an XML string, this function returns a XML representation of a NMWG port.
def NMWGXML_Port (domain, node, port, links):
    PortOpen = NMWGXMLPortOpen % (domain, node, port);
    return '%s%s%s' % (PortOpen, links, NMWGXMLPortClose);
    
# Given a domain, a node id, and a set of ports (obtained using NMWGXML_Port) in the
# format of an XML string, this function returns a XML representation of a NMWG Node.
def NMWGXML_Node (domain, node, ports, address='0.0.0.0'):
    NodeOpen = NMWGXMLNodeOpen % (domain, node, address);
    return '%s%s%s' % (NodeOpen, ports, NMWGXMLNodeClose);
