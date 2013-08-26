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
parser.add_argument('--controller', dest='controllerRestIp', action='store', default='localhost:8080', help='controller IP:RESTport, e.g., localhost:8080 or A.B.C.D:8080')
parser.add_argument('--output', dest='output', action='store', default='topology.xml', help='The output file. Defaults to topology.xml')
parser.add_argument('--domain', dest='domain', action='store', default='testdomain-1', help='The domain name to use. Defaults to testdomain-1')

args             = parser.parse_args()
controllerRestIp = args.controllerRestIp
domain           = args.domain
outputFile       = args.output

class topology(dict):
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

    def addLink(self, jsonLink, reverse=False):
        srcSwitch, srcPort = jsonLink['src-switch'],jsonLink['src-port']
        dstSwitch, dstPort = jsonLink['dst-switch'],jsonLink['dst-port']
        if reverse:
            srcSwitch, dstSwitch = dstSwitch, srcSwitch
            srcPort, dstPort = dstPort, srcPort
        
        if not self.has_key(srcSwitch):
            raise Exception('Node %s does not exist!' % srcSwitch);
        self[srcSwitch][srcPort] = {'switch': dstSwitch, 'port': dstPort}
    
    def addBidirectionalLink(self, jsonLink):
        self.addLink(jsonLink)
        self.addLink(jsonLink, reverse=True)

def getJSON(url):
    return json.loads(os.popen('curl -s %s ' % url).read())

switches = getJSON('http://%s/wm/core/controller/switches/json' % (args.controllerRestIp))
internalLinks = getJSON("http://%s/wm/topology/links/json" % (args.controllerRestIp))

# Build a graph (python dictionary) indexed by
# node id (= Floodlight dpid)
floodlightTopology = topology(switches)
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
        if port: 
            # if port connecting srcPort isn't defined, this is a remote link
            dstSwitch = port['switch']
            dstPort = port['port']
            dstDomain = domain
        
        srcSwitch = srcSwitch.replace(':', '.')
        dstSwitch = dstSwitch.replace(':', '.')
        nodeLink = NMWGXML_Link(domain,srcSwitch,srcPort,
                                dstDomain,dstSwitch,dstPort)
        # TODO: this is assuming that a node's port only has one link. FIXME for multiple
        # links per port
        nodePorts += NMWGXML_Port(domain, srcSwitch, srcPort, nodeLink)
    nodes += NMWGXML_Node (domain, srcSwitch, nodePorts, '127.0.0.1')

try: 
    f = open(outputFile, 'w');
    f.write(NMWGXML_Domain(domain, nodes))
    f.close()
except Exception, e:
    print 'Error writing to file: %s' % str(e)
