#!/usr/bin/env python

import sys

"""
SYNOPSIS
 
    Usage: %s [options]
 
DESCRIPTION
 
    This program extracts Floodlight network topology information using Floodlight's 
    REST interface and creates an XML file with the extracted topology using the
    NMWG format

@author Henrique Rodrigues hsr@cs.ucsd.edu

""" % sys.argv[0]

import os
import sys
import subprocess
import json
import argparse
import io
import time
import pprint


from NMWG import *

parser = argparse.ArgumentParser(description=sys.argv[0])
parser.add_argument('--controller', dest='controllerRestIp', action='store', 
    default='localhost:8080', 
    help='controller IP:RESTport, e.g., localhost:8080 or A.B.C.D:8080')
parser.add_argument('--inputfile', dest='inputfile', action='store', 
    help='Read topology links from file. Nodes are fetched from Floodlight',
    default='')
parser.add_argument('--output', dest='output', action='store',
    default='topology.xml', help='The output file. Defaults to topology.xml')
parser.add_argument('--domain', dest='domain', action='store',
    default='testdomain-1',
    help='The domain name to use. Defaults to testdomain-1')

args             = parser.parse_args()
controllerRestIp = args.controllerRestIp
domain           = args.domain
outputFile       = args.output

class topology(dict):
    """docstring for topology"""
    def __init__(self):
        super(topology, self).__init__()
    def addLink(self, *args):
        """docstring for addLink"""
        raise NotImplementedError
    def addBidirectionalLink(self, *args):
        """docstring for addBidirectionalLink"""
        raise NotImplementedError

class oscarsTopology(topology):
    """docstring for topology"""
    def __init__(self, jsonSwitches):
        super(topology, self).__init__()
        for switch in jsonSwitches:
    
            switch_dpid = switch['dpid']
            self[switch_dpid] = {}
            
            for port in switch['ports']:
                portNumber = port['portNumber']
                if str(portNumber) == '65534':
                    continue # Skip OVS local port
                if not self[switch_dpid].has_key(portNumber):
                    self[switch_dpid][portNumber] = None

    def addLink(self, jsonLink, reverse=False, capacity=1e9):
        srcSwitch, srcPort = jsonLink['src-switch'],jsonLink['src-port']
        dstSwitch, dstPort = jsonLink['dst-switch'],jsonLink['dst-port']
        if reverse:
            srcSwitch, dstSwitch = dstSwitch, srcSwitch
            srcPort, dstPort = dstPort, srcPort
        
        if (jsonLink.has_key('capacity')):
            capacity = jsonLink['capacity']
        
        if not self.has_key(srcSwitch):
            raise Exception('Node %s does not exist!' % srcSwitch);
        
        self[srcSwitch][srcPort] = {'switch': dstSwitch,
                                    'port': dstPort,
                                    'capacity': capacity,
                                    'maxReservation': capacity,
                                    'minReservation': 1,
                                    'granularity': 1}

        # TODO: FIXME to use a proper L1 switch identification instead of
        # using a pattern mathing on DPID
        if srcSwitch.rfind('11:11:') == 0 or dstSwitch.rfind('11:11:') == 0:
            self[srcSwitch][srcPort]['minReservation'] = capacity
            self[srcSwitch][srcPort]['granularity']    = capacity
    
    def addBidirectionalLink(self, jsonLink):
        self.addLink(jsonLink)
        self.addLink(jsonLink, reverse=True)

def getJSON(url):
    return json.loads(os.popen('curl -s %s ' % url).read())

def getJSONFromFile(filename):
    file = open(filename, 'r')
    contents = file.read();
    file.close()
    return json.loads(contents)

switches = getJSON('http://%s/wm/core/controller/switches/json' % \
    (args.controllerRestIp))
if len(args.inputfile) > 0:
    internalLinks = getJSONFromFile(args.inputfile)
else:
    internalLinks = getJSON("http://%s/wm/topology/links/json" % \
        (args.controllerRestIp))

# Build a graph (python dictionary) indexed by
# node id (= Floodlight dpid)
floodlightTopology = oscarsTopology(switches)
for link in internalLinks:
    floodlightTopology.addBidirectionalLink(link)

print 'Floodlight topology:'
pprint.PrettyPrinter(depth=4).pprint(floodlightTopology)
# sys.exit(1)

# Create NMWG XML file from graph (topologyNodes)
nodes=''
for srcSwitch,ports in floodlightTopology.items():
    nodePorts = ''
    

    for srcPort,port in ports.items():
        dstDomain = 'external'
        dstSwitch = '00:00:00:00:00:00:00:00'
        dstPort = 0
        # Default external link capacity: 100G. Since this link is connecting us
        # to another domain, "we don't really care" how much bandwidth we use
        capacity, maxReservation, minReservation, granularity = 100e9, 100e9, 1, 1
        if port: 
            # if port connecting srcPort isn't defined, this is a remote link
            dstSwitch = port['switch']
            dstPort = port['port']
            dstDomain = domain
            capacity = port['capacity']
            maxReservation = port['maxReservation']
            minReservation = port['minReservation']
            granularity = port['granularity']
            
        
        srcSwitch = srcSwitch.replace(':', '.')
        dstSwitch = dstSwitch.replace(':', '.')
        nodeLink = NMWGXML_Link(domain,srcSwitch,srcPort,
                                dstDomain,dstSwitch,dstPort)

        # TODO: this is assuming that a node's port only has one link. 
        # FIXME for multiple links per port
        nodePorts += NMWGXML_Port(domain, srcSwitch, srcPort, nodeLink, 
                                  capacity, maxReservation, minReservation,
                                  granularity)
    nodes += NMWGXML_Node (domain, srcSwitch, nodePorts, '127.0.0.1')

try: 
    f = open(outputFile, 'w');
    f.write(NMWGXML_Domain(domain, nodes))
    f.close()
except Exception, e:
    print 'Error writing to file: %s' % str(e)
