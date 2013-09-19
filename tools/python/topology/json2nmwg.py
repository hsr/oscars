#!/usr/bin/env python

import sys

"""
SYNOPSIS
 
    Usage: %s [options]
 
DESCRIPTION
 
    This program reads a JSON file with a network topology information
    and creates an XML file with the topology in NMWG format.
    
    The input can be fetched from Floodlight using its REST API or can be
    read from a file. The list of network devices and their ports, however,
    can only be fetched from Floodlight. This makes a Floodlight Controller
    a pre-requisite to transform a json topology file to an NMWG topology.

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
        
        srcLink = '1'
        if jsonLink.has_key('src-link'):
            srcLink = jsonLink['src-link']
        dstLink = '1'
        if jsonLink.has_key('dst-link'):
            dstLink = jsonLink['dst-link']
        
        if reverse:
            srcSwitch, dstSwitch = dstSwitch, srcSwitch
            srcPort, dstPort = dstPort, srcPort
            srcLink, dstLink = dstLink, srcLink
        
        if (jsonLink.has_key('capacity')):
            capacity = jsonLink['capacity']
        
        if not self.has_key(srcSwitch):
            raise Exception('Node %s does not exist!' % srcSwitch);
        
        self[srcSwitch][srcPort] = {'switch': dstSwitch,
                                    'port': dstPort,
                                    'link': dstLink,
                                    'capacity': capacity,
                                    'maxReservation': capacity,
                                    'minReservation': 1,
                                    'granularity': 1}

        # TODO: FIXME to use a proper L1 switch identification instead of
        # using a pattern matching on DPID
        if srcSwitch.rfind('11:11:') == 0 or dstSwitch.rfind('11:11:') == 0:
            self[srcSwitch][srcPort]['minReservation'] = capacity
            self[srcSwitch][srcPort]['granularity']    = capacity
    
    def addBidirectionalLink(self, jsonLink):
        self.addLink(jsonLink)
        self.addLink(jsonLink, reverse=True)

def getJSON(url):
    try:
        return json.loads(os.popen('curl -s %s ' % url).read())
    except Exception, e:
        print "Couldn't fetch file at the given URL. " + \
              "Are you sure your controller is running?"

def getJSONFromFile(filename):
    try:
        file = open(filename, 'r')
        contents = file.read();
        file.close()
        return json.loads(contents)
    except Exception, e:
        print "Couldn't read topology from specified file. " + \
              "Are you sure the file %s exists?" % filename

switches = getJSON('http://%s/wm/core/controller/switches/json' % \
    (args.controllerRestIp))
if len(args.inputfile) > 0:
    internalLinks = getJSONFromFile(args.inputfile)
else:
    internalLinks = getJSON("http://%s/wm/topology/links/json" % \
        (args.controllerRestIp))

if not switches or not internalLinks:
    sys.exit(1)  

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
        dstLink = '1'
        dstPort = 0
        
        # Default external link capacity: 100G. Since this link is 
        # connecting us to another domain, "we don't really care" 
        # how much bandwidth we use
        capacity, maxReservation, minReservation, granularity = \
            100e9, 100e9, 1, 1
        if port: 
            # if port connecting srcPort isn't defined, this is a remote link
            dstSwitch = port['switch']
            dstPort = port['port']
            dstLink = port['link']
            dstDomain = domain
            capacity = port['capacity']
            maxReservation = port['maxReservation']
            minReservation = port['minReservation']
            granularity = port['granularity']
            

        srcLink = '1'
        try:
            srcLink = floodlightTopology[dstSwitch][dstPort]['link']
            # This might be confusing, why am I getting srcLink info from
            # the destination? 
            #
            # A: The topology dict is indexed by src (Switch and Port),
            #    and it contains information about the dst. Thus, to get
            #    info about the src, you need to look at the dst.
            # 
            # There is a limitation with this scheme, however. 
            # Ports facing external domains won't have link information 
            # because they don't have a explicit link in the topology
            # file specifying their link part.
            #
            # TODO: FIXME for ports facing external domains
        except:
            #print 'Could not find %s:%s' % (srcSwitch, srcPort)
            pass

        nodeLink = NMWGXML_Link(domain,
                                srcSwitch.replace(':', '.'),srcPort,
                                dstDomain,
                                dstSwitch.replace(':', '.'),dstPort,
                                srcLink=srcLink, dstLink=dstLink)

        # TODO: this is assuming that a node's port only has one link. 
        # FIXME for multiple links per port
        nodePorts += NMWGXML_Port(domain,
                                  srcSwitch.replace(':', '.'), 
                                  srcPort, nodeLink, 
                                  capacity, maxReservation, minReservation,
                                  granularity)
    nodes += NMWGXML_Node (domain, srcSwitch.replace(':', '.'), 
                           nodePorts, '127.0.0.1')

try: 
    f = open(outputFile, 'w');
    f.write(NMWGXML_Domain(domain, nodes))
    f.close()
except Exception, e:
    print 'Error writing to file: %s' % str(e)
